package main.java.cyder.handlers.external;

import com.google.common.base.Preconditions;
import main.java.cyder.console.Console;
import main.java.cyder.files.DirectoryWatcher;
import main.java.cyder.files.FileUtil;
import main.java.cyder.files.WatchDirectoryEvent;
import main.java.cyder.files.WatchDirectorySubscriber;
import main.java.cyder.getter.GetInputBuilder;
import main.java.cyder.getter.GetterUtil;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.threads.CyderThreadRunner;
import main.java.cyder.ui.drag.button.LeftButton;
import main.java.cyder.ui.drag.button.RightButton;
import main.java.cyder.ui.frame.CyderFrame;
import main.java.cyder.ui.frame.TitlePosition;
import main.java.cyder.user.UserUtil;
import main.java.cyder.utils.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Optional;

/**
 * A widget which displays the images supported by Cyder in a provided directory.
 */
public class PhotoViewer {
    /**
     * The list of valid image files in the current directory, not recursive.
     */
    private final LinkedList<File> validDirectoryImages = new LinkedList<>();

    /**
     * The starting directory/file.
     */
    private final File photoDirectory;

    /**
     * The current index of the valid directory images list.
     */
    private int currentIndex;

    /**
     * The image frame.
     */
    private CyderFrame pictureFrame;

    /**
     * The next image button.
     */
    private RightButton nextButton;

    /**
     * The last image button.
     */
    private LeftButton lastButton;

    /**
     * The watcher for the photo directory.
     */
    private final DirectoryWatcher photoDirectoryWatcher;

    /**
     * Returns a new instance of photo viewer with the provided starting directory.
     *
     * @param startDir the starting directory
     * @return a new instance of the PhotoViewer
     */
    public static PhotoViewer getInstance(File startDir) {
        return new PhotoViewer(startDir);
    }

    /**
     * Creates a new photo viewer object.
     *
     * @param photoDirectory the photo directory of the photo viewer.
     *                       If a file is provided, the file's parent is
     *                       used as the photo directory
     */
    private PhotoViewer(File photoDirectory) {
        Preconditions.checkNotNull(photoDirectory);
        Preconditions.checkArgument(photoDirectory.exists());
        Preconditions.checkArgument(photoDirectory.exists());

        this.photoDirectory = photoDirectory;

        File watcherDirectory = photoDirectory;
        if (photoDirectory.isFile()) {
            watcherDirectory = photoDirectory.getParentFile();
        }
        this.photoDirectoryWatcher = new DirectoryWatcher(watcherDirectory);
    }

    /**
     * Opens the instance of photo viewer.
     */
    public void showGui() {
        String threadName = "PhotoViewer showGui thread, initial directory: " + photoDirectory;
        CyderThreadRunner.submit(() -> {
            Logger.log(LogTag.OBJECT_CREATION, this);

            refreshValidFiles();

            File currentImage = validDirectoryImages.get(0);

            if (photoDirectory.isFile()) {
                for (File validDirectoryImage : validDirectoryImages) {
                    if (validDirectoryImage.equals(photoDirectory)) {
                        currentImage = validDirectoryImage;
                        break;
                    }
                }
            }

            ImageIcon newImage;
            newImage = scaleImageIfNeeded(currentImage);

            pictureFrame = new CyderFrame(newImage.getIconWidth(), newImage.getIconHeight(), newImage);
            pictureFrame.setBackground(Color.BLACK);
            pictureFrame.setTitlePosition(TitlePosition.CENTER);
            revalidateTitle(FileUtil.getFilename(currentImage.getName()));
            pictureFrame.setVisible(true);
            pictureFrame.addWindowListener(generateWindowAdapter());

            pictureFrame.finalizeAndShow();

            pictureFrame.setMenuEnabled(true);
            pictureFrame.addMenuItem("Rename", this::rename);

            nextButton = new RightButton();
            nextButton.setToolTipText(NEXT);
            nextButton.setClickAction(() -> transition(true));
            pictureFrame.getTopDragLabel().addRightButton(nextButton, 0);

            lastButton = new LeftButton();
            lastButton.setToolTipText(LAST);
            lastButton.setClickAction(() -> transition(false));
            pictureFrame.getTopDragLabel().addRightButton(lastButton, 0);

            revalidateNavigationButtonVisibility();
            startDirectoryWatcher();
        }, threadName);
    }

