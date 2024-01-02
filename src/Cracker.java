import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cracker extends JDialog {

    public static void main(String[] args) {
        new Cracker().setVisible(true);
    }

    ArrayList<String> lines = new ArrayList<>();
    ArrayList<JTextField> fields = new ArrayList<>();
    JTextArea result = new JTextArea(40, 60);
    JTextField nLetters = new JTextField(2);
    int letters = 10;
    String x = "";
    boolean changing = false;

    Cracker() {
        super((Frame) null);

        setLayout(new BorderLayout());
        JPanel line1 = new JPanel();
        add(line1, BorderLayout.NORTH);
        line1.setLayout(new FlowLayout(FlowLayout.LEFT));
        line1.add(new JLabel("Number of letters"));
        line1.add(nLetters);
        nLetters.setText("10");
        nLetters.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkLetters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkLetters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkLetters();
            }
        });
        line1.add(new JLabel("Letters: "));

        for(int i = 0; i < 10; i++) {
            JTextField text = new JTextField(2);
            fields.add(text);
            line1.add(text);
        }

        JScrollPane sp = new JScrollPane(result);
        add(sp, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        add(buttons, BorderLayout.SOUTH);
        buttons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton search = new JButton("Search");
        buttons.add(search);
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder sb = new StringBuilder();
                for(JTextField f : fields) {
                    if(! f.isVisible())
                        continue;
                    String s = f.getText().trim().toUpperCase();
                    if(s.isEmpty())
                        sb.append('.');
                    else if(s.startsWith("!"))
                        sb.append("[^" + s + "]");
                    else
                        sb.append(s);
                }
                Pattern pattern = Pattern.compile(sb.toString());
                search(pattern);
            }
        });
        JButton exit = new JButton("Exit");
        buttons.add(exit);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        loadFile("ignis-10M.txt");
//        loadFile("ignis-1M.txt");
    }

    void checkLetters() {
        if(changing)
            return;
        x = "";
        try {
            int nl = Integer.parseInt(nLetters.getText());
            if(nl > 0 && nl <= 10) {
                letters = nl;
                for(int i = 1; i < 10; i++) {
                    if (i < letters)
                        fields.get(i).setVisible(true);
                    else {
                        fields.get(i).setVisible(false);
                    }
                }
                x = "" + nl;
            }
        } catch (NumberFormatException ex) {
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                changing = true;
                nLetters.setText(x);
                changing = false;
            }
        });
   }

    void loadFile(String fileName) {
        try {
            File f = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(f));
            while(true) {
                String line = br.readLine();
                if(line == null)
                    break;
                lines.add(line.toUpperCase());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    void search(Pattern pattern) {
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        int n = 0;
        result.setText("");
        for(String line : lines) {
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
}
