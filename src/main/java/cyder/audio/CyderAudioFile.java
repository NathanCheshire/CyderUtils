package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.audio.validation.SupportedAudioFileType;
import cyder.files.CyderTemporaryFile;
import cyder.files.FileUtil;
import cyder.process.CyderProcessException;
import cyder.process.ProcessResult;
import cyder.process.ProcessUtil;
import cyder.threads.CyderThreadFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * A Cyder wrapper class around a {@link java.io.File} of a supported audio type, as defined by
 * {@link SupportedAudioFileType#isSupported(File)}, for performing certain operations or mutations.
 */
public final class CyderAudioFile {
    /**
     * The encapsulated audio file.
     */
    private final File audioFile;

    // todo from String and from File

    /**
     * Constructs a new CyderAudioFile.
     *
     * @param audioFile the audio file
     * @throws NullPointerException     if the provided audio file is null
     * @throws IllegalArgumentException if the provided audio file does not
     *                                  exist or is not a file or is not a supported audio type
     */
    public CyderAudioFile(File audioFile) {
        Preconditions.checkNotNull(audioFile);
        Preconditions.checkArgument(audioFile.exists());
        Preconditions.checkArgument(audioFile.isFile());
        Preconditions.checkArgument(SupportedAudioFileType.isSupported(audioFile));

        this.audioFile = audioFile;
    }

    /**
     * Converts the internal audio file from the current format to the provided format.
     * The file will be named to the new name plus the new extension and will exist
     * in the same directory as this file.
     *
     * @param audioFileType the audio file type to convert to
     * @param newName the new filename
     * @return the converted file
     * @throws NullPointerException if either the provided audio file type or new name are null
     * @throws IllegalArgumentException if the provided new name is invalid
     */
    @CanIgnoreReturnValue
    public Future<CyderAudioFile> convertTo(SupportedAudioFileType audioFileType, String newName) {
        Preconditions.checkNotNull(audioFileType);
        Preconditions.checkNotNull(newName);
        Preconditions.checkArgument(FileUtil.isValidFilename(newName));

        CyderTemporaryFile temporaryConversionFile = new CyderTemporaryFile.Builder()
                .setOutputFilename(newName)
                .setOutputExtension(audioFileType.getExtension())
                .build();

        List<String> process = ImmutableList.of(
                "ffmpeg", "-y", "-i", this.audioFile.getAbsolutePath(),
                temporaryConversionFile.buildFile().getAbsolutePath());

        String threadName = "CyderAudioFile.convertTo process";
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName)).submit(() -> {
            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(process);
            while (!futureResult.isDone()) Thread.onSpinWait();

            try {
                ProcessResult result = futureResult.get();

                if (result.hasErrors()) throw new CyderProcessException("CyderAudioFile.convertTo process"
                        + " result contains errors: " + result.getErrorOutput());
                return new CyderAudioFile(temporaryConversionFile.buildFile());
            } catch (Exception e) {
                throw new CyderProcessException(e);
            }
        });
    }

    public static void main(String[] args) throws IOException {
        // todo test convert to
        CyderAudioFile mp3 = new CyderAudioFile(
                new File("/Users/nathancheshire/Downloads/Test/WhereTheyAt.mp3"));
        Future<Optional<CyderAudioFile>> result = mp3.dreamify();
        while (!result.isDone()) Thread.onSpinWait();
        System.out.println("Done");
        try {
            System.out.println(result.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the length of the encapsulated audio file using the provided method.
     *
     * @param method the audio length computation method
     * @return the length of the audio file
     */
    public Future<Duration> getAudioLength(DetermineAudioLengthMethod method) {
        Preconditions.checkNotNull(method);
        return method.determineAudioLength(audioFile);
    }

    /**
     * The highpass value for dreamifying an audio file.
     */
    private static final int HIGHPASS = 200;

    /**
     * The low pass value for dreamifying an audio file.
     */
    private static final int LOW_PASS = 1500;

    /**
     * The -filter:a flag for setting high and low pass data.
     */
    private static final String FILTER_DASH_A = "-filter:a";

    private String constructHighpassLowpassFilter() {
        return "\"" + "highpass=f=" + HIGHPASS + ", " + "lowpass=f=" + LOW_PASS + "\"";
    }

    /**
     * Dreamifies this audio file and returns a new audio file representing the newly created dreamified audio file.
     *
     * @return the dreamified audio file
     */
    public Future<Optional<CyderAudioFile>> dreamify() {
        String executorThreadName = "CyderAudioFile dreamifier, " + this;
        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(executorThreadName)).submit(() -> {

            String safeFilename = "\"" + audioFile.getAbsolutePath() + "\"";

            File outputFile = FileUtil.addSuffixToFilename(audioFile, "_dreamy");
            String safeOutputFilename = "\"" + outputFile.getAbsolutePath() + "\"";

            String[] command = {
                    "ffmpeg",
                    "-i",
                    safeFilename,
                    FILTER_DASH_A,
                    constructHighpassLowpassFilter(),
                    safeOutputFilename};
            System.out.println(Arrays.stream(command).collect(Collectors.joining(" ")));
            Future<ProcessResult> result = ProcessUtil.getProcessOutput(command);
            while (!result.isDone()) Thread.onSpinWait();
            System.out.println(result.get());

            CyderAudioFile ret = new CyderAudioFile(outputFile);
            return Optional.of(ret);
        });
    }

    /**
     * Dreamifies the encapsulated file and returns a reference to the new dreamified audio file.
     *
     * @return returns the dreamified audio file
     */
    public Future<CyderAudioFile> dreamify(File saveToFile) {
        return Futures.immediateFuture(null);
    }
}
