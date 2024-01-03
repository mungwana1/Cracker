import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cracker extends JDialog {

    public static void main(String[] args) {
        new Cracker().setVisible(true);
    }

    /**
     * the sorted password database
     */
    TreeSet<String> lines = new TreeSet<>();
    /**
     * ten text fields
     */
    ArrayList<Field> fields = new ArrayList<>();
    /**
     * the text area to display password matches
     */
    JTextArea result = new JTextArea(40, 60);
    /**
     * a numeric spinner to select the number of letters
     */
    JSpinner nLetters;
    int letters = 10;
    boolean changing = false;
    int x = 1;
    String fileName = "ignis-11M.txt";

    Cracker() {
        super((Frame) null);

        setLayout(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());
        add(top, BorderLayout.NORTH);
        JPanel line1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel line2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(line1, BorderLayout.NORTH);
        line1.add(new JLabel("Missing letters match any character. Use !X for known incorrect matches."));
        top.add(line2, BorderLayout.CENTER);
        line2.add(new JLabel("Number of letters"));
        SpinnerNumberModel model = new SpinnerNumberModel(10, 2, 10, 1);
        nLetters = new JSpinner(model);
        line2.add(nLetters);
        nLetters.addChangeListener(e -> checkLetters());
        line2.add(new JLabel("Letters: "));

        for(int i = 0; i < 10; i++) {
            Field text = new Field(2);
            fields.add(text);
            line2.add(text);
        }

        result.setEditable(false);
        JScrollPane sp = new JScrollPane(result, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(sp, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        add(buttons, BorderLayout.SOUTH);
        buttons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton search = new JButton("Search");
        buttons.add(search);
        search.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            for(JTextField f : fields) {
                if(! f.isVisible())
                    continue;
                String s = f.getText().trim().toUpperCase();
                if(s.isEmpty())
                    sb.append('.');
                else if(s.startsWith("!") && s.length() == 2)
                    sb.append("[^").append(s.charAt(1)).append("]");
                else
                    sb.append(s);
            }
            Pattern pattern = Pattern.compile(sb.toString());
            search(pattern);
        });
        JButton exit = new JButton("Exit");
        buttons.add(exit);
        exit.addActionListener(e -> dispose());

        pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        loadFile(fileName);
    }

    /**
     * Called on a change to the number of letters field. Resets all letters to blank and displays
     * only the requested number.
     */
    void checkLetters() {
        if(changing)
            return;
        x = 1;
        try {
            int nl = (int) nLetters.getValue();
            if(nl > 0 && nl <= 10) {
                letters = nl;
                for(int i = 0; i < 10; i++) {
                    fields.get(i).setText("");
                    fields.get(i).setVisible(i < letters);
                }
                x = nl;
            }
        } catch (NumberFormatException ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            changing = true;
            nLetters.setValue(x);
            changing = false;
        });
   }

    /**
     * Read in the password database file
     * @param fileName the database text file
     */
    void loadFile(String fileName) {
        try {
            File f = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(f));
            while(true) {
                String line = br.readLine();
                if(line == null)
                    break;
                lines.add(line.trim().toUpperCase());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Searches the password database for matching entries
     * @param pattern the pattern to match
     */
    void search(Pattern pattern) {
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        int n = 0;
        result.setText("");
        for(String line : lines) {
            if(line.length() != letters)
                continue;
            Matcher matcher = pattern.matcher(line);
            if(matcher.find()) {
                result.append(line + System.lineSeparator());
                n++;
            }
        }
        if(n == 0)
            result.append("No matches!");
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    static class Field extends JTextField {
        Field(int columns) {
            super(columns);
            addKeyListener(new KeyAdapter() {

                public void keyTyped(KeyEvent e) {
                    char keyChar = e.getKeyChar();
                    if (Character.isLowerCase(keyChar)) {
                        e.setKeyChar(Character.toUpperCase(keyChar));
                    }
                    if((keyChar != '!' && keyChar != KeyEvent.VK_DELETE && keyChar != KeyEvent.VK_BACK_SPACE)) {
                        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                        manager.focusNextComponent();
                    }}

            });
        }
    }
}
