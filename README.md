# Java Error Helper

Небольшое бесплатное приложение для начинающих: вставь сообщение об ошибке Java и получи объяснение на русском, возможную причину и пример, что проверить.

- Работает без интернета и не отправляет введённый текст.
- Разбирает распространённые ошибки компиляции и исключения во время работы.
- Показывает файл и номер строки, если они есть в сообщении.
- Не меняет твой код. Подсказки приблизительные: иногда причина находится в предыдущей строке.
- Исходный код открыт под лицензией MIT.

## Запуск на Windows

1. Установи [Java](https://adoptium.net/temurin/releases/) версии 8 или новее.
2. Распакуй папку проекта целиком.
3. Дважды нажми `start.cmd` или запусти `java -jar java-error-helper.jar`.
4. Скопируй полный текст ошибки из терминала, вставь его слева и нажми «Разобрать ошибку». Кнопка «Выбрать пример» поможет попробовать приложение.

Для запуска на другой системе: `java -jar java-error-helper.jar`. Для графического интерфейса нужен рабочий стол.

## Какие ошибки распознаются

Например, `cannot find symbol`, `';' expected`, несовпадение имени публичного класса и файла, несовместимые типы, недостающие скобки, проблемы с `static` или `import`, `NullPointerException`, `NumberFormatException`, выход индекса за границы массива и деление на ноль. Если ошибка неизвестна, приложение прямо об этом сообщает.

В первую очередь поддерживаются стандартные англоязычные сообщения `javac` и названия исключений Java. Другие варианты локализации пока могут не распознаваться.

## Сборка из исходного кода

Нужен JDK 11 или новее (создаётся приложение, совместимое с Java 8+).

В Windows:

```bat
build.cmd
test.cmd
```

В Linux/macOS из папки проекта:

```sh
mkdir -p build/classes
javac --release 8 -Xlint:-options -encoding UTF-8 -d build/classes src/main/java/*.java
jar cfe java-error-helper.jar JavaErrorHelper -C build/classes .
mkdir -p build/test-classes
javac --release 8 -Xlint:-options -encoding UTF-8 -cp build/classes -d build/test-classes src/test/java/*.java
java -cp build/classes:build/test-classes ErrorAnalyzerTest
```

## Как помочь проекту

Сообщи об ошибке, приложив минимальный пример вывода компилятора **без паролей и личных данных**, или предложи новую диагностику. Для улучшений кода запусти `test.cmd` и проверь, что подсказка понятна начинающему.

## English

Java Error Helper is an offline, beginner-friendly Java desktop app. Paste a compiler message or stack trace to receive a Russian explanation, a possible cause, and a small example. It does not send input anywhere or modify your code. See the build commands above. Licensed under MIT.
