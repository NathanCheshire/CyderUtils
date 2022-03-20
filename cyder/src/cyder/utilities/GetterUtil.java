package cyder.utilities;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.enums.LoggerTag;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.utilities.objects.BoundsString;
import cyder.utilities.objects.GetterBuilder;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A getter utility for getting strings, confirmations, files, etc. from the user.
 */
public class GetterUtil {
    /**
     * To obtain an instance, use {@link GetterUtil#getInstance()}.
     */
    private GetterUtil() {
        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    /**
     * Returns a GetterUtil instance.
     *
     * @return a GetterUtil instance
     */
    public static GetterUtil getInstance() {
        return new GetterUtil();
    }

    private static final int getStringWidth = 400;
    private static final int getStringHeight = 170;
    private static final int getStringYPadding = 10;
    private static final int getStringXPadding = 40;

    /**
     * Custom getString() method, see usage below for how to
     *  setup so that the calling thread is not blocked.
     *
     * USAGE:
     *  <pre>
     *  {@code
     *  CyderThreadRunner.submit(() -> {
     *      try {
     *          String input = GetterUtil().getInstance().getString(getterBuilder);
     *          //other operations using input
     *      } catch (Exception e) {
     *          ErrorHandler.handle(e);
     *      }
     *  }, "THREAD_NAME").start();
     *  }
     *  </pre>
     *
     * @param builder the builder pattern to use
     * @return the user entered input string. NOTE: if any improper
     * input is ateempted to be returned, this function returns
     * the string literal of "NULL" instead of {@code null}
     */
    public String getString(GetterBuilder builder) {
        AtomicReference<String> returnString = new AtomicReference<>();

        CyderThreadRunner.submit(() -> {
            try {
                CyderFrame inputFrame = new CyderFrame(getStringWidth, getStringHeight, CyderIcons.defaultBackground);
                inputFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                inputFrame.setTitle(builder.getTitle());

                CyderTextField inputField = new CyderTextField(0);
                inputField.setHorizontalAlignment(JTextField.CENTER);
                inputField.setBackground(Color.white);

                if (!StringUtil.isNull(builder.getInitialString()))
                    inputField.setText(builder.getInitialString());
                if (!StringUtil.isNull(builder.getFieldTooltip()))
                    inputField.setToolTipText(builder.getFieldTooltip());

                inputField.setBounds(getStringXPadding, CyderDragLabel.DEFAULT_HEIGHT + getStringYPadding,
                        getStringWidth - 2 * getStringXPadding,40);
                inputFrame.getContentPane().add(inputField);

                CyderButton submit = new CyderButton(builder.getSubmitButtonText());
                submit.setBackground(builder.getSubmitButtonColor());
                inputField.addActionListener(e1 -> submit.doClick());
                submit.setBorder(new LineBorder(CyderColors.navy,5,false));
                submit.setFont(CyderFonts.segoe20);
                submit.setForeground(CyderColors.navy);
                submit.addActionListener(e12 -> {
                    returnString.set((inputField.getText() == null || inputField.getText().isEmpty() ?
                            "NULL" : inputField.getText()));
                    inputFrame.dispose();
                });
                submit.setBounds(getStringXPadding,100,
                        getStringWidth - 2 * getStringXPadding,40);
                inputFrame.getContentPane().add(submit);

                inputFrame.addPreCloseAction(submit::doClick);

                inputFrame.setAlwaysOnTop(true);
                inputFrame.setLocationRelativeTo(builder.getRelativeTo());
                inputFrame.setVisible(true);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "getString() thread, title = [" + builder.getTitle() + "]");

        try {
            while (returnString.get() == null) {
                Thread.onSpinWait();
            }
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return returnString.get();
    }

    /**
     * The scroll list of files.
     */
    private CyderScrollList cyderScrollList;

    /**
     * The label which holds the scroll
     */
    private JLabel dirScrollLabel;

    /**
     * The last button for the file traversal stack.
     */
    private CyderButton last;

    /**
     * The next button for the file traversal stack./
     */
    private CyderButton next;

    /**
     * The list of strings to display for the current files.
     */
    private final LinkedList<String> directoryNameList = new LinkedList<>();

    /**
     * The files which correspond to the current files.
     */
    private final LinkedList<File> directoryFileList = new LinkedList<>();

    // stacks for traversal
    private final Stack<File> backward = new Stack<>();
    private final Stack<File> forward = new Stack<>();

    /**
     * The current location for the file getter.
     */
    private File currentDirectory;

    /**
     * Custom getFile method, see usage below for how to setup so that the program doesn't
     * spin wait on the main GUI thread forever. Ignoring the below setup
     * instructions will make the application spin wait possibly forever.
     *
     * USAGE:
     * <pre>
     * {@code
     *   CyderThreadRunner.submit(() -> {
     *         try {
     *             File input = GetterUtil().getInstance().getFile(getterBuilder);
     *             //other operations using input
     *         } catch (Exception e) {
     *             ErrorHandler.handle(e);
     *         }
     *  }, THREAD_NAME).start();
     * }
     * </pre>
     * @param builder the builder to use for the required params
     * @return the user-chosen file
     */
    public File getFile(GetterBuilder builder) {
        AtomicReference<File> setOnFileChosen = new AtomicReference<>();
        AtomicReference<CyderFrame> dirFrameAtomicRef = new AtomicReference<>();

        boolean darkMode = UserUtil.getCyderUser().getDarkmode().equals("1");
        dirFrameAtomicRef.set(new CyderFrame(630,510, darkMode
                ? CyderColors.darkModeBackgroundColor : CyderColors.regularBackgroundColor));

        CyderThreadRunner.submit(() -> {
            try {
                //reset needed vars in case an instance was already ran
                backward.clear();
                forward.clear();
                directoryFileList.clear();
                directoryNameList.clear();

                if (!StringUtil.isNull(builder.getInitialString())
                        && new File(builder.getInitialString()).exists()) {
                    currentDirectory = new File(builder.getInitialString());
                } else {
                    currentDirectory = new File(OSUtil.USER_DIR);
                }

                CyderFrame dirFrame = dirFrameAtomicRef.get();

                //frame setup
                dirFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                dirFrame.setTitle(currentDirectory.getName());

                CyderTextField dirField = new CyderTextField(0);

                if (!StringUtil.isNull(builder.getFieldTooltip()))
                    dirField.setToolTipText(builder.getFieldTooltip());

                dirField.setBackground(darkMode ? CyderColors.darkModeBackgroundColor : Color.white);
                dirField.setForeground(darkMode ? CyderColors.defaultDarkModeTextColor : CyderColors.navy);
                dirField.setBorder(new LineBorder(darkMode ? CyderColors.defaultDarkModeTextColor
                        : CyderColors.navy, 5, false));
                dirField.setText(currentDirectory.getAbsolutePath());
                dirField.addActionListener(e -> {
                    File ChosenDir = new File(dirField.getText());

                    if (ChosenDir.isDirectory()) {
                        refreshBasedOnDir(ChosenDir,setOnFileChosen, dirFrame, dirField);
                    } else if (ChosenDir.isFile()) {
                        setOnFileChosen.set(ChosenDir);
                    }
                });
                dirField.setBounds(60,40,500,40);
                dirFrame.getContentPane().add(dirField);

                //last setup
                last = new CyderButton(" < ");
                last.setFocusPainted(false);
                last.setForeground(CyderColors.navy);
                last.setBackground(CyderColors.regularRed);
                last.setFont(CyderFonts.segoe20);
                last.setBorder(new LineBorder(CyderColors.navy,5,false));
                last.addActionListener(e -> {
                    //we may only go back if there's something in the back and it's different from where we are now
                    if (backward != null && !backward.isEmpty() && !backward.peek().equals(currentDirectory)) {
                        //traversing so push where we are to forward
                        forward.push(currentDirectory);

                        //get where we're going
                        currentDirectory = backward.pop();

                        //now simply refresh based on currentDir
                        refreshFromTraversalButton(setOnFileChosen, dirFrame, dirField);
                    }
                });
                last.setBounds(10,40,40,40);
                dirFrame.getContentPane().add(last);

                //next setup
                next = new CyderButton(" > ");
                next.setFocusPainted(false);
                next.setForeground(CyderColors.navy);
                next.setBackground(CyderColors.regularRed);
                next.setFont(CyderFonts.segoe20);
                next.setBorder(new LineBorder(CyderColors.navy,5,false));
                next.addActionListener(e -> {
                    //only traverse forward if the stack is not empty and forward is different from where we are
                    if (forward != null && !forward.isEmpty() && !forward.peek().equals(currentDirectory)) {
                        //push where we are
                        backward.push(currentDirectory);

                        //figure out where we need to go
                        currentDirectory = forward.pop();

                        //refresh based on where we should go
                        refreshFromTraversalButton(setOnFileChosen, dirFrame, dirField);
                    }
                });
                next.setBounds(620 - 50,40,40, 40);
                dirFrame.getContentPane().add(next);

                File chosenDir = new File("c:/users/"
                        + OSUtil.getSystemUsername() + "/");
                File[] startDir = chosenDir.listFiles();

                Collections.addAll(directoryFileList, startDir);

                for (File file : directoryFileList) {
                    directoryNameList.add(file.getName());
                }

                //files scroll list setup
                cyderScrollList = new CyderScrollList(600, 400, CyderScrollList.SelectionPolicy.SINGLE, darkMode);
                cyderScrollList.setScrollFont(CyderFonts.segoe20.deriveFont(16f));

                //adding things to the list and setting up actions for what to do when an element is clicked
                for (int i = 0 ; i < directoryNameList.size() ; i++) {
                    int finalI = i;
                    cyderScrollList.addElement(directoryNameList.get(i), () -> {
                        if (directoryFileList.get(finalI).isDirectory()) {
                            refreshBasedOnDir(directoryFileList.get(finalI), setOnFileChosen, dirFrame, dirField);
                        } else {
                            setOnFileChosen.set(directoryFileList.get(finalI));
                        }
                    });
                }

                //generate the scroll label
                dirScrollLabel = cyderScrollList.generateScrollList();
                dirScrollLabel.setBounds(10,90,600, 400);
                dirFrame.getContentPane().add(dirScrollLabel);

                //final frame setup
                dirFrame.setLocationRelativeTo(builder.getRelativeTo());
                dirFrame.setVisible(true);
                dirField.requestFocus();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, " getFile() thread, title = [" + builder.getTitle() + "]");

        try {
            while (setOnFileChosen.get() == null)
                Thread.onSpinWait();
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        } finally {
            dirFrameAtomicRef.get().dispose();
        }

        return setOnFileChosen.get().getName().equals("NULL") ? null : setOnFileChosen.get();
    }

    /**
     * Refrehses the current file list scroll.
     *
     * @param setOnFileChosen a reference for the current file.
     * @param dirFrame the directory frame
     * @param dirField the directory field
     */
    private void refreshFromTraversalButton(AtomicReference<? super File> setOnFileChosen,
                                            CyderFrame dirFrame, CyderTextField dirField) {
        //get files
        File[] files = currentDirectory.listFiles();

        //remove old files
        cyderScrollList.removeAllElements();
        dirFrame.remove(dirScrollLabel);

        //wipe name and files lists
        directoryFileList.clear();
        directoryNameList.clear();

        //add new files arr to LL
        Collections.addAll(directoryFileList, files);

        //get corresponding names for name list
        for (File file : directoryFileList) {
            directoryNameList.add(file.getName());
        }

        //setup scroll
        cyderScrollList = new CyderScrollList(600, 400, CyderScrollList.SelectionPolicy.SINGLE);
        cyderScrollList.setScrollFont(CyderFonts.segoe20.deriveFont(16f));

        //add new items to scroll and actions
        for (int i = 0 ; i < directoryNameList.size() ; i++) {
            int finalI = i;
            cyderScrollList.addElement(directoryNameList.get(i), () -> {
                if (directoryFileList.get(finalI).isDirectory()) {
                    refreshBasedOnDir(directoryFileList.get(finalI), setOnFileChosen, dirFrame, dirField);
                } else {
                    setOnFileChosen.set(directoryFileList.get(finalI));
                }
            });
        }

        //regenerate scroll
        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10,90,600, 400);
        dirFrame.getContentPane().add(dirScrollLabel);

        //frame revalidation
        dirFrame.revalidate();
        dirFrame.repaint();
        dirFrame.setTitle(currentDirectory.getName());
        dirField.setText(currentDirectory.getAbsolutePath());
    }

    /**
     * Refrehses the current file list scroll.
     *
     * @param directory the selected element.
     * @param setOnFileChosen a reference for the current file.
     * @param dirFrame the directory frame
     * @param dirField the directory field
     */
    private void refreshBasedOnDir(File directory, AtomicReference<? super File> setOnFileChosen,
                                   CyderFrame dirFrame, CyderTextField dirField) {
        //clear forward since a new path
        forward.clear();

        //before where we were is wiped, put it in backwards if it's not the last
        if (backward.isEmpty() || !backward.peek().equals(currentDirectory)) {
            backward.push(currentDirectory);
        }

        //this is our current now
        currentDirectory = directory;

        //get files to display
        File[] files = directory.listFiles();

        //remove old list
        cyderScrollList.removeAllElements();
        dirFrame.remove(dirScrollLabel);

        //clear display lists
        directoryFileList.clear();
        directoryNameList.clear();

        //add array files to LL files
        Collections.addAll(directoryFileList, files);

        //add corresponding names of files to names list
        for (File file : directoryFileList) {
            directoryNameList.add(file.getName());
        }

        //regenerate scroll
        cyderScrollList = new CyderScrollList(600, 400, CyderScrollList.SelectionPolicy.SINGLE);
        cyderScrollList.setScrollFont(CyderFonts.segoe20.deriveFont(16f));

        //add items with coresponding actions to scroll
        for (int i = 0 ; i < directoryNameList.size() ; i++) {
            int finalI = i;
            cyderScrollList.addElement(directoryNameList.get(i), () -> {
                if (directoryFileList.get(finalI).isDirectory()) {
                    refreshBasedOnDir(directoryFileList.get(finalI), setOnFileChosen, dirFrame, dirField);
                } else {
                    setOnFileChosen.set(directoryFileList.get(finalI));
                }
            });
        }

        //generate scroll and add it
        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10,90,600, 400);
        dirFrame.getContentPane().add(dirScrollLabel);

        //frame revalidation
        dirFrame.revalidate();
        dirFrame.repaint();
        dirFrame.setTitle(directory.getName());
        dirField.setText(directory.getAbsolutePath());
    }

    /**
     * Custom getInput() method, see usage below for how to
     *  setup so that the calling thread is not blocked.
     *
     * USAGE:
     *  <pre>
     *  {@code
     *  CyderThreadRunner.submit(() -> {
     *      try {
     *          String input = GetterUtil().getInstance().getConfirmation(getterBuilder);
     *          //other operations using input
     *      } catch (Exception e) {
     *          ErrorHandler.handle(e);
     *      }
     *  }, "THREAD_NAME").start();
     *  }
     *  </pre>
     *
     * @param builder the builder pattern to use
     * @return whether the user confirmed the operation
     */
    public boolean getConfirmation(GetterBuilder builder) {
        AtomicReference<String> ret = new AtomicReference<>();
        AtomicReference<CyderFrame> frameReference = new AtomicReference<>();

        CyderThreadRunner.submit(() -> {
            try {
                CyderLabel textLabel = new CyderLabel();

                BoundsString bs = BoundsUtil.widthHeightCalculation(builder.getInitialString(), textLabel.getFont());
                int w = bs.getWidth();
                int h = bs.getHeight();
                textLabel.setText(bs.getText());

                CyderFrame frame = new CyderFrame(w + 40,
                        h + 25 + 20 + 40 + 40, CyderIcons.defaultBackgroundLarge);
                frameReference.set(frame);
                frame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                frame.setTitle(builder.getTitle());
                frame.addPreCloseAction(() -> ret.set("false"));

                textLabel.setBounds(10,35, w, h);
                frame.getContentPane().add(textLabel);

                //accounting for offset above
                w += 40;

                CyderButton yes = new CyderButton(builder.getYesButtonText());
                yes.setColors(builder.getSubmitButtonColor());
                yes.addActionListener(e -> ret.set("true"));
                yes.setBounds(20,35 + h + 20, (w - 60) / 2, 40);
                frame.getContentPane().add(yes);

                CyderButton no = new CyderButton(builder.getNoButtonText());
                no.setColors(builder.getSubmitButtonColor());
                no.addActionListener(e -> ret.set("false"));
                no.setBounds(20 + 20 + ((w - 60) / 2),35 + h + 20, (w - 60) / 2, 40);
                frame.getContentPane().add(no);

                frame.setLocationRelativeTo(builder.getRelativeTo());
                frame.setVisible(true);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, " getConfirmation() thread, title = [" + builder.getTitle() + "]");

        try {
            while (ret.get() == null) {
                Thread.onSpinWait();
            }

            frameReference.get().removePreCloseActions();
            frameReference.get().dispose();
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return ret.get().equals("true");
    }
}
