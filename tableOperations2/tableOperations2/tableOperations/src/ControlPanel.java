import java.awt.*;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

import com.itextpdf.text.*;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.Image;
import org.w3c.dom.ls.LSOutput;

public class ControlPanel extends JPanel implements ActionListener, IPrintable {
    MainPanel mainPanel;
    JPanel buttonPanel;
    JButton newQuestionButton;
    JButton rulesButton;
    JTextArea description;
    boolean busy = false; // for new question button
    private Thread thread = null;  // for new question button
    HashSet<String> uniqueQuestions;
    QuestionTracker allQuestions;
    // for pdf
    JButton printButton;
    JLabel arrow;
    private String[] gameConcept = { "Harf", "Renk", "Rakam" };
    JComboBox<String> boyut, copylist, type;


    JComboBox<String> letcolnum;

    JButton ayarlar;

    JButton kopyala, cik, yazdir;


    JSpinner startIndex, endIndex;



    private String[] kopyalamaSecenekleri = { "Yan Yana", "Alt Alta", "Soru", "Cevap" };

    public ControlPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setLayout(new BorderLayout());
        // Initialize components
        buttonPanel = new JPanel(new GridLayout(2, 3)); // Adjusted grid layout to fit all components
        Font font = new Font("Windows", Font.PLAIN, 14);

        uniqueQuestions = new HashSet<>();
        allQuestions = new QuestionTracker(null);
        allQuestions.addLast(mainPanel.questionPannel);

