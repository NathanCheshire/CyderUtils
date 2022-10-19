package cyder.user;

import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.*;
import cyder.enums.CyderInspection;
import cyder.enums.Direction;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.layouts.CyderPartitionedLayout;
import cyder.login.LoginHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderPasswordField;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.utils.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * A widget to create a Cyder user.
 */
@Vanilla
@CyderAuthor
@SuppressCyderInspections(CyderInspection.VanillaInspection)
public final class UserCreator {
    /**
     * The user creator frame.
     */
    private static CyderFrame createUserFrame;

    /**
     * The password field for the user's password.
     */
    private static CyderPasswordField newUserPasswordField;

    /**
     * The password field to confirm the new user's password.
     */
    private static CyderPasswordField newUserPasswordConfirmationField;

    /**
     * The field for the new user's name.
     */
    private static CyderTextField newUserNameField;

    /**
     * The background chosen by the user as their initial background.
     */
    private static File newUserBackgroundFile;

    /**
     * The button to finalize the creation of a new user.
     */
    private static CyderButton createNewUserButton;

    /**
     * The label to display information on to help the user with their account creation.
     */
    private static JLabel informationLabel;

    /**
     * The button to choose a background file for the proposed user.
     */
    private static CyderButton chooseBackgroundButton;

    /**
     * The border used for fields and buttons.
     */
    private static final LineBorder BORDER = new LineBorder(CyderColors.navy, 5, false);

