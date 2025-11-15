import json
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import pandas as pd
import numpy as np
import os
from glob import glob

class BenchmarkRPSAnalyzer:
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
    
    def extract_aggregated_data(self):
        """Извлечение и агрегация данных по RPS"""
        aggregated_data = {}
        
        for filename, file_data in self.data.items():
            service_type, metric_type, load = self.parse_filename(filename)
            
            result_data = file_data['raw']['data']['result']
            if result_data:
                values = result_data[0]['values']
                metric_values = [float(point[1]) for point in values]
                
                # Конвертируем память в MB для удобства чтения
                if metric_type == 'mem':
                    metric_values = [v / 1024 / 1024 for v in metric_values]  # Bytes to MB
                
                # Ключ для агрегации
                key = (service_type, metric_type, load)
                
                if key not in aggregated_data:
                    aggregated_data[key] = []
                
                aggregated_data[key].extend(metric_values)
        
        # Вычисляем статистики для каждого ключа
        result = []
        for (service_type, metric_type, load), values in aggregated_data.items():
            values_array = np.array(values)
            
            result.append({
                'service_type': service_type,
                'metric_type': metric_type,
                'load_rps': load,
                'mean': np.mean(values_array),
                'median': np.median(values_array),
                'std': np.std(values_array),
                'p95': np.percentile(values_array, 95),
                'p99': np.percentile(values_array, 99),
                'min': np.min(values_array),
                'max': np.max(values_array),
                'count': len(values_array)
            })
        
        return result
    
    def create_rps_comparison_dashboard(self):
        """Создание дашборда сравнения по RPS"""
        aggregated_data = self.extract_aggregated_data()
        df = pd.DataFrame(aggregated_data)
        
        # Создаем подграфики
        fig = make_subplots(
            rows=2, cols=2,
            subplot_titles=(
                'Average CPU Usage vs RPS',
                'Average Memory Usage vs RPS',
                'P95 CPU Usage vs RPS', 
                'P95 Memory Usage vs RPS'
            ),
            specs=[
                [{"secondary_y": False}, {"secondary_y": False}],
                [{"secondary_y": False}, {"secondary_y": False}]
            ],
            vertical_spacing=0.12,
            horizontal_spacing=0.08
        )
        
        # Цвета для сервисов
        service_colors = {
            'common': '#1f77b4',
            'reactive': '#ff7f0e'
        }
        
        marker_symbols = {
            'common': 'circle',
            'reactive': 'square'
        }
        
        # 1. Average CPU Usage vs RPS
        cpu_data = df[df['metric_type'] == 'cpu']
        for service in ['common', 'reactive']:
            service_data = cpu_data[cpu_data['service_type'] == service].sort_values('load_rps')
            color = service_colors[service]
            
            fig.add_trace(
                go.Scatter(
                    x=service_data['load_rps'],
                    y=service_data['mean'],
                    name=f'{service.title()} - Avg CPU',
                    mode='lines+markers',
                    line=dict(color=color, width=3),
                    marker=dict(
                        symbol=marker_symbols[service],
                        size=10,
                        line=dict(width=2, color='white')
                    ),
                    error_y=dict(
                        type='data',
                        array=service_data['std'],
                        visible=True,
                        color=color,
                        thickness=1.5,
                        width=3
                    )
                ),
                row=1, col=1
            )
        
        # 2. Average Memory Usage vs RPS
        mem_data = df[df['metric_type'] == 'mem']
        for service in ['common', 'reactive']:
            service_data = mem_data[mem_data['service_type'] == service].sort_values('load_rps')
            color = service_colors[service]
            
            fig.add_trace(
                go.Scatter(
                    x=service_data['load_rps'],
                    y=service_data['mean'],
                    name=f'{service.title()} - Avg Memory',
                    mode='lines+markers',
                    line=dict(color=color, width=3),
                    marker=dict(
                        symbol=marker_symbols[service],
                        size=10,
                        line=dict(width=2, color='white')
                    ),
                    error_y=dict(
                        type='data',
                        array=service_data['std'],
                        visible=True,
                        color=color,
                        thickness=1.5,
                        width=3
                    ),
                    showlegend=False
                ),
                row=1, col=2
            )
        
        # 3. P95 CPU Usage vs RPS
        for service in ['common', 'reactive']:
            service_data = cpu_data[cpu_data['service_type'] == service].sort_values('load_rps')
            color = service_colors[service]
            
            fig.add_trace(
                go.Scatter(
                    x=service_data['load_rps'],
                    y=service_data['p95'],
                    name=f'{service.title()} - P95 CPU',
                    mode='lines+markers',
                    line=dict(color=color, width=3, dash='dot'),
                    marker=dict(
                        symbol=marker_symbols[service],
                        size=8,
                        line=dict(width=2, color='white')
                    ),
                    showlegend=False
                ),
                row=2, col=1
            )
        
        # 4. P95 Memory Usage vs RPS
        for service in ['common', 'reactive']:
            service_data = mem_data[mem_data['service_type'] == service].sort_values('load_rps')
            color = service_colors[service]
            
            fig.add_trace(
                go.Scatter(
                    x=service_data['load_rps'],
                    y=service_data['p95'],
                    name=f'{service.title()} - P95 Memory',
                    mode='lines+markers',
                    line=dict(color=color, width=3, dash='dot'),
                    marker=dict(
                        symbol=marker_symbols[service],
                        size=8,
                        line=dict(width=2, color='white')
                    ),
                    showlegend=False
                ),
                row=2, col=2
            )
        
        # Обновление layout
        fig.update_layout(
            title_text="Benchmark Analysis: Performance vs Request Load (RPS)",
            height=800,
            showlegend=True,
            legend=dict(
                orientation="h",
                yanchor="bottom",
                y=1.02,
                xanchor="right",
                x=1
            ),
            template="plotly_white"
        )
        
        # Обновление осей
        fig.update_xaxes(title_text="Request Load (RPS)", row=1, col=1)
        fig.update_xaxes(title_text="Request Load (RPS)", row=1, col=2)
        fig.update_xaxes(title_text="Request Load (RPS)", row=2, col=1)
        fig.update_xaxes(title_text="Request Load (RPS)", row=2, col=2)
        
        fig.update_yaxes(title_text="CPU Usage (%)", row=1, col=1)
        fig.update_yaxes(title_text="Memory Usage (MB)", row=1, col=2)
        fig.update_yaxes(title_text="CPU Usage (%)", row=2, col=1)
        fig.update_yaxes(title_text="Memory Usage (MB)", row=2, col=2)
        
        return fig
    
    def create_efficiency_analysis(self):
        """Анализ эффективности использования ресурсов"""
        aggregated_data = self.extract_aggregated_data()
        df = pd.DataFrame(aggregated_data)
        
        # Вычисляем эффективность (производительность на единицу ресурсов)
        efficiency_data = []
        for service in ['common', 'reactive']:
            service_data = df[df['service_type'] == service]
            
            for _, row in service_data.iterrows():
                if row['metric_type'] == 'cpu':
                    # Эффективность CPU: RPS / CPU usage
                    efficiency = row['load_rps'] / max(row['mean'], 0.1)  # избегаем деления на 0
                    efficiency_data.append({
                        'service_type': service,
                        'load_rps': row['load_rps'],
                        'metric': 'cpu_efficiency',
                        'value': efficiency,
                        'label': f"RPS/CPU%"
                    })
                elif row['metric_type'] == 'mem':
                    # Эффективность памяти: RPS / Memory MB
                    efficiency = row['load_rps'] / max(row['mean'], 1)  # избегаем деления на 0
                    efficiency_data.append({
                        'service_type': service,
                        'load_rps': row['load_rps'],
                        'metric': 'memory_efficiency',
                        'value': efficiency,
                        'label': f"RPS/MemoryMB"
                    })
        
        efficiency_df = pd.DataFrame(efficiency_data)
        
        # Создаем график эффективности
        fig = make_subplots(
            rows=1, cols=2,
            subplot_titles=(
                'CPU Efficiency (RPS per 1% CPU)',
                'Memory Efficiency (RPS per 1MB Memory)'
            )
        )
        
        service_colors = {'common': '#1f77b4', 'reactive': '#ff7f0e'}
        
        # CPU Efficiency
        for service in ['common', 'reactive']:
            service_data = efficiency_df[
                (efficiency_df['service_type'] == service) & 
                (efficiency_df['metric'] == 'cpu_efficiency')
            ].sort_values('load_rps')
            
            color = service_colors[service]
            
            fig.add_trace(
                go.Scatter(
                    x=service_data['load_rps'],
                    y=service_data['value'],
                    name=f'{service.title()} CPU Eff.',
                    mode='lines+markers',
                    line=dict(color=color, width=3),
                    marker=dict(size=10)
                ),
                row=1, col=1
            )
        
        # Memory Efficiency
        for service in ['common', 'reactive']:
            service_data = efficiency_df[
                (efficiency_df['service_type'] == service) & 
                (efficiency_df['metric'] == 'memory_efficiency')
            ].sort_values('load_rps')
            
            color = service_colors[service]
            
            fig.add_trace(
                go.Scatter(
                    x=service_data['load_rps'],
                    y=service_data['value'],
                    name=f'{service.title()} Memory Eff.',
                    mode='lines+markers',
                    line=dict(color=color, width=3),
                    marker=dict(size=10),
                    showlegend=False
                ),
                row=1, col=2
            )
        
        fig.update_layout(
            title_text="Resource Efficiency Analysis",
            height=500,
            showlegend=True
        )
        
        fig.update_xaxes(title_text="Request Load (RPS)", row=1, col=1)
        fig.update_xaxes(title_text="Request Load (RPS)", row=1, col=2)
        fig.update_yaxes(title_text="RPS per 1% CPU", row=1, col=1)
        fig.update_yaxes(title_text="RPS per 1MB Memory", row=1, col=2)
        
        return fig
    
    def create_statistics_table(self):
        """Создание таблицы со статистикой"""
        aggregated_data = self.extract_aggregated_data()
        df = pd.DataFrame(aggregated_data)
        
        # Форматируем данные для таблицы
        table_data = []
        for _, row in df.iterrows():
            if row['metric_type'] == 'cpu':
                unit = '%'
            else:
                unit = 'MB'
                
            table_data.append({
                'Service': row['service_type'].title(),
                'Metric': row['metric_type'].upper(),
                'Load RPS': row['load_rps'],
                f'Mean ({unit})': f"{row['mean']:.2f}",
                f'P95 ({unit})': f"{row['p95']:.2f}",
                f'Std Dev ({unit})': f"{row['std']:.2f}",
                'Samples': row['count']
            })
        
        table_df = pd.DataFrame(table_data)
        
        fig = go.Figure(data=[go.Table(
            header=dict(
                values=list(table_df.columns),
                fill_color='#2E86AB',
                align='left',
                font=dict(color='white', size=12)
            ),
            cells=dict(
                values=[table_df[col] for col in table_df.columns],
                fill_color='#F5F5F5',
                align='left',
                font=dict(size=11)
            ))
        ])
        
        fig.update_layout(
            title="Performance Statistics by Request Load",
            height=400,
            margin=dict(l=10, r=10, t=60, b=10)
        )
        
        return fig

