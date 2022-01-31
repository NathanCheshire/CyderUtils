package cyder.user;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.LoginHandler;
import cyder.handlers.internal.PopupHandler;
import cyder.handlers.internal.Logger;
import cyder.ui.*;
import cyder.utilities.*;
import cyder.widgets.WidgetBase;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class UserCreator implements WidgetBase {
    /**
     * The user creator frame.
     */
    private static CyderFrame createUserFrame;

    /**
     * The password field to confirm the new user's password.
     */
    private static CyderPasswordField newUserPasswordconf;

    /**
     * The password field for the user's password.
     */
    private static CyderPasswordField newUserPassword;

    /**
     * The field for the new user's name.
     */
    private static CyderTextField newUserName;


    private static File createUserBackground;

    /**
     * No instances of user creator allowed.
     */
    private UserCreator() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    @Widget(trigger = "createuser", description = "A user creating widget")
    public static void showGUI() {
        if (createUserFrame != null)
            createUserFrame.dispose();

        Logger.log(Logger.Tag.WIDGET_OPENED, "USER CREATED");

        createUserBackground = null;

        createUserFrame = new CyderFrame(356, 473, CyderIcons.defaultBackground);
        createUserFrame.setTitle("Create User");


        JLabel namelabel = new JLabel("Username: ", SwingConstants.CENTER);
        namelabel.setFont(CyderFonts.segoe20);
        namelabel.setForeground(CyderColors.navy);
        namelabel.setBounds(120, 30, 121, 30);
        createUserFrame.getContentPane().add(namelabel);

        //initialize here since we need to update its tooltip
        CyderButton createNewUser = new CyderButton("Create User");

        newUserName = new CyderTextField(0);
        newUserName.setBackground(Color.white);
        newUserName.setFont(CyderFonts.segoe20);
        newUserName.setBorder(new LineBorder(new Color(0, 0, 0)));
        newUserName.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");
            }
        });

        newUserName.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserName.setBounds(60, 70, 240, 40);
        createUserFrame.getContentPane().add(newUserName);

        JLabel passwordLabel = new JLabel("Password: ", SwingConstants.CENTER);
        passwordLabel.setFont(CyderFonts.segoe20);
        passwordLabel.setForeground(CyderColors.navy);
        passwordLabel.setBounds(60, 120, 240, 30);
        createUserFrame.getContentPane().add(passwordLabel);

        //initialize here since we need to update it for both fields
        JLabel matchPasswords = new JLabel("Passwords match", SwingConstants.CENTER);

        newUserPassword = new CyderPasswordField();
        newUserPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                    matchPasswords.setText("Passwords match");
                    matchPasswords.setForeground(CyderColors.regularGreen);
                } else {
                    matchPasswords.setText("Passwords do not match");
                    matchPasswords.setForeground(CyderColors.regularRed);
                }
            }
        });
        newUserPassword.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserPassword.setBounds(60, 160, 240, 40);
        newUserPassword.setCaret(new CyderCaret(CyderColors.navy));
        createUserFrame.getContentPane().add(newUserPassword);

        JLabel passwordLabelConf = new JLabel("Confirm Password: ", SwingConstants.CENTER);
        passwordLabelConf.setFont(CyderFonts.segoe20);
        passwordLabelConf.setForeground(CyderColors.navy);
        passwordLabelConf.setBounds(60, 210, 240, 30);
        createUserFrame.getContentPane().add(passwordLabelConf);

        newUserPasswordconf = new CyderPasswordField();
        newUserPasswordconf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                    matchPasswords.setText("Passwords match");
                    matchPasswords.setForeground(CyderColors.regularGreen);
                } else {
                    matchPasswords.setText("Passwords do not match");
                    matchPasswords.setForeground(CyderColors.regularRed);
                }
            }
        });
        newUserPasswordconf.setBounds(60, 250, 240, 40);
        createUserFrame.getContentPane().add(newUserPasswordconf);

        matchPasswords.setFont(CyderFonts.segoe20);
        matchPasswords.setForeground(CyderColors.regularGreen);
        matchPasswords.setBounds(32, 300, 300, 30);
        createUserFrame.getContentPane().add(matchPasswords);

        CyderButton chooseBackground = new CyderButton("Choose background");
        chooseBackground.setToolTipText("ClickMe me to choose a background");
        chooseBackground.setFont(CyderFonts.segoe20);
        chooseBackground.setBackground(CyderColors.regularRed);
        chooseBackground.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    chooseBackground(chooseBackground);
                } catch (Exception exc) {
                    ExceptionHandler.handle(exc);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                try {
                    if (createUserBackground != null) {
                        chooseBackground.setText(createUserBackground.getName());
                    } else {
                        chooseBackground.setText("No File Chosen");
                    }
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                try {
                    if (createUserBackground != null) {
                        chooseBackground.setText(createUserBackground.getName());
                    } else {
                        chooseBackground.setText("Choose Background");
                    }
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            }
        });

        chooseBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        chooseBackground.setBounds(60, 340, 240, 40);
        createUserFrame.getContentPane().add(chooseBackground);

        createNewUser.setFont(CyderFonts.segoe20);
        createNewUser.setBackground(CyderColors.regularRed);
        createNewUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    if (!createUser(newUserName.getText(), newUserPassword.getPassword(),
                            newUserPasswordconf.getPassword(), createUserBackground)) {
                        createUserFrame.notify("Failed to create user");

                        if (lastGeneratedUUID != null) {
                            File deleteMe = new File(OSUtil.buildPath("dynamic","users",lastGeneratedUUID));

                            if (deleteMe.exists() && !deleteMe.delete()) {
                                throw new RuntimeException("Failed to delete failed user creation folder");
                            }
                        }
                    } else {
                        createUserFrame.dispose();

                        PopupHandler.inform("The new user \"" + newUserName.getText().trim()
                                + "\" has been created successfully.", "", CyderCommon.getDominantFrame());

                        //attempt to log in new user if it's the only user
                        if (new File(OSUtil.buildPath("dynamic","users")).listFiles().length == 1) {
                            LoginHandler.getLoginFrame().dispose();
                            LoginHandler.recognize(newUserName.getText().trim(),
                                    SecurityUtil.toHexString(SecurityUtil.getSHA256(
                                            newUserPassword.getPassword())), false);
                        }
                    }
                } catch (Exception ex) {
                    ExceptionHandler.silentHandle(ex);
                }
            }
        });

        createNewUser.setBorder(new LineBorder(CyderColors.navy, 5, false));
        createNewUser.setFont(CyderFonts.segoe20);
        createNewUser.setBounds(60, 390, 240, 40);
        createUserFrame.getContentPane().add(createNewUser);

        createUserFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());

        createUserFrame.setVisible(true);
        newUserName.requestFocus();
    }

    /**
     * The last generated UUID.
     */
    private static String lastGeneratedUUID = null;

    /**
     * Initializes the new user's background.
     *
     * @param referenceButton the button to set to the tooltip
     *                       of the chosen background if a valid one is chosen
     */
    private static void chooseBackground(CyderButton referenceButton) {
        new Thread(() -> {
            try {
                File temp = new GetterUtil().getFile("Choose new user's background file");
                if (temp != null) {
                    createUserBackground = temp;
                    referenceButton.setText(createUserBackground.getName());
                }

                if (temp == null || !Files.probeContentType(Paths.get(
                        createUserBackground.getAbsolutePath())).endsWith("png")) {
                    createUserBackground = null;
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }, "wait thread for GetterUtil().getFile()").start();
    }

    /**
     * Closes the create user frame if open.
     */
    public static void close() {
        if (createUserFrame != null)
            createUserFrame.dispose();
    }

    /**
     * Attempts to create a user based off of the provided necessary initial data.
     *
     * @param name the requested name of the new user
     * @param password the password of the new user
     * @param passwordConf the password confirmation of the new user
     * @param chosenBackground the background file of the new user
     *
     * @return whether the user was created
     */
    public static boolean createUser(String name, char[] password,
                                     char[] passwordConf, File chosenBackground) {

        //validate data for basic correctness
        if (StringUtil.empytStr(name) ) {
            return false;
        }

        if (password == null || passwordConf == null) {
            return false;
        }

        if (!Arrays.equals(password, passwordConf)) {
            createUserFrame.notify("Passwords are not equal");
            newUserPassword.setText("");
            newUserPasswordconf.setText("");
            return false;
        }

        if (password.length < 5 || passwordConf.length < 5) {
            createUserFrame.notify("Password length must be at least 5 characters");
            newUserPassword.setText("");
            newUserPasswordconf.setText("");
            return false;
        }

        boolean alphabet = false;
        boolean number = false;

        for (char c : password) {
            if (Character.isDigit(c))
                number = true;
            else if (Character.isAlphabetic(c))
                alphabet = true;

            if (number && alphabet)
                break;
        }

        if (!number || !alphabet) {
            createUserFrame.notify("Password must contain at least one number," +
                    " one letter, and be 5 characters long");
            newUserPassword.setText("");
            newUserPasswordconf.setText("");
            return false;
        }

        //trim data
        name = name.trim();

        //generate the user uuid and ensure it is unique
        String uuid = SecurityUtil.generateUUID();
        File folder = new File(OSUtil.buildPath("dynamic", "users",uuid));

        while (folder.exists()) {
            uuid = SecurityUtil.generateUUID();
            folder = new File(OSUtil.buildPath("dynamic", "users",uuid));
        }

        //set the uuid so that we can delete the folder if something fails later
        lastGeneratedUUID = uuid;

        //ensure that the username doesn't already exist
        boolean userNameExists = false;

        for (File f : folder.getParentFile().listFiles()) {
            File jsonFile = new File(OSUtil.buildPath(f.getAbsolutePath(), UserFile.USERDATA.getName()));

            if (!jsonFile.exists())
                continue;

            String currentName = UserUtil.extractUserData(jsonFile, "name");

            if (currentName.equalsIgnoreCase(newUserName.getText())) {
                userNameExists = true;
                break;
            }
        }

        if (userNameExists) {
            createUserFrame.inform("Sorry, but that username is already in use. " +
                    "Please choose a different one.", "");
            newUserName.setText("");
            return false;
        }

        //create the user folder
        File userFolder = new File(OSUtil.buildPath("dynamic","users",uuid));

        if (!userFolder.mkdir())
            return false;

        //create the default user files
        for (UserFile f : UserFile.getFiles()) {
            File makeMe = new File(OSUtil.buildPath("dynamic","users",uuid,f.getName()));

            if (f.isFile()) {
                try {
                   if (!makeMe.createNewFile())
                       return false;
               } catch (Exception e) {
                   ExceptionHandler.handle(e);
                   return false;
               }
            } else {
                if (!makeMe.mkdir())
                    return false;
            }
        }

        //if the background was not chosen, create a random gradient one
        if (createUserBackground == null) {
            Image img = CyderIcons.defaultBackground.getImage();

            BufferedImage bi = null;

            //try to get default image that isn't bundled with Cyder
            try {
                bi = ImageIO.read(new URL("https://i.imgur.com/kniH8y9.png"));
            } catch (Exception e) {
                ExceptionHandler.handle(e);

                bi = new BufferedImage(img.getWidth(null),
                        img.getHeight(null),BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
            }

            File backgroundFile = new File(OSUtil.buildPath("dynamic","users",uuid,
                    UserFile.BACKGROUNDS.getName(),"Default.png"));

            try {
                ImageIO.write(bi, "png", backgroundFile);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                return false;
            }

            createUserBackground = backgroundFile;
        }

        //create the user background in the directory
        try {
            File destination = new File(OSUtil.buildPath("dynamic", "users", uuid,
                    UserFile.BACKGROUNDS.getName(), createUserBackground.getName()));
            Files.copy(Paths.get(createUserBackground.getAbsolutePath()), destination.toPath());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            return false;
        }

        //build the user
        User user = new User();

        //name and password
        user.setName(newUserName.getText().trim());
        user.setPass(SecurityUtil.toHexString(SecurityUtil.getSHA256(
                SecurityUtil.toHexString(SecurityUtil.getSHA256(password)).toCharArray())));

        //default perferences
        for (Preferences.Preference pref : Preferences.getPreferences()) {
            //as per convention, IGNORE for tooltip means ignore when creating user
            // whilst IGNORE for default value means ignore for edit user
            if (!pref.getTooltip().equals("IGNORE"))
                UserUtil.setUserData(user, pref.getID(), pref.getDefaultValue());
        }

        BufferedImage background = null;

        //screen stat initializing
        try {
            background = ImageIO.read(createUserBackground);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            return false;
        }

        int monitorNum = -1;
        int x = -1;
        int y = -1;
        //figure out the monitor we should be using for the user's screen stats
        if (createUserFrame != null) {
            GraphicsConfiguration gc = createUserFrame.getGraphicsConfiguration();
            String monitorID = gc.getDevice().getIDstring().replaceAll("[^0-9]","");

            try {
                monitorNum = Integer.parseInt(monitorID);
                int monitorWidth = (int) gc.getBounds().getWidth();
                int monitorHeight = (int) gc.getBounds().getHeight();
                int monitorX = (int) gc.getBounds().getX();
                int monitorY = (int) gc.getBounds().getY();

                x = monitorX + (monitorWidth - background.getWidth()) / 2;
                y = monitorY + (monitorHeight - background.getHeight()) / 2;
            } catch (Exception e) {
                ExceptionHandler.silentHandle(e);

                //error so default the screen stats
                x = (ScreenUtil.getScreenWidth() - background.getWidth()) / 2;
                y = (ScreenUtil.getScreenHeight() - background.getHeight()) / 2;
            }
        }
        //otherwise default monitor stats
        else {
            x = (ScreenUtil.getScreenWidth() - background.getWidth()) / 2;
            y = (ScreenUtil.getScreenHeight() - background.getHeight()) / 2;
        }

        user.setScreenStat(new User.ScreenStat(x, y, background.getWidth(),
                background.getHeight(), monitorNum, false));

        //executables
        user.setExecutables(null);
        UserUtil.setUserData(new File(OSUtil.buildPath(
                "dynamic","users",uuid,UserFile.USERDATA.getName())), user);

        //password security
        for (char c : password)
            c = '\0';

        for (char c : passwordConf)
            c = '\0';

        return true;
    }
}
