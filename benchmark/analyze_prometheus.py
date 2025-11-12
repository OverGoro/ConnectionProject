#!/usr/bin/env python3
"""
Анализатор метрик Prometheus для нагрузочного тестирования
Адаптирован для gateway-service
"""

import json
import glob
import os
import re
from datetime import datetime
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
from pathlib import Path

class PrometheusMetricsAnalyzer:
    def __init__(self, results_dir="./gatling-results", output_dir="./analysis/prometheus"):
        self.results_dir = Path(results_dir)
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        
        # Цвета для графиков - только gateway-service
        self.colors = {
            'gateway-service': '#1f77b4'
        }
        
    def parse_directory_structure(self):
        """Парсит структуру директорий и извлекает RPS из названий папок"""
        pattern = r"(gateway-service)-results-(\d+)-(\d{8}T\d{6})"
        results = []
        
        for dir_path in self.results_dir.glob("*-results-*"):
            dir_name = dir_path.name
            match = re.match(pattern, dir_name)
            if match:
                service = match.group(1)
                rps = int(match.group(2))
                timestamp = match.group(3)
                
                # Ищем JSON файлы с метриками
                cpu_files = list(dir_path.glob("*_cpu.json"))
                mem_files = list(dir_path.glob("*_mem.json"))
                
                results.append({
                    'service': service,
                    'rps': rps,
                    'timestamp': timestamp,
                    'dir_path': dir_path,
                    'cpu_files': cpu_files,
                    'mem_files': mem_files
                })
        
        return sorted(results, key=lambda x: (x['service'], x['rps']))
    
    def load_prometheus_json(self, file_path):
        """Загружает JSON данные из Prometheus API response"""
        try:
            with open(file_path, 'r') as f:
                data = json.load(f)
            
            if data['status'] != 'success':
                print(f"Warning: Status not success in {file_path}")
                return None
            
            results = data['data']['result']
            if not results:
                print(f"Warning: No data in {file_path}")
                return None
            
            # Извлекаем временные ряды
            timestamps = []
            values = []
            
            for result in results:
                for value in result['values']:
                    timestamp = float(value[0])
                    val = float(value[1])
                    timestamps.append(timestamp)
                    values.append(val)
            
            return {
                'timestamps': timestamps,
                'values': values,
                'timestamps_datetime': [datetime.fromtimestamp(ts) for ts in timestamps]
            }
        except Exception as e:
            print(f"Error loading {file_path}: {e}")
            return None
    
    def calculate_metrics_stats(self, data):
        """Вычисляет статистики для временного ряда"""
        if not data or not data['values']:
            return None
        
        values = data['values']
        return {
            'mean': np.mean(values),
            'max': np.max(values),
            'min': np.min(values),
            'p95': np.percentile(values, 95),
            'p99': np.percentile(values, 99),
            'std': np.std(values)
        }
    
    def aggregate_metrics_by_rps(self, directory_data):
        """Агрегирует метрики по RPS и сервисам"""
        aggregated = {}
        
        for item in directory_data:
            service = item['service']
            rps = item['rps']
            
            if service not in aggregated:
                aggregated[service] = {}
            
            # Обрабатываем CPU метрики
            cpu_data = None
            for cpu_file in item['cpu_files']:
                data = self.load_prometheus_json(cpu_file)
                if data:
                    cpu_data = data
                    break
            
            # Обрабатываем Memory метрики
            mem_data = None
            for mem_file in item['mem_files']:
                data = self.load_prometheus_json(mem_file)
                if data:
                    mem_data = data
                    break
            
            aggregated[service][rps] = {
                'cpu': self.calculate_metrics_stats(cpu_data) if cpu_data else None,
                'memory': self.calculate_metrics_stats(mem_data) if mem_data else None,
                'raw_cpu': cpu_data,
                'raw_memory': mem_data
            }
        
        return aggregated
    
    def plot_cpu_comparison(self, aggregated_data):
        """Строит график сравнения CPU использования"""
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 6))
        
        for service in aggregated_data:
            rps_values = []
            cpu_mean = []
            cpu_max = []
            cpu_p95 = []
            
            for rps in sorted(aggregated_data[service].keys()):
                cpu_stats = aggregated_data[service][rps]['cpu']
                if cpu_stats:
                    rps_values.append(rps)
                    cpu_mean.append(cpu_stats['mean'])
                    cpu_max.append(cpu_stats['max'])
                    cpu_p95.append(cpu_stats['p95'])
            
            color = self.colors.get(service, 'gray')
            ax1.plot(rps_values, cpu_mean, 'o-', label=f'{service} (mean)', color=color, linewidth=2)
            ax1.plot(rps_values, cpu_p95, 's--', label=f'{service} (p95)', color=color, alpha=0.7)
            ax2.plot(rps_values, cpu_max, '^-', label=f'{service} (max)', color=color, linewidth=2)
        
        ax1.set_xlabel('RPS')
        ax1.set_ylabel('CPU Usage (%)')
        ax1.set_title('Gateway Service CPU Usage: Mean and P95')
        ax1.legend()
        ax1.grid(True, alpha=0.3)
        
        ax2.set_xlabel('RPS')
        ax2.set_ylabel('CPU Usage (%)')
        ax2.set_title('Gateway Service CPU Usage: Maximum')
        ax2.legend()
        ax2.grid(True, alpha=0.3)
        
        plt.tight_layout()
        plt.savefig(self.output_dir / 'cpu_comparison.png', dpi=300, bbox_inches='tight')
        plt.close()
    
    def plot_memory_comparison(self, aggregated_data):
        """Строит график сравнения использования памяти"""
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 6))
        
        for service in aggregated_data:
            rps_values = []
            mem_mean_mb = []
            mem_max_mb = []
            
            for rps in sorted(aggregated_data[service].keys()):
                mem_stats = aggregated_data[service][rps]['memory']
                if mem_stats:
                    rps_values.append(rps)
                    # Конвертируем байты в мегабайты
                    mem_mean_mb.append(mem_stats['mean'] / 1024 / 1024)
                    mem_max_mb.append(mem_stats['max'] / 1024 / 1024)
            
            color = self.colors.get(service, 'gray')
            ax1.plot(rps_values, mem_mean_mb, 'o-', label=f'{service} (mean)', color=color, linewidth=2)
            ax2.plot(rps_values, mem_max_mb, 's-', label=f'{service} (max)', color=color, linewidth=2)
        
        ax1.set_xlabel('RPS')
        ax1.set_ylabel('Memory Usage (MB)')
        ax1.set_title('Gateway Service Memory Usage: Mean')
        ax1.legend()
        ax1.grid(True, alpha=0.3)
        
        ax2.set_xlabel('RPS')
        ax2.set_ylabel('Memory Usage (MB)')
        ax2.set_title('Gateway Service Memory Usage: Maximum')
        ax2.legend()
        ax2.grid(True, alpha=0.3)
        
        plt.tight_layout()
        plt.savefig(self.output_dir / 'memory_comparison.png', dpi=300, bbox_inches='tight')
        plt.close()
    
    def plot_time_series_examples(self, aggregated_data, sample_rps=[100, 500, 1000]):
        """Строит временные ряды для примеров RPS"""
        for rps in sample_rps:
            fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(12, 8))
            
            for service in aggregated_data:
                if rps in aggregated_data[service]:
                    color = self.colors.get(service, 'gray')
                    
                    # CPU временной ряд
                    cpu_data = aggregated_data[service][rps]['raw_cpu']
                    if cpu_data:
                        ax1.plot(cpu_data['timestamps_datetime'], cpu_data['values'], 
                                label=f'{service}', color=color, linewidth=1.5)
                    
                    # Memory временной ряд
                    mem_data = aggregated_data[service][rps]['raw_memory']
                    if mem_data:
                        # Конвертируем в MB
                        mem_values_mb = [v / 1024 / 1024 for v in mem_data['values']]
                        ax2.plot(mem_data['timestamps_datetime'], mem_values_mb,
                                label=f'{service}', color=color, linewidth=1.5)
            
            ax1.set_ylabel('CPU Usage (%)')
            ax1.set_title(f'Gateway Service CPU Usage Over Time - {rps} RPS')
            ax1.legend()
            ax1.grid(True, alpha=0.3)
            
            ax2.set_ylabel('Memory Usage (MB)')
            ax2.set_xlabel('Time')
            ax2.set_title(f'Gateway Service Memory Usage Over Time - {rps} RPS')
            ax2.legend()
            ax2.grid(True, alpha=0.3)
            
            plt.tight_layout()
            plt.savefig(self.output_dir / f'time_series_{rps}_rps.png', dpi=300, bbox_inches='tight')
            plt.close()
    
    def create_summary_table(self, aggregated_data):
        """Создает сводную таблицу с метриками"""
        rows = []
        
        for service in aggregated_data:
            for rps in sorted(aggregated_data[service].keys()):
                cpu_stats = aggregated_data[service][rps]['cpu']
                mem_stats = aggregated_data[service][rps]['memory']
                
                row = {
                    'service': service,
                    'rps': rps
                }
                
                if cpu_stats:
                    row.update({
                        'cpu_mean': f"{cpu_stats['mean']:.2f}%",
                        'cpu_p95': f"{cpu_stats['p95']:.2f}%",
                        'cpu_max': f"{cpu_stats['max']:.2f}%"
                    })
                else:
                    row.update({
                        'cpu_mean': 'N/A',
                        'cpu_p95': 'N/A',
                        'cpu_max': 'N/A'
                    })
                
                if mem_stats:
                    row.update({
                        'mem_mean': f"{mem_stats['mean'] / 1024 / 1024:.2f} MB",
                        'mem_max': f"{mem_stats['max'] / 1024 / 1024:.2f} MB"
                    })
                else:
                    row.update({
                        'mem_mean': 'N/A',
                        'mem_max': 'N/A'
                    })
                
                rows.append(row)
        
        # Сохраняем как CSV
        df = pd.DataFrame(rows)
        csv_path = self.output_dir / 'metrics_summary.csv'
        df.to_csv(csv_path, index=False)
        
        # Создаем текстовый отчет
        report_path = self.output_dir / 'metrics_report.txt'
        with open(report_path, 'w') as f:
            f.write("Gateway Service Prometheus Metrics Analysis Report\n")
            f.write("=" * 60 + "\n\n")
            
            for service in aggregated_data:
                f.write(f"Service: {service}\n")
                f.write("-" * 40 + "\n")
                
                for rps in sorted(aggregated_data[service].keys()):
                    cpu_stats = aggregated_data[service][rps]['cpu']
                    mem_stats = aggregated_data[service][rps]['memory']
                    
                    f.write(f"RPS: {rps}\n")
                    if cpu_stats:
                        f.write(f"  CPU - Mean: {cpu_stats['mean']:.2f}%, P95: {cpu_stats['p95']:.2f}%, Max: {cpu_stats['max']:.2f}%\n")
                    if mem_stats:
                        f.write(f"  Memory - Mean: {mem_stats['mean'] / 1024 / 1024:.2f} MB, Max: {mem_stats['max'] / 1024 / 1024:.2f} MB\n")
                    f.write("\n")
        
        return df
    
    def plot_efficiency_comparison(self, aggregated_data):
        """Строит график эффективности (RPS на процент CPU)"""
        plt.figure(figsize=(10, 6))
        
        for service in aggregated_data:
            rps_values = []
            efficiency = []
            
            for rps in sorted(aggregated_data[service].keys()):
                cpu_stats = aggregated_data[service][rps]['cpu']
                if cpu_stats and cpu_stats['mean'] > 0:
                    rps_values.append(rps)
                    efficiency.append(rps / cpu_stats['mean'])  # RPS на 1% CPU
            
            color = self.colors.get(service, 'gray')
            plt.plot(rps_values, efficiency, 'o-', label=service, color=color, linewidth=2)
        
        plt.xlabel('RPS')
        plt.ylabel('RPS per 1% CPU')
        plt.title('Gateway Service Efficiency: RPS per CPU Percentage')
        plt.legend()
        plt.grid(True, alpha=0.3)
        
        plt.tight_layout()
        plt.savefig(self.output_dir / 'efficiency_comparison.png', dpi=300, bbox_inches='tight')
        plt.close()
    
    def analyze(self):
        """Основной метод анализа"""
        print("Analyzing Prometheus metrics for Gateway Service...")
        
        # Парсим структуру директорий
        directory_data = self.parse_directory_structure()
        print(f"Found {len(directory_data)} test runs")
        
        # Агрегируем данные
        aggregated_data = self.aggregate_metrics_by_rps(directory_data)
        
        if not aggregated_data:
            print("No data found for analysis")
            return
        
        # Строим графики
        print("Creating comparison plots...")
        self.plot_cpu_comparison(aggregated_data)
        self.plot_memory_comparison(aggregated_data)
        self.plot_time_series_examples(aggregated_data)
        self.plot_efficiency_comparison(aggregated_data)
        
        # Создаем отчеты
        print("Generating reports...")
        summary_df = self.create_summary_table(aggregated_data)
        
        print(f"Analysis complete! Results saved to {self.output_dir}")
        print(f"Summary: {len(summary_df)} data points analyzed")
        
        return aggregated_data, summary_df

def main():
    analyzer = PrometheusMetricsAnalyzer()
    aggregated_data, summary_df = analyzer.analyze()
    
    # Выводим краткую сводку в консоль
    if summary_df is not None:
        print("\nBrief Summary:")
        print(summary_df[['service', 'rps', 'cpu_mean', 'mem_mean']].to_string(index=False))

if __name__ == "__main__":
    main()