# Использование
if __name__ == "__main__":
    analyzer = BenchmarkRPSAnalyzer()
    
    # Загрузка данных
    file_patterns = [
        "analysis/prometheus/*.json",
        "auth-service-*-results-*.json"
    ]
    
    analyzer.load_data(file_patterns)
    
    print("Создание дашборда сравнения по RPS...")
    rps_dashboard = analyzer.create_rps_comparison_dashboard()
    rps_dashboard.show()
    
    print("Создание анализа эффективности...")
    efficiency_chart = analyzer.create_efficiency_analysis()
    efficiency_chart.show()
    
    print("Создание таблицы статистики...")
    stats_table = analyzer.create_statistics_table()
    stats_table.show()
    
    # Дополнительный анализ: тренды роста
    aggregated_data = analyzer.extract_aggregated_data()
    df = pd.DataFrame(aggregated_data)
    
    print("\n=== АНАЛИЗ ТРЕНДОВ ===")
    for service in ['common', 'reactive']:
        service_data = df[df['service_type'] == service]
        cpu_data = service_data[service_data['metric_type'] == 'cpu'].sort_values('load_rps')
        mem_data = service_data[service_data['metric_type'] == 'mem'].sort_values('load_rps')
        
        print(f"\n{service.upper()} Service:")
        print(f"CPU рост от 100 до 400 RPS: {cpu_data['mean'].iloc[-1] / cpu_data['mean'].iloc[0]:.1f}x")
        print(f"Memory рост от 100 до 400 RPS: {mem_data['mean'].iloc[-1] / mem_data['mean'].iloc[0]:.1f}x")
