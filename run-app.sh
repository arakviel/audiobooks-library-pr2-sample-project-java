#!/bin/bash

# Скрипт для запуску JavaFX додатку "Бібліотека Аудіокниг"

echo "🎵 Запуск додатку 'Бібліотека Аудіокниг'..."
echo ""

# Перевірка наявності Java
if ! command -v java &> /dev/null; then
    echo "❌ Java не знайдено. Будь ласка, встановіть Java 21 або новішу версію."
    exit 1
fi

# Перевірка наявності Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven не знайдено. Будь ласка, встановіть Apache Maven."
    exit 1
fi

# Перевірка версії Java
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "⚠️  Увага: Рекомендується Java 21 або новіша версія. Поточна версія: $JAVA_VERSION"
fi

echo "✅ Java знайдено"
echo "✅ Maven знайдено"
echo ""

# Компіляція проекту
echo "🔨 Компіляція проекту..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Помилка компіляції. Перевірте код та залежності."
    exit 1
fi

echo "✅ Компіляція завершена успішно"
echo ""

# Запуск додатку
echo "🚀 Запуск JavaFX додатку..."
echo "📝 Для входу можете зареєструвати нового користувача або використати існуючого"
echo ""

mvn javafx:run

echo ""
echo "👋 Дякуємо за використання додатку!"
