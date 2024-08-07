import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class QuestionInputPanel extends JPanel implements ActionListener {
    MainPanel mainPanel;
    JTextField questionAnswerInput;
    JTextField questionGenerateInput;
    JButton submitButton;
    JButton generateButton;
    StringBuilder questionInputString;
    boolean busy = false;
    Thread thread;
    Solution solution;

    QuestionInputPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        questionAnswerInput = new JTextField(20);
        questionGenerateInput = new JTextField(20);
        submitButton = new JButton("Submit answer");
        submitButton.addActionListener(this);
        questionAnswerInput.addActionListener((ActionEvent e) -> {});
        questionGenerateInput.addActionListener((ActionEvent e) -> {});
        generateButton = new JButton("Generate question");
        generateButton.addActionListener(this);
        this.add(generateButton);
        this.add(questionGenerateInput);
        this.add(submitButton);
        this.add(questionAnswerInput);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == submitButton) {
            if (busy) {
                busy = false;
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                    mainPanel.questionInputPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                submitButton.setText("Submit Answer");
                mainPanel.questionInputPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } else {
                busy = true;
                submitButton.setText("Durdur");
                mainPanel.questionInputPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                thread = new Thread(() -> {
                    try {
                        String inputAnswer = this.questionAnswerInput.getText().replaceAll("\\s", "");
                        String tryAnswer = mainPanel.questionPannel.answer.getAnswer().replaceAll("\\s", "");
                        if (inputAnswer.equals(tryAnswer)) {
                            JOptionPane.showMessageDialog(mainPanel, "Congratulations! That is the correct answer.");
                        } else {
                            JOptionPane.showMessageDialog(mainPanel, "That is not the correct answer.");
                        }
                    } finally {
                        busy = false;
                        SwingUtilities.invokeLater(() -> {
                            submitButton.setText("Submit Answer");
                            mainPanel.questionInputPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        });
                    }
                });
                thread.start();
            }
        } else if (source == generateButton) {
            if (busy) {
                busy = false;
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                    mainPanel.questionInputPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                generateButton.setText("Generate Question");
                mainPanel.questionInputPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } else {
                busy = true;
                generateButton.setText("Durdur");
                mainPanel.questionInputPanel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                thread = new Thread(() -> {
                    try {
                        String generate = questionGenerateInput.getText();
                        mainPanel.questionPannel.question = new Squares(generate);
                        ArrayList<ArrayList<Integer>> newBoard = new ArrayList<>();
                        //Take a deep copy
                        for (ArrayList<Integer> row : mainPanel.questionPannel.question.board) {
                            newBoard.add(new ArrayList<>(row));
                        }
                        mainPanel.questionPannel.answer.board = newBoard;
                        mainPanel.questionInputPanel.questionGenerateInput.setText("");
                        mainPanel.questionPannel.answer.askQuestion();
                        SwingUtilities.invokeLater(() -> {
                            mainPanel.questionPannel.repaint();
                        });
                    } finally {
                        busy = false;
                        SwingUtilities.invokeLater(() -> {
                            generateButton.setText("Generate Question");
                            mainPanel.questionInputPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        });
                    }
                });
                thread.start();
            }
        }
    }
}