    /**
     * The next keyword.
     */
    private static final String NEXT = "Next";

    /**
     * The last keyword.
     */
    private static final String LAST = "Last";

    /**
     * Generates and returns the window adapter for this photo viewer.
     *
     * @return the window adapter for this photo viewer
     */
    private WindowAdapter generateWindowAdapter() {
        return new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                photoDirectoryWatcher.stopWatching();
            }
        };
    }

    /**
     * Refreshes the valid files list.
     */
    private void refreshValidFiles() {
        validDirectoryImages.clear();

        if (photoDirectory.isDirectory()) {
            File[] files = photoDirectory.listFiles();

            if (files == null || files.length == 0) return;

            for (File f : files) {
                if (FileUtil.isSupportedImageExtension(f)) {
                    validDirectoryImages.add(f);
                }
            }
        } else {
            File parent = photoDirectory.getParentFile();
            File[] neighbors = parent.listFiles();

            if (neighbors == null || neighbors.length == 0) return;

            for (File f : neighbors) {
                if (FileUtil.isSupportedImageExtension(f)) {
                    validDirectoryImages.add(f);
                }
            }
        }
    }

    /**
     * Transitions to a new image in the directory if more exist.
     *
     * @param forward whether to transition forwards.
     *                If false, the direction traversed is backwards
     */
    private void transition(boolean forward) {
        refreshValidFiles();

        if (validDirectoryImages.size() <= 1) return;

        // change the index
        if (forward) {
            if (currentIndex + 1 < validDirectoryImages.size()) {
                currentIndex += 1;
            } else {
                currentIndex = 0;
            }
        } else {
            if (currentIndex - 1 >= 0) {
                currentIndex -= 1;
            } else {
                currentIndex = validDirectoryImages.size() - 1;
            }
        }

        Point center = pictureFrame.getCenterPointOnScreen();

        ImageIcon newImage = scaleImageIfNeeded(validDirectoryImages.get(currentIndex));
        pictureFrame.setSize(newImage.getIconWidth(), newImage.getIconHeight());
        pictureFrame.setBackground(newImage);

        pictureFrame.setLocation((int) (center.getX() - newImage.getIconWidth() / 2),
                (int) (center.getY() - newImage.getIconHeight() / 2));

        pictureFrame.refreshBackground();
        revalidateTitle(FileUtil.getFilename(validDirectoryImages.get(currentIndex).getName()));
    }

    /**
     * The maximum length of the photo viewer frame.
     */
    private static final int MAX_LEN = 800;

    /**
     * The maximum dimension of the photo viewer frame.
     */
    private static final Dimension MAX_DIMENSION = new Dimension(MAX_LEN, MAX_LEN);

    /**
     * Returns a scaled image icon for the provided image
     * file if the image is bigger than MAX_LEN x MAX_LEN.
     *
     * @param imageFile the image file to process
     * @return the ImageIcon from the image file guaranteed to be no bigger than MAX_LEN x MAX_LEN
     */
    private ImageIcon scaleImageIfNeeded(File imageFile) {
        try {
            BufferedImage bufferedImage = ImageUtil.read(imageFile);
            bufferedImage = ImageUtil.ensureFitsInBounds(bufferedImage, MAX_DIMENSION);
            return ImageUtil.toImageIcon(bufferedImage);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new IllegalStateException("Could not generate ImageIcon for file: " + imageFile.getAbsolutePath());
    }

    /**
     * Attempts to rename the current image file if not in use by the Console.
     */
    private void rename() {
        File currentRename = new File(validDirectoryImages.get(currentIndex).getAbsolutePath());
        File currentBackground = Console.INSTANCE
                .getCurrentBackground().getReferenceFile().getAbsoluteFile();

        if (currentRename.getAbsolutePath().equals(currentBackground.getAbsolutePath())) {
            pictureFrame.notify("Sorry, " + UserUtil.getCyderUser().getName()
                    + ", but you're not allowed to" + " rename the background you are currently using");
            return;
        }

        CyderThreadRunner.submit(() -> {
            try {
                Optional<String> optionalName = GetterUtil.getInstance().getInput(
                        new GetInputBuilder("Rename", "New filename for " + CyderStrings.quote
                                + validDirectoryImages.get(currentIndex).getName() + CyderStrings.quote)
                                .setRelativeTo(pictureFrame)
                                .setInitialFieldText(FileUtil.getFilename(validDirectoryImages.get(currentIndex)))
                                .setSubmitButtonText("Rename"));
                if (optionalName.isEmpty()) {
                    pictureFrame.notify("File not renamed");
                    return;
                }
                String name = optionalName.get();

                File oldName = new File(validDirectoryImages.get(currentIndex).getAbsolutePath());
                String replaceOldName = FileUtil.getFilename(oldName);
                File newName = new File(oldName.getAbsolutePath().replace(replaceOldName, name));

                if (oldName.renameTo(newName)) {
                    pictureFrame.notify("Successfully renamed to \"" + name + CyderStrings.quote);

                    refreshValidFiles();

                    // update index based on new name
                    for (int i = 0 ; i < validDirectoryImages.size() ; i++) {
                        if (FileUtil.getFilename(validDirectoryImages.get(i)).equals(name)) {
                            currentIndex = i;
                        }
                    }

                    revalidateTitle(name);

                    if (onRenameCallback != null) {
                        onRenameCallback.run();
                    }
                } else {
                    pictureFrame.notify("Could not rename at this time");
                }

            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "PhotoViewer Image Renamer: " + this);
    }

    /**
     * The callback to run whenever a photo is renamed.
     */
    private Runnable onRenameCallback;

    /**
     * Invokes the provided runnable whenever a file is renamed via this photo viewer instance.
     *
     * @param runnable the runnable to invoke
     */
    public void setRenameCallback(Runnable runnable) {
        Preconditions.checkNotNull(runnable);
        onRenameCallback = runnable;
    }

    /**
     * Revalidates the frame title based on the provided name.
     *
     * @param title the title of the frame
     */
    public void revalidateTitle(String title) {
        try {
            BufferedImage bi = ImageUtil.read(validDirectoryImages.get(currentIndex));
            int w = bi.getWidth();
            int h = bi.getHeight();
            pictureFrame.setTitle(title + CyderStrings.openingBracket + w + "x" + h + CyderStrings.closingBracket);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            pictureFrame.setTitle(title);
        }
    }

    /**
     * Starts the directory watcher to update the visibilities of the
     * next and last buttons based on the contents of the photo directory.
     */
    private void startDirectoryWatcher() {
        WatchDirectorySubscriber subscriber = new WatchDirectorySubscriber() {
            @Override
            public void onEvent(DirectoryWatcher broker, WatchDirectoryEvent event, File eventFile) {
                refreshValidFiles();
                revalidateNavigationButtonVisibility();
            }
        };
        subscriber.subscribeTo(WatchDirectoryEvent.FILE_ADDED,
                WatchDirectoryEvent.FILE_DELETED,
                WatchDirectoryEvent.FILE_MODIFIED);
        photoDirectoryWatcher.addSubscriber(subscriber);
        photoDirectoryWatcher.startWatching();
    }

    /**
     * Revalidates the visibility of the navigation buttons.
     */
    private void revalidateNavigationButtonVisibility() {
        refreshValidFiles();
        setNavigationButtonsVisible(validDirectoryImages.size() > 1);
    }

    /**
     * Sets the visibility of the navigation buttons.
     *
     * @param visible the visibility of the navigation buttons
     */
    private void setNavigationButtonsVisible(boolean visible) {
        nextButton.setVisible(visible);
        lastButton.setVisible(visible);
    }
}