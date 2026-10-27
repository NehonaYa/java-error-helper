import java.util.List;

public final class ErrorAnalyzerTest {
    public static void main(String[] args) {
        expectEmpty(null);
        expectEmpty("  \n");
        expect("Main.java:5: error: cannot find symbol\n  symbol: variable userName\n", "Не найдено имя", "Main.java:5", "userName");
        expect("C:\\Users\\Student\\My File.java:14: error: ';' expected\n", "Ожидается точка с запятой", "C:\\Users\\Student\\My File.java:14", ";");
        expect("Main.java:3: error: class Student is public, should be declared in a file named Student.java",
                "Имя файла и public-класса не совпадают", "Main.java:3", "Student.java");
        expect("Main.java:8: error: incompatible types: String cannot be converted to int", "Несовместимые типы", "Main.java:8", "тип");
        expect("Main.java:9: error: reached end of file while parsing", "Не хватает закрывающей скобки", "Main.java:9", "скобок");
        expect("Main.java:9: error: unclosed string literal", "Строка не закрыта", "Main.java:9", "кавыч");
        expect("Main.java:4: error: non-static variable name cannot be referenced from a static context",
                "Обращение к нестатическому полю или методу", "Main.java:4", "объект");
        expect("Main.java:1: error: package org.example does not exist", "Не найден пакет", "Main.java:1", "import");
        expect("Exception in thread \"main\" java.lang.NullPointerException\n\tat Main.main(Main.java:8)",
                "Обращение к null", "Main.java:8", "null");
        expect("Exception in thread \"main\" java.lang.NumberFormatException: For input string: \"x\"\n"
                + "\tat java.base/java.lang.Integer.parseInt(Integer.java:668)\n"
                + "\tat app.Main.main(Main.java:12)",
                "Строка не является числом", "Main.java:12", "Integer.parseInt");
        expect("java.lang.ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3\n"
                + "\tat Main.main(Main.java:4)",
                "Индекс массива за пределами", "Main.java:4", "array.length");
        expect("java.lang.StringIndexOutOfBoundsException: String index out of range: 4",
                "Индекс строки за пределами", "", "строк");
        expect("java.lang.ArithmeticException: / by zero\n\tat Main.main(Main.java:7)",
                "Деление на ноль", "Main.java:7", "делител");
        expect("Error: Could not find or load main class Main", "Не найден класс для запуска", "", "classpath");
        expect("Main.java:6: error: some entirely new diagnostic", "Не удалось определить ошибку", "Main.java:6", "полный вывод");
        expect("Обычная строка без ошибки", "Не удалось определить ошибку", "", "полный вывод");
        List<ErrorAnalyzer.Finding> multiple = ErrorAnalyzer.analyze(
                "Main.java:3: error: ';' expected\n int a = 1\nMain.java:8: error: cannot find symbol\n  symbol: variable value\n");
        check(multiple.size() == 2, "Two javac errors should be separated");
        check("Main.java:3".equals(multiple.get(0).location), "First location");
        check("Main.java:8".equals(multiple.get(1).location), "Second location");
        check(multiple.get(1).meaning.contains("value"), "Symbol details stay with the correct error");
        System.out.println("All Java Error Helper checks passed.");
    }

    private static void expectEmpty(String input) {
        check(ErrorAnalyzer.analyze(input).isEmpty(), "Empty input should not produce a finding");
    }

    private static void expect(String input, String title, String location, String detail) {
        List<ErrorAnalyzer.Finding> findings = ErrorAnalyzer.analyze(input);
        check(findings.size() == 1, "Expected one finding for " + input);
        ErrorAnalyzer.Finding finding = findings.get(0);
        check(finding.title.equals(title), "Unexpected title for " + input + ": " + finding.title);
        check(finding.location.equals(location), "Unexpected location: " + finding.location);
        check((finding.meaning + finding.advice + finding.example).toLowerCase().contains(detail.toLowerCase()),
                "Expected helpful detail: " + detail);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
