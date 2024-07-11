package com.github.natche.cyderutils.files.temporary;

import com.github.natche.cyderutils.enumerations.SystemPropertyKey;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/** A builder for constructing a temporary file, it's parent directory, and it's name and extension. */
@Immutable
public final class CyderTemporaryFile {
    /** The directory to place this temporary file in. */
    private final File outputDirectory;

    /** The name of the output file. */
    private final String outputName;

    /** The extension of the output file. */
    private final String outputExtension;

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private CyderTemporaryFile() {
        throw new IllegalMethodException("Invalid constructor; builder required");
    }

    /**
     * Constructs a new instance of {@link CyderTemporaryFile} using the provided builder.
     *
     * @param builder the builder to construct this instance from
     */
    private CyderTemporaryFile(Builder builder) {
        this.outputDirectory = builder.outputDirectory;
        this.outputName = builder.outputFilename;
        this.outputExtension = builder.outputExtension;
    }

    /**
     * Builds and returns a {@link File} pointer for this temporary file.
     *
     * @return a {@link File} pointer for this temporary file
     */
    public File buildFile() {
        String extension = outputExtension.startsWith(".") ? outputExtension : "." + outputExtension;
        return new File(outputDirectory, outputName + extension);
    }

    /**
     * Returns whether this temporary file exists.
     *
     * @return whether this temporary file exists
     */
    public boolean exists() {
        return buildFile().exists();
    }

    /**
     * Attempts to create this file.
     *
     * @return whether the file was created and now exists
     */
    @CanIgnoreReturnValue
    public boolean create() {
        File pointer = buildFile();

        try {
            return pointer.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Deletes this file if it exists and returns whether the file exists.
     *
     * @return whether the file exists after the delete operation
     */
    public boolean delete() {
        File pointer = buildFile();
        return pointer.delete();
    }

    /**
     * Returns the outputName of this temporary file.
     *
     * @return the outputName of this temporary file
     */
    public String getOutputName() {
        return this.outputName;
    }

    /**
     * Returns the outputExtension of this temporary file.
     *
     * @return the outputExtension of this temporary file
     */
    public String getOutputExtension() {
        return this.outputExtension;
    }

    /**
     * Reads the content from this file.
     *
     * @param mode the mode to read the file as
     * @return the data read from the file
     * @throws NullPointerException if the provided file mode is null
     * @throws IllegalStateException if this file does not exist
     * @throws IOException if an error occurs when reading this file
     * @throws IllegalArgumentException if the provided file mode is invalid
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Object readAs(FileMode mode) throws IOException {
        Preconditions.checkNotNull(mode);
        Preconditions.checkState(exists());

        if (mode == FileMode.BINARY) {
            File file = buildFile();

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] data = new byte[fis.available()];
                fis.read(data);
                return data;
            } catch (Exception e) {
                throw new IOException("An error occurred when reading, {file="
                        + file.getAbsolutePath() + ", mode=" + mode + "}");
            }
        } else if (mode == FileMode.TEXT) {
            File file = buildFile();

            try (BufferedReader bis = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = bis.readLine()) != null) content.append(line).append("\n");
                return content.toString();
            } catch (Exception e) {
                throw new IOException("An error occurred when reading, {file="
                        + file.getAbsolutePath() + ", mode=" + mode + "}");
            }
        }

        throw new IllegalArgumentException("Unsupported file mode: " + mode);
    }

    /**
     * Writes the provided content to this file, creating it if it does not exist.
     *
     * @param mode the write mode
     * @param content the content to write
     * @return whether the content was successfully written
     * @throws NullPointerException if the provided mode or content are null
     * @throws IllegalStateException if this file does not exist
     * @throws IOException if an error occurs when reading this file
     * @throws IllegalArgumentException if the provided file mode is invalid
     */
    public boolean writeAs(FileMode mode, Object content) throws IOException {
        Preconditions.checkNotNull(mode);
        Preconditions.checkNotNull(content);
        Preconditions.checkState(exists());

        File file = buildFile();
        if (mode == FileMode.BINARY) {
            try (FileOutputStream fis = new FileOutputStream(file)) {
                fis.write((byte[]) content);
            }
        } else if (mode == FileMode.TEXT) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
                bufferedWriter.write((String) content);
            }
        }

        throw new IllegalArgumentException("Unsupported file mode: " + mode);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof CyderTemporaryFile)) {
            return false;
        }

        CyderTemporaryFile other = (CyderTemporaryFile) o;

        return other.getOutputExtension().equals(getOutputExtension())
                && other.getOutputName().equals(getOutputName())
                && other.outputDirectory.getAbsolutePath().equals(outputDirectory.getAbsolutePath());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = outputDirectory.hashCode();
        ret = 31 * ret + outputName.hashCode();
        ret = 31 * ret + outputExtension.hashCode();
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "CyderTemporaryFile{"
                + "\"outputDirectory\"=" + outputDirectory + ","
                + "\"outputName\"=" + outputName + ","
                + "\"outputExtension\"=" + outputExtension
                + "}";
    }

    /** A builder for constructing instances of {@link CyderTemporaryFile}. */
    public static final class Builder {
        @SuppressWarnings("SpellCheckingInspection")
        private static final SimpleDateFormat deafultFilenameDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

        private File outputDirectory = new File(SystemPropertyKey.JAVA_IO_TMPDIR.getProperty());
        private String outputFilename;
        private String outputExtension = ".tmp";

        /**
         * Constructs a new instance of a {@link Builder} with the following defaults:
         * <ul>
         *     <li>outputDirectory: JAVA_IO_TMPDIR</li>
         *     <li>outputName: a date string in the format 20240615_065622_500</li>
         *     <li>outputExtension: .tmp</li>
         * </ul>
         */
        public Builder() {
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
        public Builder setOutputFilename(String outputFilename) {
            Preconditions.checkNotNull(outputFilename);
            Preconditions.checkArgument(!outputFilename.trim().isEmpty());

            this.outputFilename = outputFilename;
            return this;
        }

        /**
         * Sets the file extension for this builder.
         * Both the extension and the extension prefixed with a period are allowed.
         * When building the File, the instance will automatically prefix a period
         * to the extension if needed.
         *
         * @param outputExtension the extension
         * @return this builder
         * @throws NullPointerException if the provided String is null
         * @throws IllegalArgumentException if the provided String is empty
         */
        @CanIgnoreReturnValue
        public Builder setOutputExtension(String outputExtension) {
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
        public Builder setOutputDirectory(File outputDirectory) {
            Preconditions.checkNotNull(outputDirectory);
            Preconditions.checkArgument(!outputDirectory.isFile());

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
        public Builder setFilenameAndExtension(String nameAndExtension) {
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