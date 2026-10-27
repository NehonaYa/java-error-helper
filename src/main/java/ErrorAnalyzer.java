import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Identifies common javac messages and Java runtime errors without network access. */
public final class ErrorAnalyzer {
    private static final Pattern JAVAC_HEADER = Pattern.compile(
            "(?im)^([^\\r\\n]*?\\.java):(\\d+):\\s*(?:error|warning|ошибка|предупреждение):\\s*([^\\r\\n]*)");
    private static final Pattern STACK_FRAME = Pattern.compile(
            "(?m)^\\s*at\\s+([^\\s(]+)\\(([^():\\r\\n]+\\.java):(\\d+)\\)");
    private static final Pattern SYMBOL = Pattern.compile(
            "(?im)^\\s*symbol:\\s*(?:variable|class|method|constructor)\\s+([^\\r\\n]+)");
    private static final Pattern PUBLIC_CLASS = Pattern.compile(
            "(?i)class\\s+([\\w$]+)\\s+is public, should be declared in a file named\\s+([^\\s]+\\.java)");

    private ErrorAnalyzer() { }

    public static List<Finding> analyze(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String text = input.replace("\r\n", "\n").replace('\r', '\n');
        Matcher matcher = JAVAC_HEADER.matcher(text);
        List<Header> headers = new ArrayList<Header>();
        while (matcher.find()) {
            headers.add(new Header(matcher.start(), matcher.group(1).trim(),
                    matcher.group(2), matcher.group(3).trim()));
        }

        List<Finding> results = new ArrayList<Finding>();
        if (!headers.isEmpty()) {
            for (int i = 0; i < headers.size(); i++) {
                Header header = headers.get(i);
                int end = i + 1 < headers.size() ? headers.get(i + 1).start : text.length();
                String block = text.substring(header.start, end);
                results.add(classify(block, header.file + ":" + header.line, header.message));
            }
            return results;
        }

        String location = findUserFrame(text);
        Finding finding = classify(text, location, firstNonEmptyLine(text));
        results.add(finding);
        return results;
    }

