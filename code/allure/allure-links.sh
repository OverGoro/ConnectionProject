#!/bin/bash

# Директория для объединенных результатов
COMBINED_DIR="combined-allure-results"
REPORT_DIR="combined-allure-report"

# Очистка предыдущих результатов
rm -rf $COMBINED_DIR $REPORT_DIR
mkdir -p $COMBINED_DIR

# Проекты для объединения
PROJECTS=("../shared/client-shared" "../shared/device-shared")

# Копирование результатов из каждого проекта
for project in "${PROJECTS[@]}"; do
    if [ -d "$project/allure-results" ]; then
        cp -r $project/allure-results/* $COMBINED_DIR/
    elif [ -d "$project/build/allure-results" ]; then
        cp -r $project/build/allure-results/* $COMBINED_DIR/
    fi
done

# Генерация отчета
allure generate $COMBINED_DIR -o $REPORT_DIR --clean

# Открытие отчета
allure open $REPORT_DIR