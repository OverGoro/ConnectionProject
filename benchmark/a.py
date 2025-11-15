import json
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import pandas as pd
import numpy as np
import os
from glob import glob

class BenchmarkAnalyzer:
    def __init__(self):
        self.data = {}
        
    def load_data(self, file_patterns):
        """Загрузка данных из JSON файлов"""
        for pattern in file_patterns:
            files = glob(pattern)
            for file_path in files:
                filename = os.path.basename(file_path)
                with open(file_path, 'r') as f:
                    try:
                        content = json.load(f)
                        self.data[filename] = {
                            'raw': content,
                            'filename': filename
                        }
                    except json.JSONDecodeError as e:
                        print(f"Ошибка чтения {filename}: {e}")
    
    def parse_filename(self, filename):
        """Парсинг имени файла для извлечения метаданных"""
        parts = filename.split('-')
        service_type = 'common' if 'common' in filename else 'reactive'
        metric_type = 'cpu' if '_cpu.json' in filename else 'mem'
        
        # Извлекаем нагрузку (100, 200, 300, 400)
        for part in parts:
            if part.isdigit() and len(part) == 3:
                load = int(part)
                break
        else:
            load = 100  # значение по умолчанию
            
        return service_type, metric_type, load
    
    def extract_time_series_data(self):
        """Извлечение временных рядов из данных"""
        time_series_data = []
        
        for filename, file_data in self.data.items():
            service_type, metric_type, load = self.parse_filename(filename)
            
            result_data = file_data['raw']['data']['result']
            if result_data:
                values = result_data[0]['values']
                
                timestamps = [int(point[0]) for point in values]
                # Нормализуем временные метки относительно начала теста
                start_time = min(timestamps)
                normalized_timestamps = [ts - start_time for ts in timestamps]
                
                metric_values = [float(point[1]) for point in values]
                
                # Конвертируем память в MB для удобства чтения
                if metric_type == 'mem':
                    metric_values = [v / 1024 / 1024 for v in metric_values]  # Bytes to MB
                
                time_series_data.append({
                    'service_type': service_type,
                    'metric_type': metric_type,
                    'load': load,
                    'timestamps': normalized_timestamps,
                    'values': metric_values,
                    'filename': filename
                })
        
        return time_series_data
    
    def calculate_percentiles(self, values):
        """Расчет процентилей"""
        return {
            'min': np.min(values),
            'p10': np.percentile(values, 10),
            'p25': np.percentile(values, 25),
            'p50': np.percentile(values, 50),
            'p75': np.percentile(values, 75),
            'p90': np.percentile(values, 90),
            'p95': np.percentile(values, 95),
            'p99': np.percentile(values, 99),
            'max': np.max(values)
        }
    
    def create_comparison_dashboard(self):
        """Создание комплексной дашборда анализа"""
        time_series_data = self.extract_time_series_data()
        
        # Создаем подграфики
        fig = make_subplots(
            rows=3, cols=2,
            subplot_titles=(
                'CPU Usage - Time Series',
                'Memory Usage - Time Series',
                'CPU Percentiles by Load',
                'Memory Percentiles by Load',
                'CPU Comparison: Common vs Reactive',
                'Memory Comparison: Common vs Reactive'
            ),
            specs=[
                [{"secondary_y": False}, {"secondary_y": False}],
                [{"secondary_y": False}, {"secondary_y": False}],
                [{"secondary_y": False}, {"secondary_y": False}]
            ],
            vertical_spacing=0.08,
            horizontal_spacing=0.08
        )
        
        # Цвета для разных нагрузок
        load_colors = {
            100: '#1f77b4',
            200: '#ff7f0e', 
            300: '#2ca02c',
            400: '#d62728'
        }
        
        # 1. CPU Usage - Time Series
        cpu_data = [d for d in time_series_data if d['metric_type'] == 'cpu']
        for data in cpu_data:
            color = load_colors[data['load']]
            name = f"{data['service_type'].title()} {data['load']} RPS"
            
            fig.add_trace(
                go.Scatter(
                    x=data['timestamps'],
                    y=data['values'],
                    name=name,
                    line=dict(color=color),
                    opacity=0.7,
                    legendgroup=data['service_type'],
                    showlegend=True
                ),
                row=1, col=1
            )
        
        # 2. Memory Usage - Time Series  
        mem_data = [d for d in time_series_data if d['metric_type'] == 'mem']
        for data in mem_data:
            color = load_colors[data['load']]
            name = f"{data['service_type'].title()} {data['load']} RPS"
            
            fig.add_trace(
                go.Scatter(
                    x=data['timestamps'],
                    y=data['values'],
                    name=name,
                    line=dict(color=color),
                    opacity=0.7,
                    legendgroup=data['service_type'],
                    showlegend=False
                ),
                row=1, col=2
            )
        
        # 3. CPU Percentiles by Load
        cpu_percentiles_data = {}
        for data in cpu_data:
            key = (data['service_type'], data['load'])
            if key not in cpu_percentiles_data:
                cpu_percentiles_data[key] = []
            cpu_percentiles_data[key].extend(data['values'])
        
        for (service_type, load), values in cpu_percentiles_data.items():
            percentiles = self.calculate_percentiles(values)
            color = load_colors[load]
            
            fig.add_trace(
                go.Box(
                    y=list(percentiles.values()),
                    x=[f"{service_type}\n{load}RPS"] * len(percentiles),
                    name=f"{service_type} {load}RPS",
                    marker_color=color,
                    boxpoints=False,
                    showlegend=False
                ),
                row=2, col=1
            )
        
        # 4. Memory Percentiles by Load
        mem_percentiles_data = {}
        for data in mem_data:
            key = (data['service_type'], data['load'])
            if key not in mem_percentiles_data:
                mem_percentiles_data[key] = []
            mem_percentiles_data[key].extend(data['values'])
        
        for (service_type, load), values in mem_percentiles_data.items():
            percentiles = self.calculate_percentiles(values)
            color = load_colors[load]
            
            fig.add_trace(
                go.Box(
                    y=list(percentiles.values()),
                    x=[f"{service_type}\n{load}RPS"] * len(percentiles),
                    name=f"{service_type} {load}RPS",
                    marker_color=color,
                    boxpoints=False,
                    showlegend=False
                ),
                row=2, col=2
            )
        
        # 5. CPU Comparison
        comparison_data = {}
        for data in cpu_data:
            key = data['service_type']
            if key not in comparison_data:
                comparison_data[key] = []
            comparison_data[key].extend(data['values'])
        
        for service_type, values in comparison_data.items():
            color = '#1f77b4' if service_type == 'common' else '#ff7f0e'
            
            fig.add_trace(
                go.Violin(
                    y=values,
                    x=[service_type.title()] * len(values),
                    name=f"{service_type.title()} CPU",
                    box_visible=True,
                    meanline_visible=True,
                    fillcolor=color,
                    line_color='black',
                    opacity=0.6,
                    showlegend=False
                ),
                row=3, col=1
            )
        
        # 6. Memory Comparison
        comparison_data_mem = {}
        for data in mem_data:
            key = data['service_type']
            if key not in comparison_data_mem:
                comparison_data_mem[key] = []
            comparison_data_mem[key].extend(data['values'])
        
        for service_type, values in comparison_data_mem.items():
            color = '#1f77b4' if service_type == 'common' else '#ff7f0e'
            
            fig.add_trace(
                go.Violin(
                    y=values,
                    x=[service_type.title()] * len(values),
                    name=f"{service_type.title()} Memory",
                    box_visible=True,
                    meanline_visible=True,
                    fillcolor=color,
                    line_color='black',
                    opacity=0.6,
                    showlegend=False
                ),
                row=3, col=2
            )
        
        # Обновление layout
        fig.update_layout(
            title_text="Benchmark Analysis: Auth Service Common vs Reactive",
            height=1200,
            showlegend=True,
            legend=dict(orientation="h", yanchor="bottom", y=1.02, xanchor="right", x=1)
        )
        
        # Обновление осей
        fig.update_yaxes(title_text="CPU Usage (%)", row=1, col=1)
        fig.update_yaxes(title_text="Memory Usage (MB)", row=1, col=2)
        fig.update_yaxes(title_text="CPU Usage (%)", row=2, col=1)
        fig.update_yaxes(title_text="Memory Usage (MB)", row=2, col=2)
        fig.update_yaxes(title_text="CPU Usage (%)", row=3, col=1)
        fig.update_yaxes(title_text="Memory Usage (MB)", row=3, col=2)
        
        fig.update_xaxes(title_text="Time (seconds)", row=1, col=1)
        fig.update_xaxes(title_text="Time (seconds)", row=1, col=2)
        fig.update_xaxes(title_text="Service & Load", row=2, col=1)
        fig.update_xaxes(title_text="Service & Load", row=2, col=2)
        fig.update_xaxes(title_text="Service Type", row=3, col=1)
        fig.update_xaxes(title_text="Service Type", row=3, col=2)
        
        return fig
    
    def create_percentile_table(self):
        """Создание таблицы с процентилями"""
        time_series_data = self.extract_time_series_data()
        
        percentile_data = []
        for data in time_series_data:
            percentiles = self.calculate_percentiles(data['values'])
            
            percentile_data.append({
                'Service': data['service_type'].title(),
                'Metric': data['metric_type'].upper(),
                'Load (RPS)': data['load'],
                'Min': f"{percentiles['min']:.2f}",
                'P10': f"{percentiles['p10']:.2f}",
                'P50': f"{percentiles['p50']:.2f}",
                'P90': f"{percentiles['p90']:.2f}",
                'P95': f"{percentiles['p95']:.2f}",
                'P99': f"{percentiles['p99']:.2f}",
                'Max': f"{percentiles['max']:.2f}"
            })
        
        df = pd.DataFrame(percentile_data)
        
        fig = go.Figure(data=[go.Table(
            header=dict(values=list(df.columns),
                       fill_color='paleturquoise',
                       align='left'),
            cells=dict(values=[df[col] for col in df.columns],
                      fill_color='lavender',
                      align='left'))
        ])
        
        fig.update_layout(
            title="Percentile Analysis Summary",
            height=400
        )
        
        return fig

# Использование
if __name__ == "__main__":
    analyzer = BenchmarkAnalyzer()
    
    # Загрузка данных
    file_patterns = [
        "analysis/prometheus/*.json",
        "auth-service-*-results-*.json"
    ]
    
    analyzer.load_data(file_patterns)
    
    # Создание дашборда
    dashboard = analyzer.create_comparison_dashboard()
    dashboard.show()
    
    # Создание таблицы процентилей
    percentile_table = analyzer.create_percentile_table()
    percentile_table.show()
