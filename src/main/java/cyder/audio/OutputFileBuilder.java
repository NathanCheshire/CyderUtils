package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.enumerations.SystemPropertyKey;

import java.io.File;

public class OutputFileBuilder {
    private File outputDirectory = new File(SystemPropertyKey.JAVA_IO_TMPDIR.getProperty());
    private String outputNameAndExtension;

    private OutputFileBuilder(String outputNameAndExtension) {
        Preconditions.checkNotNull(outputNameAndExtension);
        Preconditions.checkArgument(!outputNameAndExtension.trim().isEmpty());
    }

    public static OutputFileBuilder fromNameAndExtension(String outputNameAndExtension) {
        Preconditions.checkNotNull(outputNameAndExtension);
        Preconditions.checkArgument(!outputNameAndExtension.isEmpty());

        return new OutputFileBuilder(outputNameAndExtension);
    }

    public static OutputFileBuilder fromExistingFile(File existingFile) {
        Preconditions.checkNotNull(existingFile);
        Preconditions.checkArgument(existingFile.exists());

        String proposedName = existingFile.getName();
        // todo unique name from this
    }

    @CanIgnoreReturnValue
    public OutputFileBuilder setOutputDirectory(File outputDirectory) {
        Preconditions.checkNotNull(outputDirectory);
        Preconditions.checkArgument(outputDirectory.exists());

        this.outputDirectory = outputDirectory;
        return this;
    }

    public File buildOutputFile() {

    }
}