    private static Finding classify(String text, String location, String original) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("cannot find symbol") || lower.contains("не удается найти символ")
                || lower.contains("не удаётся найти символ")) {
            Matcher symbol = SYMBOL.matcher(text);
            String symbolHint = symbol.find() ? "\nВ сообщении указан: " + symbol.group(1).trim() + "." : "";
            return finding("Не найдено имя", location, original,
                    "Java не видит указанную переменную, метод или класс." + symbolHint,
                    "Проверь написание и регистр букв; убедись, что имя объявлено в нужной области видимости. "
                    + "Для класса из другой библиотеки проверь import и наличие зависимости.",
                    "System.out.println(name);  // name должен быть объявлен заранее");
        }
        Matcher className = PUBLIC_CLASS.matcher(text);
        if (className.find()) {
            return finding("Имя файла и public-класса не совпадают", location, original,
                    "Публичный класс " + className.group(1) + " должен находиться в файле " + className.group(2) + ".",
                    "Переименуй файл или класс так, чтобы имена полностью совпадали, включая заглавные буквы.",
                    "public class Main { }  // файл Main.java");
        }
        if (lower.contains("';' expected") || lower.contains("ожидается ';'")) {
            return finding("Ожидается точка с запятой", location, original,
                    "Компилятор ожидал символ ; в конце инструкции.",
                    "Проверь указанную и предыдущую строки. В конце объявления переменной или вызова метода обычно нужен ;.",
                    "int age = 18;\nSystem.out.println(age);");
        }
        if (lower.contains("reached end of file while parsing")) {
            return finding("Не хватает закрывающей скобки", location, original,
                    "Файл закончился, пока Java ещё ждала завершения блока.",
                    "Проверь пары фигурных скобок { и } у классов, методов, условий и циклов.",
                    "if (age > 18) {\n    System.out.println(age);\n}");
        }
        if (lower.contains("unclosed string literal")) {
            return finding("Строка не закрыта", location, original,
                    "Текст начался с двойной кавычки, но компилятор не нашёл её завершение.",
                    "Проверь парные двойные кавычки. Кавычку внутри строки записывай как \\\".",
                    "String message = \"Привет\";");
        }
        if (lower.contains("incompatible types") || lower.contains("cannot be converted to")) {
            return finding("Несовместимые типы", location, original,
                    "Значение одного типа используется там, где требуется другой тип.",
                    "Сравни тип переменной с типом значения. Для числа в строке можно использовать Integer.parseInt, "
                    + "если строка действительно содержит целое число.",
                    "int age = Integer.parseInt(\"18\");");
        }
        if (lower.contains("non-static") && lower.contains("static context")) {
            return finding("Обращение к нестатическому полю или методу", location, original,
                    "Из статического метода используется поле или метод, принадлежащий объекту.",
                    "Создай объект класса и обратись через него. Делай член static только если он действительно общий для класса.",
                    "Student student = new Student();\nstudent.show();");
        }
        if (lower.contains("cannot be applied to given types") || lower.contains("no suitable method found")) {
            return finding("Аргументы метода не подходят", location, original,
                    "Количество или типы переданных аргументов не совпадают с параметрами метода.",
                    "Сравни вызов с объявлением метода: проверь порядок, количество и тип аргументов.",
                    "void greet(String name) { }\ngreet(\"Anna\");");
        }
        if (lower.contains("package ") && lower.contains(" does not exist")) {
            return finding("Не найден пакет", location, original,
                    "Компилятор не нашёл указанный пакет или подключённую библиотеку.",
                    "Проверь имя import, структуру папок и подключение зависимости в проекте.",
                    "import java.util.ArrayList;");
        }
        if (lower.contains("illegal start of expression") || lower.contains("class, interface, enum, or record expected")
                || lower.contains("class, interface, or enum expected")) {
            return finding("Ошибка в структуре кода", location, original,
                    "Java встретила код в неожиданном месте. Частая причина — неверные фигурные скобки.",
                    "Проверь скобки и строки перед ошибкой; убедись, что методы находятся внутри класса, а инструкции — внутри метода.",
                    "class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello\");\n    }\n}");
        }
        if (lower.contains("arrayindexoutofboundsexception")) {
            return finding("Индекс массива за пределами", location, original,
                    "Индекс меньше нуля или больше последнего допустимого индекса массива.",
                    "Для массива длины n допустимы индексы от 0 до n - 1. В цикле используй условие i < array.length.",
                    "for (int i = 0; i < numbers.length; i++) {\n    System.out.println(numbers[i]);\n}");
        }
        if (lower.contains("stringindexoutofboundsexception")) {
            return finding("Индекс строки за пределами", location, original,
                    "Индекс символа находится за пределами строки.",
                    "Проверь длину строки: допустимые индексы от 0 до text.length() - 1.",
                    "if (!text.isEmpty()) {\n    System.out.println(text.charAt(0));\n}");
        }
        if (lower.contains("indexoutofboundsexception")) {
            return finding("Индекс за пределами коллекции", location, original,
                    "Программа обратилась к элементу по недопустимому индексу.",
                    "Проверь размер списка или коллекции перед обращением. Первый индекс — 0.",
                    "if (!items.isEmpty()) {\n    System.out.println(items.get(0));\n}");
        }
        if (lower.contains("nullpointerexception")) {
            return finding("Обращение к null", location, original,
                    "Программа попыталась использовать объект, значение которого равно null.",
                    "В указанной строке найди объект перед точкой и проверь, где ему должно присваиваться значение. "
                    + "Если null допустим, проверь его перед использованием.",
                    "if (name != null) {\n    System.out.println(name.length());\n}");
        }
        if (lower.contains("numberformatexception")) {
            return finding("Строка не является числом", location, original,
                    "Метод преобразования получил текст, который не соответствует ожидаемому числу.",
                    "Проверь ввод и обработай неверное значение через try/catch. Integer.parseInt принимает целое число.",
                    "try {\n    int n = Integer.parseInt(text.trim());\n} catch (NumberFormatException e) {\n    System.out.println(\"Введите целое число\");\n}");
        }
        if (lower.contains("inputmismatchexception")) {
            return finding("Неверный тип введённых данных", location, original,
                    "Scanner ожидал другой тип значения, например число вместо текста.",
                    "Проверь ввод перед чтением с помощью hasNextInt() или прочитай строку и обработай её отдельно.",
                    "if (scanner.hasNextInt()) {\n    int number = scanner.nextInt();\n}");
        }
        if (lower.contains("arithmeticexception") && (lower.contains("/ by zero") || lower.contains("divide by zero"))) {
            return finding("Деление на ноль", location, original,
                    "При целочисленном делении или вычислении остатка делитель оказался равен нулю.",
                    "Проверь значение делителя перед операцией и реши, что программа должна делать при нуле.",
                    "if (divisor != 0) {\n    System.out.println(number / divisor);\n}");
        }
        if (lower.contains("stackoverflowerror")) {
            return finding("Слишком глубокая рекурсия", location, original,
                    "Вызовы методов заняли весь стек. Часто метод вызывает сам себя без правильного условия остановки.",
                    "Проверь базовый случай рекурсии и убедись, что каждый вызов приближает программу к нему.",
                    "int sum(int n) {\n    if (n <= 0) return 0;\n    return n + sum(n - 1);\n}");
        }
        if (lower.contains("could not find or load main class") || lower.contains("classnotfoundexception")) {
            return finding("Не найден класс для запуска", location, original,
                    "Java не нашла указанный класс в доступных папках и библиотеках.",
                    "Проверь имя класса и package, затем запускай из правильной папки или укажи classpath.",
                    "javac Main.java\njava Main");
        }
        if (lower.contains("main method not found")) {
            return finding("Не найден метод main", location, original,
                    "В запускаемом классе нет метода с ожидаемой сигнатурой точки входа.",
                    "Добавь public static void main(String[] args) в нужный класс и проверь регистр букв.",
                    "public static void main(String[] args) {\n    System.out.println(\"Hello\");\n}");
        }
        if (lower.contains("unsupportedclassversionerror")) {
            return finding("Версия Java слишком старая", location, original,
                    "Класс скомпилирован более новой версией Java, чем та, которая его запускает.",
                    "Сравни вывод java -version и javac -version; используй совместимую версию JDK и Java.",
                    "java -version\njavac -version");
        }
        return finding("Не удалось определить ошибку", location, original,
                "Для этого сообщения пока нет готового объяснения.",
                "Скопируй полный вывод терминала, включая строку error или Exception и строки после неё. "
                + "Также проверь строку, которую указывает компилятор.",
                "javac Main.java\njava Main");
    }

    private static String findUserFrame(String text) {
        Matcher matcher = STACK_FRAME.matcher(text);
        while (matcher.find()) {
            String method = matcher.group(1);
            if (method.startsWith("java.") || method.startsWith("javax.") || method.startsWith("jdk.")
                    || method.startsWith("sun.") || method.startsWith("java.base/")) {
                continue;
            }
            return matcher.group(2) + ":" + matcher.group(3);
        }
        return "";
    }

    private static String firstNonEmptyLine(String text) {
        for (String line : text.split("\\n")) {
            if (!line.trim().isEmpty()) {
                return line.trim();
            }
        }
        return "";
    }

    private static Finding finding(String title, String location, String original, String meaning,
                                   String advice, String example) {
        return new Finding(title, location, original, meaning, advice, example);
    }

    private static final class Header {
        private final int start;
        private final String file;
        private final String line;
        private final String message;

        Header(int start, String file, String line, String message) {
            this.start = start;
            this.file = file;
            this.line = line;
            this.message = message;
        }
    }

    public static final class Finding {
        public final String title;
        public final String location;
        public final String original;
        public final String meaning;
        public final String advice;
        public final String example;

        Finding(String title, String location, String original, String meaning, String advice, String example) {
            this.title = title;
            this.location = location;
            this.original = original;
            this.meaning = meaning;
            this.advice = advice;
            this.example = example;
        }
    }
}