        // Add button panel to the control panel
        createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

    }

    void createButtonPanel() {
        // Set FlowLayout for buttonPanel to keep all components in a single row
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Add "New Question" button
        newQuestionButton = new JButton("New Question");
        newQuestionButton.addActionListener(this);
        buttonPanel.add(newQuestionButton);

        // Add "Print PDF" button
        printButton = new JButton("Print PDF");
        printButton.addActionListener(this);
        buttonPanel.add(printButton);

        startIndex = new JSpinner();
        JComponent startSpinnerEditor = startIndex.getEditor();
        JFormattedTextField jftf = ((JSpinner.DefaultEditor) startSpinnerEditor).getTextField();
        jftf.setColumns(3);

        endIndex = new JSpinner();
        JComponent endSpinnerEditor = endIndex.getEditor();
        JFormattedTextField jftfend = ((JSpinner.DefaultEditor) endSpinnerEditor).getTextField();
        jftfend.setColumns(3);

        //Create description
        description = new JTextArea("Tablo kodu: " + mainPanel.questionPannel.question.getColorCode());
        description.setVisible(true);
        description.setEditable(false);
        description.setBackground(Color.WHITE);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(new Font("Verdana", Font.PLAIN, 14));
        description.setPreferredSize(new Dimension(200, 60));
        add(description);
        // Create a panel for label and JComboBox to keep them together
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel("Sekil:");
        comboPanel.add(label);

        letcolnum = new JComboBox<>(gameConcept);
        letcolnum.setPreferredSize(new Dimension(100, 30)); // Ensure JComboBox is visible
        letcolnum.addActionListener(this);
        letcolnum.setSelectedIndex(1);

        comboPanel.add(letcolnum);

        // Add comboPanel to buttonPanel
        buttonPanel.add(comboPanel);

        ayarlar = new JButton("Ayarlar");
        ayarlar.addActionListener(this);
        buttonPanel.add(ayarlar);

        kopyala = new JButton("Kopyala");
        kopyala.addActionListener(this);
        buttonPanel.add(kopyala);

        yazdir = new JButton("Yazdır");
        yazdir.addActionListener((ActionEvent event) -> {
            yazdir.setText("...");
            new Thread(() -> {
                try {
                    buildPDF(this);
                } catch (FileNotFoundException | DocumentException e) {
                    e.printStackTrace();
                } finally {
                    yazdir.setText("Yazdır");
                }
            }).start();
        });
        buttonPanel.add(yazdir);

        cik = new JButton("\u00c7\u0131k");
        cik.addActionListener(this);

        copylist = new JComboBox(kopyalamaSecenekleri);
        buttonPanel.add(copylist);

        buttonPanel.add(new JLabel("İlk :"));
        buttonPanel.add(startIndex);
        buttonPanel.add(new JLabel("Son :"));
        buttonPanel.add(endIndex);

        rulesButton = new JButton("Kurallar");
        rulesButton.addActionListener(this);
        buttonPanel.add(rulesButton);

        // Add buttonPanel to ControlPanel
        add(buttonPanel, BorderLayout.SOUTH);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // Handle new question button actions
        if (source == newQuestionButton) {
            if (busy) {
                busy = false;
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                    mainPanel.questionPannel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }

                newQuestionButton.setText("New Question");
                mainPanel.questionPannel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } else {
                busy = true;
                newQuestionButton.setText("Stop");
                mainPanel.questionPannel.setCursor(new Cursor(Cursor.WAIT_CURSOR));

                thread = new Thread(() -> {
                    try {
                        int numberOfAnswers = -1;
                        String key = "";
                            key = "";
                            // Generate a new question
                            mainPanel.questionPannel.question = new Squares(4, 4);
                            ArrayList<ArrayList<Integer>> newBoard = new ArrayList<>();
                            // Take a deep copy
                            for (ArrayList<Integer> row : mainPanel.questionPannel.question.board) {
                                newBoard.add(new ArrayList<>(row));
                            }
                            mainPanel.questionPannel.answer.board = newBoard;
                            mainPanel.questionInputPanel.questionAnswerInput.setText("");
                            mainPanel.questionPannel.answer.askQuestion();
                            Solution solution = new Solution(mainPanel.questionPannel.question, mainPanel.questionPannel.answer);
                            numberOfAnswers = solution.getSolutionWithBruteForce();
                            key = mainPanel.questionPannel.question.getColorCode() + " " + mainPanel.questionPannel.answer.getColorCode();
                        description.setText("Tablo kodu: " + mainPanel.questionPannel.question.getColorCode());
                        allQuestions.addLast(mainPanel.questionPannel);
                        System.out.println(mainPanel.questionPannel.answer.getAnswer());
                        SwingUtilities.invokeLater(() -> {
                            mainPanel.questionPannel.repaint();
                        });
                    } finally {
                        busy = false;
                        SwingUtilities.invokeLater(() -> {
                            newQuestionButton.setText("Yeni Soru");
                            mainPanel.questionPannel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                            // Increase the end index by 1
                            int currentEndIndex = (Integer) endIndex.getValue();
                            endIndex.setValue(currentEndIndex + 1);
                        });
                    }
                });
                thread.start();
            }
        }

        if (source == rulesButton) {
            if (busy) {
                busy = false;
                if (thread != null && thread.isAlive()) {
                    thread.interrupt();
                    mainPanel.questionPannel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                rulesButton.setText("Kurallar");
                mainPanel.questionPannel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } else {
                busy = true;
                rulesButton.setText("Stop");
                mainPanel.questionPannel.setCursor(new Cursor(Cursor.WAIT_CURSOR));

                thread = new Thread(() -> {
                    try {
                        String description = """
                Soldaki tabloya bazı işlemler yaparak sağdaki tablo elde edilmiştir. İşlemler:
                -İki satırın yerleri değiştirilebilir.
                -İki sütunun yerleri değiştirilebilir.
                -Tablo 90 derece döndürülebilir.
                -Tablo 180 derece döndürülebilir.

                Kural:
                Bu dört işlemden her biri en fazla bir kez uygulanabilir.
                Cevap olarak hangi işlemlerin yapıldığını sırasıyla ve aralarına virgül koyarak giriniz. 

                -Satır ve sütunların yer değiştirlmesinde değişen satırların ve ve sütunların harflerini giriniz.
                -Tablo döndürülmesinde ise dönme derecesini giriniz.
                """;
                        JOptionPane.showMessageDialog(mainPanel, description, "Kurallar", JOptionPane.INFORMATION_MESSAGE);
                    } finally {
                        busy = false;
                        SwingUtilities.invokeLater(() -> {
                            rulesButton.setText("Kurallar");
                            mainPanel.questionPannel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        });
                    }
                });
                thread.start();
            }
        }

        else if (source == printButton) {
            System.out.println("Print Button Clicked"); // Debug statement
            try {
                mainPanel.questionPannel.printPanelContent();
            /*} catch (FileNotFoundException | DocumentException ex) {
                ex.printStackTrace();*/
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        else if (source == ayarlar) {
            new Settings(mainPanel);
        }
        else if (source == kopyala) {
            kopyala();
        }
        else if (source == cik) {
            System.exit(0);
        }
        else if (source == yazdir) {

        }
    }

    /*public void printQuestionsToPDF(int startIndex, int endIndex, String outputPath) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            Node current = tracker.getQuestion(startIndex);
            for (int i = startIndex; i <= endIndex && current != null; i++) {
                QuestionPannel panel = current.getPanel();
                BufferedImage image = panel.getPanelImage();
                Image pdfImage = Image.getInstance(image, null);

                // Scale image to fit page width
                pdfImage.scaleToFit(PageSize.A4.getWidth() - 72, PageSize.A4.getHeight() - 72);

                document.add(pdfImage);

                current = current.getNext();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }*/

    void buildPDF(IPrintable printable) throws FileNotFoundException, DocumentException {
        Integer start = (Integer) startIndex.getValue();
        Integer range = (Integer) endIndex.getValue();
        com.itextpdf.text.Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream("Sorular.pdf"));
        document.open();
        for (Integer i = start; i <= range; i++) {
            BufferedImage question = printable.getQuestionImage();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(question, "png", baos);
                Image iText = Image.getInstance(baos.toByteArray());
                float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                        - document.rightMargin()) / iText.getWidth()) * 100;
                iText.scalePercent(scaler);
                document.setPageSize(new Rectangle(document.getPageSize().getWidth(), iText.getHeight() + 20));
                document.newPage();
                document.add(new Paragraph(Integer.toString(i) + ". Soru"));
                document.add(iText);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        document.close();
    }

    /*
    public void buildPDF(int startIndex, int endIndex) throws FileNotFoundException, DocumentException, IOException {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
        try (FileOutputStream fos = new FileOutputStream("output.pdf")) {
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();

            // Loop through questions and answers from startIndex to endIndex
            for (int i = startIndex  ; i <= endIndex; i++) {
                QuestionTracker.Node currentNode = allQuestions.getQuestion(i);

                if (currentNode == null) {
                    break; // Exit the loop if the node is null (end of list)
                }

                // Create a table with 2 columns for side-by-side images
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100); // Set table width to fill the page
                table.setSpacingBefore(0f);
                table.setSpacingAfter(0f);

                // Calculate available width for each cell
                float availableWidth = PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin();
                float cellWidth = availableWidth / 2; // Two columns

                // Add question image to the first column
                BufferedImage questionImage = currentNode.getPanel().getImage3();
                if (questionImage != null) {
                    Image pdfQuestionImage = Image.getInstance(toByteArray(questionImage));
                    // Reduce the size slightly to leave margin from the left and bottom
                    float reducedWidth = cellWidth * 0.95f; // 5% reduction for left/right margins
                    float reducedHeight = (PageSize.A4.getHeight() - document.topMargin() - document.bottomMargin()) * 0.95f; // 5% reduction for top/bottom margins
                    pdfQuestionImage.scaleToFit(reducedWidth, reducedHeight);
                    PdfPCell questionCell = new PdfPCell(pdfQuestionImage);
                    questionCell.setBorder(PdfPCell.NO_BORDER);
                    questionCell.setPadding(5); // Add padding
                    questionCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    questionCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(questionCell);
                } else {
                    table.addCell(new PdfPCell(new Paragraph("No question image")));
                }

                // Add answer image to the second column
                BufferedImage answerImage = currentNode.getAnswer().getImage3();
                if (answerImage != null) {
                    Image pdfAnswerImage = Image.getInstance(toByteArray(answerImage));
                    // Reduce the size slightly to leave margin from the left and bottom,
                    float reducedWidth = cellWidth * 0.95f; // 5% reduction for left/right margins
                    float reducedHeight = (PageSize.A4.getHeight() - document.topMargin() - document.bottomMargin()) * 0.95f; // 5% reduction for top/bottom margins
                    pdfAnswerImage.scaleToFit(reducedWidth, reducedHeight);
                    PdfPCell answerCell = new PdfPCell(pdfAnswerImage);
                    answerCell.setBorder(PdfPCell.NO_BORDER);
                    answerCell.setPadding(5); // Add padding
                    answerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    answerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(answerCell);
                } else {
                    table.addCell(new PdfPCell(new Paragraph("No answer image")));
                }

                document.add(new Paragraph("Question " + (i + 1)));

                // Add the table to the document
                document.add(table);

                // Add description or title for each question (optional)


                // Add spacing between questions
                document.add(new Paragraph(" "));
            }

            document.close(); // Ensure document is closed before finishing the method
        } catch (IOException e) {
            e.printStackTrace();
            throw e; // Re-throw to ensure the caller is aware of the issue
        } catch (DocumentException e) {
            e.printStackTrace();
            throw e; // Re-throw to ensure the caller is aware of the issue
        }
    }*/

    // Helper method to convert BufferedImage to byte array
    private byte[] toByteArray(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }


    void createPdfWithPanelImage(String dest) throws Exception {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();

        BufferedImage panelImage = mainPanel.questionPannel.getPanelImage();
        File tempFile = File.createTempFile("panelImage", ".png");
        ImageIO.write(panelImage, "png", tempFile);

        Image image = Image.getInstance(tempFile.getAbsolutePath());
        image.scaleToFit(PageSize.A4.getWidth() - 50, PageSize.A4.getHeight() - 50); // Adjust scale as needed
        image.setAbsolutePosition(25, PageSize.A4.getHeight() - image.getScaledHeight() - 25); // Adjust position as needed
        document.add(image);

        document.close();
        tempFile.delete();
    }

    private void addImageToDocument(com.itextpdf.text.Document document, BufferedImage image, String title) throws IOException, DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush(); // Ensure data is written to ByteArrayOutputStream
        com.itextpdf.text.Image pdfImage = com.itextpdf.text.Image.getInstance(baos.toByteArray());
        baos.close(); // Close ByteArrayOutputStream

        pdfImage.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
        pdfImage.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER);
        document.add(pdfImage);
        document.add(new Paragraph(title));
    }

    @Override
    public BufferedImage getQuestionImage() {
        mainPanel.questionPannel.question = new Squares(4, 8);
        ArrayList<ArrayList<Integer>> newBoard = new ArrayList<>();
        // Take a deep copy
        for (ArrayList<Integer> row : mainPanel.questionPannel.question.board) {
            newBoard.add(new ArrayList<>(row));
        }
        mainPanel.questionPannel.answer.board = newBoard;
        mainPanel.questionInputPanel.questionAnswerInput.setText("");
        mainPanel.questionPannel.answer.askQuestion();
        BufferedImage image = new BufferedImage(mainPanel.questionPannel.getWidth(),
                mainPanel.questionPannel.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        mainPanel.questionPannel.paintComponent(image.createGraphics());
        int hucreBoyu = Math.min(mainPanel.questionPannel.getWidth(), mainPanel.questionPannel.getHeight()) / 6;
        BufferedImage question = image.getSubimage(
                mainPanel.questionPannel.left_space - (int) (hucreBoyu * 0.2) - 50,
                mainPanel.questionPannel.up_space - (int) (hucreBoyu * 0.2) - 10,
                mainPanel.questionPannel.board_length + (int) (hucreBoyu * 0.4) + 3,
                mainPanel.questionPannel.board_length + (int) (hucreBoyu * 0.4) + 3);
        BufferedImage answer = image.getSubimage(
                mainPanel.questionPannel.right_board - (int) (hucreBoyu * 0.2) + 435 ,
                mainPanel.questionPannel.up_space - (int) (hucreBoyu * 0.2) - 10,
                mainPanel.questionPannel.board_length + (int) (hucreBoyu * 0.4) + 3,
                mainPanel.questionPannel.board_length + (int) (hucreBoyu * 0.4) + 3);
        BufferedImage retval = new BufferedImage(
                question.getWidth() + answer.getWidth() + mainPanel.questionPannel.middle_space,
                question.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics g = retval.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.drawImage(question, 0, 0, this);
        g.drawImage(answer, question.getWidth() + mainPanel.questionPannel.middle_space, 0, this);
        return retval;
    }

    private BufferedImage getAnswerImage() {
        return mainPanel.questionPannel.answer.getImage3();
    }

    public void kopyala() {
        int copy_mode = copylist.getSelectedIndex();
        int width = mainPanel.questionPannel.getWidth();
        int height = mainPanel.questionPannel.getHeight();

        // Debugging: Check the dimensions of the main panel
        System.out.println("Main Panel Dimensions: Width = " + width + ", Height = " + height);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        mainPanel.questionPannel.paintComponent(g2d);
        g2d.dispose(); // Ensure graphics object is disposed properly

        // Debugging: Save the image to a file for verification
        try {
            ImageIO.write(image, "png", new File("mainPanel.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Correctly extract the subimages for question and answer
        int leftX = mainPanel.questionPannel.left_space;
        int rightX = mainPanel.questionPannel.right_board;
        int upY = mainPanel.questionPannel.up_space;
        int boardLength = mainPanel.questionPannel.board_length;

        // Debugging: Print the calculated values for subimage extraction
        System.out.println("leftX: " + leftX + ", rightX: " + rightX + ", upY: " + upY + ", boardLength: " + boardLength);

        // Extract the subimages
        BufferedImage question = image.getSubimage(leftX - 1, upY - 1, boardLength + 3, boardLength + 3);
        BufferedImage answer = image.getSubimage(rightX + 450, upY - 20, boardLength + 35, boardLength + 35);

        // Debugging: Save subimages for verification
        try {
            ImageIO.write(question, "png", new File("question.png"));
            ImageIO.write(answer, "png", new File("answer.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (copy_mode == 0) {
            image = new BufferedImage(question.getWidth() + answer.getWidth() + mainPanel.questionPannel.middle_space,
                    question.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.drawImage(question, 0, 0, null);
            g.drawImage(answer, question.getWidth() + mainPanel.questionPannel.middle_space, 0, null);
        } else if (copy_mode == 1) {
            image = new BufferedImage(question.getWidth(),
                    question.getHeight() + answer.getHeight() + mainPanel.questionPannel.middle_space,
                    BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.drawImage(question, 0, 0, null);
            g.drawImage(answer, 0, question.getHeight() + mainPanel.questionPannel.middle_space, this);
        } else if (copy_mode == 2) {
            image = question;
        } else if (copy_mode == 3) {
            image = answer;
        }

        // Debugging: Save final image for verification
        try {
            ImageIO.write(image, "png", new File("finalImage.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(image), null);
    }
}
