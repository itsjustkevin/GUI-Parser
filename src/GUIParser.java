import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;


public class GUIParser {

    private final String DELIMITERREGEX = "(?<=[(),\".;:])|(?=[(),\".;:])| ";
    private ArrayList<String> tokenList;
    private BufferedReader buffReader;
    private JFrame mainFrame;
    private int currentIndex;
    private boolean isFrame;
    private JPanel panel;
    private String widgetText;
    private ButtonGroup radioGroup;
    private JRadioButton radioButton;
    private JTextField textField;


    /**
     * Constructor begins the parsing process.
     */
    public GUIParser() {
        tokenList = new ArrayList<>();
        currentIndex = 0;
        retrieveFile();
    }


    /**
     * Found something called JFileChooser that lets you pick the file you want read in to the program
     * used this to let the user pick the file that they want parsed with the program.
     */
    private void retrieveFile() {
        // try statement used to handle errors in the program.
        try {
            int option = -1;
            JFileChooser chooser = new JFileChooser(".");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter("TEXT FILES", "txt", "text"));
            chooser.setDialogTitle("GUI Parser");

            while (option != JFileChooser.APPROVE_OPTION && option != JFileChooser.CANCEL_OPTION) {
                option = chooser.showOpenDialog(null);
            }
            if (option == JFileChooser.CANCEL_OPTION) {
                return;
            }
            buffReader = new BufferedReader(new FileReader(chooser.getSelectedFile()));
            System.out.println("Loaded: " + chooser.getSelectedFile().getName() + "\n\n");
            analyzeInput();
            parseGUI();
        } catch (IOException e) {
            System.out.println("File Error");
        }
    }


    /**
     * Pull in the text file to an ArrayList with all of the tokens and uses the
     * regular expressions delim that I created to parse the tokens.
     */
    private void analyzeInput() {
        try {
            String line;
            boolean isInQuotes = false;
            String text = "";

            while ((line = buffReader.readLine()) != null) {
                line = line.trim();
                String[] split = line.split(DELIMITERREGEX);
                System.out.println("Input Line: " + line);

                for (int i = 0; i < split.length; i++) {
                    split[i] = split[i].trim();

                    if (isInQuotes) {
                        if (split[i].equals("\"")) {
                            tokenList.add(text.trim());
                            System.out.println("\tToken: " + text.trim());
                            text = "";
                            tokenList.add(split[i]);
                            System.out.println("\tToken: " + split[i]);
                        } else {
                            text += split[i] + " ";
                        }
                    } else if (split[i].trim().length() > 0) {
                        tokenList.add(split[i].trim());
                        System.out.println("\tToken: " + split[i]);
                    }
                    if (split[i].equals("\"")) {
                        isInQuotes = !isInQuotes;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Problem parsing the content");
            return;
        }
    }


    /**
     * The meat of the recursive descent parser.
     */
    private void parseGUI() {
        int width, height;

        //Start JFrame Creation:
        if (tokenList.get(currentIndex).equalsIgnoreCase("Window")) {
            isFrame = true;
            mainFrame = new JFrame();
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            currentIndex++;

            //Start String:
            if (tokenList.get(currentIndex).equals("\"")) {
                currentIndex++;
                mainFrame.setTitle(tokenList.get(currentIndex));
                currentIndex++;

                //End String
                if (tokenList.get(currentIndex).equals("\"")) {
                    currentIndex++;

                    //JFrame parsing
                    if (tokenList.get(currentIndex).equals("(")) {
                        currentIndex++;
                        try {
                            width = Integer.parseInt(tokenList.get(currentIndex));
                        } catch (NumberFormatException e) {
                            System.out.println("Syntax Error: " + tokenList.get(currentIndex) + " is an invalid window width.");
                            return;
                        }
                        currentIndex++;
                        if (tokenList.get(currentIndex).equals(",")) {
                            currentIndex++;
                            try {
                                height = Integer.parseInt(tokenList.get(currentIndex));
                            } catch (NumberFormatException e) {
                                System.out.println("Syntax Error: " + tokenList.get(currentIndex) + " is an invalid window height.");
                                return;
                            }
                            currentIndex++;

                            //End JFrame Parameters
                            if (tokenList.get(currentIndex).equals(")")) {
                                mainFrame.setSize(width, height);
                                currentIndex++;
                                if (parseLayout()) {;
                                    if (parseWidgets()) {;
                                        if (tokenList.get(currentIndex).equalsIgnoreCase("End")) {
                                            currentIndex++;
                                            if (tokenList.get(currentIndex).equals(".")) {
                                                mainFrame.setVisible(true);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Syntax error, unable to parse.");
    }


    /**
     * Determines if it was parsed correctly.
     */
    private boolean parseLayout() {
        if (tokenList.get(currentIndex).equalsIgnoreCase("Layout")) {
            currentIndex++;
            if (parseLayoutType()) {
                if (tokenList.get(currentIndex).equals(":")) {
                    currentIndex++;
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Parses either flow or grid.
     */
    private boolean parseLayoutType() {
        int rows, cols, colSpacing, rowSpacing;

        if (tokenList.get(currentIndex).equalsIgnoreCase("flow")) {
            if (isFrame) {
                mainFrame.setLayout(new FlowLayout());
            } else {
                panel.setLayout(new FlowLayout());
            }
            currentIndex++;
            return true;
        } else if (tokenList.get(currentIndex).equalsIgnoreCase("grid")) {
            currentIndex++;

            //Start parsing Grid Layout parameters:
            if (tokenList.get(currentIndex).equals("(")) {
                currentIndex++;
                try {
                    rows = Integer.parseInt(tokenList.get(currentIndex));
                } catch (NumberFormatException e) {
                    System.out.println("Syntax Error: " + tokenList.get(currentIndex) + " is an invalid Grid Layout Row Value.");
                    return false;
                }
                currentIndex++;
                if (tokenList.get(currentIndex).equals(",")) {
                    currentIndex++;
                    try {
                        cols = Integer.parseInt(tokenList.get(currentIndex));
                    } catch (NumberFormatException e) {
                        System.out.println("Syntax Error: " + tokenList.get(currentIndex) + " is an invalid Grid Layout Column Value.");
                        return false;
                    }
                    currentIndex++;

                    //End parameter parsing if only two parameters are given:
                    if (tokenList.get(currentIndex).equals(")")) {
                        if (isFrame) {
                            mainFrame.setLayout(new GridLayout(rows, cols));
                        } else {
                            panel.setLayout(new GridLayout(rows, cols));
                        }
                        currentIndex++;
                        return true;
                    }

                    //Continue parameter parsing for Grid Layout if more are given:
                    else if (tokenList.get(currentIndex).equals(",")) {
                        currentIndex++;
                        try {
                            colSpacing = Integer.parseInt(tokenList.get(currentIndex));
                        } catch (NumberFormatException e) {
                            System.out.println("Syntax Error: " + tokenList.get(currentIndex) + " is an invalid Grid Layout Column Spacing Value.");
                            return false;
                        }
                        currentIndex++;
                        if (tokenList.get(currentIndex).equals(",")) {
                            currentIndex++;
                            try {
                                rowSpacing = Integer.parseInt(tokenList.get(currentIndex));
                            } catch (NumberFormatException e) {
                                System.out.println("Syntax Error: " + tokenList.get(currentIndex) + " is an invalid Grid Layout Row Spacing Value.");
                                return false;
                            }
                            currentIndex++;
                            if (tokenList.get(currentIndex).equals(")")) {
                                if (isFrame) {
                                    mainFrame.setLayout(new GridLayout(rows, cols, colSpacing, rowSpacing));
                                } else {
                                    panel.setLayout(new GridLayout(rows, cols, colSpacing, rowSpacing));
                                }
                                currentIndex++;
                                return true;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Syntax error detected.");
        return false;
    }


    /**
     * recursively parses widgets.
     */
    private boolean parseWidgets() {
        if (parseWidget()) {
            if (parseWidgets()) {
                return true;
            }
            return true;
        }
        return false;
    }


    /**
     * Parse widgets based on type.
     */
    private boolean parseWidget() {

        if (tokenList.get(currentIndex).equalsIgnoreCase("Button")) {
            currentIndex++;
            if (tokenList.get(currentIndex).equals("\"")) {
                currentIndex++;
                widgetText = tokenList.get(currentIndex);
                currentIndex++;
                if (tokenList.get(currentIndex).equals("\"")) {
                    currentIndex++;
                    if (tokenList.get(currentIndex).equals(";")) {
                        if (isFrame) {
                            JButton button = new JButton(widgetText);
                            button.addActionListener((ActionEvent e) -> {
                                textField.setText(button.getText());
                            });
                            mainFrame.add(button);
                        } else {
                            JButton button = new JButton(widgetText);
                            button.addActionListener((ActionEvent e) -> {
                                textField.setText(button.getText());
                            });
                            panel.add(button);
                        }
                        currentIndex++;
                        return true;
                    }
                }
            }
        } else if (tokenList.get(currentIndex).equalsIgnoreCase("Group")) {
            radioGroup = new ButtonGroup();
            currentIndex++;
            if (parseRadioButtons()) {
                if (tokenList.get(currentIndex).equalsIgnoreCase("End")) {
                    currentIndex++;
                    if (tokenList.get(currentIndex).equals(";")) {
                        currentIndex++;
                        return true;
                    }
                }
            }
        } else if (tokenList.get(currentIndex).equalsIgnoreCase("Label")) {
            currentIndex++;
            if (tokenList.get(currentIndex).equals("\"")) {
                currentIndex++;
                widgetText = tokenList.get(currentIndex);
                currentIndex++;
                if (tokenList.get(currentIndex).equals("\"")) {
                    currentIndex++;
                    if (tokenList.get(currentIndex).equals(";")) {
                        if (isFrame) {
                            mainFrame.add(new JLabel(widgetText));
                        } else {
                            panel.add(new JLabel(widgetText));
                        }
                        currentIndex++;
                        return true;
                    }
                }
            }
        } else if (tokenList.get(currentIndex).equalsIgnoreCase("Panel")) {
            if (isFrame) {
                mainFrame.add(panel = new JPanel());
            } else {
                panel.add(panel = new JPanel());
            }
            isFrame = false;
            currentIndex++;
            if (parseLayout()) {
                if (parseWidgets()) {
                    if (tokenList.get(currentIndex).equalsIgnoreCase("End")) {
                        currentIndex++;
                        if (tokenList.get(currentIndex).equals(";")) {
                            currentIndex++;
                            return true;
                        }
                    }
                }
            }
        } else if (tokenList.get(currentIndex).equalsIgnoreCase("Textfield")) {
            int length;
            currentIndex++;
            try {
                length = Integer.parseInt(tokenList.get(currentIndex));
            } catch (NumberFormatException e) {
                System.out.println("Syntax Error: " + tokenList.get(currentIndex) + " is an invalid Textfield Length.");
                return false;
            }
            currentIndex++;
            if (tokenList.get(currentIndex).equals(";")) {
                if (isFrame) {
                    mainFrame.add(textField = new JTextField(length));
                } else {
                    panel.add(textField = new JTextField(length));
                }
                currentIndex++;
                return true;
            }
        }
        return false;
    }


    /**
     * parses radio buttons
     */
    private boolean parseRadioButtons() {
        if (parseRadioButton()) {
            if (parseRadioButtons()) {
                return true;
            }
            return true;
        }
        return false;
    }


    /**
     * Parses radio buttons and returns false if it wasnt able to happen
     */
    private boolean parseRadioButton() {
        if (tokenList.get(currentIndex).equalsIgnoreCase("Radio")) {
            currentIndex++;
            if (tokenList.get(currentIndex).equals("\"")) {
                currentIndex++;
                widgetText = tokenList.get(currentIndex);
                currentIndex++;
                if (tokenList.get(currentIndex).equals("\"")) {
                    currentIndex++;
                    if (tokenList.get(currentIndex).equals(";")) {
                        radioButton = new JRadioButton(widgetText);
                        radioGroup.add(radioButton);
                        if (isFrame) {
                            mainFrame.add(radioButton);
                        } else {
                            panel.add(radioButton);
                        }
                        currentIndex++;
                        return true;
                    }
                }
            }
        }
        return false;
    }
}