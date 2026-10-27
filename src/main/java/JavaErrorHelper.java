import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/** A small offline desktop application for reading Java error messages. */
public final class JavaErrorHelper {
    private static final Color PAGE = new Color(239, 243, 249);
    private static final Color INK = new Color(27, 41, 63);
    private static final Color MUTED = new Color(88, 102, 124);
    private static final Color BLUE = new Color(46, 86, 210);
    private static final Color BORDER = new Color(211, 220, 233);

    private final JTextArea input = new JTextArea();
    private final JTextArea output = new JTextArea();
    private final JLabel status = new JLabel("Вставь ошибку или выбери пример");
    private final JFrame frame = new JFrame("Java Error Helper");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                new JavaErrorHelper().show();
            }
        });
    }

    private void show() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // The default Swing appearance is also supported.
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(760, 540));
        frame.setSize(1120, 740);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(PAGE);
        root.setBorder(new EmptyBorder(22, 24, 18, 24));
        root.add(makeHeader(), BorderLayout.NORTH);

        configureArea(input, true);
        configureArea(output, false);
        output.setText("Здесь появится объяснение.\n\n"
                + "1. Скопируй полную ошибку из терминала.\n"
                + "2. Вставь её слева.\n"
                + "3. Нажми «Разобрать ошибку».\n\n"
                + "Если не знаешь, что вставить, выбери пример.");

        JPanel inputCard = card("Текст ошибки", "Вставь вывод javac или сообщение об исключении", input);
        JPanel outputCard = card("Объяснение", "Возможная причина и что проверить", output);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputCard, outputCard);
        split.setResizeWeight(0.5);
        split.setDividerSize(12);
        split.setBorder(null);
        split.setBackground(PAGE);
        if (split.getUI() instanceof BasicSplitPaneUI) {
            ((BasicSplitPaneUI) split.getUI()).getDivider().setBackground(PAGE);
            ((BasicSplitPaneUI) split.getUI()).getDivider().setBorder(null);
        }
        root.add(split, BorderLayout.CENTER);
        root.add(makeFooter(), BorderLayout.SOUTH);

        input.getInputMap().put(KeyStroke.getKeyStroke("control ENTER"), "analyze");
        input.getActionMap().put("analyze", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) {
                analyzeInput();
            }
        });
        frame.setContentPane(root);
        frame.setVisible(true);
    }

    private JPanel makeHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        JPanel labels = new JPanel(new GridLayout(2, 1, 0, 5));
        labels.setOpaque(false);
        JLabel title = new JLabel("Java Error Helper");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(INK);
        JLabel subtitle = new JLabel("Понятные объяснения ошибок Java для начинающих");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);
        labels.add(title);
        labels.add(subtitle);
        header.add(labels, BorderLayout.WEST);
        JLabel badge = new JLabel("БЕЗ ИНТЕРНЕТА");
        badge.setForeground(new Color(39, 117, 84));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(192, 226, 209)),
                new EmptyBorder(8, 12, 8, 12)));
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private JPanel card(String title, String subtitle, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 16, 16, 16)));
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 3));
        heading.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(INK);
        JLabel description = new JLabel(subtitle);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        description.setForeground(MUTED);
        heading.add(titleLabel);
        heading.add(description);
        panel.add(heading, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(227, 232, 241)));
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void configureArea(JTextArea area, boolean editable) {
        area.setEditable(editable);
        area.setFont(new Font(editable ? "Monospaced" : "Segoe UI", Font.PLAIN, 14));
        area.setForeground(INK);
        area.setBackground(editable ? new Color(248, 250, 253) : Color.WHITE);
        area.setCaretColor(INK);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(12, 12, 12, 12));
    }

    private JPanel makeFooter() {
        JPanel footer = new JPanel(new BorderLayout(10, 8));
        footer.setOpaque(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton analyze = button("Разобрать ошибку", true);
        analyze.addActionListener(event -> analyzeInput());
        JButton clear = button("Очистить", false);
        clear.addActionListener(event -> {
            input.setText("");
            output.setText("");
            status.setText("Поле очищено");
            input.requestFocusInWindow();
        });
        JButton copy = button("Скопировать ответ", false);
        copy.addActionListener(event -> copyOutput());
        JComboBox<String> examples = new JComboBox<String>(new String[] {
                "Выбрать пример", "cannot find symbol", "Ожидается ;", "NullPointerException"
        });
        examples.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        examples.addActionListener(event -> selectExample(examples.getSelectedIndex()));
        actions.add(analyze);
        actions.add(clear);
        actions.add(copy);
        actions.add(examples);
        footer.add(actions, BorderLayout.NORTH);

        JPanel info = new JPanel(new BorderLayout());
        info.setOpaque(false);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(MUTED);
        info.add(status, BorderLayout.WEST);
        JLabel privacy = new JLabel("Текст остаётся на твоём компьютере");
        privacy.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        privacy.setForeground(MUTED);
        info.add(privacy, BorderLayout.EAST);
        footer.add(info, BorderLayout.SOUTH);
        return footer;
    }

    private JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(primary ? BLUE : Color.WHITE);
        button.setForeground(primary ? Color.WHITE : INK);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? BLUE : BORDER),
                new EmptyBorder(10, 14, 10, 14)));
        return button;
    }

    private void selectExample(int index) {
        if (index == 1) {
            input.setText("Main.java:5: error: cannot find symbol\n"
                    + "        System.out.println(nam);\n"
                    + "                           ^\n"
                    + "  symbol:   variable nam\n"
                    + "  location: class Main");
        } else if (index == 2) {
            input.setText("Main.java:4: error: ';' expected\n"
                    + "        int age = 18\n"
                    + "                    ^\n1 error");
        } else if (index == 3) {
            input.setText("Exception in thread \"main\" java.lang.NullPointerException\n"
                    + "    at Main.main(Main.java:8)");
        } else {
            return;
        }
        input.setCaretPosition(0);
        status.setText("Пример добавлен — нажми «Разобрать ошибку»");
    }

    private void analyzeInput() {
        List<ErrorAnalyzer.Finding> findings = ErrorAnalyzer.analyze(input.getText());
        if (findings.isEmpty()) {
            output.setText("Сначала вставь текст ошибки или выбери пример.");
            status.setText("Текст ошибки пока пуст");
            return;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < findings.size(); i++) {
            ErrorAnalyzer.Finding finding = findings.get(i);
            if (i > 0) result.append("\n\n──────────────────────────────\n\n");
            if (findings.size() > 1) result.append("Ошибка ").append(i + 1).append(" из ").append(findings.size()).append("\n\n");
            result.append(finding.title).append("\n");
            if (!finding.location.isEmpty()) result.append("Где: ").append(finding.location).append("\n");
            if (!finding.original.isEmpty()) result.append("Сообщение: ").append(shorten(finding.original)).append("\n");
            result.append("\nЧТО ЭТО ЗНАЧИТ\n").append(finding.meaning)
                    .append("\n\nЧТО ПРОВЕРИТЬ\n").append(finding.advice)
                    .append("\n\nПРИМЕР\n").append(finding.example);
        }
        result.append("\n\nПодсказка: причина иногда находится в строке перед указанной ошибкой.");
        output.setText(result.toString());
        output.setCaretPosition(0);
        status.setText("Разобрано сообщений: " + findings.size() + " · Ctrl+Enter для повторного анализа");
    }

    private String shorten(String value) {
        return value.length() <= 220 ? value : value.substring(0, 217) + "...";
    }

    private void copyOutput() {
        if (output.getText().trim().isEmpty()) {
            status.setText("Пока нечего копировать");
            return;
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(output.getText()), null);
            status.setText("Ответ скопирован");
        } catch (IllegalStateException error) {
            JOptionPane.showMessageDialog(frame, "Буфер обмена занят. Попробуй ещё раз.",
                    "Не удалось скопировать", JOptionPane.WARNING_MESSAGE);
        }
    }
}
