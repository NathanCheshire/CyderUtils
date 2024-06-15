package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.enumerations.SystemPropertyKey;
import cyder.files.FileUtil;

import java.io.File;

/**
 * A builder for constructing a file-to-be-created, it's parent directory, and it's name and extension.
 */
public class OutputFileBuilder {
    /**
     * The directory the file will be created in.
     */
    private File outputDirectory = new File(SystemPropertyKey.JAVA_IO_TMPDIR.getProperty());

    /**
     * The name and extension of the output file.
     */
    private String outputNameAndExtension;

    /**
     * Constructs a new OutputFileBuilder instance.
     *
     * @param outputNameAndExtension the name and extension of the file such as "MyFile.txt"
     * @throws NullPointerException     if the provided name and extension is null
     * @throws IllegalArgumentException if the provided name and extension is empty or invalid
     */
    private OutputFileBuilder(String outputNameAndExtension) {
        Preconditions.checkNotNull(outputNameAndExtension);
        Preconditions.checkArgument(!outputNameAndExtension.trim().isEmpty());
        Preconditions.checkArgument(FileUtil.isValidFilename(outputNameAndExtension));
    }

    /**
     * Returns a new OutputFileBuilder instance using the provided
     * string as the name and extension of the file to be created
     *
     * @param outputNameAndExtension the name and extension of the file
     * @return a new OutputFileBuilder instance
     * @throws NullPointerException if the provided name and extension is null
     * @throws IllegalArgumentException if the provided name and extension is empty or invalid
     */
    public static OutputFileBuilder fromNameAndExtension(String outputNameAndExtension) {
        Preconditions.checkNotNull(outputNameAndExtension);
        Preconditions.checkArgument(!outputNameAndExtension.isEmpty());
        Preconditions.checkArgument(FileUtil.isValidFilename(outputNameAndExtension));

        return new OutputFileBuilder(outputNameAndExtension);
    }

    public static OutputFileBuilder fromExistingFile(File existingFile) {
        Preconditions.checkNotNull(existingFile);
        Preconditions.checkArgument(existingFile.exists());
        Preconditions.checkArgument(existingFile.isFile());

        String proposedName = existingFile.getName();
        // todo unique name from this
    }

    /**
     * Sets the output directory of this OutputFileBuilder.
     *
     * @param outputDirectory the directory this file will be output to
     * @return this OutputFileBuilder
     * @throws NullPointerException if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist or is not a file
     */
    @CanIgnoreReturnValue
    public OutputFileBuilder setOutputDirectory(File outputDirectory) {
        Preconditions.checkNotNull(outputDirectory);
        Preconditions.checkArgument(outputDirectory.exists());
        Preconditions.checkArgument(outputDirectory.isDirectory());

        this.outputDirectory = outputDirectory;
        return this;
    }

    public File buildOutputFile() {

    }

    public File create() {

    }

    public boolean buildAndCreate() {

    }
}
