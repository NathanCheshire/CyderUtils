import com.cyder.exception.CyderException;
import com.cyder.exception.FatalException;
import com.cyder.games.Hangman;
import com.cyder.games.TicTacToe;
import com.cyder.handler.PhotoViewer;
import com.cyder.handler.TestClass;
import com.cyder.threads.YoutubeThread;
import com.cyder.ui.*;
import com.cyder.utilities.*;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

//todo put all background checking things in one thread
//todo fix double chime on hour glitch
//todo when doing confirmations through the console, pull it to front and then push it back
//todo make prefs for filled output area and input field
//todo let color for text be inputed in rgb format too
//todo be able to set background to a solid color and make that an image and save it

//todo utilize colors, fonts, font weights, and new lines now
//<html>test<br/><i>second line but italics<i/><br/>third!!<br/><p style="color:rgb(252, 251, 227)">fourth with color</p>
// <p style="font-family:verdana">fifth with font</p></html>

//todo notes and textviewer non-swing dependent

//todo redo edit user GUI, put in a scrollable UI, tooltips for everything, seconds for console clock option, make checkboxes smaller

//todo perlin-noise GUI swap between 2D and 3D and add color range too
//todo make a widget version of cyder that you can swap between big window and widget version, background is get cropped image
//todo make pixelating pictures it's own widget

//todo hangman use cyder frame
//todo photo viewer renmaing needs to be cyderframe
//todo utilize start animations after you fix it

//todo make an animation util class
//todo network util class
//todo ui utils class

//todo further class separation from GeneralUtil.java

//todo add a systems error dir if no users <- if possibility of no user put here too (see readData() loop)
//todo add a handle that you can use when unsure if there is a user to avoid looping until stackoverflow

//todo I feel like a lot of stuff should be static since it means it belongs to the class an not an instance of it

//todo make use of nbt where nst can be nbt
//todo make nbt extend nst

//todo cyder frame should have a notify method that will drop down from center and back up
//todo enter animation toggle for notification

//todo make the frame and drag label stay when switching backgrounds and the image be separate
//todo you kind of did this in login with the sliding text, then notification will not go over it

//todo double hash sha perhaps to avoid someone just hashing their own password and pasting it in

//todo hot key in menu to kill background processes like bletchy and youtube threads, anything that makes the icon yellow basically

//todo allow users to map up to three internet links on the menu, add a bar to sep system from user stuff

public class CyderMain{
    //console vars
    private static JTextPane outputArea;
    private JTextField inputField;
    private JFrame consoleFrame;
    private JButton minimize;
    private JButton close;
    private JLabel consoleClockLabel;
    private boolean updateConsoleClock;
    private JLabel loginLabel;
    private JLabel loginLabel2;
    private JLabel loginLabel3;
    private JLabel parentLabel;
    private JLabel temporaryLabel;
    private JLabel loginDragLabel;
    private CyderScrollPane outputScroll;
    private JButton alternateBackground;
    private JLabel consoleDragLabel;
    private JLayeredPane parentPane;
    private JButton suggestionButton;
    private JButton menuButton;
    private JFrame loginFrame;
    private JTextField nameField;
    private JPasswordField pass;
    private JLabel newUserLabel;
    private JLabel menuLabel;

    //Objects for main use
    private GeneralUtil mainGeneralUtil;
    private StringUtil stringUtil;
    private CyderAnimation animation;
    private Notes userNotes;

    //operation var
    private static ArrayList<String> operationList = new ArrayList<>();
    private static int scrollingIndex;

    //deiconified restore vars
    private int restoreX;
    private int restoreY;

    //drag pos vars;
    private int xMouse;
    private int yMouse;

    //handler case vars
    private String operation;

    //anagram one var
    private String anagram;

    //Edit user vars
    private JFrame editUserFrame;
    private CyderScrollPane backgroundListScroll;
    private CyderButton openBackground;
    private CyderButton addMusic;
    private CyderButton openMusic;
    private JList<?> backgroundSelectionList;
    private CyderScrollPane musicListScroll;
    private List<File> musicList;
    private List<String> musicNameList;
    private JList<?> musicSelectionList;
    private List<File> backgroundsList;
    private List<String> backgroundsNameList;
    private CyderButton changeUsername;
    private CyderButton changePassword;

    //font vars
    private JList fontList;

    //create user vars
    private CyderFrame createUserFrame;
    private JPasswordField newUserPasswordconf;
    private JPasswordField newUserPassword;
    private JTextField newUserName;
    private CyderButton createNewUser;
    private CyderButton chooseBackground;
    private File createUserBackground;

    //minecraft class so we can only have one
    private MinecraftWidget mw;

    //notificaiton
    private static Notification consoleNotification;

    //pixealte file
    private File pixelateFile;

    //Linked List of youtube scripts
    private LinkedList<YoutubeThread> youtubeThreads = new LinkedList<>();

    //sliding background var
    private boolean slidLeft;

    //notifications for holidays
    private SpecialDay specialDayNotifier;

    //notify test vars
    private int notificaitonTestWidth;
    private String notificationTestString;

    //call constructor
    public static void main(String[] CA) {
        new CyderMain();
        logArgs(CA);
    }