    /**
     * Suppress default constructor.
     */
    private UserCreator() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = {"create user", "create"}, description = "A widget for creating new users")
    public static void showGui() {
        UiUtil.closeIfOpen(createUserFrame);

        newUserBackgroundFile = null;

        createUserFrame = new CyderFrame(350, 500, CyderIcons.defaultBackground);
        createUserFrame.setTitle("Create User");

        JLabel nameLabel = new JLabel("Username: ", SwingConstants.CENTER);
        nameLabel.setFont(CyderFonts.SEGOE_20);
        nameLabel.setForeground(CyderColors.navy);
        nameLabel.setSize(120, 30);

        createNewUserButton = new CyderButton("Create User");

        newUserNameField = new CyderTextField();
        newUserNameField.setHorizontalAlignment(JTextField.CENTER);
        newUserNameField.setBackground(Color.white);
        newUserNameField.setFont(CyderFonts.SEGOE_20);
        newUserNameField.setBorder(new LineBorder(Color.black));
        newUserNameField.addKeyListener(newUserNameFieldListener);
        newUserNameField.setBorder(BORDER);
        newUserNameField.setSize(240, 40);

        if (!defaultCyderUserAlreadyExists()) {
            newUserNameField.setText(OsUtil.getOsUsername());
        }

        JLabel passwordLabel = new JLabel("Password: ", SwingConstants.CENTER);
        passwordLabel.setFont(CyderFonts.SEGOE_20);
        passwordLabel.setForeground(CyderColors.navy);
        passwordLabel.setSize(240, 30);

        informationLabel = new JLabel("Passwords match", SwingConstants.CENTER);

        newUserPasswordField = new CyderPasswordField();
        newUserPasswordField.addKeyListener(passwordFieldKeyListener);
        newUserPasswordField.setSize(240, 40);
        newUserPasswordField.setToolTipText("Shift shows password");

        JLabel passwordLabelConf = new JLabel("Confirm Password: ", SwingConstants.CENTER);
        passwordLabelConf.setFont(CyderFonts.SEGOE_20);
        passwordLabelConf.setForeground(CyderColors.navy);
        passwordLabelConf.setSize(240, 30);

        newUserPasswordConfirmationField = new CyderPasswordField();
        newUserPasswordConfirmationField.addKeyListener(passwordFieldKeyListener);
        newUserPasswordConfirmationField.setSize(240, 40);
        newUserPasswordConfirmationField.setToolTipText("Shift shows password");

        informationLabel.setFont(CyderFonts.SEGOE_20);
        informationLabel.setForeground(CyderColors.regularGreen);
        informationLabel.setSize(300, 30);

        chooseBackgroundButton = new CyderButton("Choose Background");
        chooseBackgroundButton.setToolTipText("Choose a background for the console");
        chooseBackgroundButton.setFont(CyderFonts.SEGOE_20);
        chooseBackgroundButton.setBackground(CyderColors.regularRed);
        chooseBackgroundButton.addMouseListener(chooseBackgroundButtonMouseListener);
        chooseBackgroundButton.addActionListener(chooseBackgroundButtonActionListener);

        chooseBackgroundButton.setBorder(BORDER);
        chooseBackgroundButton.setSize(240, 40);

        createNewUserButton.setFont(CyderFonts.SEGOE_20);
        createNewUserButton.setBackground(CyderColors.regularRed);
        createNewUserButton.setToolTipText("Create");
        createNewUserButton.addActionListener(createNewUserActionListener);
        createNewUserButton.setBorder(BORDER);
        createNewUserButton.setFont(CyderFonts.SEGOE_20);
        createNewUserButton.setSize(240, 40);

        CyderPartitionedLayout cyderPartitionedLayout = new CyderPartitionedLayout();

        cyderPartitionedLayout.spacer(10);
        cyderPartitionedLayout.addComponent(nameLabel, 5);
        cyderPartitionedLayout.addComponent(newUserNameField, 8);
        cyderPartitionedLayout.spacer(5);
        cyderPartitionedLayout.addComponent(passwordLabel, 5);
        cyderPartitionedLayout.addComponent(newUserPasswordField, 8);
        cyderPartitionedLayout.spacer(5);
        cyderPartitionedLayout.addComponent(passwordLabelConf, 5);
        cyderPartitionedLayout.addComponent(newUserPasswordConfirmationField, 8);
        cyderPartitionedLayout.spacer(5);
        cyderPartitionedLayout.addComponent(informationLabel, 5);
        cyderPartitionedLayout.spacer(5);
        cyderPartitionedLayout.addComponent(chooseBackgroundButton, 10);
        cyderPartitionedLayout.addComponent(createNewUserButton, 10);

        createUserFrame.setCyderLayout(cyderPartitionedLayout);
        createUserFrame.finalizeAndShow();
        updateInformationLabel();

        newUserNameField.requestFocus();
    }

    /**
     * The key listener for the create user button.
     */
    private static final KeyListener newUserNameFieldListener = new KeyListener() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            createNewUserButton.setText("Create " + newUserNameField.getText().trim());
            updateInformationLabel();
        }

        @Override
        public void keyReleased(java.awt.event.KeyEvent e) {
            createNewUserButton.setText("Create " + newUserNameField.getText().trim());
            updateInformationLabel();
        }

        @Override
        public void keyTyped(java.awt.event.KeyEvent e) {
            createNewUserButton.setText("Create " + newUserNameField.getText().trim());
            updateInformationLabel();
        }
    };

    /**
     * Whether the currently entered new user credentials are valid
     * and can be used for construction of a new user.
     */
    private static boolean validCredentials = false;

    /**
     * Updates the information label based off of the current field values.
     */
    private static void updateInformationLabel() {
        informationLabel.setForeground(CyderColors.regularRed);
        validCredentials = false;

        String name = newUserNameField.getText().trim();
        char[] password = newUserPasswordField.getPassword();
        char[] passwordConfirmation = newUserPasswordConfirmationField.getPassword();

        UserUtil.Validation nameValid = UserUtil.validateUsername(name);
        UserUtil.Validation passwordValid = UserUtil.validatePassword(password, passwordConfirmation);

        if (!nameValid.valid()) {
            informationLabel.setText(nameValid.message());
        } else {
            informationLabel.setText(passwordValid.message());

            if (passwordValid.valid()) {
                informationLabel.setForeground(CyderColors.regularGreen);
                validCredentials = true;
            }
        }

        Arrays.fill(password, '\0');
        Arrays.fill(passwordConfirmation, '\0');
    }

    /**
     * The key listener for password fields to update the information label.
     */
    private static final KeyListener passwordFieldKeyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            updateInformationLabel();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            updateInformationLabel();
        }

        @Override
        public void keyTyped(KeyEvent e) {
            updateInformationLabel();
        }
    };

    /**
     * The last generated uuid so that in the event of an account creation
     * failure, we can delete the created user file.
     */
    private static String lastGeneratedUuid;

    /**
     * The action user for the create user button.
     */
    private static final ActionListener createNewUserActionListener = e -> {
        try {
            String name = newUserNameField.getText().trim();
            char[] password = newUserPasswordField.getPassword();

            if (!attemptToCreateUser(name, password)) {
                if (lastGeneratedUuid != null) {
                    OsUtil.deleteFile(OsUtil.buildFile(
                            Dynamic.PATH, Dynamic.USERS.getDirectoryName(), lastGeneratedUuid));
                }
            } else {
                createUserFrame.dispose();

                InformHandler.inform(new InformHandler.Builder("The new user \"" + name
                        + "\" has been created successfully.").setTitle("User Created")
                        .setRelativeTo(CyderFrame.getDominantFrame()));

                if (onlyOneUser()) {
                    LoginHandler.getLoginFrame().dispose();
                    LoginHandler.recognize(name, SecurityUtil.toHexString(
                            SecurityUtil.getSha256(password)), false);
                }
            }

            Arrays.fill(password, '\0');
        } catch (Exception ex) {
            ExceptionHandler.silentHandle(ex);
        }
    };

    /**
     * Returns whether only one valid user exists within Cyder.
     *
     * @return whether only one valid user exists within Cyder
     */
    private static boolean onlyOneUser() {
        File[] userFiles = OsUtil.buildFile(
                Dynamic.PATH, Dynamic.USERS.getDirectoryName()).listFiles();

        return userFiles != null && userFiles.length == 1;
    }

    /**
     * The action listener for the choose background button.
     */
    private static final ActionListener chooseBackgroundButtonActionListener = e -> chooseBackground();

    /**
     * The mouse listener for changing the text of the choose background button.
     */
    private static final MouseAdapter chooseBackgroundButtonMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            try {
                if (newUserBackgroundFile != null) {
                    chooseBackgroundButton.setText(newUserBackgroundFile.getName());
                } else {
                    chooseBackgroundButton.setText("No Background");
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            try {
                if (newUserBackgroundFile != null) {
                    chooseBackgroundButton.setText(newUserBackgroundFile.getName());
                } else {
                    chooseBackgroundButton.setText("Choose Background");
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }
    };

    /**
     * Returns whether the user with the user name of {@link  OsUtil#getOsUsername()} exists.
     *
     * @return whether the user with the user name of {@link  OsUtil#getOsUsername()} exists
     */
    private static boolean defaultCyderUserAlreadyExists() {
        String osUsername = OsUtil.getOsUsername();

        for (File userJson : UserUtil.getUserJsons()) {
            User user = UserUtil.extractUser(userJson);
            if (user.getName().equalsIgnoreCase(osUsername)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Initializes the new user's background.
     */
    private static void chooseBackground() {
        CyderThreadRunner.submit(() -> {
            try {
                File temp = GetterUtil.getInstance()
                        .getFile(new GetterUtil.Builder("Choose new user's background file")
                                .setRelativeTo(CyderFrame.getDominantFrame()));
                if (temp != null) {
                    newUserBackgroundFile = temp;
                    chooseBackgroundButton.setText(newUserBackgroundFile.getName());
                }

                if (temp == null || !FileUtil.isSupportedImageExtension(temp)) {
                    newUserBackgroundFile = null;
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }, "wait thread for GetterUtil().getFile()");
    }

    /**
     * Creates the default screen stat object based on the current monitor and the provided default background.
     *
     * @param background the default background the user will be using
     * @return the default screen stat
     */
    private static ScreenStat createDefaultScreenStat(BufferedImage background) {
        int monitorNum = -1;

        int w = background.getWidth();
        int h = background.getHeight();

        int x = 0;
        int y = 0;

        if (createUserFrame != null) {
            Rectangle monitorBounds = createUserFrame.getMonitorBounds();
            int screenWidth = (int) monitorBounds.getWidth();
            int screenHeight = (int) monitorBounds.getHeight();

            GraphicsConfiguration gc = createUserFrame.getGraphicsConfiguration();
            String monitorID = gc.getDevice().getIDstring()
                    .replaceAll(CyderRegexPatterns.nonNumberRegex, "");

            try {
                monitorNum = Integer.parseInt(monitorID);

                int monitorWidth = (int) gc.getBounds().getWidth();
                int monitorHeight = (int) gc.getBounds().getHeight();

                int monitorX = (int) gc.getBounds().getX();
                int monitorY = (int) gc.getBounds().getY();

                x = monitorX + (monitorWidth - w) / 2;
                y = monitorY + (monitorHeight - h) / 2;
            } catch (Exception e) {
                x = (screenWidth - w) / 2;
                y = (screenHeight - h) / 2;
            }
        }

        return new ScreenStat(x, y, w, h, monitorNum, false, Direction.TOP);
    }

    /**
     * Attempts to create a user based off of the provided necessary initial data.
     *
     * @param name     the requested name of the new user
     * @param password the password of the new user
     * @return whether the user was created
     */
    private static boolean attemptToCreateUser(String name, char[] password) {
        if (!validCredentials) {
            createUserFrame.toast(informationLabel.getText());
            return false;
        }

        String uuid = SecurityUtil.generateUuidForUser();
        lastGeneratedUuid = uuid;

        if (!OsUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(), uuid).mkdir()) {
            createUserFrame.toast("Failed to create user folder");
            return false;
        }

        for (UserFile userFile : UserFile.values()) {
            File makeMe = OsUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(), uuid, userFile.getName());

            if (userFile.isFile()) {
                try {
                    if (!makeMe.createNewFile()) {
                        createUserFrame.toast("Failed to create file: " + userFile.getName());
                        return false;
                    }
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                    createUserFrame.toast("Failed to create file");
                    return false;
                }
            } else {
                if (!makeMe.mkdir()) {
                    createUserFrame.toast("Failed to create folder: " + userFile.getName());
                    return false;
                }
            }
        }

        if (newUserBackgroundFile == null) {
            newUserBackgroundFile = UserUtil.createDefaultBackground(uuid);
        }

        try {
            File destination = OsUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                    uuid, UserFile.BACKGROUNDS.getName(), newUserBackgroundFile.getName());
            Files.copy(Paths.get(newUserBackgroundFile.getAbsolutePath()), destination.toPath());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            createUserFrame.toast("Failed to create default background");
            return false;
        }

        User user = new User();
        UserUtil.resetUser(user);
        user.setName(name);
        user.setPass(SecurityUtil.doubleHashToHex(password));

        BufferedImage background;
        try {
            background = ImageIO.read(newUserBackgroundFile);
        } catch (Exception e) {
            background = UserUtil.DEFAULT_USER_SOLID_COLOR_BACKGROUND;
        }

        user.setScreenStat(createDefaultScreenStat(background));
        user.setExecutables(new LinkedList<>());

        UserUtil.setUserData(OsUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(), uuid, UserFile.USERDATA.getName()), user);

        return true;
    }
}
