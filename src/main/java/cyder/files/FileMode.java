package cyder.files;

import cyder.files.CyderTemporaryFile;

/**
 * The supported modes for reading/writing to/from a {@link CyderTemporaryFile}.
 */
public enum FileMode {
    /**
     * A binary file; the extension is not expected to be ".bin"
     * but the content provided for writing and the content
     * returned from reading will be a byte array.
     */
    BINARY,

    /**
     * A text file; the extension is not expected to be ".txt"
     * but the content provided for writing and the content
     * returned from reading will be a {@link String}.
     */
    TEXT
}