    private CyderMain() {
        //this adds a shutdown hook so that we always do certain things on exit
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown,"exit-hook"));

        initObjects();
        initSystemProperties();
        initUIManager();

        mainGeneralUtil.cleanUpUsers();
        mainGeneralUtil.deleteTempDir();
        mainGeneralUtil.varInit();

        backgroundProcessChecker();

        boolean nathanLenovo = mainGeneralUtil.compMACAddress(mainGeneralUtil.getMACAddress());

        if (nathanLenovo) {
            mainGeneralUtil.setDebugMode(true);
            autoCypher();
        }

        else if (!mainGeneralUtil.released()) {
            System.exit(0);
        }

        else {
            login(false);
        }
    }

    private void initObjects() {
        mainGeneralUtil = new GeneralUtil();
        animation = new CyderAnimation();
        stringUtil = new StringUtil();
        stringUtil.setOutputArea(outputArea);
    }

    private void initSystemProperties() {
        //Fix scaling issue for high DPI displays like nathanLenovo which is 2560x1440
        System.setProperty("sun.java2d.uiScale","1.0");
    }

    private void initUIManager() {
        //this sets up special looking tooltips
        UIManager.put("ToolTip.background", mainGeneralUtil.consoleColor);
        UIManager.put("ToolTip.border", mainGeneralUtil.tooltipBorderColor);
        UIManager.put("ToolTip.font", mainGeneralUtil.tahoma);
        UIManager.put("ToolTip.foreground", mainGeneralUtil.tooltipForegroundColor);
    }

    private void autoCypher() {
        try {
            File autoCypher = new File("../autocypher.txt");
            File Users = new File("src\\com\\cyder\\users\\");

            if (autoCypher.exists() && Users.listFiles().length != 0) {
                BufferedReader ac = new BufferedReader(new FileReader(autoCypher));

                String line = ac.readLine();
                String[] parts = line.split(":");

                if (parts.length == 2 && !parts[0].equals("") && !parts[1].equals(""))
                    recognize(parts[0], parts[1].toCharArray());
            }

            else {
                login(false);
            }
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void console() {
        try{
            mainGeneralUtil.initBackgrounds();
            mainGeneralUtil.getScreenSize();
            mainGeneralUtil.resizeImages();
            mainGeneralUtil.getValidBackgroundPaths();
            mainGeneralUtil.initBackgrounds();
            mainGeneralUtil.getScreenSize();
            mainGeneralUtil.getBackgroundSize();

            consoleFrame = new JFrame();
            consoleFrame.setUndecorated(true);
            consoleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            if (mainGeneralUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                mainGeneralUtil.setBackgroundX((int) mainGeneralUtil.getScreenSize().getWidth());
                mainGeneralUtil.setBackgroundY((int) mainGeneralUtil.getScreenSize().getHeight());
            }

            consoleFrame.setBounds(0, 0, mainGeneralUtil.getBackgroundX(), mainGeneralUtil.getBackgroundY());
            consoleFrame.setTitle(mainGeneralUtil.getCyderVer() + " Cyder [" + mainGeneralUtil.getUsername() + "]");

            parentPane = new JLayeredPane();
            parentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

            consoleFrame.setContentPane(parentPane);

            parentPane.setLayout(null);

            parentLabel = new JLabel();

            if (mainGeneralUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                parentLabel.setIcon(new ImageIcon(mainGeneralUtil.resizeImage((int) mainGeneralUtil.getScreenSize().getWidth(), (int) mainGeneralUtil.getScreenSize().getHeight(), mainGeneralUtil.getCurrentBackground())));
                parentLabel.setBounds(0, 0, mainGeneralUtil.getBackgroundX(), mainGeneralUtil.getBackgroundY());
                mainGeneralUtil.setBackgroundX((int) mainGeneralUtil.getScreenSize().getWidth());
                mainGeneralUtil.setBackgroundY((int) mainGeneralUtil.getScreenSize().getHeight());
            }

            else {
                parentLabel.setIcon(new ImageIcon(mainGeneralUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().toString())));
                parentLabel.setBounds(0, 0, mainGeneralUtil.getBackgroundX(), mainGeneralUtil.getBackgroundY());
            }

            parentLabel.setBorder(new LineBorder(mainGeneralUtil.navy,8,false));
            parentLabel.setToolTipText(mainGeneralUtil.getCurrentBackground().getName().replace(".png", ""));

            parentPane.add(parentLabel,1,0);

            consoleFrame.setIconImage(mainGeneralUtil.getCyderIcon().getImage());

            outputArea = new JTextPane() {
                @Override
                public void setBorder(Border border) {
                    //no border
                }
            };
            outputArea.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    minimizeMenu();
                    inputField.requestFocus();
                }

                @Override
                public void focusLost(FocusEvent e) {

                }
            });

            outputArea.setEditable(false);
            outputArea.setAutoscrolls(true);
            outputArea.setBounds(10, 62, mainGeneralUtil.getBackgroundX() - 20, mainGeneralUtil.getBackgroundY() - 204);
            outputArea.setFocusable(true);
            outputArea.setSelectionColor(new Color(204,153,0));
            outputArea.setOpaque(false);
            outputArea.setBackground(new Color(0,0,0,0));

            outputScroll = new CyderScrollPane(outputArea,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            outputScroll.setThumbColor(mainGeneralUtil.intellijPink);
            outputScroll.getViewport().setBorder(null);
            outputScroll.getViewport().setOpaque(false);
            outputScroll.setOpaque(false);

            if (mainGeneralUtil.getUserData("OutputBorder").equalsIgnoreCase("1")) {
                outputScroll.setBorder(new LineBorder(mainGeneralUtil.vanila,3,true));
            }

            else {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            }

            outputScroll.setBounds(10, 62, mainGeneralUtil.getBackgroundX() - 20, mainGeneralUtil.getBackgroundY() - 204);

            parentLabel.add(outputScroll);

            inputField = new JTextField(40);

            if (mainGeneralUtil.getUserData("InputBorder").equalsIgnoreCase("1")) {
                inputField.setBorder(new LineBorder(mainGeneralUtil.vanila,3,true));
            }

            else {
                inputField.setBorder(BorderFactory.createEmptyBorder());
            }


            inputField.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (inputField.getText().length() == 1) {
                        inputField.setText(inputField.getText().toUpperCase());
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
                        handle("controlc");
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_DOWN) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainGeneralUtil.setConsoleDirection(mainGeneralUtil.CYDER_DOWN);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_RIGHT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainGeneralUtil.setConsoleDirection(mainGeneralUtil.CYDER_RIGHT);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_UP) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainGeneralUtil.setConsoleDirection(mainGeneralUtil.CYDER_UP);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_LEFT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainGeneralUtil.setConsoleDirection(mainGeneralUtil.CYDER_LEFT);
                        exitFullscreen();
                    }
                }

                @Override
                public void keyReleased(java.awt.event.KeyEvent e) {
                    if (inputField.getText().length() == 1) {
                        inputField.setText(inputField.getText().toUpperCase());
                    }
                }

                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    if (inputField.getText().length() == 1) {
                        inputField.setText(inputField.getText().toUpperCase());
                    }
                }
            });

            inputField.setToolTipText("Input Field");
            inputField.setSelectionColor(mainGeneralUtil.selectionColor);
            inputField.addKeyListener(commandScrolling);

            consoleFrame.addWindowListener(consoleEcho);

            inputField.setBounds(10, 82 + outputArea.getHeight(),
                    mainGeneralUtil.getBackgroundX() - 20, mainGeneralUtil.getBackgroundY() - (outputArea.getHeight() + 62 + 40));
            inputField.setOpaque(false);

            parentLabel.add(inputField);

            inputField.addActionListener(inputFieldAction);
            inputField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    minimizeMenu();
                }

                @Override
                public void focusLost(FocusEvent e) {

                }
            });

            inputField.setCaretColor(mainGeneralUtil.vanila);

            mainGeneralUtil.readUserData();

            Font Userfont = new Font(mainGeneralUtil.getUserData("Font"),Font.BOLD, 30);
            Color Usercolor = new Color(Integer.parseInt(mainGeneralUtil.getUserData("Red")),
                    Integer.parseInt(mainGeneralUtil.getUserData("Green")),
                    Integer.parseInt(mainGeneralUtil.getUserData("Blue")));

            mainGeneralUtil.setUsercolor(Usercolor);
            mainGeneralUtil.setUserfont(Userfont);

            inputField.setForeground(Usercolor);
            outputArea.setForeground(Usercolor);

            inputField.setFont(Userfont);
            outputArea.setFont(Userfont);

            suggestionButton = new JButton("");
            suggestionButton.setToolTipText("Suggestions");
            suggestionButton.addActionListener(e -> {
                println("What feature would you like to suggestion? (Please include as much detail as possible such as what" +
                        "key words you should type and how it should be responded to and any options you think might be necessary)");
                mainGeneralUtil.setUserInputDesc("suggestion");
                mainGeneralUtil.setUserInputMode(true);
                inputField.requestFocus();
            });

            suggestionButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\suggestion2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\suggestion1.png"));
                }
            });

            suggestionButton.setBounds(32, 4, 22, 22);

            ImageIcon DebugIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\suggestion1.png");

            suggestionButton.setIcon(DebugIcon);

            parentLabel.add(suggestionButton);

            suggestionButton.setFocusPainted(false);
            suggestionButton.setOpaque(false);
            suggestionButton.setContentAreaFilled(false);
            suggestionButton.setBorderPainted(false);

            menuButton = new JButton("");

            menuLabel = new JLabel();
            menuLabel.setVisible(false);

            menuButton.setToolTipText("Menu");

            menuButton.addMouseListener(consoleMenu);

            menuButton.setBounds(4, 4, 22, 22);

            ImageIcon MenuIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\menuSide1.png");

            menuButton.setIcon(MenuIcon);

            parentLabel.add(menuButton);

            menuButton.setVisible(true);
            menuButton.setFocusPainted(false);
            menuButton.setOpaque(false);
            menuButton.setContentAreaFilled(false);
            menuButton.setBorderPainted(false);

            minimize = new JButton("");
            minimize.setToolTipText("Minimize");
            minimize.addActionListener(e -> {
                restoreX = consoleFrame.getX();
                restoreY = consoleFrame.getY();
                mainGeneralUtil.minimizeAnimation(consoleFrame);
                updateConsoleClock = false;
                consoleFrame.setState(Frame.ICONIFIED);
                minimizeMenu();
            });

            minimize.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    minimize.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Minimize2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    minimize.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Minimize1.png"));
                }
            });

            minimize.setBounds(mainGeneralUtil.getBackgroundX() - 81, 4, 22, 20);

            ImageIcon mini = new ImageIcon("src\\com\\cyder\\io\\pictures\\Minimize1.png");
            minimize.setIcon(mini);
            parentLabel.add(minimize);
            minimize.setFocusPainted(false);
            minimize.setOpaque(false);
            minimize.setContentAreaFilled(false);
            minimize.setBorderPainted(false);

            alternateBackground = new JButton("");
            alternateBackground.setToolTipText("Alternate Background");
            alternateBackground.addActionListener(e -> {
                mainGeneralUtil.initBackgrounds();

                if (mainGeneralUtil.canSwitchBackground() && mainGeneralUtil.getValidBackgroundPaths().length > 1) {
                    mainGeneralUtil.setCurrentBackgroundIndex(mainGeneralUtil.getCurrentBackgroundIndex() + 1);
                    switchBackground();
                }

                else if (mainGeneralUtil.OnLastBackground() && mainGeneralUtil.getValidBackgroundPaths().length > 1) {
                    mainGeneralUtil.setCurrentBackgroundIndex(0);
                    switchBackground();
                }

                else if (mainGeneralUtil.getValidBackgroundPaths().length == 1) {
                    println("You only have one background image. Would you like to add more? (Enter yes/no)");
                    inputField.requestFocus();
                    mainGeneralUtil.setUserInputMode(true);
                    mainGeneralUtil.setUserInputDesc("addbackgrounds");
                }

                else {
                    try {
                        mainGeneralUtil.handle(new FatalException("Background DNE"));
                        println("Error in parsing background; perhaps it was deleted.");
                    }

                    catch (Exception ex) {
                        mainGeneralUtil.handle(ex);
                    }
                }
            });

            alternateBackground.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\ChangeSize2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\ChangeSize1.png"));
                }
            });

            alternateBackground.setBounds(mainGeneralUtil.getBackgroundX() - 54, 4, 22, 20);

            ImageIcon Size = new ImageIcon("src\\com\\cyder\\io\\pictures\\ChangeSize1.png");
            alternateBackground.setIcon(Size);

            parentLabel.add(alternateBackground);

            alternateBackground.setFocusPainted(false);
            alternateBackground.setOpaque(false);
            alternateBackground.setContentAreaFilled(false);
            alternateBackground.setBorderPainted(false);

            close = new JButton("");
            close.setToolTipText("Close");
            close.addActionListener(e -> {
                if (loginFrame != null && loginFrame.isVisible()) {
                    mainGeneralUtil.closeAnimation(consoleFrame);
                    consoleFrame = null;
                }

                else
                    exit();
            });

            close.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    close.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Close2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    close.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Close1.png"));
                }
            });

            close.setBounds(mainGeneralUtil.getBackgroundX() - 27, 4, 22, 20);

            ImageIcon exit = new ImageIcon("src\\com\\cyder\\io\\pictures\\Close1.png");

            close.setIcon(exit);

            parentLabel.add(close);

            close.setFocusPainted(false);
            close.setOpaque(false);
            close.setContentAreaFilled(false);
            close.setBorderPainted(false);

            consoleDragLabel = new JLabel();
            consoleDragLabel.setBounds(0,0, mainGeneralUtil.getBackgroundX(),30);
            consoleDragLabel.setOpaque(true);
            consoleDragLabel.setBackground(new Color(26,32,51));
            consoleDragLabel.addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = e.getXOnScreen();
                    int y = e.getYOnScreen();

                    if (consoleFrame != null && consoleFrame.isFocused()) {
                        consoleFrame.setLocation(x - xMouse, y - yMouse);
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    xMouse = e.getX();
                    yMouse = e.getY();
                }
            });

            consoleDragLabel.setFont(mainGeneralUtil.weatherFontSmall);
            consoleDragLabel.setForeground(mainGeneralUtil.vanila);

            boolean showClock = mainGeneralUtil.getUserData("ClockOnConsole").equalsIgnoreCase("1");

            consoleClockLabel = new JLabel(mainGeneralUtil.consoleTime());
            consoleClockLabel.setFont(mainGeneralUtil.weatherFontSmall.deriveFont(20f));
            consoleClockLabel.setForeground(mainGeneralUtil.vanila);
            consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2,
                    2,(consoleClockLabel.getText().length() * 17), 25);

            consoleDragLabel.add(consoleClockLabel, SwingConstants.CENTER);

            updateConsoleClock = showClock;

            refreshConsoleClock();

            consoleClockLabel.setVisible(showClock);

            if (mainGeneralUtil.getUserData("HourlyChimes").equalsIgnoreCase("1"))
                checkChime();

            parentLabel.add(consoleDragLabel);

            consoleFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeiconified(WindowEvent e) {
                    updateConsoleClock = true;
                    consoleFrame.setLocation(restoreX, restoreY);
                }
            });

            if (mainGeneralUtil.getUserData("RandomBackground").equals("1")) {
                int len = mainGeneralUtil.getValidBackgroundPaths().length;

                if (len <= 1)
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but you only have one background file so there's no random element to be chosen.");

                else if (len > 1) {
                    try {
                        File[] backgrounds = mainGeneralUtil.getValidBackgroundPaths();

                        mainGeneralUtil.setCurrentBackgroundIndex(mainGeneralUtil.randInt(0, (backgrounds.length) - 1));

                        String newBackFile = mainGeneralUtil.getCurrentBackground().toString();

                        ImageIcon newBack;
                        int tempW = 0;
                        int tempH = 0;

                        if (mainGeneralUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                            newBack = new ImageIcon(mainGeneralUtil.resizeImage((int) mainGeneralUtil.getScreenSize().getWidth(),
                                    (int) mainGeneralUtil.getScreenSize().getHeight(), new File(newBackFile)));
                            tempW = newBack.getIconWidth();
                            tempH = newBack.getIconHeight();
                        }

                        else {
                            newBack = new ImageIcon(newBackFile);
                            tempW = newBack.getIconWidth();
                            tempH = newBack.getIconHeight();
                        }

                        mainGeneralUtil.getBackgroundSize();

                        parentLabel.setIcon(newBack);

                        consoleFrame.setBounds(0, 0, tempW, tempH);
                        parentPane.setBounds(0, 0,  tempW, tempH);
                        parentLabel.setBounds(0, 0,  tempW, tempH);

                        outputArea.setBounds(0, 0, tempW - 20, tempH - 204);
                        outputScroll.setBounds(10, 62, tempW - 20, tempH - 204);
                        inputField.setBounds(10, 82 + outputArea.getHeight(), tempW - 20, tempH - (outputArea.getHeight() + 62 + 40));
                        consoleDragLabel.setBounds(0,0,tempW,30);
                        minimize.setBounds(tempW - 81, 4, 22, 20);
                        alternateBackground.setBounds(tempW - 54, 4, 22, 20);
                        close.setBounds(tempW - 27, 4, 22, 20);

                        inputField.requestFocus();

                        parentLabel.setIcon(newBack);

                        parentLabel.setToolTipText(mainGeneralUtil.getCurrentBackground().getName().replace(".png", ""));
                        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2,
                                2,(consoleClockLabel.getText().length() * 17), 25);
                    }

                    catch (Exception e) {
                        mainGeneralUtil.handle(e);
                    }
                }

                else
                   throw new FatalException("Only one but also more than one background.");
            }

            mainGeneralUtil.startAnimation(consoleFrame);

            new Thread(() -> {
                if (!mainGeneralUtil.internetReachable())
                    notify("Internet connection slow or unavailble",
                            3000, Notification.TOP_ARROW, Notification.TOP_VANISH, parentPane,450);
            },"slow-internet-checker").start();


            if (mainGeneralUtil.getUserData("DebugWindows").equals("1")) {
                mainGeneralUtil.systemProperties();
                mainGeneralUtil.computerProperties();
                mainGeneralUtil.javaProperties();
                mainGeneralUtil.debugMenu(outputArea);
            }
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private MouseAdapter consoleMenu = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            if (!menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\menu2.png"));

                menuLabel = new JLabel("");
                menuLabel.setOpaque(true);
                menuLabel.setBackground(new Color(26,32,51));

                parentPane.add(menuLabel,1,0);

                menuLabel.setBounds(-150,30, 130,260);
                menuLabel.setVisible(true);

                JLabel calculatorLabel = new JLabel("Calculator");
                calculatorLabel.setFont(mainGeneralUtil.weatherFontSmall);
                calculatorLabel.setForeground(mainGeneralUtil.vanila);
                calculatorLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Calculator c = new Calculator();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        calculatorLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        calculatorLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                menuLabel.add(calculatorLabel);
                calculatorLabel.setBounds(5,20,150,20);

                JLabel musicLabel = new JLabel("Music");
                musicLabel.setFont(mainGeneralUtil.weatherFontSmall);
                musicLabel.setForeground(mainGeneralUtil.vanila);
                musicLabel.setBounds(5,50,150,20);
                menuLabel.add(musicLabel);
                musicLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        mainGeneralUtil.mp3("", mainGeneralUtil.getUsername(), mainGeneralUtil.getUserUUID());
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        musicLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        musicLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                JLabel weatherLabel = new JLabel("Weather");
                weatherLabel.setFont(mainGeneralUtil.weatherFontSmall);
                weatherLabel.setForeground(mainGeneralUtil.vanila);
                menuLabel.add(weatherLabel);
                weatherLabel.setBounds(5,80,150,20);
                weatherLabel.setOpaque(false);
                weatherLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        WeatherWidget ww = new WeatherWidget();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        weatherLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        weatherLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                JLabel noteLabel = new JLabel("Notes");
                noteLabel.setFont(mainGeneralUtil.weatherFontSmall);
                noteLabel.setForeground(mainGeneralUtil.vanila);
                menuLabel.add(noteLabel);
                noteLabel.setBounds(5,110,150,20);
                noteLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        userNotes = new Notes(mainGeneralUtil.getUserUUID());
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        noteLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        noteLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                JLabel editUserLabel = new JLabel("Edit user");
                editUserLabel.setFont(mainGeneralUtil.weatherFontSmall);
                editUserLabel.setForeground(mainGeneralUtil.vanila);
                menuLabel.add(editUserLabel);
                editUserLabel.setBounds(5,140,150,20);
                editUserLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        editUser();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        editUserLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        editUserLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                JLabel temperatureLabel = new JLabel("Temp conv");
                temperatureLabel.setFont(mainGeneralUtil.weatherFontSmall);
                temperatureLabel.setForeground(mainGeneralUtil.vanila);
                menuLabel.add(temperatureLabel);
                temperatureLabel.setBounds(5,170,150,20);
                temperatureLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        TempConverter tc = new TempConverter();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        temperatureLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        temperatureLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                JLabel youtubeLabel = new JLabel("YouTube");
                youtubeLabel.setFont(mainGeneralUtil.weatherFontSmall);
                youtubeLabel.setForeground(mainGeneralUtil.vanila);
                menuLabel.add(youtubeLabel);
                youtubeLabel.setBounds(5,200,150,20);
                youtubeLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        mainGeneralUtil.internetConnect("https://youtube.com");
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        youtubeLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        youtubeLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                JLabel twitterLabel = new JLabel("Twitter");
                twitterLabel.setFont(mainGeneralUtil.weatherFontSmall);
                twitterLabel.setForeground(mainGeneralUtil.vanila);
                menuLabel.add(twitterLabel);
                twitterLabel.setBounds(5,230,150,20);
                twitterLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        mainGeneralUtil.internetConnect("https://twitter.com");
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        twitterLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        twitterLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                animation.jLabelXRight(-150,0,10,8, menuLabel);
            }

            else if (menuLabel.isVisible()){
                minimizeMenu();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\menu2.png"));
            }

            else {
                menuButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\menuSide2.png"));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\menu1.png"));
            }

            else {
                menuButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\menuSide1.png"));
            }
        }
    };

    private KeyListener commandScrolling = new KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent event) {
        int code = event.getKeyCode();

        try {
            if ((event.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == 0 && ((event.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) == 0)) {
                if (code == KeyEvent.VK_DOWN) {
                    if (scrollingIndex + 1 < operationList.size()) {
                        scrollingIndex = scrollingIndex + 1;
                        inputField.setText(operationList.get(scrollingIndex));
                    }
                }

                else if (code == KeyEvent.VK_UP) {
                    boolean Found = false;

                    for (int i = 0; i < operationList.size() ; i++) {
                        if (operationList.get(i).equals(inputField.getText())) {
                            Found = true;
                            break;
                        }

                        else if (!operationList.get(i).equals(inputField.getText()) && i == operationList.size() - 1) {
                            Found = false;
                            break;
                        }
                    }

                    if (inputField.getText() == null || inputField.getText().equals("")) {
                        mainGeneralUtil.setCurrentDowns(0);
                    }

                    else if (!Found) {
                        mainGeneralUtil.setCurrentDowns(0);
                    }

                    if (scrollingIndex - 1 >= 0) {
                        if (mainGeneralUtil.getCurrentDowns() != 0) {
                            scrollingIndex = scrollingIndex - 1;
                        }

                        inputField.setText(operationList.get(scrollingIndex));
                        mainGeneralUtil.setCurrentDowns(mainGeneralUtil.getCurrentDowns() + 1);
                    }

                    if (operationList.size() == 1) {
                        inputField.setText(operationList.get(0));
                    }
                }

                for (int i = 61440 ; i < 61452 ; i++) {
                    if (code == i) {
                        int seventeen = (i - 61427);

                        if (seventeen == 17)
                            mainGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\f17.mp3");
                        else
                           println("Interesting F" + (i - 61427) + " key");
                    }
                }
            }
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
        }
    };

    //when we first launch this will check for any special days in the special days class
    private WindowAdapter consoleEcho = new WindowAdapter() {
        public void windowOpened(WindowEvent e) {
        inputField.requestFocus();
        specialDayNotifier = new SpecialDay(parentPane);
        }
    };

    private void backgroundProcessChecker() {
        try {
            new Thread(() -> {
                try {
                    //todo don't count threads, just look at names so ignore: Monitor Ctrl-Break, Image Fetcher 0,
                    // AWT-EventQueue-0, DestroyJavaVM, and then the thread checker that you will
                    // make that combines all system checking threads
                    while (true) {
                        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
                        int noThreads = currentGroup.activeCount();

                        if (noThreads > 6 && consoleFrame != null) {
                            consoleFrame.setIconImage(mainGeneralUtil.getCyderIconBlink().getImage());

                            Thread.sleep(5000);
                        }

                        else {
                            if (consoleFrame != null) {
                                consoleFrame.setIconImage(mainGeneralUtil.getCyderIcon().getImage());
                                Thread.sleep(5000);
                            }
                        }
                    }
                }

                catch (Exception e) {
                    mainGeneralUtil.handle(e);
                }
            },"background-process-checker").start();
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private Action inputFieldAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
        try {
            String originalOp = inputField.getText().trim();
            String op = originalOp;

            if (!mainGeneralUtil.empytStr(op)) {
                if (!(operationList.size() > 0 && operationList.get(operationList.size() - 1).equals(op))) {
                    operationList.add(op);
                }

                scrollingIndex = operationList.size() - 1;
                mainGeneralUtil.setCurrentDowns(0);

                if (!mainGeneralUtil.getUserInputMode()) {
                    handle(op);
                }

                else if (mainGeneralUtil.getUserInputMode()) {
                    mainGeneralUtil.setUserInputMode(false);
                    handleSecond(op);
                }
            }

            inputField.setText("");
        }

        catch (Exception ex) {
            mainGeneralUtil.handle(ex);
        }
        }
    };

    private void login(boolean AlreadyOpen) {
        if (loginFrame != null) {
            mainGeneralUtil.closeAnimation(loginFrame);
        }

        mainGeneralUtil.cleanUpUsers();

        //todo make cyderframe
        loginFrame = new JFrame();
        loginFrame.setUndecorated(true);

        if (!AlreadyOpen) {
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        else if (AlreadyOpen) {
            loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }

        loginFrame.setBounds(0, 0, 440, 520);
        loginFrame.setTitle("Cyder login");
        loginFrame.setIconImage(mainGeneralUtil.getCyderIcon().getImage());

        loginLabel = new JLabel();
        loginLabel.setVerticalTextPosition(SwingConstants.TOP);
        loginLabel.setVerticalAlignment(SwingConstants.TOP);
        loginLabel.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\login.png"));
        loginLabel.setBounds(0, 0, 440, 520);
        loginLabel.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));

        loginFrame.setContentPane(loginLabel);

        loginLabel2 = new JLabel();
        loginLabel2.setVerticalTextPosition(SwingConstants.TOP);
        loginLabel2.setVerticalAlignment(SwingConstants.TOP);
        loginLabel2.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Login2.png"));
        loginLabel2.setBounds(440,0 , 440, 520);

        loginLabel.add(loginLabel2);

        loginLabel3 = new JLabel();
        loginLabel3.setVerticalTextPosition(SwingConstants.TOP);
        loginLabel3.setVerticalAlignment(SwingConstants.TOP);
        loginLabel3.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Login3.png"));
        loginLabel3.setBounds(880,0 , 440, 520);

        loginLabel.add(loginLabel3);

        loginAnimation();

        DragLabel LoginDragLabel = new DragLabel(440,30,loginFrame);
        JLabel buildLabel = new JLabel("Build " + mainGeneralUtil.getCyderVer());
        buildLabel.setForeground(mainGeneralUtil.vanila);
        buildLabel.setFont(mainGeneralUtil.weatherFontSmall.deriveFont(20f));
        buildLabel.setBounds(LoginDragLabel.getWidth() / 2 - (buildLabel.getText().length() * 11)/2,
                2,(buildLabel.getText().length() * 17), 25);
        LoginDragLabel.add(buildLabel);
        loginLabel.add(LoginDragLabel);

        nameField = new JTextField(20);
        nameField.setToolTipText("Username");
        nameField.setBounds(64,279,327,41);
        nameField.setBackground(new Color(0,0,0,0));
        nameField.setSelectionColor(mainGeneralUtil.selectionColor);
        nameField.setBorder(null);
        nameField.setFont(mainGeneralUtil.weatherFontSmall.deriveFont(30f));
        nameField.setForeground(new Color(42,52,61));
        nameField.setCaretColor(mainGeneralUtil.navy);
        nameField.addActionListener(e -> nameField.requestFocusInWindow());
        nameField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (nameField.getText().length() == 1) {
                    nameField.setText(nameField.getText().toUpperCase());
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (nameField.getText().length() == 1) {
                    nameField.setText(nameField.getText().toUpperCase());
                }
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (nameField.getText().length() == 1) {
                    nameField.setText(nameField.getText().toUpperCase());
                }
            }
        });

        nameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
            if (nameField.getText().length() > 15) {
                evt.consume();
            }

            if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
                pass.requestFocus();
            }
            }
        });

        nameField.setBorder(BorderFactory.createEmptyBorder());
        nameField.setOpaque(false);

        loginLabel.add(nameField);

        pass = new JPasswordField();
        pass.setToolTipText("Password");
        pass.setBounds(64,348,327,41);
        pass.setBackground(new Color(0,0,0,0));
        pass.setSelectionColor(mainGeneralUtil.selectionColor);
        pass.setBorder(null);
        pass.setFont(mainGeneralUtil.weatherFontBig.deriveFont(50f));
        pass.setForeground(new Color(42,52,61));
        pass.setCaretColor(mainGeneralUtil.navy);
        pass.addActionListener(e -> {
            String Username = nameField.getText().trim();

            if (!mainGeneralUtil.empytStr(Username)) {
                Username = Username.substring(0, 1).toUpperCase() + Username.substring(1);

                char[] Password = pass.getPassword();

                if (!mainGeneralUtil.empytStr(Username)) {
                    recognize(Username, Password);
                }

                for (char c : Password) {
                    c = '\0';
                }
            }
        });

        pass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
            if (pass.getPassword().length > 30) {
                evt.consume();
            }
            }
        });

        pass.setBorder(BorderFactory.createEmptyBorder());
        pass.setOpaque(false);

        loginLabel.add(pass);

        newUserLabel = new JLabel("Don't have an account?", SwingConstants.CENTER);
        newUserLabel.setFont(new Font("tahoma",Font.BOLD,18));
        newUserLabel.setForeground(mainGeneralUtil.vanila);
        newUserLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                createUser();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                newUserLabel.setText("Create an account!");
                newUserLabel.setForeground(mainGeneralUtil.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                newUserLabel.setText("Don't have an account?");
                newUserLabel.setForeground(mainGeneralUtil.vanila);
            }
        });

        newUserLabel.setBounds(89,425,262,33);

        loginLabel.add(newUserLabel);

        loginFrame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                nameField.requestFocus();
            }
        });

        File Users = new File("src\\com\\cyder\\users\\");
        String[] directories = Users.list((current, name) -> new File(current, name).isDirectory());

        mainGeneralUtil.startAnimation(loginFrame);

        if (directories != null && directories.length == 0)
            notify("Psssst! Create a user, " + System.getProperty("user.name"),
                2000, Notification.RIGHT_ARROW, Notification.RIGHT_VANISH, loginLabel, 230);
    }

    private void recognize(String Username, char[] Password) {
        try {
            mainGeneralUtil.setUsername(Username);

            if (mainGeneralUtil.checkPassword(Username, mainGeneralUtil.toHexString(mainGeneralUtil.getSHA(Password)))) {
                mainGeneralUtil.readUserData();
                mainGeneralUtil.closeAnimation(loginFrame);

                if (consoleFrame != null)
                    mainGeneralUtil.closeAnimation(consoleFrame);

                console();

                if (mainGeneralUtil.getUserData("IntroMusic").equals("1")) {
                    LinkedList<String> MusicList = new LinkedList<>();

                    File UserMusicDir = new File("src\\com\\cyder\\users\\" + mainGeneralUtil.getUserUUID() + "\\Music");

                    String[] FileNames = UserMusicDir.list();

                    if (FileNames != null)
                        for (String fileName : FileNames)
                            if (fileName.endsWith(".mp3"))
                                MusicList.add(fileName);

                    if (!MusicList.isEmpty())
                        mainGeneralUtil.playMusic(
                                "src\\com\\cyder\\users\\" + mainGeneralUtil.getUserUUID() + "\\Music\\" +
                                        (FileNames[mainGeneralUtil.randInt(0,FileNames.length - 1)]));
                    else
                        mainGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\Suprise.mp3");
                }
            }

            else if (loginFrame.isVisible()){
                nameField.setText("");
                pass.setText("");
                nameField.requestFocusInWindow();
                notify("Could not recognize user",
                        2000, Notification.TOP_ARROW, Notification.TOP_VANISH, loginLabel, 280);
            }

            else {
                login(false);
            }
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void exitFullscreen() {
        mainGeneralUtil.initBackgrounds();
        File[] backgrounds = mainGeneralUtil.getValidBackgroundPaths();
        int index = mainGeneralUtil.getCurrentBackgroundIndex();
        String backFile = backgrounds[index].toString();

        int width = 0;
        int height = 0;

        if (mainGeneralUtil.getConsoleDirection() == mainGeneralUtil.CYDER_UP) {
            ImageIcon backIcon = new ImageIcon(backFile);
            width = backIcon.getIconWidth();
            height = backIcon.getIconHeight();
            parentLabel.setIcon(backIcon);
        }

        else if (mainGeneralUtil.getConsoleDirection() == mainGeneralUtil.CYDER_DOWN) {
            ImageIcon backIcon = new ImageIcon(backFile);
            width = backIcon.getIconWidth();
            height = backIcon.getIconHeight();
            parentLabel.setIcon(new ImageIcon(mainGeneralUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().toString())));
        }

        else {
            ImageIcon backIcon = new ImageIcon(backFile);

            if (mainGeneralUtil.getConsoleDirection() == mainGeneralUtil.CYDER_LEFT || mainGeneralUtil.getConsoleDirection() == mainGeneralUtil.CYDER_RIGHT) {
                height = backIcon.getIconWidth();
                width = backIcon.getIconHeight();
            }

            parentLabel.setIcon(new ImageIcon(mainGeneralUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().toString())));
        }

        mainGeneralUtil.getBackgroundSize();

        consoleFrame.setBounds(0, 0, width, height);
        parentPane.setBounds(0, 0,  width, height);
        parentLabel.setBounds(0, 0,  width, height);

        outputArea.setBounds(0, 0, width - 20, height - 204);
        outputScroll.setBounds(10, 62, width - 20, height - 204);
        inputField.setBounds(10, 82 + outputArea.getHeight(), width - 20, height - (outputArea.getHeight() + 62 + 40));
        consoleDragLabel.setBounds(0,0,width,30);
        minimize.setBounds(width - 81, 4, 22, 20);
        alternateBackground.setBounds(width - 54, 4, 22, 20);
        close.setBounds(width - 27, 4, 22, 20);
        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2,
                2,(consoleClockLabel.getText().length() * 17), 25);

        consoleFrame.repaint();
        consoleFrame.setVisible(true);
        consoleFrame.requestFocus();
        inputField.requestFocus();

        consoleFrame.setLocationRelativeTo(null);
    }

    private void refreshFullscreen() {
        mainGeneralUtil.initBackgrounds();
        File[] backgrounds = mainGeneralUtil.getValidBackgroundPaths();
        int index = mainGeneralUtil.getCurrentBackgroundIndex();
        String backFile = backgrounds[index].toString();

        ImageIcon backIcon = new ImageIcon(backFile);

        BufferedImage fullimg = mainGeneralUtil.resizeImage((int) mainGeneralUtil.getScreenSize().getWidth(),
                (int) mainGeneralUtil.getScreenSize().getHeight(), new File(backFile));
        int fullW = fullimg.getWidth();
        int fullH = fullimg.getHeight();

        parentLabel.setIcon(new ImageIcon(fullimg));

        mainGeneralUtil.getBackgroundSize();

        consoleFrame.setBounds(0, 0, fullW, fullH);
        parentPane.setBounds(0, 0,  fullW, fullH);
        parentLabel.setBounds(0, 0,  fullW, fullH);

        outputArea.setBounds(0, 0, fullW - 20, fullH - 204);
        outputScroll.setBounds(10, 62, fullW - 20, fullH - 204);
        inputField.setBounds(10, 82 + outputArea.getHeight(), fullW - 20, fullH - (outputArea.getHeight() + 62 + 40));
        consoleDragLabel.setBounds(0,0,fullW,30);
        minimize.setBounds(fullW - 81, 4, 22, 20);
        alternateBackground.setBounds(fullW - 54, 4, 22, 20);
        close.setBounds(fullW - 27, 4, 22, 20);
        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2,
                2,(consoleClockLabel.getText().length() * 17), 25);

        consoleFrame.repaint();
        consoleFrame.setVisible(true);
        consoleFrame.requestFocus();
        inputField.requestFocus();

        consoleFrame.setLocationRelativeTo(null);
    }

    private void switchBackground() {
        Thread slideThread = new Thread(() -> {
            try {
                mainGeneralUtil.initBackgrounds();

                File[] backgrounds = mainGeneralUtil.getValidBackgroundPaths();
                int oldIndex = (mainGeneralUtil.getCurrentBackgroundIndex() == 0 ? backgrounds.length - 1 : mainGeneralUtil.getCurrentBackgroundIndex() - 1);
                String oldBackFile = backgrounds[oldIndex].toString();
                String newBackFile = mainGeneralUtil.getCurrentBackground().toString();

                ImageIcon oldBack = new ImageIcon(oldBackFile);
                BufferedImage newBack = ImageIO.read(new File(newBackFile));

                BufferedImage temporaryImage;
                int tempW = 0;
                int tempH = 0;
                
                if (mainGeneralUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                    oldBack = new ImageIcon(mainGeneralUtil.resizeImage((int) mainGeneralUtil.getScreenSize().getWidth(),
                            (int) mainGeneralUtil.getScreenSize().getHeight(),new File(oldBackFile)));
                    newBack = mainGeneralUtil.resizeImage((int) mainGeneralUtil.getScreenSize().getWidth(), (int) mainGeneralUtil.getScreenSize().getHeight(),
                            new File(newBackFile));
                    temporaryImage = mainGeneralUtil.resizeImage((int) mainGeneralUtil.getScreenSize().getWidth(), (int) mainGeneralUtil.getScreenSize().getHeight(),
                            new File(oldBackFile));
                    tempW = temporaryImage.getWidth();
                    tempH = temporaryImage.getHeight();
                }

                else {
                    newBack = mainGeneralUtil.resizeImage(newBack.getWidth(), newBack.getHeight(),new File(newBackFile));
                    temporaryImage = mainGeneralUtil.resizeImage(newBack.getWidth(), newBack.getHeight(), new File(oldBackFile));
                    tempW = temporaryImage.getWidth();
                    tempH = temporaryImage.getHeight();
                }

                mainGeneralUtil.getBackgroundSize();

                consoleFrame.setBounds(0, 0, tempW, tempH);
                parentPane.setBounds(0, 0,  tempW, tempH);
                parentLabel.setBounds(0, 0,  tempW, tempH);

                outputArea.setBounds(0, 0, tempW - 20, tempH - 204);
                outputScroll.setBounds(10, 62, tempW - 20, tempH - 204);
                inputField.setBounds(10, 82 + outputArea.getHeight(), tempW - 20, tempH - (outputArea.getHeight() + 62 + 40));
                consoleDragLabel.setBounds(0,0,tempW,30);
                minimize.setBounds(tempW - 81, 4, 22, 20);
                alternateBackground.setBounds(tempW - 54, 4, 22, 20);
                close.setBounds(tempW - 27, 4, 22, 20);

                consoleFrame.repaint();
                consoleFrame.setVisible(true);
                consoleFrame.requestFocus();
                inputField.requestFocus();

                consoleFrame.setLocationRelativeTo(null);

                if (slidLeft) {
                    temporaryLabel = new JLabel();
                    parentLabel.setIcon(new ImageIcon(newBack));
                    temporaryLabel.setIcon(new ImageIcon(temporaryImage));
                    parentPane.add(temporaryLabel);
                    parentLabel.setBounds(-tempW, 0, tempW, tempH);
                    temporaryLabel.setBounds(0, 0 ,tempW, tempH);

                    int[] parts = getDelayIncrement(tempW);

                    animation.jLabelXRight(0, tempW, parts[0], parts[1], temporaryLabel);
                    animation.jLabelXRight(-tempW, 0 ,parts[0], parts[1], parentLabel);
                }

                else {
                    temporaryLabel = new JLabel();
                    parentLabel.setIcon(new ImageIcon(newBack));
                    temporaryLabel.setIcon(new ImageIcon(temporaryImage));
                    parentPane.add(temporaryLabel);
                    parentLabel.setBounds(tempW, 0, tempW, tempH);
                    temporaryLabel.setBounds(0, 0 ,tempW, tempH);

                    int[] parts = getDelayIncrement(tempW);

                    animation.jLabelXLeft(0, -tempW, parts[0], parts[1], temporaryLabel);
                    animation.jLabelXLeft(tempW, 0 ,parts[0], parts[1], parentLabel);
                }

                slidLeft = !slidLeft;

                parentLabel.setToolTipText(mainGeneralUtil.getCurrentBackground().getName().replace(".png", ""));
                consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2,
                        2,(consoleClockLabel.getText().length() * 17), 25);
            }

            catch (Exception e) {
                mainGeneralUtil.handle(e);
            }
        });

        slideThread.start();
    }

    private int[] getDelayIncrement(int width) {
        try {
            LinkedList<Integer> divisibles = new LinkedList<>();

            for (int i = 1 ; i <= width / 2 ; i++) {
                if (width % i == 0)
                    divisibles.add(i);
            }

            int desired = 10;
            int distance = Math.abs(divisibles.get(0)- desired);
            int index = 0;

            for(int i = 1; i < divisibles.size(); i++){
                int curDist = Math.abs(divisibles.get(i) - desired);

                if(curDist < distance){
                    index = i;

                    distance = curDist;
                }
            }

            int inc = divisibles.get(index);
            return new int[] {1, inc};
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }

        return null;
    }

    private void loginAnimation() {
        Thread slideThread = new Thread() {
            int count;

            @Override
            public void run() {
                try {
                    while (true) {
                        long scrollDelay = 2000;
                        int miliDelay = 5;
                        int increment = 2;

                        switch (count) {
                            case 0:
                                loginLabel.setBounds(0,0,440,520);
                                loginLabel2.setBounds(440,0,440,520);
                                
                                Thread.sleep(scrollDelay);

                                animation.jLabelXLeft(440, 0 ,miliDelay, increment, loginLabel2);

                                Thread.sleep(scrollDelay);

                                count = 1;
                                break;
                            case 1:
                                Thread.sleep(scrollDelay);

                                loginLabel2.setBounds(0,0,440,520);
                                loginLabel3.setBounds(440,0,440,520);
                                animation.jLabelXLeft(0, -440, miliDelay, increment, loginLabel2);
                                animation.jLabelXLeft(440, 0 ,miliDelay, increment, loginLabel3);

                                Thread.sleep(scrollDelay);

                                count = 2;
                                break;
                            case 2:
                                Thread.sleep(scrollDelay);

                                loginLabel3.setBounds(0,0,440,520);
                                loginLabel2.setBounds(-440,0,440,520);
                                animation.jLabelXRight(0, 440, miliDelay, increment, loginLabel3);
                                animation.jLabelXRight(-440,0,miliDelay,increment, loginLabel2);

                                Thread.sleep(scrollDelay);

                                count = 1;
                                break;
                        }
                    }
                }

                catch (Exception e) {
                    mainGeneralUtil.handle(e);
                }
            }
        };

        slideThread.start();
    }

    private void clc() {
        outputArea.setText("");
        inputField.setText("");
    }

    private void handleSecond(String input) {
        try {
            String desc = mainGeneralUtil.getUserInputDesc();

            if (desc.equalsIgnoreCase("url") && !mainGeneralUtil.empytStr(input)) {
                URI URI = new URI(input);
                println("Attempting to connect...");
                mainGeneralUtil.internetConnect(URI);
            }

            else if (desc.equalsIgnoreCase("prime") && input != null && !input.equals("")) {
                int num = Integer.parseInt(input);

                if (num <= 0) {
                    println("The inger " + num + " is not a prime number because it is negative.");
                }

                else if (num == 1) {
                    println("The inger 1 is not a prime number by the definition of a prime number.");
                }

                else if (num == 2) {
                    println("The integer 2 is indeed a prime number.");
                }

                ArrayList<Integer> Numbers = new ArrayList<>();

                for (int i = 3 ; i < Math.ceil(Math.sqrt(num)) ; i += 2) {
                    if (num % i == 0) {
                        Numbers.add(i);
                    }
                }

                if (Numbers.isEmpty()) {
                    println("The integer " + num + " is indeed a prime number.");
                }

                else {
                    println("The integer " + num + " is not a prime number because it is divisible by " + Numbers);
                }
            }

            else if (desc.equalsIgnoreCase("google") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                mainGeneralUtil.internetConnect("https://www.google.com/search?q=" + input);
            }

            else if (desc.equalsIgnoreCase("youtube")&& input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                mainGeneralUtil.internetConnect("https://www.youtube.com/results?search_query=" + input);
            }

            else if (desc.equalsIgnoreCase("math") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                mainGeneralUtil.internetConnect("https://www.wolframalpha.com/input/?i=" + input);
            }

            else if (desc.equalsIgnoreCase("binary")) {
                if (input.matches("[0-9]+") && !mainGeneralUtil.empytStr(input)) {
                    String Print = mainGeneralUtil.toBinary(Integer.parseInt(input));
                    println(input + " converted to binary equals: " + Print);
                }

                else {
                    println("Your value must only contain numbers.");
                }
            }

            else if (desc.equalsIgnoreCase("wiki") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ","_");
                println("Attempting to connect...");
                mainGeneralUtil.internetConnect("https://en.wikipedia.org/wiki/" + input);
            }

            else if (desc.equalsIgnoreCase("disco") && input != null && !input.equals("")) {
                println("I hope you're not the only one at this party.");
                mainGeneralUtil.disco(Integer.parseInt(input));
            }

            else if (desc.equalsIgnoreCase("youtube word search") && input != null && !input.equals("")) {
                String browse = "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";
                browse = browse.replace("REPLACE", input).replace(" ", "+");
                mainGeneralUtil.internetConnect(browse);
            }

            else if (desc.equalsIgnoreCase("random youtube")) {
               try {
                    int threads = Integer.parseInt(input);

                    notify("The" + (threads > 1 ? " scripts have " : " script has ") + "started. At any point, type \"stop script\"",
                            4000, Notification.TOP_ARROW, Notification.TOP_VANISH, parentPane, (threads > 1 ? 620 : 610));

                    for (int i = 0 ; i < threads ; i++) {
                        YoutubeThread current = new YoutubeThread(outputArea);
                        youtubeThreads.add(current);
                    }
                }

                catch (NumberFormatException e) {
                    println("Invalid input for number of threads to start.");
                }

               catch (Exception e) {
                   mainGeneralUtil.handle(e);
               }
            }

            else if (desc.equalsIgnoreCase("anagram1")) {
                println("Enter your second word");
                anagram = input;
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("anagram2");
            }

            else if (desc.equalsIgnoreCase("anagram2")) {
                if (anagram.length() != input.length()) {
                    println("These words are not anagrams of each other.");
                }

                else if (anagram.equalsIgnoreCase(input)) {
                    println("These words are in fact anagrams of each other.");
                }

                else {
                    char[] W1C = anagram.toLowerCase().toCharArray();
                    char[] W2C = input.toLowerCase().toCharArray();
                    Arrays.sort(W1C);
                    Arrays.sort(W2C);

                    if (Arrays.equals(W1C, W2C)) {
                        println("These words are in fact anagrams of each other.");
                    }

                    else {
                        println("These words are not anagrams of each other.");
                    }
                }

                anagram = "";
            }

            else if (desc.equalsIgnoreCase("pixelate") && input != null && !input.equals("")) {
                println("Pixelating " + pixelateFile.getName() + " with a pixel block size of " + input + "...");
                mainGeneralUtil.pixelate(pixelateFile, Integer.parseInt(input));
            }

            else if (desc.equalsIgnoreCase("alphabetize")) {
                char[] Sorted = input.toCharArray();
                Arrays.sort(Sorted);
                println("\"" + input + "\" alphabetically organized is \"" + new String(Sorted) + "\".");
            }

            else if (desc.equalsIgnoreCase("suggestion")) {
                logToDo(input);
            }

            else if (desc.equalsIgnoreCase("addbackgrounds")) {
                if (mainGeneralUtil.confirmation(input)) {
                    editUser();
                    mainGeneralUtil.internetConnect("https://images.google.com/");
                }

                else
                    println("Okay nevermind then");
            }

            else if (desc.equalsIgnoreCase("logoff")) {
                if (mainGeneralUtil.confirmation(input)) {
                    String shutdownCmd = "shutdown -l";
                    Runtime.getRuntime().exec(shutdownCmd);
                }

                else
                    println("Okay nevermind then");
            }

            else if (desc.equalsIgnoreCase("deletebackground")) {
                List<?> ClickedSelectionListBackground = backgroundSelectionList.getSelectedValuesList();

                File ClickedSelectionPath = null;

                if (!ClickedSelectionListBackground.isEmpty() && !ClickedSelectionListBackground.get(0).toString().equalsIgnoreCase(mainGeneralUtil.getCurrentBackground().getName().replace(".png",""))) {
                    String ClickedSelection = ClickedSelectionListBackground.get(0).toString();

                    for (int i = 0; i < backgroundsNameList.size() ; i++) {
                        if (ClickedSelection.equals(backgroundsNameList.get(i))) {
                            ClickedSelectionPath = backgroundsList.get(i);
                            break;
                        }
                    }

                    if (ClickedSelectionPath != null) {
                        ClickedSelectionPath.delete();
                    }
                    initializeBackgroundsList();
                    backgroundListScroll.setViewportView(backgroundSelectionList);
                    backgroundListScroll.revalidate();

                    if (mainGeneralUtil.confirmation(input)) {
                        println("Background: " + ClickedSelectionPath.getName().replace(".png","") + " successfully deleted.");
                        mainGeneralUtil.initBackgrounds();
                    }

                    else {
                        println("Background: " + ClickedSelectionPath.getName().replace(".png","") + " was not deleted.");
                    }

                    File[] paths = mainGeneralUtil.getValidBackgroundPaths();
                    for (int i = 0 ; i < paths.length ; i++) {
                        if (paths[i].equals(mainGeneralUtil.getCurrentBackground())) {
                            mainGeneralUtil.setCurrentBackgroundIndex(i);
                            break;
                        }
                    }
                }

                else {
                    println("You can't delete your current background.");
                }
            }

            else if (desc.equalsIgnoreCase("deletemusic")) {
                List<?> ClickedSelectionListMusic = musicSelectionList.getSelectedValuesList();

                File ClickedSelectionPath = null;

                if (!ClickedSelectionListMusic.isEmpty()) {
                    String ClickedSelection = ClickedSelectionListMusic.get(0).toString();

                    for (int i = 0; i < musicNameList.size() ; i++) {
                        if (ClickedSelection.equals(musicNameList.get(i))) {
                            ClickedSelectionPath = musicList.get(i);

                            break;
                        }
                    }

                    ClickedSelectionPath.delete();
                    initializeMusicList();
                    musicListScroll.setViewportView(musicSelectionList);
                    musicListScroll.revalidate();

                    if (mainGeneralUtil.confirmation(input)) {
                        println("Music: " + ClickedSelectionPath.getName().replace(".png","") + " successfully deleted.");
                    }

                    else {
                        println("Music: " + ClickedSelectionPath.getName().replace(".png","") + " was not deleted.");
                    }
                }
            }

            else if (desc.equalsIgnoreCase("deleteuser")) {
                if (!mainGeneralUtil.confirmation(input)) {
                    println("User " + mainGeneralUtil.getUsername() + " was not removed.");
                    return;
                }

                mainGeneralUtil.closeAnimation(consoleFrame);
                mainGeneralUtil.deleteFolder(new File("src\\com\\cyder\\users\\" + mainGeneralUtil.getUserUUID()));

                String dep = mainGeneralUtil.getDeprecatedUUID();

                File renamed = new File("src\\com\\cyder\\users\\" + dep);
                while (renamed.exists()) {
                    dep = mainGeneralUtil.getDeprecatedUUID();
                    renamed = new File("src\\com\\cyder\\users\\" + dep);
                }

                File old = new File("src\\com\\cyder\\users\\" + mainGeneralUtil.getUserUUID());
                old.renameTo(renamed);

                login(false);
            }

            else if (desc.equalsIgnoreCase("pixelatebackground")) {
                BufferedImage img = ImageUtil.pixelate(ImageIO.read(mainGeneralUtil.getCurrentBackground().getAbsoluteFile()), Integer.parseInt(input));

                String searchName = mainGeneralUtil.getCurrentBackground().getName().replace(".png", "")
                        + "_Pixelated_Pixel_Size_" + Integer.parseInt(input) + ".png";

                File saveFile = new File("src\\com\\cyder\\users\\" + mainGeneralUtil.getUserUUID() +
                        "\\Backgrounds\\" + searchName);

                ImageIO.write(img, "png", saveFile);

                mainGeneralUtil.initBackgrounds();

                File[] backgrounds = mainGeneralUtil.getValidBackgroundPaths();

                for (int i = 0 ; i < backgrounds.length ; i++) {
                    if (backgrounds[i].getName().equals(searchName)) {
                        parentLabel.setIcon(new ImageIcon(backgrounds[i].toString()));
                        parentLabel.setToolTipText(backgrounds[i].getName().replace(".png",""));
                        mainGeneralUtil.setCurrentBackgroundIndex(i);
                    }
                }

                println("Background pixelated and saved as a separate background file.");

                exitFullscreen();
            }

            else if (desc.equalsIgnoreCase("test notify one")) {
                notificationTestString = input;
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("test notify two");
                println("Enter notify width in pixels");
            }

            else if (desc.equalsIgnoreCase("test notify two")) {
                notificaitonTestWidth = Integer.parseInt(input);
                notify(notificationTestString, 2000,
                        Notification.TOP_ARROW, Notification.TOP_VANISH, parentPane, notificaitonTestWidth);
            }
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void handle(String input) {
        try {
            operation = input;

            String firstWord = mainGeneralUtil.firstWord(operation);

            mainGeneralUtil.setHandledMath(false);

            handleMath(operation);

            if (mainGeneralUtil.filter(operation)) {
                println("Sorry, " + mainGeneralUtil.getUsername() + ", but that language is prohibited.");
                operation = "";
            }

            else if (mainGeneralUtil.isPalindrome(operation.replace(" ", "").toCharArray()) && operation.length() > 3){
                println("Nice palindrome.");
            }

            else if (((hasWord("quit") && !hasWord("db")) ||
                    (eic("leave") || (hasWord("stop") && !hasWord("music") && !hasWord("script") && !hasWord("scripts")) ||
                            hasWord("exit") || eic("close"))) && !has("dance"))
            {
                exit();
            }

            else if (hasWord("test") && hasWord("notify")) {
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("test notify one");
                println("Enter notify string");
            }

            else if (hasWord("consolidate") && (hasWord("windows") || hasWord("frames"))) {
                Frame[] frames = Frame.getFrames();

                int x = consoleFrame.getX();
                int y = consoleFrame.getY();

                for(Frame f: frames)
                   if (f.isVisible())
                       f.setLocation(x,y);
            }

            else if (hasWord("bletchy")) {
                stringUtil.setOutputArea(outputArea);
                stringUtil.bletchy(operation,false,50);
            }

            else if ((hasWord("flip") &&  hasWord("coin")) || (hasWord("heads") && hasWord("tails"))) {
                if (Math.random() <= 0.0001) {
                    println("You're not going to beleive this, but it landed on its side.");
                }

                else if (Math.random() <= 0.5) {
                    println("It's Heads!");
                }

                else {
                    println("It's Tails!");
                }
            }

            else if ((eic("hello") || has("whats up") || hasWord("hi"))
                    && (!hasWord("print") &&  !hasWord("bletchy") && !hasWord("echo") &&
                    !hasWord("youtube") && !hasWord("google") && !hasWord("wikipedia") &&
                    !hasWord("synonym") && !hasWord("define"))) {
                int choice = mainGeneralUtil.randInt(1,6);

                switch(choice) {
                    case 1:
                        println("Hello " + mainGeneralUtil.getUsername() + ".");
                        break;
                    case 2:
                        println("Hi " + mainGeneralUtil.getUsername() + "." );
                        break;
                    case 3:
                        println("What's up " + mainGeneralUtil.getUsername() + "?");
                        break;
                    case 4:
                        println("How are you doing, " + mainGeneralUtil.getUsername() + "?");
                        break;
                    case 5:
                        println("Greetings, human " + mainGeneralUtil.getUsername() + ".");
                        break;
                    case 6:
                        println("Hi, " + mainGeneralUtil.getUsername() + ", I'm Cyder.");
                        break;
                }
            }

            else if (hasWord("bye") || (hasWord("james") && hasWord("arthur"))) {
                println("Just say you won't let go.");
            }

            else if (hasWord("time") && hasWord("what")) {
                println(mainGeneralUtil.weatherTime());
            }

            else if (eic("die") || (hasWord("roll") && hasWord("die"))) {
                int Roll = ThreadLocalRandom.current().nextInt(1, 7);
                println("You rolled a " + Roll + ".");
            }

            else if (eic("lol")) {
                println("My memes are better.");
            }

            else if ((hasWord("thank") && hasWord("you")) || hasWord("thanks")) {
                println("You're welcome.");
            }

            else if (hasWord("you") && hasWord("cool")) {
                println("I know.");
            }

            else if (has("paint")) {
                String param = "C:\\Windows\\system32\\mspaint.exe";
                Runtime.getRuntime().exec(param);
            }

            else if (eic("pi")) {
                println(Math.PI);
            }

            else if (hasWord("euler") || eic("e")) {
                println("Leonhard Euler's number is " + Math.E);
            }

            else if (hasWord("scrub")) {
                stringUtil.setOutputArea(outputArea);
                stringUtil.bletchy("No you!",false,50);
            }

            else if (eic("break;")) {
                println("Thankfully I am over my infinite while loop days.");
            }

            else if (hasWord("url")) {
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("url");
                println("Enter your desired URL");
            }

            else if (hasWord("temperature") || eic("temp")) {
                TempConverter tc = new TempConverter();
            }

            else if (has("click me")) {
                mainGeneralUtil.clickMe();
            }

            else if ((hasWord("how") && hasWord("are") && hasWord("you")) && !hasWord("age") && !hasWord("old")) {
                println("I am feeling like a programmed response. Thank you for asking.");
            }

            else if (hasWord("how") && hasWord("day")) {
                println("I was having fun until you started asking me questions.");
            }

            else if (has("How old are you") || (hasWord("what") && hasWord("age"))) {
                stringUtil.setOutputArea(outputArea);
                stringUtil.bletchy("I am 2^8",false,50);
            }

            else if (((hasWord("who") || hasWord("what")) && has("you")) && hasWord("name")) {
                println("I am Cyder (Acronym pending :P)");
            }

            else if (hasWord("helpful") && hasWord("you")) {
                println("I will always do my best to serve you.");
            }

            else if (eic("k")) {
                println("Fun Fact: the letter 'K' comes from the Greek letter kappa, which was taken "
                        + "from the Semitic kap, the symbol for an open hand. It is this very hand which "
                        + "will be slapping you in the face for saying 'k' to me.");
            }


            else if (hasWord("phone") || hasWord("dialer") || hasWord(" call")) {
                Phone p = new Phone();
            }

            else if (hasWord("reset") && hasWord("mouse")) {
                mainGeneralUtil.resetMouse();
            }

            else if (eic("logoff")) {
               println("Are you sure you want to log off your computer?\nThis is not Cyder we are talking about (Enter yes/no)");
               mainGeneralUtil.setUserInputDesc("logoff");
               inputField.requestFocus();
               mainGeneralUtil.setUserInputMode(true);
            }

            else if (eic("clc") || eic("cls") || eic("clear") || (hasWord("clear") && hasWord("screen"))) {
                clc();
            }

            else if (eic("no")) {
                println("Yes");
            }

            else if (eic("nope")) {
                println("yep");
            }

            else if (eic("yes")) {
                println("no");
            }

            else if (eic("yep")) {
                println("nope");
            }

            else if (has("how can I help")) {
                println("That's my line :P");
            }

            else if (hasWord("siri") || hasWord("jarvis") || hasWord("alexa")) {
                println("Whata bunch of losers.");
            }

            else if ((hasWord("mississippi") && hasWord("state") && hasWord("university")) || eic("msu")) {
                printImage("src\\com\\cyder\\io\\pictures\\msu.png");
            }

            else if (hasWord("toystory")) {
                mainGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\TheClaw.mp3");
            }

            else if (has("stop") && has("music")) {
                mainGeneralUtil.stopMusic();
            }

            else if (hasWord("reset") && hasWord("clipboard")) {
                StringSelection selection = new StringSelection(null);
                java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                println("Clipboard has been reset.");
            }

            else if ((has("graphing") && has("calculator")) || has("desmos") || has("graphing")) {
                mainGeneralUtil.internetConnect("https://www.desmos.com/calculator");
            }

            else if (has("airHeads xtremes") || has("candy")) {
                mainGeneralUtil.internetConnect("http://airheads.com/candy#xtremes");
            }

            else if (hasWord("prime")) {
                println("Enter any positive integer and I will tell you if it's prime and what it's divisible by.");
                mainGeneralUtil.setUserInputDesc("prime");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
            }

            else if (hasWord("youtube") && (!has("word search") && !has("mode") && !has("random") && !has("thumbnail"))) {
                println("What would you like to search YouTube for?");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("youtube");
            }

            else if ((hasWord("google") && !has("mode") && !has("stupid"))) {
                println("What would you like to Google?");
                mainGeneralUtil.setUserInputDesc("google");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
            }

            else if (eic("404")) {
                mainGeneralUtil.internetConnect("http://google.com/=");
            }

            else if (hasWord("calculator") && !has("graphing")) {
                Calculator c = new Calculator();
            }

            else if (firstWord.equalsIgnoreCase("echo")) {
                String[] sentences = operation.split(" ");
                for (int i = 1; i<sentences.length;i++) {
                    print(sentences[i] + " ");
                }

                println("");
            }

            else if ((firstWord.equalsIgnoreCase("print") || firstWord.equalsIgnoreCase("println")) && !has("mode")) {
                String[] sentences = operation.split(" ");

                for (int i = 1 ; i < sentences.length ; i++) {
                    print(sentences[i] + " ");
                }

                println("");
            }

            else if (hasWord("triangle")) {
                mainGeneralUtil.internetConnect("https://www.triangle-calculator.com/");
            }

            else if (hasWord("why")) {
                println("Why not?");
            }

            else if (hasWord("why not")) {
                println("Why?");
            }

            else if (hasWord("groovy")) {
                println("Alright Scooby Doo.");
            }

            else if (eic("dbquit")) {
                println("Debug mode exited");
                mainGeneralUtil.setDebugMode(false);
            }

            else if (hasWord("luck")) {
                if (Math.random() * 100 <= 0.001) {
                    println("YOU WON!!");
                }

                else {
                    println("You are not lucky today.");
                }
            }

            else if (has("are you sure") || has("are you certain")) {
                if (Math.random() <= 0.5) {
                    println("No");
                }

                else {
                    println("Yes");
                }
            }

            else if (has("math") && !eic("mathsh")) {
                println("What math operation would you like to perform?");
                mainGeneralUtil.setUserInputDesc("math");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
            }

            else if (eic("nathan")) {
                printlnImage("src\\com\\cyder\\io\\pictures\\me.png");
            }

            else if (has("always on top mode")) {
                if (hasWord("true")) {
                    println("Always on top mode has been set to true.");
                    mainGeneralUtil.setAlwaysOnTopMode(true);
                    consoleFrame.setAlwaysOnTop(true);
                }

                else if (hasWord("false")) {
                    println("Always on top mode has been set to false.");
                    mainGeneralUtil.setAlwaysOnTopMode(false);
                    consoleFrame.setAlwaysOnTop(false);
                }

                else {
                    println("Please specify the boolean value of always on top mode.");
                }
            }

            else if ((eic("error") || eic("errors")) && !hasWord("throw")) {
                if (mainGeneralUtil.getDebugMode()) {
                    File WhereItIs = new File("src\\com\\cyder\\users\\" + mainGeneralUtil.getUserUUID() + "\\Throws\\");
                    Desktop.getDesktop().open(WhereItIs);
                }

                else {
                    println("There are no errors here.");
                }
            }

            else if (eic("help")) {
                stringUtil.help(outputArea);
            }

            else if (hasWord("light") && hasWord("saber")) {
                mainGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\Lightsaber.mp3");
            }

            else if (hasWord("xbox")) {
                mainGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\xbox.mp3");
            }

            else if (has("star") && has("trek")) {
                mainGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\StarTrek.mp3");
            }

            else if (eic("cmd") || (hasWord("command") && hasWord("prompt"))) {
                File WhereItIs = new File("c:\\Windows\\System32\\cmd.exe");
                Desktop.getDesktop().open(WhereItIs);
            }

            else if (hasWord("shakespeare")) {
                int rand = mainGeneralUtil.randInt(1,2);

                if (rand == 1) {
                    println("Glamis hath murdered sleep, and therefore Cawdor shall sleep no more, Macbeth shall sleep no more.");
                }

                else {
                    println("To be, or not to be, that is the question: Whether 'tis nobler in the mind to suffer the slings and arrows of "
                            + "outrageous fortune, or to take arms against a sea of troubles and by opposing end them.");
                }
            }

            else if (hasWord("windows")) {
                mainGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\windows.mp3");
            }

            else if (hasWord("binary")) {
                println("Enter a decimal number to be converted to binary.");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("binary");
            }

            else if (hasWord("pizza")) {
                Pizza p = new Pizza();
            }

            else if (hasWord("imposible")) {
                println("¿Lo es?");
            }

            else if (eic("look")) {
                println("L()()K ---->> !FREE STUFF! <<---- L()()K");
            }

            else if (eic("Cyder?")) {
                println("Yes?");
            }

            else if (firstWord.equalsIgnoreCase("define")) {
                String Define = operation.toLowerCase().replace("'", "").replace(" ", "+").replace("define", "");

                mainGeneralUtil.internetConnect("http://www.dictionary.com/browse/" + Define + "?s=t");
            }

            else if (hasWord("wikipedia")) {
                println("What would you like to look up on Wikipedia?");
                mainGeneralUtil.setUserInputDesc("wiki");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
            }

            else if (firstWord.equalsIgnoreCase("synonym")) {
                String Syn = operation.replace("synonym","");
                Syn = Syn.replace("'", "").replace(" ", "+");
                mainGeneralUtil.internetConnect("http://www.thesaurus.com//browse//" + Syn);
            }

            else if (hasWord("board")) {
                mainGeneralUtil.internetConnect("http://gameninja.com//games//fly-squirrel-fly.html");
            }

            else if (hasWord("open cd")) {
                mainGeneralUtil.openCD("D:\\");
            }

            else if (hasWord("close cd")) {
                mainGeneralUtil.closeCD("D:\\");
            }

            else if (hasWord("font") && hasWord("reset")) {
                inputField.setFont(mainGeneralUtil.defaultFont);
                outputArea.setFont(mainGeneralUtil.defaultFont);
                println("The font has been reset.");
            }

            else if (hasWord("reset") && hasWord("color")) {
                outputArea.setForeground(mainGeneralUtil.vanila);
                inputField.setForeground(mainGeneralUtil.vanila);
                println("The text color has been reset.");
            }

            else if (eic("top left")) {
                consoleFrame.setLocation(0,0);
            }

            else if (eic("top right")) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
                Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
                int X = (int) rect.getMaxX() - consoleFrame.getWidth();
                consoleFrame.setLocation(X, 0);
            }

            else if (eic("bottom left")) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
                Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
                int Y = (int) rect.getMaxY() - consoleFrame.getHeight();
                consoleFrame.setLocation(0, Y);
            }

            else if (eic("bottom right")) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
                Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
                int X = (int) rect.getMaxX();
                int Y = (int) rect.getMaxY();
                consoleFrame.setLocation(X - consoleFrame.getWidth(),Y - consoleFrame.getHeight());
            }

            else if (eic("middle") || eic("center")) {
                consoleFrame.setLocationRelativeTo(null);
            }

            else if (hasWord("random") && hasWord("youtube")) {
                println("How many isntances of the script do you want to start?");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("random youtube");
            }

            else if (hasWord("arduino")) {
                mainGeneralUtil.internetConnect("https://www.arduino.cc/");
            }

            else if (has("rasberry pi")) {
                mainGeneralUtil.internetConnect("https://www.raspberrypi.org/");
            }

            else if (eic("&&")) {
                println("||");
            }

            else if (eic("||")) {
                println("&&");
            }

            else if (eic("youtube word search")) {
                println("Enter the desired word you would like to find in a YouTube URL");
                mainGeneralUtil.setUserInputDesc("youtube word search");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
            }

            else if (hasWord("disco")) {
                println("How many iterations would you like to disco for? (Enter a positive integer)");
                mainGeneralUtil.setUserInputMode(true);
                inputField.requestFocus();
                mainGeneralUtil.setUserInputDesc("disco");
            }

            else if (hasWord("game")) {
                File WhereItIs = new File("src\\com\\cyder\\io\\jars\\Jailbreak.jar");
                Desktop.getDesktop().open(WhereItIs);
            }

            else if (hasWord("there") && hasWord("no") && hasWord("internet")) {
                println("Sucks to be you.");
            }

            else if (eic("i hate you")) {
                println("That's not very nice.");
            }

            else if (eic("netsh")) {
                File WhereItIs = new File("C:\\Windows\\system32\\netsh.exe");

                Desktop.getDesktop().open(WhereItIs);
            }

            else if (hasWord("java") && hasWord("properties")) {
                mainGeneralUtil.javaProperties();
            }

            else if ((hasWord("edit") && hasWord ("user")) || (hasWord("font") && !hasWord("reset")) || (hasWord("color") && !hasWord("reset")) || (eic("preferences") || eic("prefs"))) {
                editUser();
            }

            else if (hasWord("story") && hasWord("tell")) {
                println("It was a lazy day. Cyder was enjoying a deep sleep when suddenly " + mainGeneralUtil.getUsername() + " started talking to Cyder."
                        + " It was at this moment that Cyder knew its day had been ruined.");
            }

            else if (eic("hey")) {
                mainGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\heyya.mp3");
            }

            else if (eic("panic")) {
                exit();
            }

            else if (hasWord("hash") || hasWord("hashser")) {
                new Hasher();
            }

            else if (hasWord("home")) {
                println("There's no place like localhost/127.0.0.1");
            }

            else if (eic("search") || eic("dir") || (hasWord("file") && hasWord("search")) || eic("directory") || eic("ls")) {
                DirectorySearch ds = new DirectorySearch();
            }

            else if (hasWord("I") && hasWord("love")) {
                println("Sorry, " + mainGeneralUtil.getUsername() + ", but I don't understand human emotions or affections.");
            }

            else if (hasWord("vexento")) {
                mainGeneralUtil.internetConnect("https://www.youtube.com/user/Vexento/videos");
            }

            else if (hasWord("minecraft")) {
                mw = new MinecraftWidget();
            }

            else if (eic("loop")) {
                println("mainGeneralUtil.handle(\"loop\");");
            }

            else if (hasWord("cyder") && has("dir")) {
                if (mainGeneralUtil.getDebugMode()) {
                    String CurrentDir = System.getProperty("user.dir");
                    mainGeneralUtil.openFile(CurrentDir);
                }

                else {
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but you don't have permission to do that.");
                }
            }

            else if ((has("tic") && has("tac") && has("toe")) || eic("TTT")) {
                TicTacToe ttt = new TicTacToe();
                ttt.startTicTacToe();
            }

            else if (hasWord("note") || hasWord("notes")) {
                userNotes = new Notes(mainGeneralUtil.getUserUUID());
            }

            else if ((hasWord("youtube") && hasWord("thumbnail")) || (hasWord("yt") && hasWord("thumb"))) {
                YouTubeThumbnail yttn = new YouTubeThumbnail();
            }

            else if (hasWord("papers") && hasWord("please")) {
                mainGeneralUtil.internetConnect("http://papersplea.se/");
            }

            else if (eic("java")) {
                println("public class main {");
                println("      public static void main(String[] args) {");
                println("            System.out.println(\"Hello World!\");");
                println("      }");
                println("}");
            }

            else if (hasWord("coffee")) {
                mainGeneralUtil.internetConnect("https://www.google.com/search?q=coffe+shops+near+me");
            }

            else if (hasWord("ip")) {
                println(InetAddress.getLocalHost().getHostAddress());
            }

            else if(hasWord("html") || hasWord("html5")) {
                consoleFrame.setIconImage(new ImageIcon("src\\com\\cyder\\io\\pictures\\html5.png").getImage());
                printlnImage("src\\com\\cyder\\io\\pictures\\html5.png");
            }

            else if (hasWord("css")) {
                consoleFrame.setIconImage(new ImageIcon("src\\com\\cyder\\io\\pictures\\css.png").getImage());
                printlnImage("src\\com\\cyder\\io\\pictures\\css.png");
            }

            else if(hasWord("computer") && hasWord("properties")) {
                println("This may take a second, stand by...");
                mainGeneralUtil.computerProperties();
            }

            else if (hasWord("system") && hasWord("properties")) {
                mainGeneralUtil.systemProperties();
            }

            else if ((hasWord("pixelate") || hasWord("distort")) && (hasWord("image") || hasWord("picture"))) {
                pixelateFile = mainGeneralUtil.getFile();

                if (!pixelateFile.getName().endsWith(".png")) {
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but this feature only supports PNG images");
                }

                else if (pixelateFile != null) {
                    println("Enter your pixel size (Enter a positive integer)");
                    mainGeneralUtil.setUserInputDesc("pixelate");
                    inputField.requestFocus();
                    mainGeneralUtil.setUserInputMode(true);
                }
            }

            else if (hasWord("donuts")) {
                mainGeneralUtil.internetConnect("https://www.dunkindonuts.com/en/food-drinks/donuts/donuts");
            }

            else if (hasWord("anagram")) {
                println("This function will tell you if two"
                        + "words are anagrams of each other."
                        + " Enter your first word");
                mainGeneralUtil.setUserInputDesc("anagram1");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);

            }

            else if (eic("controlc")) {
                mainGeneralUtil.setUserInputMode(false);
                killAllYoutube();
                stringUtil.killBletchy();
                println("Escaped");
            }

            else if (has("alphabet") && (hasWord("sort") || hasWord("organize") || hasWord("arrange"))) {
                println("Enter your word to be alphabetically rearranged");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("alphabetize");
            }

            else if (hasWord("mp3") || hasWord("music")) {
                mainGeneralUtil.mp3("", mainGeneralUtil.getUsername(), mainGeneralUtil.getUserUUID());
            }

            else if (hasWord("bai")) {
                mainGeneralUtil.internetConnect("http://www.drinkbai.com");
            }

            else if (has("occam") && hasWord("razor")) {
                mainGeneralUtil.internetConnect("http://en.wikipedia.org/wiki/Occam%27s_razor");
            }

            else if (hasWord("cyder") && (hasWord("picture") ||hasWord("image"))) {
                if (mainGeneralUtil.getDebugMode()) {
                    mainGeneralUtil.openFile("src\\com\\cyder\\io\\pictures");
                }

                else {
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but you do not have permission to access that.");
                }
            }

            else if (hasWord("when") && hasWord("thanksgiving")) {
                int year = Calendar.getInstance().get(Calendar.YEAR);
                LocalDate RealTG = LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
                println("Thanksgiving this year is on the " + RealTG.getDayOfMonth() + " of November.");
            }

            else if (hasWord("location") || (hasWord("where") && hasWord("am") && hasWord("i"))) {
                println("You are currently in " + new InternetProtocolUtil().getUserCity() + ", " +
                        new InternetProtocolUtil().getUserState() + " and your Internet Service Provider is " + new InternetProtocolUtil().getIsp());
            }

            else if (hasWord("fibonacci")) {
                fib(0,1);
            }

            else if (hasWord("throw") && hasWord("error")) {
                throw new CyderException("Error thrown on " + mainGeneralUtil.userTime());
            }

            else if (hasWord("asdf")) {
                println("Who is the spiciest meme lord?");
            }

            else if (hasWord("qwerty")) {
                println("I prefer Dvorak, but I also like Colemak, Maltron, and JCUKEN.");
            }

            else if (hasWord("thor")) {
                println("Piss off, ghost.");
            }

            else if (eic("about:blank")) {
                mainGeneralUtil.internetConnect("about:blank");
            }

            else if (hasWord("weather")) {
                WeatherWidget ww = new WeatherWidget();
            }

            else if (eic("hide")) {
                minimize.doClick();
            }

            else if (hasWord("stop") && hasWord("script")) {
                println("YouTube scripts have been killed.");
                killAllYoutube();
                consoleFrame.setTitle(mainGeneralUtil.getCyderVer() + " [" + mainGeneralUtil.getUsername() + "]");
            }

            else if (hasWord("debug") && hasWord("menu")) {
                if (mainGeneralUtil.getDebugMode()) {
                    mainGeneralUtil.debugMenu(outputArea);
                }

                else {
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but you do not have permission to use that feature.");
                }
            }

            else if (hasWord("hangman")) {
                Hangman Hanger = new Hangman();
                Hanger.startHangman();
            }

            else if (hasWord("rgb") || hasWord("hex")) {
                mainGeneralUtil.colorConverter();
            }

            else if (hasWord("dance")) {
                mainGeneralUtil.dance(consoleFrame);
            }

            else if (hasWord("clear") && (
                    hasWord("operation") || hasWord("command")) &&
                    hasWord("list")) {
                operationList.clear();
                scrollingIndex = 0;
                println("The operation list has been cleared.");
            }

            else if (eic("pin") || eic("login")) {
                login(true);
            }

            else if ((hasWord("delete") ||
                    hasWord("remove")) &&
                    (hasWord("user") ||
                            hasWord("account"))) {

                println("Are you sure you want to permanently delete this account? This action cannot be undone! (yes/no)");
                mainGeneralUtil.setUserInputMode(true);
                inputField.requestFocus();
                mainGeneralUtil.setUserInputDesc("deleteuser");
            }

            else if ((hasWord("create") || hasWord("new")) &&
                    hasWord("user")) {
                createUser();
            }

            else if (hasWord("pixelate") && hasWord("background")) {
                println("Enter your pixel size (a positive integer)");
                mainGeneralUtil.setUserInputDesc("pixelatebackground");
                mainGeneralUtil.setUserInputMode(true);
                inputField.requestFocus();
            }

            else if (hasWord("long") && hasWord("word")) {
                int count = 0;

                String[] words = operation.split(" ");

                for (String word: words)
                    if (word.equalsIgnoreCase("long"))
                        count++;

                for (int i = 0 ; i < count ; i++)
                    print("pneumonoultramicroscopicsilicovolcanoconiosis");

                println("");
            }

            else if (eic("logic")) {
                mainGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\commando.mp3");
            }

            else if (eic("1-800-273-8255") || eic("18002738255")) {
                mainGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\1800.mp3");
            }

            else if (hasWord("resize") && (hasWord("image") || hasWord("picture"))) {
                ImageResizer IR = new ImageResizer();
            }

            else if (hasWord("barrel") && hasWord("roll")) {
                barrelRoll();
            }

            else if (hasWord("lines") && hasWord("code")) {
                println("Total lines of code: " + mainGeneralUtil.totalCodeLines(new File(System.getProperty("user.dir"))));
            }

            else if (hasWord("threads")) {
                ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                int num = threadGroup.activeCount();
                Thread[] printThreads = new Thread[num];
                threadGroup.enumerate(printThreads);

                for (int i = 0; i < num ; i++)
                    println(printThreads[i].getName());
            }

            else if (eic("askew")) {
                askew();
            }

            else if (hasWord("press") && (hasWord("F17") || hasWord("f17"))) {
                Robot rob = new Robot();
                rob.keyPress(KeyEvent.VK_F17);
            }

            else if (hasWord("logout")) {
                mainGeneralUtil.closeAnimation(consoleFrame);
                login(false);
            }

            else if (eic("test")) {
                new TestClass();
            }

            else if ((hasWord("wipe") || hasWord("clear") || hasWord("delete")) && has("error")) {
                if (mainGeneralUtil.getDebugMode()) {
                    mainGeneralUtil.wipeErrors();

                    println("Deleted all user erorrs");
                }

                else
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but you don't have permission to do that.");
            }

            else if (!mainGeneralUtil.getHandledMath()){
                println("Sorry, " + mainGeneralUtil.getUsername() + ", but I don't recognize that command." +
                        " You can make a suggestion by clicking the \"Suggest something\" button.");
            }
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void handleMath(String op) {
        int firstParen = op.indexOf("(");
        int comma = op.indexOf(",");
        int lastParen = op.indexOf(")");

        String mathop;
        double param1 = 0.0;
        double param2 = 0.0;

        try {
            if (firstParen != -1) {
                mathop = op.substring(0,firstParen);

                if (comma != -1) {
                    param1 = Double.parseDouble(op.substring(firstParen+1,comma));

                    if (lastParen != -1) {
                        param2 =  Double.parseDouble(op.substring(comma+1,lastParen));
                    }
                }

                else if (lastParen != -1) {
                    param1 =  Double.parseDouble(op.substring(firstParen+1,lastParen));
                }

                if (mathop.equalsIgnoreCase("abs")) {
                    println(Math.abs(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("ceil")) {
                    println(Math.ceil(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("floor")) {
                    println(Math.floor(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("log")) {
                    println(Math.log(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("log10")) {
                    println(Math.log10(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("max")) {
                    println(Math.max(param1,param2));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("min")) {
                    println(Math.min(param1,param2));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("pow")) {
                    println(Math.pow(param1,param2));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("round")) {
                    println(Math.round(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("sqrt")) {
                    println(Math.sqrt(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("convert2")) {
                    println(mainGeneralUtil.toBinary((int)(param1)));
                    mainGeneralUtil.setHandledMath(true);
                }
            }
        }

        catch(Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void printlnImage(String filename) {
        outputArea.insertIcon(new ImageIcon(filename));
        println("");
    }

    public static void printImage(String filename) {
        outputArea.insertIcon(new ImageIcon(filename));
    }

    private void print(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage, null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Integer.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Double.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Boolean.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Float.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Long.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), String.valueOf(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString(), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString() + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private boolean eic(String EIC) {
        return operation.equalsIgnoreCase(EIC);
    }

    private boolean has(String compare) {
        String ThisComp = compare.toLowerCase();
        String ThisOp = operation.toLowerCase();

        return ThisOp.contains(ThisComp);
    }

    private boolean hasWord(String compare) {
        String ThisComp = compare.toLowerCase();
        String ThisOp = operation.toLowerCase();

        if (ThisOp.equals(ThisComp) || ThisOp.contains(' ' + ThisComp + ' ') || ThisOp.contains(' ' + ThisComp))
            return true;

        else return ThisOp.contains(ThisComp + ' ');
    }

    //todo move to separate handler
    private void logToDo(String input) {
        try {
            if (input != null && !input.equals("") && !mainGeneralUtil.filter(input) && input.length() > 10 && !mainGeneralUtil.filter(input)) {
                BufferedWriter sugWriter = new BufferedWriter(new FileWriter("src\\com\\cyder\\io\\text\\add.txt", true));

                sugWriter.write("User " + mainGeneralUtil.getUsername() + " at " + mainGeneralUtil.weatherThreadTime() + " made the suggestion: ");
                sugWriter.write(System.getProperty("line.separator"));

                sugWriter.write(input);

                sugWriter.write(System.getProperty("line.separator"));
                sugWriter.write(System.getProperty("line.separator"));

                sugWriter.flush();
                sugWriter.close();

                println("Request registered.");
                sugWriter.close();
            }
        }

        catch (Exception ex) {
            mainGeneralUtil.handle(ex);
        }
    }

    public void fib(int a, int b) {
        try {
            int c = a + b;
            println(c);
            if (c < 2147483647/2)
                fib(b, c);
            else
                println("Integer limit reached");
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void changeUsername(String newName) {
        try {
            mainGeneralUtil.readUserData();
            mainGeneralUtil.writeUserData("name",newName);

            mainGeneralUtil.setUsername(newName);
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void changePassword(char[] newPassword) {
        try {
            mainGeneralUtil.readUserData();
            mainGeneralUtil.writeUserData("password", mainGeneralUtil.toHexString(mainGeneralUtil.getSHA(newPassword)));
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    //todo barrel roll and switching console dir doesn't work in full screen

    //todo make a cyderframe
    //todo consolidate music and backgrounds into same jlist
    //todo inform you can only add png and mp3s if they select something else
    //todo add background opacity slider if user wants background border and fill
    public void editUser() {
        if (editUserFrame != null)
            mainGeneralUtil.closeAnimation(editUserFrame);

        editUserFrame = new JFrame();
        editUserFrame.setResizable(false);
        editUserFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editUserFrame.setResizable(false);
        editUserFrame.setIconImage(mainGeneralUtil.getCyderIcon().getImage());
        editUserFrame.setTitle("Edit User");

        JPanel ParentPanel = new JPanel();
        ParentPanel.setLayout(new BoxLayout(ParentPanel, BoxLayout.Y_AXIS));
        ParentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel BackgroundLabel = new JLabel("Backgrounds", SwingConstants.CENTER);
        BackgroundLabel.setFont(mainGeneralUtil.weatherFontSmall);

        JPanel LabelPanel = new JPanel();
        LabelPanel.add(BackgroundLabel);

        initializeBackgroundsList();

        backgroundSelectionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        backgroundListScroll = new CyderScrollPane(backgroundSelectionList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        backgroundListScroll.setSize(400, 400);
        backgroundListScroll.setBackground(mainGeneralUtil.vanila);
        backgroundListScroll.setFont(mainGeneralUtil.weatherFontBig);
        backgroundListScroll.setThumbColor(mainGeneralUtil.regularRed);
        backgroundSelectionList.setBackground(new Color(0,0,0,0));
        backgroundListScroll.getViewport().setBackground(new Color(0,0,0,0));

        JPanel ButtonPanel = new JPanel();
        ButtonPanel.setLayout(new GridLayout(1, 3, 5, 5));
        ButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        CyderButton addBackground = new CyderButton("Add Background");
        addBackground.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        addBackground.setColors(mainGeneralUtil.regularRed);
        ButtonPanel.add(addBackground);
        addBackground.setFocusPainted(false);
        addBackground.setBackground(mainGeneralUtil.regularRed);
        addBackground.addActionListener(e -> {
            try {
                File AddBackground = mainGeneralUtil.getFile();

                if (AddBackground != null && AddBackground.getName().endsWith(".png")) {
                    File Destination = new File("src\\com\\cyder\\users\\" + mainGeneralUtil.getUserUUID() + "\\Backgrounds\\" + AddBackground.getName());
                    Files.copy(new File(AddBackground.getAbsolutePath()).toPath(), Destination.toPath());
                    initializeBackgroundsList();
                    backgroundListScroll.setViewportView(backgroundSelectionList);
                    backgroundListScroll.revalidate();
                }
            }

            catch (Exception exc) {
                mainGeneralUtil.handle(exc);
            }
        });
        addBackground.setFont(mainGeneralUtil.weatherFontSmall);

        openBackground = new CyderButton("Open Background");
        openBackground.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        openBackground.setColors(mainGeneralUtil.regularRed);
        ButtonPanel.add(openBackground);
        openBackground.setFocusPainted(false);
        openBackground.setBackground(mainGeneralUtil.regularRed);
        openBackground.setFont(mainGeneralUtil.weatherFontSmall);
        openBackground.addActionListener(e -> {
            List<?> ClickedSelectionList = backgroundSelectionList.getSelectedValuesList();

            if (!ClickedSelectionList.isEmpty()) {
                String ClickedSelection = ClickedSelectionList.get(0).toString();

                File ClickedSelectionPath = null;

                for (int i = 0; i < backgroundsNameList.size() ; i++) {
                    if (ClickedSelection.equals(backgroundsNameList.get(i))) {
                        ClickedSelectionPath = backgroundsList.get(i);
                        break;
                    }
                }

                if (ClickedSelectionPath != null) {
                    PhotoViewer pv = new PhotoViewer(ClickedSelectionPath);
                    pv.start();
                }
            }
        });

        CyderButton deleteBackground = new CyderButton("Delete Background");
        deleteBackground.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        deleteBackground.setColors(mainGeneralUtil.regularRed);
        ButtonPanel.add(deleteBackground);
        deleteBackground.addActionListener(e -> {
            if (mainGeneralUtil.getValidBackgroundPaths().length == 1) {
                println("Sorry, but that is your only background. Try adding a different one and then " +
                        "removing it if you still don't want " + mainGeneralUtil.getValidBackgroundPaths()[0].getName() + ".");
            }

            else if (!backgroundSelectionList.getSelectedValuesList().isEmpty()){
                println("You are about to delete a background file. This action cannot be undone."
                        + " Are you sure you wish to continue? (yes/no)");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("deletebackground");
                mainGeneralUtil.initBackgrounds();
            }
        });

        deleteBackground.setBackground(mainGeneralUtil.regularRed);
        deleteBackground.setFont(mainGeneralUtil.weatherFontSmall);

        JPanel BackgroundsPanel = new JPanel();
        BackgroundsPanel.setLayout(new BoxLayout(BackgroundsPanel, BoxLayout.Y_AXIS));
        BackgroundsPanel.add(LabelPanel);
        BackgroundsPanel.add(backgroundListScroll);
        BackgroundsPanel.add(ButtonPanel);
        BackgroundsPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(mainGeneralUtil.navy,5,false)));
        ParentPanel.add(BackgroundsPanel);

        JLabel MusicLabel = new JLabel("Music", SwingConstants.CENTER);
        MusicLabel.setFont(mainGeneralUtil.weatherFontSmall);

        JPanel MusicLabelPanel = new JPanel();
        MusicLabelPanel.add(MusicLabel);

        initializeMusicList();

        musicSelectionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        musicListScroll = new CyderScrollPane(musicSelectionList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        musicListScroll.setThumbColor(mainGeneralUtil.regularRed);
        musicListScroll.setSize(400, 400);
        musicListScroll.setBackground(mainGeneralUtil.vanila);
        musicListScroll.setFont(mainGeneralUtil.weatherFontSmall);
        musicSelectionList.setBackground(new Color(0,0,0,0));
        musicListScroll.getViewport().setBackground(new Color(0,0,0,0));

        JPanel BottomButtonPanel = new JPanel();
        BottomButtonPanel.setLayout(new GridLayout(1, 3, 5, 5));

        addMusic = new CyderButton("Add Music");
        addMusic.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        addMusic.setBackground(mainGeneralUtil.regularRed);
        addMusic.setColors(mainGeneralUtil.regularRed);
        addMusic.setFont(mainGeneralUtil.weatherFontSmall);
        BottomButtonPanel.add(addMusic);
        addMusic.addActionListener(e -> {
            try {
                File AddMusic = mainGeneralUtil.getFile();

                if (AddMusic != null && AddMusic.getName().endsWith(".mp3")) {
                    File Destination = new File("src\\com\\cyder\\users\\" + mainGeneralUtil.getUserUUID() + "\\Music\\" + AddMusic.getName());
                    Files.copy(new File(AddMusic.getAbsolutePath()).toPath(), Destination.toPath());
                    initializeMusicList();
                    musicListScroll.setViewportView(musicSelectionList);
                    musicListScroll.revalidate();
                }
            }

            catch (Exception exc) {
                mainGeneralUtil.handle(exc);
            }
        });

        openMusic = new CyderButton("Open Music");
        openMusic.setColors(mainGeneralUtil.regularRed);
        openMusic.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        openMusic.setBackground(mainGeneralUtil.regularRed);
        openMusic.setFont(mainGeneralUtil.weatherFontSmall);
        BottomButtonPanel.add(openMusic);
        openMusic.setFocusPainted(false);
        openMusic.setBackground(mainGeneralUtil.regularRed);
        openMusic.addActionListener(e -> {
            List<?> ClickedSelectionList = musicSelectionList.getSelectedValuesList();

            if (!ClickedSelectionList.isEmpty()) {
                String ClickedSelection = ClickedSelectionList.get(0).toString();

                File ClickedSelectionPath = null;

                for (int i = 0; i < musicNameList.size() ; i++) {
                    if (ClickedSelection.equals(musicNameList.get(i))) {
                        ClickedSelectionPath = musicList.get(i);

                        break;
                    }
                }

                mainGeneralUtil.mp3(ClickedSelectionPath.getAbsolutePath(), mainGeneralUtil.getUsername(), mainGeneralUtil.getUserUUID());
            }
        });

        CyderButton deleteMusic = new CyderButton("Delete Music");
        deleteMusic.setColors(mainGeneralUtil.regularRed);
        deleteMusic.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        deleteMusic.setBackground(mainGeneralUtil.regularRed);
        deleteMusic.setFont(mainGeneralUtil.weatherFontSmall);
        BottomButtonPanel.add(deleteMusic);
        deleteMusic.setFocusPainted(false);
        deleteMusic.setBackground(mainGeneralUtil.regularRed);
        deleteMusic.addActionListener(e -> {
            if (!musicSelectionList.getSelectedValuesList().isEmpty()) {
                println("You are about to delete a music file. This action cannot be undone."
                        + " Are you sure you wish to continue? (yes/no)");
                mainGeneralUtil.setUserInputDesc("deletemusic");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
            }
        });

        BottomButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel MusicPanel = new JPanel();
        MusicPanel.setLayout(new BoxLayout(MusicPanel, BoxLayout.Y_AXIS));
        MusicPanel.add(MusicLabelPanel);
        MusicPanel.add(musicListScroll);
        MusicPanel.add(BottomButtonPanel);
        MusicPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(mainGeneralUtil.navy,5,false)));

        ParentPanel.add(MusicPanel);

        JPanel ChangeUsernamePanel = new JPanel();
        ChangeUsernamePanel.setLayout(new GridLayout(2, 1, 5, 5));
        JTextField changeUsernameField = new JTextField(10);
        changeUsernameField.addActionListener(e -> changeUsername.doClick());
        changeUsernameField.setFont(mainGeneralUtil.weatherFontSmall);
        changeUsernameField.setSelectionColor(mainGeneralUtil.selectionColor);
        changeUsername = new CyderButton("Change Username");
        changeUsername.setBackground(mainGeneralUtil.regularRed);
        changeUsername.setColors(mainGeneralUtil.regularRed);
        changeUsername.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        changeUsername.setFont(mainGeneralUtil.weatherFontSmall);
        changeUsernameField.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        ChangeUsernamePanel.add(changeUsernameField);
        ChangeUsernamePanel.add(changeUsername);
        changeUsernameField.setToolTipText("New username");
        changeUsername.addActionListener(e -> {
            String newUsername = changeUsernameField.getText();
            if (!mainGeneralUtil.empytStr(newUsername)) {
                changeUsername(newUsername);
                mainGeneralUtil.inform("Username successfully changed","", 300, 200);
                mainGeneralUtil.refreshUsername(consoleFrame);
                changeUsernameField.setText("");
            }
        });


        JPanel ChangePasswordPanel = new JPanel();
        ChangePasswordPanel.setLayout(new GridLayout(2, 1, 5, 5));

        changeUsername.setBackground(mainGeneralUtil.regularRed);
        JPasswordField changePasswordField = new JPasswordField(10);
        changePasswordField.addActionListener(e -> changePassword.doClick());
        changePasswordField.setFont(mainGeneralUtil.weatherFontSmall);
        changePasswordField.setSelectionColor(mainGeneralUtil.selectionColor);
        changePassword = new CyderButton("Change Password");
        changePassword.setBackground(mainGeneralUtil.regularRed);
        changePassword.setColors(mainGeneralUtil.regularRed);
        changePassword.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        changePassword.setFont(mainGeneralUtil.weatherFontSmall);
        changePasswordField.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        ChangePasswordPanel.add(changePasswordField);
        ChangePasswordPanel.add(changePassword);
        changePasswordField.setToolTipText("New password");
        changePassword.addActionListener(e -> {
            char[] newPassword = changePasswordField.getPassword();

            if (newPassword.length > 4) {
                changePassword(newPassword);
                mainGeneralUtil.inform("Password successfully changed","", 300, 200);
                changePasswordField.setText("");
            }

            else {
                mainGeneralUtil.inform("Sorry, " + mainGeneralUtil.getUsername() + ", " +
                        "but your password must be greater than 4 characters for security reasons.","", 500, 300);
                changePasswordField.setText("");
            }

            for (char c : newPassword) {
                c = '\0';
            }
        });

        changePassword.setBackground(mainGeneralUtil.regularRed);
        ChangeUsernamePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        ParentPanel.add(ChangeUsernamePanel);

        ImageIcon selected = new ImageIcon("src\\com\\cyder\\io\\pictures\\checkbox1.png");
        ImageIcon notSelected = new ImageIcon("src\\com\\cyder\\io\\pictures\\checkbox2.png");

        JPanel prefsPanel = new JPanel();
        prefsPanel.setLayout(new GridLayout(6,3,0,20));

        JLabel introMusicTitle = new JLabel("Intro Music");
        introMusicTitle.setFont(mainGeneralUtil.weatherFontSmall);
        introMusicTitle.setForeground(mainGeneralUtil.navy);
        introMusicTitle.setHorizontalAlignment(JLabel.CENTER);
        prefsPanel.add(introMusicTitle);

        JLabel debugWindowsLabel = new JLabel("Debug Windows");
        debugWindowsLabel.setFont(mainGeneralUtil.weatherFontSmall);
        debugWindowsLabel.setForeground(mainGeneralUtil.navy);
        debugWindowsLabel.setHorizontalAlignment(JLabel.CENTER);
        prefsPanel.add(debugWindowsLabel);

        JLabel randomBackgroundLabel = new JLabel("Random Background");
        randomBackgroundLabel.setFont(mainGeneralUtil.weatherFontSmall);
        randomBackgroundLabel.setForeground(mainGeneralUtil.navy);
        randomBackgroundLabel.setHorizontalAlignment(JLabel.CENTER);
        prefsPanel.add(randomBackgroundLabel);

        JLabel introMusic = new JLabel();
        introMusic.setHorizontalAlignment(JLabel.CENTER);
        introMusic.setSize(100,100);
        introMusic.setIcon((mainGeneralUtil.getUserData("IntroMusic").equals("1") ? selected : notSelected));
        introMusic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("IntroMusic").equals("1");
            mainGeneralUtil.writeUserData("IntroMusic", (wasSelected ? "0" : "1"));
            introMusic.setIcon((wasSelected ? notSelected : selected));
            }
        });

        prefsPanel.add(introMusic);

        JLabel debugWindows = new JLabel();
        debugWindows.setHorizontalAlignment(JLabel.CENTER);
        debugWindows.setSize(100,100);
        debugWindows.setIcon((mainGeneralUtil.getUserData("DebugWindows").equals("1") ? selected : notSelected));
        debugWindows.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("DebugWindows").equals("1");
            mainGeneralUtil.writeUserData("DebugWindows", (wasSelected ? "0" : "1"));
            debugWindows.setIcon((wasSelected ? notSelected : selected));
            }
        });

        prefsPanel.add(debugWindows);

        JLabel randBackgroundLabel = new JLabel();
        randBackgroundLabel.setHorizontalAlignment(JLabel.CENTER);
        randBackgroundLabel.setSize(100,100);
        randBackgroundLabel.setIcon((mainGeneralUtil.getUserData("RandomBackground").equals("1") ? selected : notSelected));
        randBackgroundLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("RandomBackground").equals("1");
            mainGeneralUtil.writeUserData("RandomBackground", (wasSelected ? "0" : "1"));
            randBackgroundLabel.setIcon((wasSelected ? notSelected : selected));
            }
        });

        prefsPanel.add(randBackgroundLabel);

        JLabel hourlyChimesLabel = new JLabel("Hourly Chimes");

        hourlyChimesLabel.setFont(mainGeneralUtil.weatherFontSmall);

        hourlyChimesLabel.setForeground(mainGeneralUtil.navy);

        hourlyChimesLabel.setHorizontalAlignment(JLabel.CENTER);

        prefsPanel.add(hourlyChimesLabel);

        JLabel clockLabel = new JLabel("Console Clock");

        clockLabel.setFont(mainGeneralUtil.weatherFontSmall);

        clockLabel.setForeground(mainGeneralUtil.navy);

        clockLabel.setHorizontalAlignment(JLabel.CENTER);

        prefsPanel.add(clockLabel);

        JLabel silenceLabel = new JLabel("Silence Errors");

        silenceLabel.setFont(mainGeneralUtil.weatherFontSmall);

        silenceLabel.setForeground(mainGeneralUtil.navy);

        silenceLabel.setHorizontalAlignment(JLabel.CENTER);

        prefsPanel.add(silenceLabel);

        JLabel hourlyChimes = new JLabel();

        hourlyChimes.setHorizontalAlignment(JLabel.CENTER);

        hourlyChimes.setSize(100,100);

        hourlyChimes.setIcon((mainGeneralUtil.getUserData("HourlyChimes").equals("1") ? selected : notSelected));

        hourlyChimes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("HourlyChimes").equals("1");
            mainGeneralUtil.writeUserData("HourlyChimes", (wasSelected ? "0" : "1"));
            hourlyChimes.setIcon((wasSelected ? notSelected : selected));
            }
        });

        prefsPanel.add(hourlyChimes);

        JLabel clockOnConsole = new JLabel();

        clockOnConsole.setHorizontalAlignment(JLabel.CENTER);

        clockOnConsole.setSize(100,100);

        clockOnConsole.setIcon((mainGeneralUtil.getUserData("ClockOnConsole").equals("1") ? selected : notSelected));

        clockOnConsole.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("ClockOnConsole").equals("1");
            mainGeneralUtil.writeUserData("ClockOnConsole", (wasSelected ? "0" : "1"));
            clockOnConsole.setIcon((wasSelected ? notSelected : selected));
            consoleClockLabel.setVisible(!wasSelected);
            updateConsoleClock = !wasSelected;
            consoleFrame.revalidate();
            }
        });

        prefsPanel.add(clockOnConsole);

        JLabel silenceErrors = new JLabel();

        silenceErrors.setHorizontalAlignment(JLabel.CENTER);

        silenceErrors.setSize(100,100);

        silenceErrors.setIcon((mainGeneralUtil.getUserData("SilenceErrors").equals("1") ? selected : notSelected));

        silenceErrors.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("SilenceErrors").equals("1");
            mainGeneralUtil.writeUserData("SilenceErrors", (wasSelected ? "0" : "1"));
            silenceErrors.setIcon((wasSelected ? notSelected : selected));
            }

        });

        prefsPanel.add(silenceErrors);

        JLabel fullscreenLabel = new JLabel("Fullscreen");

        fullscreenLabel.setFont(mainGeneralUtil.weatherFontSmall);

        fullscreenLabel.setForeground(mainGeneralUtil.navy);

        fullscreenLabel.setHorizontalAlignment(JLabel.CENTER);

        prefsPanel.add(fullscreenLabel);

        JLabel outputBorder = new JLabel("Output Area Border");

        outputBorder.setFont(mainGeneralUtil.weatherFontSmall);

        outputBorder.setForeground(mainGeneralUtil.navy);

        outputBorder.setHorizontalAlignment(JLabel.CENTER);

        prefsPanel.add(outputBorder);

        JLabel inputBorder = new JLabel("Input Field Border");

        inputBorder.setFont(mainGeneralUtil.weatherFontSmall);

        inputBorder.setForeground(mainGeneralUtil.navy);

        inputBorder.setHorizontalAlignment(JLabel.CENTER);

        prefsPanel.add(inputBorder);

        JLabel fullscreen = new JLabel();

        fullscreen.setHorizontalAlignment(JLabel.CENTER);

        fullscreen.setSize(100,100);

        fullscreen.setIcon((mainGeneralUtil.getUserData("FullScreen").equals("1") ? selected : notSelected));

        fullscreen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("FullScreen").equals("1");
            mainGeneralUtil.writeUserData("FullScreen", (wasSelected ? "0" : "1"));
            fullscreen.setIcon((wasSelected ? notSelected : selected));
            if (wasSelected) {
                exitFullscreen();
            }

            else {
                refreshFullscreen();
            }
            }
        });

        prefsPanel.add(fullscreen);

        prefsPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(mainGeneralUtil.navy,5,false)));

        JLabel outputborder = new JLabel();

        outputborder.setHorizontalAlignment(JLabel.CENTER);

        outputborder.setSize(100,100);

        outputborder.setIcon((mainGeneralUtil.getUserData("OutputBorder").equals("1") ? selected : notSelected));

        outputborder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("OutputBorder").equals("1");
            mainGeneralUtil.writeUserData("OutputBorder", (wasSelected ? "0" : "1"));
            outputborder.setIcon((wasSelected ? notSelected : selected));
            if (wasSelected) {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            }

            else {
                outputScroll.setBorder(new LineBorder(mainGeneralUtil.vanila,3,true));
            }

            consoleFrame.revalidate();
            }
        });

        prefsPanel.add(outputborder);

        JLabel inputborder = new JLabel();

        inputborder.setHorizontalAlignment(JLabel.CENTER);

        inputborder.setSize(100,100);

        inputborder.setIcon((mainGeneralUtil.getUserData("InputBorder").equals("1") ? selected : notSelected));

        inputborder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("InputBorder").equals("1");
            mainGeneralUtil.writeUserData("InputBorder", (wasSelected ? "0" : "1"));
            inputborder.setIcon((wasSelected ? notSelected : selected));

            if (wasSelected) {
                inputField.setBorder(BorderFactory.createEmptyBorder());
            }

            else {
                inputField.setBorder(new LineBorder(mainGeneralUtil.vanila,3,true));
            }

            consoleFrame.revalidate();
            }
        });

        prefsPanel.add(inputborder);

        JPanel masterPanel = new JPanel();

        masterPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        masterPanel.setLayout(new GridLayout(1,2));

        masterPanel.add(ParentPanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(prefsPanel);
        ChangePasswordPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel fontColorPanel = new JPanel();
        fontColorPanel.setLayout(new BoxLayout(fontColorPanel, BoxLayout.X_AXIS));
        fontColorPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        fontColorPanel.add(getFontPanel());
        fontColorPanel.add(getColorPanel());

        rightPanel.add(fontColorPanel);

        rightPanel.add(ChangePasswordPanel);
        masterPanel.add(rightPanel);

        editUserFrame.add(masterPanel);
        editUserFrame.pack();
        editUserFrame.setLocationRelativeTo(null);
        editUserFrame.setVisible(true);
        editUserFrame.setAlwaysOnTop(true);
        editUserFrame.setAlwaysOnTop(false);
        editUserFrame.requestFocus();
    }

    public void initializeMusicList() {
        File dir = new File("src\\com\\cyder\\users\\" + mainGeneralUtil.getUserUUID() + "\\Music");
        musicList = new LinkedList<>();
        musicNameList = new LinkedList<>();

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith((".mp3"))) {
                musicList.add(file.getAbsoluteFile());
                musicNameList.add(file.getName().replace(".mp3", ""));
            }
        }

        String[] MusicArray = new String[musicNameList.size()];

        MusicArray = musicNameList.toArray(MusicArray);

        musicSelectionList = new JList(MusicArray);

        musicSelectionList.setFont(mainGeneralUtil.weatherFontSmall);

        musicSelectionList.setForeground(mainGeneralUtil.navy);

        musicSelectionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
            if (evt.getClickCount() == 2 && musicSelectionList.getSelectedIndex() != -1) {
                openMusic.doClick();
            }
            }
        });

        musicSelectionList.setSelectionBackground(mainGeneralUtil.selectionColor);
    }

    //todo above and below are for editing user, make it its own widget/class
    public void initializeBackgroundsList() {
        File dir = new File("src\\com\\cyder\\users\\" + mainGeneralUtil.getUserUUID() + "\\Backgrounds");
        backgroundsList = new LinkedList<>();
        backgroundsNameList = new LinkedList<>();

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith((".png"))) {
                backgroundsList.add(file.getAbsoluteFile());
                backgroundsNameList.add(file.getName().replace(".png", ""));
            }
        }

        String[] BackgroundsArray = new String[backgroundsNameList.size()];
        BackgroundsArray = backgroundsNameList.toArray(BackgroundsArray);
        backgroundSelectionList = new JList(BackgroundsArray);

        backgroundSelectionList.setFont(mainGeneralUtil.weatherFontSmall);

        backgroundSelectionList.setForeground(mainGeneralUtil.navy);

        backgroundSelectionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
            if (evt.getClickCount() == 2 && backgroundSelectionList.getSelectedIndex() != -1) {
                openBackground.doClick();
            }
            }
        });

        backgroundSelectionList.setSelectionBackground(mainGeneralUtil.selectionColor);
    }

    private JPanel getColorPanel() {
        JPanel parentPanel = new JPanel();
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.PAGE_AXIS));

        JLabel label = new JLabel("Select your desired color");
        label.setFont(mainGeneralUtil.weatherFontSmall);
        label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        JPanel labelPanel = new JPanel();
        labelPanel.add(label);
        parentPanel.add(labelPanel, Component.CENTER_ALIGNMENT);

        JTextField hexField = new JTextField("",10);
        hexField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
            if (hexField.getText().length() > 6) {
                hexField.setText(hexField.getText().substring(0,hexField.getText().length() - 1));
                Toolkit.getDefaultToolkit().beep();
            }

            else {
                try {
                    String colorStr = hexField.getText();
                    label.setForeground(new Color(Integer.valueOf(colorStr.substring(0,2),16),
                            Integer.valueOf(colorStr.substring(2,4),16),
                            Integer.valueOf(colorStr.substring(4,6),16)));
                }

                catch (Exception ignored) {

                }
            }
            }
        });
        hexField.setFont(mainGeneralUtil.weatherFontSmall);
        hexField.setSelectionColor(mainGeneralUtil.selectionColor);
        hexField.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(2,2,2,2),
                new LineBorder(mainGeneralUtil.navy,5,false)));
        hexField.setToolTipText("Hex Color");

        JPanel fieldPanel = new JPanel();
        fieldPanel.add(hexField);
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        parentPanel.add(fieldPanel);

        CyderButton apply = new CyderButton("Apply Color");
        apply.setFocusPainted(false);
        apply.setColors(mainGeneralUtil.regularRed);
        apply.setForeground(mainGeneralUtil.navy);
        apply.setBackground(mainGeneralUtil.regularRed);
        apply.setFont(mainGeneralUtil.weatherFontSmall);
        apply.addActionListener(e -> {
            Color newColor = label.getForeground();
            outputArea.setForeground(newColor);
            inputField.setForeground(newColor);

            if (newColor != mainGeneralUtil.getUsercolor()) {
                println("The color [" + newColor.getRed() + "," + newColor.getGreen() + "," + newColor.getBlue() + "] has been applied.");
            }
        });

        JPanel applyPanel = new JPanel();
        applyPanel.add(apply);
        applyPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        parentPanel.add(applyPanel, Component.CENTER_ALIGNMENT);
        parentPanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));

        return parentPanel;
    }

    //todo move font and color to panel util
    private JPanel getFontPanel() {
        JPanel parentPanel = new JPanel();
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("Select your desired font");

        label.setFont(mainGeneralUtil.weatherFontSmall);
        label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel labelPanel = new JPanel();

        labelPanel.add(label);
        parentPanel.add(labelPanel, Component.CENTER_ALIGNMENT);

        String[] Fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        fontList = new JList(Fonts);
        fontList.setSelectionBackground(mainGeneralUtil.selectionColor);
        fontList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fontList.setFont(mainGeneralUtil.weatherFontSmall);

        CyderScrollPane FontListScroll = new CyderScrollPane(fontList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        FontListScroll.setThumbColor(mainGeneralUtil.intellijPink);
        FontListScroll.setBorder(new LineBorder(mainGeneralUtil.navy,5,true));

        CyderButton applyFont = new CyderButton("Apply Font");
        applyFont.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        applyFont.setColors(mainGeneralUtil.regularRed);
        applyFont.setToolTipText("Apply");
        applyFont.setFont(mainGeneralUtil.weatherFontSmall);
        applyFont.setFocusPainted(false);
        applyFont.setBackground(mainGeneralUtil.regularRed);
        applyFont.addActionListener(e -> {
            String FontS = (String) fontList.getSelectedValue();

            if (FontS != null) {
                Font ApplyFont = new Font(FontS, Font.BOLD, 30);
                outputArea.setFont(ApplyFont);
                inputField.setFont(ApplyFont);
                println("The font \"" + FontS + "\" has been applied.");
            }
        });

        fontList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                applyFont.doClick();
            }

            else {
                try {
                    label.setFont(new Font(fontList.getSelectedValue().toString(), Font.BOLD, 20));
                }

                catch (Exception ex) {
                    mainGeneralUtil.handle(ex);
                }
            }
            }
        });

        fontList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            JList t = (JList) e.getSource();
            int index = t.locationToIndex(e.getPoint());

            label.setFont(new Font(t.getModel().getElementAt(index).toString(), Font.BOLD, 20));
            }
        });

        parentPanel.add(FontListScroll, Component.CENTER_ALIGNMENT);

        JPanel apply = new JPanel();
        apply.add(applyFont);
        apply.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        parentPanel.add(apply, Component.CENTER_ALIGNMENT);

        return parentPanel;
    }

    public void createUser() {
        createUserBackground = null;

        if (createUserFrame != null)
            mainGeneralUtil.closeAnimation(createUserFrame);

        createUserFrame = new CyderFrame(356,473,new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        createUserFrame.setTitle("Create User");

        JLabel NameLabel = new JLabel("Username: ", SwingConstants.CENTER);
        NameLabel.setFont(mainGeneralUtil.weatherFontSmall);
        NameLabel.setBounds(120,30,121,30); //todo bounds for labels
        createUserFrame.getContentPane().add(NameLabel);

        newUserName = new JTextField(15);
        newUserName.setSelectionColor(mainGeneralUtil.selectionColor);
        newUserName.setFont(mainGeneralUtil.weatherFontSmall);
        newUserName.setForeground(mainGeneralUtil.navy);
        newUserName.setFont(mainGeneralUtil.weatherFontSmall);
        newUserName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
            if (newUserName.getText().length() > 15) {
                evt.consume();
            }
            }
        });

        newUserName.setBorder(new LineBorder(new Color(0, 0, 0)));
        newUserName.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");

                if (newUserName.getText().length() == 1) {
                    newUserName.setText(newUserName.getText().toUpperCase());
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");

                if (newUserName.getText().length() == 1) {
                    newUserName.setText(newUserName.getText().toUpperCase());
                }
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");

                if (newUserName.getText().length() == 1) {
                    newUserName.setText(newUserName.getText().toUpperCase());
                }
            }
        });

        newUserName.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        newUserName.setBounds(60,70,240,40);
        createUserFrame.getContentPane().add(newUserName);

        JLabel passwordLabel = new JLabel("Password: ", SwingConstants.CENTER);
        passwordLabel.setFont(mainGeneralUtil.weatherFontSmall);
        passwordLabel.setForeground(mainGeneralUtil.navy);
        passwordLabel.setBounds(60,120,240,30);
        createUserFrame.getContentPane().add(passwordLabel);

        JLabel matchPasswords = new JLabel("Passwords match");

        newUserPassword = new JPasswordField(15);
        newUserPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                matchPasswords.setText("<html><div style='text-align: center;'>Passwords match</div></html>");
                matchPasswords.setForeground(mainGeneralUtil.regularGreen);
            }

            else {
                matchPasswords.setText("<html><div style='text-align: center;'>Passwords don't match</div></html>");
                matchPasswords.setForeground(mainGeneralUtil.regularRed);
            }
            }
        });
        newUserPassword.setFont(mainGeneralUtil.weatherFontSmall);
        newUserPassword.setForeground(mainGeneralUtil.navy);
        newUserPassword.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        newUserPassword.setSelectedTextColor(mainGeneralUtil.selectionColor);
        newUserPassword.setBounds(60,160,240,40);
        createUserFrame.getContentPane().add(newUserPassword);

        JLabel passwordLabelConf = new JLabel("Confirm Password: ", SwingConstants.CENTER);
        passwordLabelConf.setFont(mainGeneralUtil.weatherFontSmall);
        passwordLabelConf.setForeground(mainGeneralUtil.navy);
        passwordLabelConf.setBounds(60,210,240,30);
        createUserFrame.getContentPane().add(passwordLabelConf);

        newUserPasswordconf = new JPasswordField(15);
        newUserPasswordconf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                matchPasswords.setText("<html><div style='text-align: center;'>Passwords match</div></html>");
                matchPasswords.setForeground(mainGeneralUtil.regularGreen);
            }

            else {
                matchPasswords.setText("<html><div style='text-align: center;'>Passwords don't match</div></html>");
                matchPasswords.setForeground(mainGeneralUtil.regularRed);
            }
            }
        });

        newUserPasswordconf.setFont(mainGeneralUtil.weatherFontSmall);
        newUserPasswordconf.setForeground(mainGeneralUtil.navy);
        newUserPasswordconf.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        newUserPasswordconf.setSelectedTextColor(mainGeneralUtil.selectionColor);
        newUserPasswordconf.setBounds(60,250,240,40);
        createUserFrame.getContentPane().add(newUserPasswordconf);

        matchPasswords.setFont(mainGeneralUtil.weatherFontSmall);
        matchPasswords.setForeground(mainGeneralUtil.regularGreen);
        matchPasswords.setBounds(65,300,300,30);
        createUserFrame.getContentPane().add(matchPasswords);

        chooseBackground = new CyderButton("Choose background");
        chooseBackground.setToolTipText("Click me to choose a background");
        chooseBackground.setFont(mainGeneralUtil.weatherFontSmall);
        chooseBackground.setBackground(mainGeneralUtil.regularRed);
        chooseBackground.setColors(mainGeneralUtil.regularRed);
        chooseBackground.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    File temp = mainGeneralUtil.getFile();
                    if (temp != null) {
                        createUserBackground = temp;
                    }

                    if (temp != null && !Files.probeContentType(Paths.get(createUserBackground.getAbsolutePath())).endsWith("png")) {
                        createUserBackground = null;
                    }
                }

                catch (Exception exc) {
                    exc.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                try {
                    if (createUserBackground != null) {
                        chooseBackground.setText(createUserBackground.getName());
                    }

                    else {
                        chooseBackground.setToolTipText("No File Chosen");
                    }
                }

                catch (Exception ex) {
                    mainGeneralUtil.handle(ex);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                chooseBackground.setToolTipText("Choose background");
            }
        });

        chooseBackground.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        chooseBackground.setBounds(60,340,240,40);
        createUserFrame.getContentPane().add(chooseBackground);

        createNewUser = new CyderButton("Create User");
        createNewUser.setFont(mainGeneralUtil.weatherFontSmall);
        createNewUser.setBackground(mainGeneralUtil.regularRed);
        createNewUser.setColors(mainGeneralUtil.regularRed);
        createNewUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            try {
                String uuid = mainGeneralUtil.generateUUID();
                File folder = new File("src\\com\\cyder\\users\\" + uuid);

                while (folder.exists()) {
                    uuid = mainGeneralUtil.generateUUID();
                    folder = new File("src\\com\\cyder\\users\\" + uuid);
                }

                char[] pass = newUserPassword.getPassword();
                char[] passconf = newUserPasswordconf.getPassword();

                boolean alreadyExists = false;
                File[] files = new File("src\\com\\cyder\\users").listFiles();

                for (File f: files) {
                    File data = new File(f.getAbsolutePath() + "\\Userdata.txt");
                    BufferedReader partReader = new BufferedReader(new FileReader(data));
                    String line = partReader.readLine();
                    while (line != null) {
                        String[] parts = line.split(":");
                        if (parts[0].equalsIgnoreCase("Name") && parts[1].equalsIgnoreCase(newUserName.getText().trim())) {
                            alreadyExists = true;
                            break;
                        }

                        line = partReader.readLine();
                    }

                    if (alreadyExists) break;
                }

                if (mainGeneralUtil.empytStr(newUserName.getText()) || pass == null || passconf == null
                        || uuid.equals("") || pass.equals("") || passconf.equals("") || uuid.length() == 0) {
                    mainGeneralUtil.inform("Sorry, but one of the required fields was left blank.\nPlease try again.","", 400, 300);
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else if (alreadyExists) {
                    mainGeneralUtil.inform("Sorry, but that username is already in use.\nPlease try a different one.", "", 400, 300);
                    newUserName.setText("");
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else if (!Arrays.equals(pass, passconf) && pass.length > 0) {
                    mainGeneralUtil.inform("Sorry, but your passwords did not match. Please try again.", "",400, 300);
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else if (pass.length < 5) {
                    mainGeneralUtil.inform("Sorry, but your password length should be greater than\n"
                            + "four characters for security reasons. Please add more characters.", "", 400, 300);

                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else {
                    if (createUserBackground == null) {
                        mainGeneralUtil.inform("No background image was chosen so we're going to give you a sweet one ;)", "No background", 700, 230);
                        createUserBackground = new File("src\\com\\cyder\\io\\pictures\\bobby.png");
                    }

                    File NewUserFolder = new File("src\\com\\cyder\\users\\" + uuid);
                    File backgrounds = new File("src\\com\\cyder\\users\\" + uuid + "\\Backgrounds");
                    File music = new File("src\\com\\cyder\\users\\" + uuid + "\\Music");
                    File notes = new File("src\\com\\cyder\\users\\" + uuid + "\\Notes");

                    NewUserFolder.mkdirs();
                    backgrounds.mkdir();
                    music.mkdir();
                    notes.mkdir();

                    ImageIO.write(ImageIO.read(createUserBackground), "png",
                            new File("src\\com\\cyder\\users\\" + uuid + "\\Backgrounds\\" + createUserBackground.getName()));

                    BufferedWriter newUserWriter = new BufferedWriter(new FileWriter(
                            "src\\com\\cyder\\users\\" + uuid + "\\Userdata.txt"));

                    LinkedList<String> data = new LinkedList<>();
                    data.add("Name:" + newUserName.getText().trim());
                    data.add("Font:tahoma");
                    data.add("Red:252");
                    data.add("Green:251");
                    data.add("Blue:227");
                    data.add("Password:" + mainGeneralUtil.toHexString(mainGeneralUtil.getSHA(pass)));
                    data.add("IntroMusic:0");
                    data.add("DebugWindows:0");
                    data.add("RandomBackground:0");
                    data.add("HourlyChimes:1");
                    data.add("ClockOnConsole:1");
                    data.add("SilenceErrors:1");
                    data.add("FullScreen:0");
                    data.add("OutputBorder:0");
                    data.add("InputBorder:0");

                    for (String d : data) {
                        newUserWriter.write(d);
                        newUserWriter.newLine();
                    }

                    newUserWriter.close();

                    mainGeneralUtil.closeAnimation(createUserFrame);

                    mainGeneralUtil.inform("The new user \"" + newUserName.getText().trim() + "\" has been created successfully.", "", 500, 300);

                    if (consoleFrame != null)
                        mainGeneralUtil.closeAnimation(createUserFrame);

                    else {
                        mainGeneralUtil.closeAnimation(createUserFrame);
                        mainGeneralUtil.closeAnimation(loginFrame);
                        recognize(newUserName.getText().trim(),pass);
                    }
                }

                //proper password handling in Java
                for (char c : pass)
                    c = '\0';

                for (char c : passconf)
                    c = '\0';
            }

            catch (Exception ex) {
                mainGeneralUtil.handle(ex);
            }
            }
        });

        createNewUser.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        createNewUser.setFont(mainGeneralUtil.weatherFontSmall);
        createNewUser.setBounds(60,390,240,40);
        createUserFrame.getContentPane().add(createNewUser);

        createUserFrame.setLocationRelativeTo(null);
        createUserFrame.setVisible(true);
        newUserName.requestFocus();
    }

    //todo same as below and make consoleClockLabel static or something
    private void refreshConsoleClock() {
        Thread TimeThread = new Thread(() -> {
            try {
                while (updateConsoleClock) {
                    Thread.sleep(3000);
                    consoleClockLabel.setText(mainGeneralUtil.consoleTime());
                    consoleClockLabel.setToolTipText(mainGeneralUtil.weatherThreadTime());
                }
            }

            catch (Exception e) {
                mainGeneralUtil.handle(e);
            }
        },"console-clock-updater");

        TimeThread.start();
    }

    //todo move to checking utils thread that runs in background, name all threads
    private void checkChime() {
        Thread ChimeThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(4000);
                    Calendar now = Calendar.getInstance();
                    if (now.get(Calendar.MINUTE) == 0 && now.get(Calendar.SECOND) <= 4)
                        mainGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\chime.mp3");
                }
            }

            catch (Exception e) {
                mainGeneralUtil.handle(e);
            }
        },"chime-checker");

        ChimeThread.start();
    }

    private void minimizeMenu() {
        if (menuLabel.isVisible()) {
            animation.jLabelXLeft(0,-150,10,8, menuLabel);

            Thread waitThread = new Thread(() -> {
                try {
                    Thread.sleep(186);
                }

                catch (Exception ex) {
                    mainGeneralUtil.handle(ex);
                }

                menuLabel.setVisible(false);
                menuButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\menuSide1.png"));
            });

            waitThread.start();
        }
    }

    private void killAllYoutube() {
        for (YoutubeThread ytt : youtubeThreads) {
            ytt.kill();
        }
    }

    //todo move file.txt to temp dir, file used for filechooser

    private void askew() {
        consoleFrame.setBackground(mainGeneralUtil.navy);
        parentLabel.setIcon(new ImageIcon(mainGeneralUtil.rotateImageByDegrees(mainGeneralUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().getAbsolutePath()),3)));
    }


    private void barrelRoll() {
        consoleFrame.setBackground(mainGeneralUtil.navy);
        mainGeneralUtil.getValidBackgroundPaths();

        int originConsoleDIr = mainGeneralUtil.getConsoleDirection();
        BufferedImage master = mainGeneralUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().getAbsolutePath());

        Timer timer = null;
        Timer finalTimer = timer;
        timer = new Timer(10, new ActionListener() {
            private double angle = 0;
            private double delta = 2.0;

            BufferedImage rotated;

            @Override
            public void actionPerformed(ActionEvent e) {
                angle += delta;
                if (angle > 360) {
                    return;
                }
                rotated = mainGeneralUtil.rotateImageByDegrees(master, angle);
                parentLabel.setIcon(new ImageIcon(rotated));
            }
        });
        timer.start();
    }

    //exiting method, system.exit will call shutdown hook which wil then call shutdown();
    private void exit() {
        mainGeneralUtil.closeAnimation(consoleFrame);
        killAllYoutube();
        stringUtil.killBletchy();
        System.exit(0);
    }

    //todo add more to cyderargs.log
    //todo make cyderargs push to bottom so new stuff is at top
    //todo move users out of cyder and into same dir as com

    private void shutdown() {
        try {
            Font SaveFont = outputArea.getFont();
            String SaveFontName = SaveFont.getName();
            Color SaveColor = outputArea.getForeground();

            int saveColorR = SaveColor.getRed();
            int saveColorG = SaveColor.getGreen();
            int saveColorB = SaveColor.getBlue();

            mainGeneralUtil.readUserData();
            mainGeneralUtil.writeUserData("Font",SaveFontName);
            mainGeneralUtil.writeUserData("Red",saveColorR + "");
            mainGeneralUtil.writeUserData("Green",saveColorG + "");
            mainGeneralUtil.writeUserData("Blue",saveColorB + "");

            mainGeneralUtil.deleteTempDir();
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    //todo can this go to some util method when you separate methods out of here and GeneralUtil?
    private static void logArgs(String[] cyderArgs) {
        try {
            if (cyderArgs.length == 0)
                cyderArgs = new String[]{"Started by " + System.getProperty("user.name")};

            File log = new File("src/CyderArgs.log");

            if (!log.exists())
                log.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(log, true));

            String argsString = "";

            for (int i = 0 ; i < cyderArgs.length ; i++) {
                if (i != 0)
                    argsString += ",";
                argsString += cyderArgs[i];
            }

            Date current = new Date();
            DateFormat argsFormat = new SimpleDateFormat("MM-dd-yy HH:mm:ss");
            bw.write(argsFormat.format(current) + " : " + argsString);
            bw.newLine();
            bw.flush();
            bw.close();
        }

        catch (Exception e) {
            new GeneralUtil().staticHandle(e);
        }
    }

    //todo remove this once loginFrame is cyderFrame
    public void notify(String htmltext, int delay, int arrowDir, int vanishDir, JLabel parent, int width) {
        if (consoleNotification != null && consoleNotification.isVisible())
            consoleNotification.kill();

        consoleNotification = new Notification();

        int w = width;
        int h = 40;

        int lastIndex = 0;

        while (lastIndex != -1){

            lastIndex = htmltext.indexOf("<br/>",lastIndex);

            if (lastIndex != -1){
                h += 30;
                lastIndex += "<br/>".length();
            }
        }

        if (h == 40)
            h = 30;

        consoleNotification.setWidth(w);
        consoleNotification.setHeight(h);
        consoleNotification.setArrow(arrowDir);

        JLabel text = new JLabel();
        text.setText(htmltext);
        text.setFont(mainGeneralUtil.weatherFontSmall);
        text.setForeground(mainGeneralUtil.navy);
        text.setBounds(14,10,w * 2,h);
        consoleNotification.add(text);
        consoleNotification.setBounds(parent.getWidth() / 2 - (w/2),30,w * 2,h * 2);
        parent.add(consoleNotification,1,0);
        parent.repaint();

        consoleNotification.vanish(vanishDir, parent, delay);
    }

    //todo make a centered one that vanishes to top
    public void notify(String htmltext, int delay, int arrowDir, int vanishDir, JLayeredPane parent, int width) {
        if (consoleNotification != null && consoleNotification.isVisible())
            consoleNotification.kill();

        consoleNotification = new Notification();

        int w = width;
        int h = 40;

        int lastIndex = 0;

        while (lastIndex != -1){

            lastIndex = htmltext.indexOf("<br/>",lastIndex);

            if (lastIndex != -1){
                h += 30;
                lastIndex += "<br/>".length();
            }
        }

        if (h == 40)
            h = 30;

        consoleNotification.setWidth(w);
        consoleNotification.setHeight(h);
        consoleNotification.setArrow(arrowDir);

        JLabel text = new JLabel();
        text.setText(htmltext);
        text.setFont(mainGeneralUtil.weatherFontSmall);
        text.setForeground(mainGeneralUtil.navy);
        text.setBounds(14,10,w * 2,h);
        consoleNotification.add(text);
        consoleNotification.setBounds(parent.getWidth() / 2 - (w/2),30,w * 2,h * 2);
        parent.add(consoleNotification,1,0);
        parent.repaint();

        consoleNotification.vanish(vanishDir, parent, delay);
    }
}