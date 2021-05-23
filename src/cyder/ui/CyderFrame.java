package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.enums.ArrowDirection;
import cyder.enums.StartDirection;
import cyder.enums.TitlePosition;
import cyder.enums.VanishDirection;
import cyder.handler.ErrorHandler;
import cyder.utilities.ImageUtil;
import cyder.utilities.SystemUtil;
import cyder.widgets.GenericInform;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class CyderFrame extends JFrame {

    private TitlePosition titlePosition = TitlePosition.LEFT;

    private int width;
    private int height;

    private ImageIcon background;

    private DragLabel dl;

    private JLabel titleLabel;
    private JLabel contentLabel;

    private Color backgroundColor = CyderColors.vanila;

    /**
     * returns an instance of a cyderframe which extends JFrame with the specified width and height
     * and a drag label with minimize and close buttons
     * the specified ImageIcon is used for the background (you can enable resizing and rescaling of the image should you choose)
     *
     * @param width  - the specified width of the cyder frame
     * @param height - the specified height of the cyder frame
     * @param background - the specified background image. You can choose to leave the image in the same place upon
     *                   frame resizing events or you can configure the frame instance to rescale the original background
     *                   image to fit to the new frame dimensions.
     */
    public CyderFrame(int width, int height, ImageIcon background) {
        this.width = width;
        this.height = height;
        this.background = background;
        setSize(new Dimension(width, height));

        setResizable(false);
        setUndecorated(true);
        setBackground(backgroundColor);
        setIconImage(SystemUtil.getCyderIcon().getImage());

        contentLabel = new JLabel();
        contentLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        contentLabel.setIcon(background);
        currentOrigIcon = background;

        setContentPane(contentLabel);

        dl = new DragLabel(width, 30, this);
        dl.setBounds(0, 0, width, 30);
        contentLabel.add(dl);

        titleLabel = new JLabel("");
        titleLabel.setFont(new Font("Agency FB",Font.BOLD,22));
        titleLabel.setForeground(CyderColors.vanila);

        dl.add(titleLabel);
    }

    /**
     * returns an instance of a cyderframe which extends JFrame with the specified width and height
     * and a drag label with minimize and close buttons
     *
     * @param width  - the specified width of the cyder frame
     * @param height - the specified height of the cyder frame
     */
    public CyderFrame(int width, int height) {
        this(width,height, ImageUtil.imageIconFromColor(CyderColors.vanila));
    }

    /**
     * returns an instance of a cyderframe which extends JFrame with
     * a width of 400 and a height of 400 and a drag label with minimize and close buttons
     */
    public CyderFrame() {
        this(400,400);
    }

    /**
     * This method will change the title position to the specified value. If the frame is visible to the user,
     * we will animate the change via a smooth slide transition
     * @param titlePosition the position for the title to be: left, center
     */
    public void setTitlePosition(TitlePosition titlePosition) {
        if (titlePosition == null || this.titlePosition == null)
            return;

        boolean different = titlePosition != this.titlePosition;
        this.titlePosition = titlePosition;
        long timeout = 2;

        if (different && isVisible()) {
            if (titlePosition != TitlePosition.CENTER) {
                new Thread(() -> {
                    for (int i = (getDragLabel().getWidth() / 2) - (getMinWidth(titleLabel.getText()) / 2); i > 4; i--) {
                        titleLabel.setLocation(i, 2);

                        try {
                            Thread.sleep(timeout);
                        } catch (Exception e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }).start();
            } else {
                new Thread(() -> {
                    for (int i = 5; i < (getDragLabel().getWidth() / 2) - (getMinWidth(titleLabel.getText()) / 2) + 1; i++) {
                        titleLabel.setLocation(i, 2);

                        try {
                            Thread.sleep(timeout);
                        } catch (Exception e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }).start();
            }
        }
    }


    /**
     * Getter for the title position
     * @return - position representing the title position
     */
    public TitlePosition getTitlePosition() {
        return this.titlePosition;
    }


    private boolean paintSuperTitle = true;

    /**
     * Determines whether or not to paint the windowed title. The CyderFrame label title is always painted.
     * @param enable - boolean variable of your chosen value for paintSuperTitle
     */
    public void setPaintSuperTitle(boolean enable) {
        paintSuperTitle = enable;
    }

    /**
     * Returns the value of paintSuperTitle which determines whether ot not the windowed title is painted.
     * @return - boolean describing the value of paintSuperTitle
     */
    public boolean getPaintSuperTitle() {
        return paintSuperTitle;
    }

    /**
     * Set the title of the label painted on the drag label of the CyderFrame instance. You can also configure the instance
     * to paint the windowed title as well.
     * @param title - the String representing the chosen CyderFrame title
     */
    @Override
    public void setTitle(String title) {
        super.setTitle(paintSuperTitle ? title : "");

        titleLabel.setText(title);

        switch (titlePosition) {
            case CENTER:
                titleLabel.setBounds((getDragLabel().getWidth() / 2) - (getMinWidth(title) / 2), 2, getMinWidth(title), 25);
                break;

            default:
                titleLabel.setBounds(5, 2, getMinWidth(title), 25);
        }
    }

    /**
     * Returns the minimum width required for the given String using the font of the title label.
     * @param title - the text you want to determine the width of
     * @return - an interger value determining the minimum width of a string of text (10 is added to avoid ... bug)
     */
    private int getMinWidth(String title) {
        Font notificationFont = titleLabel.getFont();
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) notificationFont.getStringBounds(title, frc).getWidth() + 10;
    }

    /**
     * Returns the minimum width required for the given String using the given font.
     * @param title - the text you want to determine the width of
     * @return - an interger value determining the minimum width of a string of text (10 is added to avoid ... bug)
     */
    public static int getMinWidth(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getWidth() + 10;
    }

    /**
     * Returns the minimum height required for the given String using the given font.
     * @param title - the text you want to determine the height of
     * @return - an interger value determining the minimum height of a string of text (10 is added to avoid ... bug)
     */
    public static int getMinHeight(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getHeight() + 10;
    }

    /**
     * This method is to be used for a quick notify. view direction is five seconds
     * @param htmlText - the text you want to notify on the callilng from
     */
    public void notify(String htmlText) {
        notify(htmlText, 5000, ArrowDirection.TOP);
    }

    /**
     * This method is to be used for a more controled notify. You may choose the duration and the arrow direction
     * @param htmltext - the text you want to display (may include HTML tags)
     * @param viewDuration - time in ms that the notification should stay on screen
     * @param direction - the enter and vanish direction for the notification
     */
    public void notify(String htmltext, int viewDuration, ArrowDirection direction) {
        Notification frameNotification = new Notification();

        StartDirection startDir;
        VanishDirection vanishDir;

        switch (direction) {
            case LEFT:
                startDir = StartDirection.LEFT;
                vanishDir = VanishDirection.LEFT;
                break;
            case RIGHT:
                startDir = StartDirection.RIGHT;
                vanishDir = VanishDirection.RIGHT;
                break;
            case BOTTOM:
                startDir = StartDirection.BOTTOM;
                vanishDir = VanishDirection.BOTTOM;
                break;
            default:
                startDir = StartDirection.TOP;
                vanishDir = VanishDirection.TOP;
                break;
        }

        notify(htmltext, viewDuration, direction, startDir, vanishDir);
    }

    /**
     * Full control over the notification function of a {@link CyderFrame}.
     * See {@link CyderFrame#notify(String, int, ArrowDirection)} for a simpler notify function
     * @param htmltext - the text you want to display (may include HTML tags)
     * @param viewDuration - the time in ms the notification should be visible for
     * @param arrowDir - the direction of the arrow on the notification
     * @param startDir - the enter direction of the notification
     * @param vanishDir - the exit direction of the notification
     */
    public void notify(String htmltext, int viewDuration, ArrowDirection arrowDir, StartDirection startDir, VanishDirection vanishDir) {
        //todo height and width calculations are a bodge, make them better
        //todo anywhere there's an affine transform for bound caluclations, fix this, try using the method ot get a rectangle2D

        //init notification object
        Notification frameNotification = new Notification();

        //set the arrow direction
        frameNotification.setArrow(arrowDir);

        //create text label to go on top of notification label
        JLabel text = new JLabel();
        //use html so that it can line break when we need it to
        text.setText("<html>" + htmltext + "</html>");

        //start of font width and height calculation
        int w = 0;
        Font notificationFont = CyderFonts.weatherFontSmall;
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);

        //parse away all html
        String parsedHTML = Jsoup.clean(htmltext, Whitelist.none());

        //get overall width
        w = (int) notificationFont.getStringBounds(parsedHTML, frc).getWidth() + 10;

        //figure out the height of a single line and set that as the height increment
        int heightIncrement = (int) notificationFont.getStringBounds(parsedHTML, frc).getHeight() + 10;
        int h = heightIncrement;
        int lastIndex = 0;

        //rectangle to keep needed text area consistent
        int area = h * w;

        while (w * 1.25 > this.getContentPane().getWidth()) {
            //decrease width by 25% and increase height accordingly to keep area constant
            h += heightIncrement;
            w = area / h;
        }

        frameNotification.setWidth(w);
        frameNotification.setHeight(h);
        text.setBounds(14,10,w + 14,h + 10);

        text.setFont(notificationFont);
        text.setForeground(CyderColors.navy);
        frameNotification.add(text);

        if (startDir == StartDirection.LEFT)
            frameNotification.setBounds(0, 30, w * 2, h * 2);
        else if (startDir == StartDirection.RIGHT)
            frameNotification.setBounds(this.getContentPane().getWidth() - (w + 30), 32, w * 2, h * 2);
        else
            frameNotification.setBounds(this.getContentPane().getWidth() / 2 - (w / 2) - 14, 32, w * 2, h * 2);

        contentLabel.add(frameNotification);
        this.getContentPane().repaint();

        frameNotification.appear(startDir, this.getContentPane());
        frameNotification.vanish(vanishDir, this.getContentPane(), viewDuration);
    }

    /**
     * Getter for the drag label associated with this CyderFrame instance. Used for frame resizing.
     * @return - The associated DragLabel
     */
    public DragLabel getDragLabel() {
        return dl;
    }

    /**
     * Pops open a window relative to this with the provided text
     * @param text - the String you wish to display
     * @param title - The title of the CyderFrame which will be opened to display the text
     */
    public void inform(String text, String title) {
        GenericInform.informRelative(text, title, this);
    }

    /**
     * Animates the window from offscreen top to the center of the screen.
     */
    public void enterAnimation() {
        if (this == null)
            return;

        dl.disableDragging();

        this.setVisible(false);
        this.setLocationRelativeTo(null);

        int to = this.getY();
        this.setLocation(this.getX(), 0 - this.getHeight());

        this.setVisible(true);

        for (int i = 0 - this.getHeight(); i < to; i += 15) {
            this.setLocation(this.getX(), i);
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }

        this.setLocationRelativeTo(null);

        dl.enableDragging();
    }

    /**
     * Close animation moves the window up until the CyderFrame is off screen. this.dispose() is then invoked
     * perhaps you might re-write this to override dispose so that closeanimation is always called and you can
     * simply dispose of a frame like normal.
     */
    public void closeAnimation() {
        if (this == null)
            return;

        dl.disableDragging();

        try {
            if (this != null && this.isVisible()) {
                Point point = this.getLocationOnScreen();
                int x = (int) point.getX();
                int y = (int) point.getY();

                for (int i = y; i >= 0 - this.getHeight(); i -= 15) {
                    Thread.sleep(1);
                    this.setLocation(x, i);
                }

                this.dispose();
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Moves the window down until it is off screen before setting the state to ICONIFIED.
     * The original position of the frame will be remembered and set when the window is deiconified.
     */
    public void minimizeAnimation() {
        if (this == null)
            return;

        dl.disableDragging();

        Point point = this.getLocationOnScreen();
        int x = (int) point.getX();
        int y = (int) point.getY();

        try {
            for (int i = y; i <= SystemUtil.getScreenHeight(); i += 15) {
                Thread.sleep(1);
                this.setLocation(x, i);
            }

            this.setState(JFrame.ICONIFIED);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        dl.enableDragging();
    }

    /**
     * Allow or disable moving the window.
     * @param relocatable - the boolean value determining if the window may be repositioned
     */
    public void setRelocatable(boolean relocatable) {
        if (relocatable)
            dl.enableDragging();
        else
            dl.disableDragging();
    }

    /**
     * moves the frame around the user's monitor before returning to the initial location.
     */
    public void dance() {
        Thread DanceThread = new Thread(() -> {
            try {
                int delay = 10;

                setAlwaysOnTop(true);
                setLocationRelativeTo(null);

                Point point = getLocationOnScreen();

                int x = (int) point.getX();
                int y = (int) point.getY();

                int restoreX = x;
                int restoreY = y;

                for (int i = y; i <= (-getHeight()); i += 10) {
                    Thread.sleep(delay);
                    setLocation(x, i);
                }

                setLocation(SystemUtil.getScreenWidth() / 2 - getWidth() / 2, SystemUtil.getScreenHeight() - getHeight());
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = x; i <= (SystemUtil.getScreenWidth() - getWidth()); i += 10) {
                    Thread.sleep(delay);
                    setLocation(i, y);
                }

                setLocation(SystemUtil.getScreenWidth() - getWidth(), SystemUtil.getScreenHeight() - getHeight());
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = y; i >= -10; i -= 10) {
                    Thread.sleep(delay);
                    setLocation(x, i);
                }

                setLocation(SystemUtil.getScreenWidth() - getWidth(), 0);
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = x; i >= 10; i -= 10) {
                    Thread.sleep(delay);
                    setLocation(i, y);
                }

                setLocation(0, 0);
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = y; i <= (SystemUtil.getScreenHeight() - getHeight()); i += 10) {
                    Thread.sleep(delay);
                    setLocation(x, i);
                }

                setLocation(0, SystemUtil.getScreenHeight() - getHeight());
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = x; i <= (SystemUtil.getScreenWidth() / 2 - getWidth() / 2); i += 10) {
                    Thread.sleep(delay);
                    setLocation(i, y);
                }

                setLocation(SystemUtil.getScreenWidth() / 2 - getWidth() / 2, SystemUtil.getScreenHeight() - getHeight());
                int acc = getY();
                x = getX();

                while (getY() >= (SystemUtil.getScreenHeight() / 2 - getHeight() / 2)) {
                    Thread.sleep(delay);
                    acc -= 10;
                    setLocation(x, acc);
                }

                setLocation(restoreX, restoreY);
                setAlwaysOnTop(false);

            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        });

        DanceThread.start();
    }

    /**
     * transforms the content pane by an incremental angle of 2 degrees emulating Google's barrel roll easter egg
     */
    public void barrelRoll() {
        setBackground(CyderColors.navy);

        ImageIcon masterIcon = (ImageIcon) ((JLabel) getContentPane()).getIcon();
        BufferedImage master = ImageUtil.getBi(masterIcon);

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
                rotated = ImageUtil.rotateImageByDegrees(master, angle);
                ((JLabel) getContentPane()).setIcon(new ImageIcon(rotated));
            }
        });
        timer.start();
    }

    //similar to googling "askew"
    public void askew(int degrees) {
        ((JLabel) (this.getContentPane())).setIcon(new ImageIcon(ImageUtil.rotateImageByDegrees(
                ImageUtil.getRotatedImage(
                        ConsoleFrame.getCurrentBackgroundFile().getAbsolutePath(),
                        ConsoleFrame.getConsoleDirection()), degrees)));
    }

    /**
     * Sets the frame bounds and also changes the underlying drag label's bounds which is why this method is overridden.
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        if (getDragLabel() != null) {
            getDragLabel().setWidth(width);
            setTitle(getTitle());
        }
    }

    //getters and setters for min size, max size, and snap size

    private Dimension minimumSize = new Dimension(200, 200);
    private Dimension maximumSize = new Dimension(800, 800);
    private Dimension snapSize = new Dimension(1, 1);

    /**
     * Sets the minimum window size if resizing is allowed.
     * @param minSize - the Dimension of the minimum allowed size
     */
    public void setMinimumSize(Dimension minSize) {
        this.minimumSize = minSize;
        cr.setMinimumSize(minimumSize);
    }

    /**
     * Sets the maximum window size if resizing is allowed.
     * @param maxSize - the Dimension of the minimum allowed size
     */
    public void setMaximumSize(Dimension maxSize) {
        this.maximumSize = maxSize;
        cr.setMaximumSize(maximumSize);
    }

    /**
     * Sets the snap size for the window if resizing is allowed.
     * @param snap - the dimension of the snap size
     */
    public void setSnapSize(Dimension snap) {
        this.snapSize = snap;
        cr.setSnapSize(snapSize);
    }

    /**
     * @return - the minimum window size if resizing is allowed
     */
    public Dimension getMinimumSize() {
        return minimumSize;
    }

    /**
     * @return - the maximum window size if resizing is allowed
     */
    public Dimension getMaximumSize() {
        return maximumSize;
    }

    /**
     * @return - the snap size for the window if resizing is allowed
     */
    public Dimension getSnapSize() {
        return snapSize;
    }

    ComponentResizer cr;

    /**
     * Choose to allow/disable background image reisizing if window resizing is allowed.
     * @param allowed - the value determining background resizing
     */
    public void setBackgroundResizing(Boolean allowed) {
        cr.setBackgroundRefreshOnResize(allowed);
    }

    /**
     * This method should be called first when attempting to allow resizing of a frame.
     * Procedural calls: init component resizer, set resizing to true, set min, max, and snap sizes to default.
     */
    public void initResizing() {
        cr = new ComponentResizer();
        cr.registerComponent(this);
        cr.setResizing(true);
        cr.setMinimumSize(getMinimumSize());
        cr.setMaximumSize(getMaximumSize());
        cr.setSnapSize(getSnapSize());
    }

    /**
     * @param allow - sets/disables resizing of the frame.
     */
    public void allowResizing(Boolean allow) {
        cr.setResizing(allow);
    }

    ImageIcon currentOrigIcon;

    /**
     * Refresh the background in the event of a frame size change or background image change.
     */
    public void refreshBackground() {
        contentLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
                .getScaledInstance(contentLabel.getWidth(), contentLabel.getHeight(), Image.SCALE_DEFAULT)));
    }

    /**
     * Set the background to a new icon and refresh the frame.
     * @param icon - the ImageIcon you want the frame background to be
     */
    public void setBackground(ImageIcon icon) {
        currentOrigIcon = icon;
        contentLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
                .getScaledInstance(contentLabel.getWidth(), contentLabel.getHeight(), Image.SCALE_DEFAULT)));
    }

    /**
     * Sets the background color of the Frame's content pane.
     * @param background - the Color object value of the content pane's desired background
     */
    @Override
    public void setBackground(Color background) {
        super.setBackground(background);
        backgroundColor = background;
        this.repaint();
    }

    /**
     * Returns the background color of the contentPane.
     * @return - Color object of the background color
     */
    @Override
    public Color getBackground() {
        return this.backgroundColor;
    }
}