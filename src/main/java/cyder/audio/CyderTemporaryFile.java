package cyder.audio;

import com.google.common.base.Preconditions;
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
    private final File outputDirectory;
    private final String outputName;
    private final String outputExtension;

    private CyderTemporaryFile() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    private CyderTemporaryFile(CyderTemporaryFileBuilder builder) {
        this.outputDirectory = builder.outputDirectory;
        this.outputName = builder.outputName;
        this.outputExtension = builder.outputExtension;
    }

    public static final class CyderTemporaryFileBuilder {
        @SuppressWarnings("SpellCheckingInspection")
        private static final SimpleDateFormat deafultFilenameDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSSS");

        private File outputDirectory = new File(SystemPropertyKey.JAVA_IO_TMPDIR.getProperty());
        private String outputName;
        private String outputExtension = ".tmp";

        public CyderTemporaryFileBuilder() {
            outputName = generateDefaultName();
        }

        public CyderTemporaryFileBuilder setOutputName(String outputName) {
            Preconditions.checkNotNull(outputName);
            Preconditions.checkArgument(!outputName.trim().isEmpty());

            this.outputName = outputName;
            return this;
        }

        public CyderTemporaryFileBuilder setOutputExtension(String outputExtension) {
            Preconditions.checkNotNull(outputExtension);
            Preconditions.checkArgument(!outputExtension.trim().isEmpty());

            this.outputExtension = outputExtension;
            return this;
        }

        public CyderTemporaryFileBuilder setOutputDirectory(File outputDirectory) {
            Preconditions.checkNotNull(outputDirectory);
            Preconditions.checkArgument(outputDirectory.isFile());

            this.outputDirectory = outputDirectory;
            return this;
        }

        public CyderTemporaryFile build() {
            return new CyderTemporaryFile(this);
        }

        private static String generateDefaultName() {
            return deafultFilenameDateFormat.format(new Date());
        }
    }
}