package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;
import cyder.enumerations.SystemPropertyKey;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A builder for constructing a temporary file, it's parent directory, and it's name and extension.
 */
@Immutable
public final class CyderTemporaryFile {
    /**
     * The directory to place this temporary file in.
     */
    private final File outputDirectory;

    /**
     * The name of the output file.
     */
    private final String outputName;

    /**
     * The extension of the output file.
     */
    private final String outputExtension;

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private CyderTemporaryFile() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Constructs a new instance of {@link CyderTemporaryFile} using the provided builder.
     *
     * @param builder the builder to construct this instance from
     */
    private CyderTemporaryFile(CyderTemporaryFileBuilder builder) {
        this.outputDirectory = builder.outputDirectory;
        this.outputName = builder.outputFilename;
        this.outputExtension = builder.outputExtension;
    }

    /**
     * A builder for constructing instances of {@link CyderTemporaryFile}.
     */
    public static final class CyderTemporaryFileBuilder {
        @SuppressWarnings("SpellCheckingInspection")
        private static final SimpleDateFormat deafultFilenameDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

        private File outputDirectory = new File(SystemPropertyKey.JAVA_IO_TMPDIR.getProperty());
        private String outputFilename;
        private String outputExtension = ".tmp";

        /**
         * Constructs a new instance of a {@link CyderTemporaryFileBuilder} with the following defaults:
         * <ul>
         *     <li>outputDirectory: JAVA_IO_TMPDIR</li>
         *     <li>outputName: a date string in the format 20240615_065622_500</li>
         *     <li>outputExtension: .tmp</li>
         * </ul>
         */
        public CyderTemporaryFileBuilder() {
            outputFilename = generateDefaultName();
        }

        /**
         * Sets the filename for this builder.
         *
         * @param outputFilename the filename
         * @return this builder
         * @throws NullPointerException if the provided String is null
         * @throws IllegalArgumentException if the provided String is empty
         */
        @CanIgnoreReturnValue
        public CyderTemporaryFileBuilder setOutputFilename(String outputFilename) {
            Preconditions.checkNotNull(outputFilename);
            Preconditions.checkArgument(!outputFilename.trim().isEmpty());

            this.outputFilename = outputFilename;
            return this;
        }

        /**
         * Sets the file extension for this builder.
         *
         * @param outputExtension the extension
         * @return this builder
         * @throws NullPointerException if the provided String is null
         * @throws IllegalArgumentException if the provided String is empty
         */
        @CanIgnoreReturnValue
        public CyderTemporaryFileBuilder setOutputExtension(String outputExtension) {
            Preconditions.checkNotNull(outputExtension);
            Preconditions.checkArgument(!outputExtension.trim().isEmpty());

            this.outputExtension = outputExtension;
            return this;
        }

        /**
         * Sets the output directory for this builder.
         *
         * @param outputDirectory the output directory
         * @return this builder
         * @throws NullPointerException if the provided File is null
         * @throws IllegalArgumentException if the provided File is not a file; see {@link File#isFile()}
         */
        @CanIgnoreReturnValue
        public CyderTemporaryFileBuilder setOutputDirectory(File outputDirectory) {
            Preconditions.checkNotNull(outputDirectory);
            Preconditions.checkArgument(outputDirectory.isFile());

            this.outputDirectory = outputDirectory;
            return this;
        }

        /**
         * Sets the filename and extension for this builder.
         *
         * @param nameAndExtension the filename and extension for this builder such as "MyFile.txt"
         * @return this builder
         * @throws NullPointerException if the provided String is null
         * @throws IllegalArgumentException if the provided String is empty or does not contain a period
         */
        public CyderTemporaryFileBuilder setFilenameAndExtension(String nameAndExtension) {
            Preconditions.checkNotNull(nameAndExtension);
            Preconditions.checkArgument(!nameAndExtension.trim().isEmpty());
            Preconditions.checkArgument(nameAndExtension.contains("."));

            this.outputFilename = nameAndExtension.substring(0, nameAndExtension.lastIndexOf('.'));
            String[] parts = nameAndExtension.split("\\.");
            this.outputExtension = parts[parts.length - 1];

            return this;
        }

        /**
         * Constructs a new instance of {@link CyderTemporaryFile} using this builder.
         *
         * @return a new instance of {@link CyderTemporaryFile}
         */
        public CyderTemporaryFile build() {
            return new CyderTemporaryFile(this);
        }

        /**
         * Generates the default name for a new {@link CyderTemporaryFile}
         * using the date/time at the instant of construction.
         *
         * @return the default name for a new {@link CyderTemporaryFile}
         */
        private static String generateDefaultName() {
            return deafultFilenameDateFormat.format(new Date());
        }
    }
}