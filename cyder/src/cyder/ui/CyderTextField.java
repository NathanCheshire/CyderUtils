package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.Logger;
import cyder.utilities.StringUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Cyder implementation of a text field.
 */
public class CyderTextField extends JTextField {
    /**
     * The character limit.
     */
    private int limit;

    /**
     * The background color of the field.
     */
    private Color backgroundColor = CyderColors.vanila;

    /**
     * The regex to restrict entered text to.
     */
    private String keyEventRegexMatcher;

    /**
     * The pattern to use for matching against keyEventRegexMatcher.
     */
    private Pattern keyEventRegexPattern;

    /**
     * Constructs a new Cyder TextField object with no character limit.
     */
    public CyderTextField() {
        this(0);
    }

    /**
     * Constructs a new Cyder TextField object.
     *
     * @param charLimit the character limit for the text field.
     */
    public CyderTextField(int charLimit) {
        super(charLimit == 0 ? Integer.MAX_VALUE : charLimit);

        if (charLimit == 0) {
            charLimit = Integer.MAX_VALUE;
        }

        limit = charLimit;
        keyEventRegexMatcher = null;

        addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                if (getText().length() > limit) {
                    setText(getText().substring(0, getText().length() - 1));
                    Toolkit.getDefaultToolkit().beep();
                } else if (keyEventRegexMatcher != null && !keyEventRegexMatcher.isEmpty()
                        && getText() != null && !getText().isEmpty()) {
                    if (!currentTextMatchesPattern()) {
                        setText(getText().substring(0, getText().length() - 1));
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }

            public void keyPressed(KeyEvent evt) {
                if (getText().length() > limit) {
                    setText(getText().substring(0, getText().length() - 1));
                    Toolkit.getDefaultToolkit().beep();
                } else if (keyEventRegexMatcher != null && !keyEventRegexMatcher.isEmpty()
                        && getText() != null && !getText().isEmpty()) {
                    if (!currentTextMatchesPattern()) {
                        setText(getText().substring(0, getText().length() - 1));
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }

            public void keyReleased(KeyEvent evt) {
                if (getText().length() > limit) {
                    setText(getText().substring(0, getText().length() - 1));
                    Toolkit.getDefaultToolkit().beep();
                } else if (keyEventRegexMatcher != null && !keyEventRegexMatcher.isEmpty()
                        && getText() != null && !getText().isEmpty()) {
                    if (!currentTextMatchesPattern()) {
                        setText(getText().substring(0, getText().length() - 1));
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.UI_ACTION, e.getComponent());
            }
        });

        setBackground(backgroundColor);
        setSelectionColor(CyderColors.selectionColor);
        setFont(CyderFonts.segoe20);
        setForeground(CyderColors.navy);
        setCaretColor(CyderColors.navy);
        setCaret(new CyderCaret(CyderColors.navy));
        setBorder(new LineBorder(CyderColors.navy, 5, false));
        setOpaque(true);

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns whether the current text matches the currently set pattern.
     *
     * @return whether the current text matches the currently set pattern
     */
    private boolean currentTextMatchesPattern() {
        checkNotNull(getText());
        checkNotNull(keyEventRegexMatcher);
        checkNotNull(keyEventRegexPattern);

        return keyEventRegexPattern.matcher(getText()).matches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBackground(Color newBackgroundColor) {
        super.setBackground(newBackgroundColor);
        backgroundColor = newBackgroundColor;
        setOpaque(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getBackground() {
        return backgroundColor;
    }

    /**
     * Sets the regex to restrict the input to.
     * Note that this is applied every key event.
     * To validate a pattern which may be valid and not complete until
     * some time after the user initially started typing, you'll need
     * to do the validation on your own by grabbing the text and matching
     * it before using the input.
     *
     * @param regex the regex to restrict the input to
     */
    public void setKeyEventRegexMatcher(String regex) {
        keyEventRegexMatcher = regex;
        keyEventRegexPattern = Pattern.compile(keyEventRegexMatcher);
    }

    /**
     * Removes the regex from the text field.
     */
    public void removeKeyEventRegexMatcher() {
        keyEventRegexMatcher = null;
        keyEventRegexPattern = null;
    }

    /**
     * Returns the regex matcher for the text field.
     *
     * @return the regex matcher for the text field
     */
    public String getKeyEventRegexMatcher() {
        return keyEventRegexMatcher;
    }

    /**
     * Returns the compiled pattern for the text field based on the keyEventRegexMatcher.
     *
     * @return the compiled pattern for the text field based on the keyEventRegexMatcher
     */
    public Pattern getKeyEventRegexPattern() {
        return keyEventRegexPattern;
    }

    /**
     * Sets the character limit. Any chars outside of the limit are trimmed away.
     *
     * @param limit the character limit
     */
    public void setCharLimit(int limit) {
        this.limit = limit;

        if (getText().length() > limit) {
            setText(getText().substring(0, limit + 1));
        }
    }

    /**
     * Returns the character limit for the text field.
     *
     * @return the character limit for the text field
     */
    public int getCharLimit() {
        return limit;
    }

    /**
     * The text field's LineBorder if applicable.
     */
    private LineBorder lineBorder;

    /**
     * The color used for valid form data.
     */
    private final Color validFormDataColor = CyderColors.regularGreen;

    /**
     * The data used for invalid form data.
     */
    private final Color invalidFormDataColor = CyderColors.regularRed;

    /**
     * {@inheritDoc}
     * <p>
     * If line borders are used, then the invalid
     * and valid form data methods may be called.
     */
    @Override
    public void setBorder(Border border) {
        if (border instanceof LineBorder) {
            lineBorder = (LineBorder) border;
        } else {
            lineBorder = null;
        }

        // no need to cast since instanceof LineBorder is ensured
        super.setBorder(border);
    }

    /**
     * Sets the border to a green color to let the user know the provided input is valid.
     */
    public void informValidData() {
        checkArgument(lineBorder != null);

        setBorder(new LineBorder(validFormDataColor,
                lineBorder.getThickness(), lineBorder.getRoundedCorners()));
    }

    /**
     * Sets the border to a red color to let the user know the provided input is invalid.
     */
    public void informInvalidData() {
        checkArgument(lineBorder != null);

        setBorder(new LineBorder(invalidFormDataColor,
                lineBorder.getThickness(), lineBorder.getRoundedCorners()));
    }

    /**
     * The key listener used to auto-capitalize the first letter of the field.
     */
    private final KeyAdapter autoCapitalizerListener = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            if (getText().length() == 1) {
                setText(getText().toUpperCase());
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (getText().length() == 1) {
                setText(getText().toUpperCase());
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (getText().length() == 1) {
                setText(getText().toUpperCase());
            }
        }
    };

    /**
     * Whether auto capitalization is on.
     */
    private boolean autoCapitalize;

    /**
     * Sets whether to capitalize the first letter of the form.
     *
     * @param enable whether to capitalize the first letter of the form
     */
    public void setAutoCapitalization(boolean enable) {
        if (enable && !autoCapitalize) {
            addKeyListener(autoCapitalizerListener);
        } else {
            removeKeyListener(autoCapitalizerListener);
        }

        autoCapitalize = enable;
    }

    /**
     * Adds auto capitalization to the provided text field.
     *
     * @param tf the text field to add auto capitalization to
     */
    public static void addAutoCapitalizationAdapter(JTextField tf) {
        tf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (tf.getText().length() == 1) {
                    tf.setText(tf.getText().toUpperCase());
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (tf.getText().length() == 1) {
                    tf.setText(tf.getText().toUpperCase());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (tf.getText().length() == 1) {
                    tf.setText(tf.getText().toUpperCase());
                }
            }
        });
    }

    /**
     * Returns the text field text but trimmed and with multiple occurences
     * of whitespace in the String replaced with one whitespace char.
     *
     * @return the text with trimming performed
     */
    public String getTrimmedText() {
        return StringUtil.getTrimmedText(getText());
    }
}
