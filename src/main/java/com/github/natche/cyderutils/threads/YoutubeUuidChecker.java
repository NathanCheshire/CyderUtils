package com.github.natche.cyderutils.threads;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.github.natche.cyderutils.exceptions.FatalException;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.image.CyderImage;
import com.github.natche.cyderutils.network.NetworkUtil;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.strings.StringUtil;
import com.github.natche.cyderutils.time.TimeUtil;
import com.github.natche.cyderutils.ui.frame.CyderFrame;
import com.github.natche.cyderutils.ui.frame.enumerations.TitlePosition;
import com.github.natche.cyderutils.ui.pane.CyderOutputPane;
import com.github.natche.cyderutils.utils.ArrayUtil;
import com.github.natche.cyderutils.youtube.YouTubeConstants;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class for generating random YouTube UUIDs and attempting to parse the resulting url for a valid video.
 * Using a single thread, this has about a 1 in {@link #CHANCE_OF_SUCCESS} chance of succeeding every iteration.
 */
public class YoutubeUuidChecker {
    /**
     * The index to pass to {@link #incrementUuid(char[], int)} as the
     * position argument when attempting to increment a uuid.
     */
    private static final int startingIndexForAttemptingIncrements = YouTubeConstants.UUID_LENGTH - 1;

    /** The chances of success for a singular thread running. */
    @SuppressWarnings("unused") // Nice to have
    public static final BigInteger CHANCE_OF_SUCCESS = new BigInteger("73786976294838206464");

    /** YouTube's base 64 system used for UUID construction. */
    private static final ImmutableList<Character> uuidChars = ImmutableList.of(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '-', '_'
    );

    /** Whether this uuid checker instance has been killed. */
    private final AtomicBoolean killed = new AtomicBoolean();

    /** The list of uuids this checker has checked. */
    private final ArrayList<String> checkedUuids = new ArrayList<>();

    /** The uuid this checker is currently on. */
    private String currentUuid;

    /** The output pane used for printing. */
    private final CyderOutputPane outputPane;

    /** The instant this checker was started at. */
    private Instant startingInstant;

    /** The instant this checker was killed at. */
    private Instant endingInstant;

    /** Suppress default constructor. Requires two parameters for instantiation. */
    private YoutubeUuidChecker() {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Starts generating UUIDs and checking them against YouTube for a valid uuid.
     * Text is appended to the provided JTextPane.
     *
     * @param outputPane output pane to use for printing output to
     */
    protected YoutubeUuidChecker(CyderOutputPane outputPane) {
        Preconditions.checkNotNull(outputPane);

        this.outputPane = outputPane;
    }

    /**
     * Kills this YouTube thread returns the last checked UUID.
     *
     * @return the last checked UUID
     */
    @CanIgnoreReturnValue
    /* Future logic will allow multiple instances of this, meaning manager will need to figure out which uuid to save */
    public String kill() {
        killed.set(true);
        endingInstant = Instant.now();
        return currentUuid;
    }

    /**
     * Returns whether this uuid checker has been killed.
     *
     * @return whether this uuid checker has been killed
     */
    public boolean isKilled() {
        return killed.get();
    }

    /**
     * Returns the list of uuids this checker has checked.
     *
     * @return the list of uuids this checker has checked
     */
    public ImmutableList<String> getCheckedUuids() {
        return ImmutableList.copyOf(checkedUuids);
    }

    /**
     * Returns the current rate of checks per second.
     * If this has been killed, the starting and ending time as well as the total checked uuids
     * are used to compute the returned value
     *
     * @return the current rate of checks per second
     */
    public float getCurrentChecksPerSecond() {
        int numChecked = checkedUuids.size();
        long endingMillis = endingInstant == null ? Instant.now().toEpochMilli() : endingInstant.toEpochMilli();
        long millisElapsed = endingMillis - startingInstant.toEpochMilli();
        float checksPerMillis = numChecked / (float) millisElapsed;

        return (float) (checksPerMillis * TimeUtil.millisInSecond);
    }

    /**
     * Constructs and returns a url for the thumbnail of the provided uuid.
     *
     * @param uuid the YouTube uuid
     * @return the url for the thumbnail of the YouTube video with the provided id if it exists
     */
    public static String constructThumbnailUrl(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(!uuid.isEmpty());

        return "https://img.youtube.com/vi/" + uuid + "/hqdefault.jpg";
    }

    /** Starts this instance checking for a valid UUID. */
    public void startChecking() {
        Preconditions.checkState(!killed.get());

        StringUtil stringUtil = outputPane.getStringUtil();

        String threadName = "YoutubeUuidChecker#" + YoutubeUuidCheckerManager.INSTANCE.getActiveUuidCheckersLength();
        CyderThreadRunner.submit(() -> {
            // todo allow accepting
            currentUuid = "aaaaaaaaaaa";

            Preconditions.checkNotNull(currentUuid);
            Preconditions.checkArgument(currentUuid.length() == YouTubeConstants.UUID_LENGTH);

            startingInstant = Instant.now();

            while (!killed.get()) {
                YoutubeUuidCheckerManager.INSTANCE.incrementUrlsChecked();

                try {
                    attemptToAcquireLock();
                    stringUtil.println("Checked uuid: " + currentUuid);
                    YoutubeUuidCheckerManager.INSTANCE.releaseLock();

                    BufferedImage thumbnail = CyderImage.fromUrl(constructThumbnailUrl(currentUuid)).getBufferedImage();
                    YoutubeUuidCheckerManager.INSTANCE.killAll();

                    attemptToAcquireLock();
                    stringUtil.println("YoutubeUuidChecker found valid video with uuid: " + currentUuid);
                    YoutubeUuidCheckerManager.INSTANCE.releaseLock();

                    showThumbnailFrame(thumbnail);
                } catch (Exception ignored) {
                    checkedUuids.add(currentUuid);
                    incrementUuid();
                }
            }
        }, threadName);
    }

    /** Attempts to acquire the {@link YoutubeUuidCheckerManager}'s lock. */
    private void attemptToAcquireLock() {
        if (!YoutubeUuidCheckerManager.INSTANCE.acquireLock()) {
            throw new FatalException("Failed to acquire lock");
        }
    }

    /**
     * Shows the thumbnail frame on the event of a successful random UUID generation.
     *
     * @param thumbnail the thumbnail image
     */
    private void showThumbnailFrame(BufferedImage thumbnail) {
        CyderFrame thumbnailFrame = new CyderFrame.Builder()
                .setWidth(thumbnail.getWidth())
                .setHeight(thumbnail.getHeight())
                .setBackgroundIcon(new ImageIcon(thumbnail))
                .build();
        thumbnailFrame.setTitlePosition(TitlePosition.CENTER);
        thumbnailFrame.setTitle(currentUuid);

        String videoUrl = "https://www.youtube.com/watch?v=" + currentUuid;
        String title = videoUrl;
        Optional<String> optionalTitle = NetworkUtil.getUrlTitle(videoUrl);
        if (optionalTitle.isPresent()) title = optionalTitle.get();

        JLabel pictureLabel = new JLabel();
        pictureLabel.setToolTipText("Open video " + title);
        pictureLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ignoredEvent) {
                try {
                    NetworkUtil.openUrl(videoUrl);
                } catch (IOException ignored) {}
            }
        });
        pictureLabel.setBounds(0, 0, thumbnailFrame.getWidth(), thumbnailFrame.getHeight());
        thumbnailFrame.getContentPane().add(pictureLabel);

        thumbnailFrame.finalizeAndShow();
    }

    /** Increments the current UUID by one ordinal. */
    private void incrementUuid() {
        currentUuid = String.valueOf(incrementUuid(currentUuid.toCharArray()));
    }

    static char[] incrementUuid(char[] uuid) {
        return incrementUuid(uuid, startingIndexForAttemptingIncrements);
    }

    /**
     * Increments the provided eleven digit YouTube uuid by one, returning the result as a character array.
     *
     * @param uuid        the uuid to increment in char array form
     * @param addPosition the position to attempt to add to first
     * @return the incremented uuid in the form of a new character array
     */
    private static char[] incrementUuid(char[] uuid, int addPosition) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(!ArrayUtil.isEmpty(uuid));
        Preconditions.checkArgument(addPosition >= 0 && addPosition < YouTubeConstants.UUID_LENGTH);

        char[] ret;

        char positionChar = uuid[addPosition];

        if (positionChar == uuidChars.get(uuidChars.size() - 1)) {
            if (addPosition - 1 < 0) {
                throw new IllegalArgumentException("YouTube uuid incrementer overflow, provided uuid: "
                        + Arrays.toString(uuid));
            } else {
                ret = incrementUuid(uuid, addPosition - 1);
            }
        } else {
            positionChar = uuidChars.get(findCharIndex(positionChar) + 1);
            char[] copy = uuid.clone();
            copy[addPosition] = positionChar;

            if (addPosition + 1 < YouTubeConstants.UUID_LENGTH) {
                copy[addPosition + 1] = uuidChars.get(0);
            }

            ret = copy;
        }

        return ret;
    }

    /**
     * Finds the index of the provided char in the provided array.
     *
     * @param c the character to find in the provided array
     * @return the index of the provided char in the provided array
     */
    static int findCharIndex(char c) {
        int i = 0;

        while (i < uuidChars.size()) {
            if (uuidChars.get(i) == c) {
                return i;
            } else {
                i = i + 1;
            }
        }

        return -1;
    }
}