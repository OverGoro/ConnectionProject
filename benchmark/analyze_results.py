#!/usr/bin/env python3
"""
analyze_results.py

Парсит результаты Gatling в папке `gatling-results` и строит перцентильные графики
и гистограммы для каждого сервиса (например async_server и mp_server), а также объединённый
график перцентилей по RPS для сравнения сервисов.

Ожидаемая структура результатов:
  gatling-results/<service>-results-<rps>-<timestamp>/
Внутри каждой папки — стандартный Gatling HTML отчет (папка `js` с данными).
"""
import json
import os
import sys
from pathlib import Path
from typing import Optional, Dict, Any, List

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt


RESULTS_DIR = Path(os.environ.get('RESULTS_DIR', './gatling-results'))
OUT_DIR = Path(os.environ.get('ANALYSIS_OUT', './analysis'))
OUT_DIR.mkdir(parents=True, exist_ok=True)


def load_json_from_report(report_dir: Path) -> Optional[Dict[str, Any]]:
    # Gatling report may place `js` directly under report_dir or under a nested folder
    # (e.g. report_dir/benchsimulation-<ts>/js). Try direct first, then one-level nested dirs.
    def try_parse_js(js_dir: Path) -> Optional[Dict[str, Any]]:
        if not js_dir.exists():
            return None
        # try json files first
        for f in js_dir.glob('*.json'):
            try:
                return json.loads(f.read_text(encoding='utf-8'))
            except Exception:
                continue
        # fallback to extracting JSON from .js files
        for f in js_dir.glob('*.js'):
            try:
                txt = f.read_text(encoding='utf-8')
                idx = txt.find('{')
                if idx != -1:
                    jtxt = txt[idx:]
                    return json.loads(jtxt)
            except Exception:
                continue
        return None

    # 1) direct js/ under report_dir
    js_dir = report_dir / 'js'
    res = try_parse_js(js_dir)
    if res is not None:
        return res

    # 2) look for a single nested directory that contains js/
    for child in report_dir.iterdir():
        if child.is_dir():
            js_dir = child / 'js'
            res = try_parse_js(js_dir)
            if res is not None:
                return res

    # 3) as a last resort, search recursively one more level deep
    for child in report_dir.iterdir():
        if child.is_dir():
            for sub in child.iterdir():
                if sub.is_dir() and sub.name == 'js':
                    res = try_parse_js(sub)
                    if res is not None:
                        return res

    return None


def extract_percentiles(js: Dict[str, Any]) -> Optional[Dict[float, float]]:
    try:
        stats = js.get('stats') or js
        # locate global stats
        if 'contents' in stats and 'Global Information' in stats['contents']:
            g = stats['contents']['Global Information']['stats']
        elif 'Global Information' in stats:
            g = stats['Global Information']
        else:
            g = stats.get('stats', stats)

        def get_total(key: str) -> float:
            return float(g.get(key, {}).get('total', np.nan))

        # Правильное маппинг перцентилей из Gatling
        # Gatling обычно использует: percentiles1=50%, percentiles2=75%, percentiles3=95%, percentiles4=99%
        p1 = get_total('percentiles1')  # p50
        p2 = get_total('percentiles2')  # p75  
        p3 = get_total('percentiles3')  # p95
        p4 = get_total('percentiles4')  # p99
        
        return {0.5: p1, 0.75: p2, 0.95: p3 / 100, 0.99: p4}
    except Exception:
        return None


def parse_run_dir_name(name: str) -> Optional[Dict[str, Any]]:
    # Expecting <service>-results-<rps>-<timestamp>
    parts = name.split('-')
    if len(parts) < 3:
        return None
    service = parts[0]
    # find first integer part that looks like rps
    rps = None
    for part in parts[2:]:
        try:
            v = int(part)
            if 1 <= v <= 200000:
                rps = v
                break
        except Exception:
            continue
    return {'service': service, 'rps': rps or 0}


