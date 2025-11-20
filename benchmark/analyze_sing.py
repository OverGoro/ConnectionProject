import json
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import pandas as pd
import numpy as np
import os
import sys

def plot_single_file_metrics(file_path):
    """
    Построение графиков CPU и памяти для одного файла
    """
    try:
        # Загрузка данных
        with open(file_path, 'r') as f:
            data = json.load(f)
        
        filename = os.path.basename(file_path)
        print(f"Анализ файла: {filename}")
        
        # Извлечение данных
        result_data = data['data']['result']
        if not result_data:
            print("Ошибка: Нет данных в файле")
            return
        
        # Создание подграфиков
        fig = make_subplots(
            rows=2, cols=1,
            subplot_titles=(
                f'CPU Usage Over Time - {filename}',
                f'Memory Usage Over Time - {filename}'
            ),
            vertical_spacing=0.1
        )
        
        # Обработка каждого метрика в файле
        for result in result_data:
            metric_name = result['metric'].get('__name__', 'unknown')
            values = result['values']
            
            # Извлечение временных меток и значений
            timestamps = [pd.to_datetime(point[0], unit='s') for point in values]
            metric_values = [float(point[1]) for point in values]
            
            # Конвертация памяти в MB если необходимо
            if 'memory' in metric_name.lower() or 'mem' in metric_name.lower():
                metric_values = [v / 1024 / 1024 for v in metric_values]  # Bytes to MB
                yaxis_title = "Memory Usage (MB)"
                row = 2
            else:
                yaxis_title = "CPU Usage (%)"
                row = 1
            
            # Добавление трассировки
            fig.add_trace(
                go.Scatter(
                    x=timestamps,
                    y=metric_values,
                    name=f"{metric_name}",
                    mode='lines',
                    line=dict(width=2),
                    showlegend=True
                ),
                row=row, col=1
            )
        
        # Обновление layout
        fig.update_layout(
            title_text=f"Resource Usage Analysis: {filename}",
            height=800,
            showlegend=True,
            template="plotly_white"
        )
        
        # Обновление осей
        fig.update_xaxes(title_text="Time", row=1, col=1)
        fig.update_xaxes(title_text="Time", row=2, col=1)
        fig.update_yaxes(title_text="CPU Usage (%)", row=1, col=1)
        fig.update_yaxes(title_text="Memory Usage (MB)", row=2, col=1)
        
        # Показать график
        fig.show()
        
        # Вывод статистики
        print_statistics(data, filename)
        
        return fig
        
    except Exception as e:
        print(f"Ошибка при обработке файла {file_path}: {e}")
        return None

def print_statistics(data, filename):
    """
    Вывод статистики по файлу
    """
    print(f"\n=== СТАТИСТИКА ДЛЯ {filename} ===")
    
    result_data = data['data']['result']
    
    for result in result_data:
        metric_name = result['metric'].get('__name__', 'unknown')
        values = result['values']
        
        if not values:
            continue
            
        metric_values = [float(point[1]) for point in values]
        
        # Конвертация памяти если необходимо
        if 'memory' in metric_name.lower() or 'mem' in metric_name.lower():
            metric_values = [v / 1024 / 1024 for v in metric_values]
            unit = "MB"
        else:
            unit = "%"
        
        values_array = np.array(metric_values)
        
        print(f"\nМетрика: {metric_name}")
        print(f"  Среднее: {np.mean(values_array):.2f} {unit}")
        print(f"  Медиана: {np.median(values_array):.2f} {unit}")
        print(f"  Стандартное отклонение: {np.std(values_array):.2f} {unit}")
        print(f"  P95: {np.percentile(values_array, 95):.2f} {unit}")
        print(f"  Максимум: {np.max(values_array):.2f} {unit}")
        print(f"  Минимум: {np.min(values_array):.2f} {unit}")
        print(f"  Количество точек: {len(values_array)}")
        print(f"  Длительность: {len(values_array) * 15 / 60:.1f} минут")  # предполагая 15s интервал

def plot_combined_metrics(file_path):
    """
    Построение комбинированного графика CPU и памяти на одном графике
    """
    try:
        with open(file_path, 'r') as f:
            data = json.load(f)
        
        filename = os.path.basename(file_path)
        
        result_data = data['data']['result']
        if not result_data:
            print("Ошибка: Нет данных в файле")
            return
        
        fig = go.Figure()
        
        # Обработка метрик
        for result in result_data:
            metric_name = result['metric'].get('__name__', 'unknown')
            values = result['values']
            
            timestamps = [pd.to_datetime(point[0], unit='s') for point in values]
            metric_values = [float(point[1]) for point in values]
            
            # Конвертация и настройка для разных метрик
            if 'memory' in metric_name.lower() or 'mem' in metric_name.lower():
                metric_values = [v / 1024 / 1024 for v in metric_values]  # Bytes to MB
                display_name = "Memory Usage (MB)"
                yaxis = "y2"  # Вторая ось Y
            else:
                display_name = "CPU Usage (%)"
                yaxis = "y1"  # Первая ось Y
            
            fig.add_trace(
                go.Scatter(
                    x=timestamps,
                    y=metric_values,
                    name=display_name,
                    mode='lines',
                    line=dict(width=3),
                    yaxis=yaxis
                )
            )
        
        # Настройка layout с двумя осями Y
        fig.update_layout(
            title=f"Combined Resource Usage: {filename}",
            xaxis=dict(title="Time"),
            yaxis=dict(
                title="CPU Usage (%)",
                titlefont=dict(color="#1f77b4"),
                tickfont=dict(color="#1f77b4")
            ),
            yaxis2=dict(
                title="Memory Usage (MB)",
                titlefont=dict(color="#ff7f0e"),
                tickfont=dict(color="#ff7f0e"),
                anchor="x",
                overlaying="y",
                side="right"
            ),
            height=600,
            template="plotly_white",
            showlegend=True
        )
        
        fig.show()
        return fig
        
    except Exception as e:
        print(f"Ошибка при создании комбинированного графика: {e}")
        return None

def main():
    """
    Основная функция для запуска скрипта
    """
    if len(sys.argv) < 2:
        print("Использование: python script.py <путь_к_файлу.json>")
        print("Пример: python script.py analysis/prometheus/auth-service-common-100_cpu.json")
        sys.exit(1)
    
    file_path = sys.argv[1]
    
    if not os.path.exists(file_path):
        print(f"Ошибка: Файл {file_path} не найден")
        sys.exit(1)
    
    print("=" * 60)
    print("АНАЛИЗАТОР НАГРУЗКИ ПРОЦЕССОРА И ПАМЯТИ")
    print("=" * 60)
    
    # Опционально: выбор типа графика
    if len(sys.argv) > 2 and sys.argv[2] == "combined":
        print("\nСоздание комбинированного графика...")
        plot_combined_metrics(file_path)
    else:
        print("\nСоздание раздельных графиков...")
        plot_single_file_metrics(file_path)

if __name__ == "__main__":
    main()