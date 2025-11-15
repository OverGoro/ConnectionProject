import json
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import pandas as pd
import numpy as np
import os
from glob import glob

class GatlingResponseTimeAnalyzer:
    def __init__(self):
        self.data = {}
        
    def extract_response_time_data(self):
        """Извлечение данных о времени ответа из simulation.log файлов"""
        response_data = []
        
        # Ищем simulation.log файлы
        log_files = glob("gatling-results/**/simulation.log", recursive=True)
        print(f"Найдено simulation.log файлов: {len(log_files)}")
        
        for log_file in log_files:
            # Получаем родительскую директорию (где лежит simulation.log)
            simulation_dir = os.path.dirname(log_file)
            # Получаем основную директорию теста (которая содержит RPS в названии)
            test_dir = os.path.basename(os.path.dirname(simulation_dir))
            
            print(f"Обработка: {test_dir}")
            print(f"  simulation.log в: {simulation_dir}")
            
            # Парсим имя основной директории для получения метаданных
            service_type = 'common' if 'common' in test_dir else 'reactive'
            
            # Извлекаем нагрузку RPS из имени основной директории
            # Формат: auth-service-common-results-100-20251115T044259
            load = None
            parts = test_dir.split('-')
            for i, part in enumerate(parts):
                if part.isdigit() and len(part) in [2, 3]:
                    # Проверяем, что это число RPS (100, 150, 200, etc.)
                    potential_rps = int(part)
                    if potential_rps >= 100 and potential_rps <= 500:
                        load = potential_rps
                        break
            
            if load is None:
                print(f"Не удалось определить RPS для директории: {test_dir}")
                continue
                
            print(f"  Сервис: {service_type}, RPS: {load}")
            
            try:
                # Читаем simulation.log и извлекаем данные о времени ответа
                with open(log_file, 'r') as f:
                    lines = f.readlines()
                
                # Собираем времена ответа для каждого типа запроса
                request_times = {
                    'register': [],
                    'login': [],
                    'validate_access': [],
                    'validate_refresh': []
                }
                
                for line in lines:
                    if 'REQUEST' in line and '\t' in line:
                        parts = line.strip().split('\t')
                        if len(parts) >= 7:
                            try:
                                request_name = parts[3].strip()
                                start_time = int(parts[4])
                                end_time = int(parts[5])
                                status = parts[6]
                                
                                # Вычисляем время ответа в миллисекундах
                                response_time = end_time - start_time
                                
                                if request_name in request_times and status == 'OK':
                                    request_times[request_name].append(response_time)
                                    
                            except (ValueError, IndexError) as e:
                                continue
                
                # Вычисляем общую статистику по всем запросам
                all_response_times = []
                request_counts = {}
                for req_name, times in request_times.items():
                    all_response_times.extend(times)
                    request_counts[req_name] = len(times)
                
                if all_response_times:
                    response_times_array = np.array(all_response_times)
                    
                    response_data.append({
                        'service_type': service_type,
                        'load_rps': load,
                        'response_time_mean': np.mean(response_times_array),
                        'response_time_median': np.median(response_times_array),
                        'response_time_p75': np.percentile(response_times_array, 75),
                        'response_time_p95': np.percentile(response_times_array, 95),
                        'response_time_p99': np.percentile(response_times_array, 99),
                        'response_time_min': np.min(response_times_array),
                        'response_time_max': np.max(response_times_array),
                        'requests_total': len(all_response_times),
                        'sample_size': len(all_response_times),
                        'request_counts': request_counts
                    })
                    
                    print(f"  Добавлены данные: {service_type} - {load} RPS")
                    print(f"    Всего запросов: {len(all_response_times)}")
                    print(f"    По типам: {request_counts}")
                    print(f"    Среднее время: {np.mean(response_times_array):.1f}ms")
                    print(f"    P95: {np.percentile(response_times_array, 95):.1f}ms")
                else:
                    print(f"  Нет данных о времени ответа в {test_dir}")
                
            except Exception as e:
                print(f"Ошибка обработки {log_file}: {e}")
                import traceback
                traceback.print_exc()
        
        print(f"Всего извлечено записей: {len(response_data)}")
        return response_data
    
    def create_response_time_dashboard(self):
        """Создание дашборда сравнения времени ответа по RPS"""
        response_data = self.extract_response_time_data()
        
        if not response_data:
            print("Нет данных для построения графиков!")
            # Создаем заглушку
            fig = go.Figure()
            fig.add_annotation(text="Нет данных для отображения",
                             xref="paper", yref="paper",
                             x=0.5, y=0.5, xanchor='center', yanchor='middle',
                             showarrow=False)
            return fig
            
        df = pd.DataFrame(response_data)
        print("Колонки в DataFrame:", df.columns.tolist())
        print("\nДанные:")
        print(df[['service_type', 'load_rps', 'response_time_p95', 'requests_total']])
        
        # Сортируем данные по RPS для корректного отображения линий
        df = df.sort_values(['service_type', 'load_rps'])
        
        # Создаем подграфики
        fig = make_subplots(
            rows=2, cols=2,
            subplot_titles=(
                '95th Percentile Response Time vs RPS',
                'Mean Response Time vs RPS',
                'Median Response Time vs RPS',
                'Max Response Time vs RPS'
            ),
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
        
        # 1. 95th Percentile Response Time vs RPS (основной график)
        for service in ['common', 'reactive']:
            service_data = df[df['service_type'] == service]
            if not service_data.empty:
                color = service_colors[service]
                
                fig.add_trace(
                    go.Scatter(
                        x=service_data['load_rps'],
                        y=service_data['response_time_p95'],
                        name=f'{service.title()} - P95',
                        mode='lines+markers',
                        line=dict(color=color, width=4),
                        marker=dict(
                            symbol=marker_symbols[service],
                            size=12,
                            line=dict(width=2, color='white')
                        )
                    ),
                    row=1, col=1
                )
        
        # 2. Mean Response Time vs RPS
        for service in ['common', 'reactive']:
            service_data = df[df['service_type'] == service]
            if not service_data.empty:
                color = service_colors[service]
                
                fig.add_trace(
                    go.Scatter(
                        x=service_data['load_rps'],
                        y=service_data['response_time_mean'],
                        name=f'{service.title()} - Mean',
                        mode='lines+markers',
                        line=dict(color=color, width=3),
                        marker=dict(
                            symbol=marker_symbols[service],
                            size=10,
                            line=dict(width=2, color='white')
                        ),
                        showlegend=False
                    ),
                    row=1, col=2
                )
        
        # 3. Median Response Time vs RPS
        for service in ['common', 'reactive']:
            service_data = df[df['service_type'] == service]
            if not service_data.empty:
                color = service_colors[service]
                
                fig.add_trace(
                    go.Scatter(
                        x=service_data['load_rps'],
                        y=service_data['response_time_median'],
                        name=f'{service.title()} - Median',
                        mode='lines+markers',
                        line=dict(color=color, width=3, dash='dash'),
                        marker=dict(
                            symbol=marker_symbols[service],
                            size=8,
                            line=dict(width=2, color='white')
                        ),
                        showlegend=False
                    ),
                    row=2, col=1
                )
        
        # 4. Max Response Time vs RPS
        for service in ['common', 'reactive']:
            service_data = df[df['service_type'] == service]
            if not service_data.empty:
                color = service_colors[service]
                
                fig.add_trace(
                    go.Scatter(
                        x=service_data['load_rps'],
                        y=service_data['response_time_max'],
                        name=f'{service.title()} - Max',
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
            title_text="Gatling Benchmark: Response Time Analysis vs Request Load (RPS)",
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
        
        fig.update_yaxes(title_text="Response Time (ms)", row=1, col=1)
        fig.update_yaxes(title_text="Response Time (ms)", row=1, col=2)
        fig.update_yaxes(title_text="Response Time (ms)", row=2, col=1)
        fig.update_yaxes(title_text="Response Time (ms)", row=2, col=2)
        
        return fig
    
    def create_focused_p95_chart(self):
        """Фокусированный график только на 95-м процентиле"""
        response_data = self.extract_response_time_data()
        
        if not response_data:
            print("Нет данных для построения графиков!")
            # Создаем заглушку
            fig = go.Figure()
            fig.add_annotation(text="Нет данных для отображения",
                             xref="paper", yref="paper",
                             x=0.5, y=0.5, xanchor='center', yanchor='middle',
                             showarrow=False)
            return fig
            
        df = pd.DataFrame(response_data)
        df = df.sort_values(['service_type', 'load_rps'])
        
        fig = go.Figure()
        
        service_colors = {
            'common': '#1f77b4',
            'reactive': '#ff7f0e'
        }
        
        line_styles = {
            'common': 'solid',
            'reactive': 'solid'
        }
        
        marker_symbols = {
            'common': 'circle',
            'reactive': 'diamond'
        }
        
        for service in ['common', 'reactive']:
            service_data = df[df['service_type'] == service]
            if not service_data.empty:
                color = service_colors[service]
                
                fig.add_trace(
                    go.Scatter(
                        x=service_data['load_rps'],
                        y=service_data['response_time_p95'],
                        name=f'{service.title()} Service - 95th Percentile',
                        mode='lines+markers',
                        line=dict(color=color, width=4, dash=line_styles[service]),
                        marker=dict(
                            symbol=marker_symbols[service],
                            size=14,
                            line=dict(width=2, color='white')
                        )
                    )
                )
        
        fig.update_layout(
            title="95th Percentile Response Time vs Request Load (RPS)",
            xaxis_title="Request Load (RPS)",
            yaxis_title="Response Time (ms)",
            height=600,
            template="plotly_white",
            showlegend=True,
            legend=dict(
                yanchor="top",
                y=0.99,
                xanchor="left",
                x=0.01
            )
        )
        
        return fig
    
    def create_statistics_table(self):
        """Создание таблицы со статистикой времени ответа"""
        response_data = self.extract_response_time_data()
        
        if not response_data:
            print("Нет данных для таблицы!")
            return go.Figure()
            
        df = pd.DataFrame(response_data)
        
        # Форматируем данные для таблицы
        table_data = []
        for _, row in df.iterrows():
            table_data.append({
                'Service': row['service_type'].title(),
                'Load RPS': row['load_rps'],
                'P95 (ms)': f"{row['response_time_p95']:.1f}",
                'Mean (ms)': f"{row['response_time_mean']:.1f}",
                'Median (ms)': f"{row['response_time_median']:.1f}",
                'Max (ms)': f"{row['response_time_max']:.1f}",
                'Total Requests': row['requests_total'],
                'Sample Size': row['sample_size']
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
            title="Response Time Statistics by Request Load",
            height=500,
            margin=dict(l=10, r=10, t=60, b=10)
        )
        
        return fig

# Использование
if __name__ == "__main__":
    analyzer = GatlingResponseTimeAnalyzer()
    
    print("Начало анализа Gatling результатов...")
    
    print("Создание дашборда времени ответа...")
    response_dashboard = analyzer.create_response_time_dashboard()
    response_dashboard.show()
    
    print("Создание фокусированного графика P95...")
    p95_chart = analyzer.create_focused_p95_chart()
    p95_chart.show()
    
    print("Создание таблицы статистики...")
    stats_table = analyzer.create_statistics_table()
    stats_table.show()
    
    # Дополнительный анализ
    response_data = analyzer.extract_response_time_data()
    if response_data:
        df = pd.DataFrame(response_data)
        
        print("\n=== АНАЛИЗ ПРОИЗВОДИТЕЛЬНОСТИ ===")
        for service in ['common', 'reactive']:
            service_data = df[df['service_type'] == service].sort_values('load_rps')
            
            if not service_data.empty:
                print(f"\n{service.upper()} Service:")
                print(f"Диапазон RPS: {service_data['load_rps'].min()} - {service_data['load_rps'].max()}")
                print(f"P95 Response Time at {service_data['load_rps'].iloc[0]} RPS: {service_data['response_time_p95'].iloc[0]:.1f}ms")
                print(f"P95 Response Time at {service_data['load_rps'].iloc[-1]} RPS: {service_data['response_time_p95'].iloc[-1]:.1f}ms")
                
                if service_data['response_time_p95'].iloc[0] > 0:
                    growth_ratio = service_data['response_time_p95'].iloc[-1] / service_data['response_time_p95'].iloc[0]
                    p95_increase = (service_data['response_time_p95'].iloc[-1] - service_data['response_time_p95'].iloc[0]) / service_data['response_time_p95'].iloc[0] * 100
                    print(f"Рост P95: {growth_ratio:.1f}x ({p95_increase:.1f}%)")
    else:
        print("Нет данных для анализа!")