def analyze() -> None:
    runs: List[Dict[str, Any]] = []
    for d in sorted(RESULTS_DIR.iterdir()):
        if not d.is_dir():
            continue
        info = parse_run_dir_name(d.name)
        if info is None:
            continue
        js = load_json_from_report(d)
        if js is None:
            print(f"No js/json report found in {d}, skipping")
            continue
        p = extract_percentiles(js)
        runs.append({'service': info['service'], 'rps': info['rps'], 'path': str(d), 'percentiles': p, 'name': d.name})

    if not runs:
        print('No runs found in', RESULTS_DIR)
        sys.exit(1)

    # Build dataframe rows
    rows = []
    for r in runs:
        p = r['percentiles'] or {}
        rows.append({
            'service': r['service'],
            'rps': r['rps'],
            'p50': p.get(0.5, np.nan),
            'p75': p.get(0.75, np.nan),
            'p90': p.get(0.9, np.nan),
            'p95': p.get(0.95, np.nan),
            'p99': p.get(0.99, np.nan),
            'path': r['path'],
            'name': r['name']
        })

    df = pd.DataFrame(rows)
    if df.empty:
        print('No percentile data extracted')
        sys.exit(1)

    # approximate missing p90 by interpolation between p75 and p95 where possible
    def approx_p90(row):
        if not np.isnan(row['p90']):
            return row['p90']
        if np.isnan(row['p75']) or np.isnan(row['p95']):
            return np.nan
        return (0.9 - 0.75) / (0.95 - 0.75) * (row['p95'] - row['p75']) + row['p75']

    df['p90'] = df.apply(approx_p90, axis=1)

    # create output dirs
    per_service_dir = OUT_DIR / 'per_service'
    per_service_dir.mkdir(parents=True, exist_ok=True)

    # plot per-service percentiles vs RPS
    services = sorted(df['service'].unique())
    for svc in services:
        sdf = df[df['service'] == svc].sort_values('rps')
        if sdf.empty:
            continue
        plt.figure(figsize=(10,6))
        plt.plot(sdf['rps'], sdf['p50'], marker='o', label='p50')
        plt.plot(sdf['rps'], sdf['p75'], marker='o', label='p75')
        plt.plot(sdf['rps'], sdf['p90'], marker='o', label='p90')
        plt.plot(sdf['rps'], sdf['p95'], marker='o', label='p95')
        plt.plot(sdf['rps'], sdf['p99'], marker='o', label='p99')
        plt.xlabel('RPS')
        plt.ylabel('response time (ms)')
        plt.title(f'Percentiles vs RPS ({svc})')
        plt.legend()
        plt.grid(True)
        outf = per_service_dir / f'percentiles_vs_rps_{svc}.png'
        plt.savefig(outf)
        plt.close()
        print('Saved', outf)

    # combined plot: service comparison
    plt.figure(figsize=(12,7))
    colors = ['C0','C1','C2','C3','C4']
    for i, svc in enumerate(services):
        sdf = df[df['service'] == svc].sort_values('rps')
        if sdf.empty:
            continue
        plt.plot(sdf['rps'], sdf['p95'], marker='o', label=f'{svc} p95', color=colors[i%len(colors)])
    plt.xlabel('RPS')
    plt.ylabel('response time (ms)')
    plt.title('P95 comparison by service')
    plt.legend()
    plt.grid(True)
    outf = OUT_DIR / 'p95_comparison_by_service.png'
    plt.savefig(outf)
    plt.close()
    print('Saved', outf)

    # per-run histograms/groups if available
    for _, row in df.iterrows():
        pth = Path(row['path'])
        js = load_json_from_report(pth)
        if js is None:
            continue
        try:
            stats = js.get('stats') or js
            if 'contents' in stats and 'Global Information' in stats['contents']:
                g = stats['contents']['Global Information']['stats']
            else:
                g = stats.get('stats', stats)
            groups = [g.get(k) for k in ('group1','group2','group3','group4') if g.get(k)]
            if groups:
                labels = [gr.get('name') for gr in groups]
                counts = [gr.get('count',0) for gr in groups]
                plt.figure(figsize=(8,4))
                plt.bar(labels, counts)
                plt.title(f'Histogram groups {row["name"]}')
                outf = per_service_dir / f'hist_groups_{row["name"]}.png'
                plt.savefig(outf)
                plt.close()
                print('Saved', outf)
        except Exception:
            continue

    print('Analysis complete. Output in', OUT_DIR)


if __name__ == '__main__':
    analyze()
