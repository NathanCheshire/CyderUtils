package com.github.natche.cyderutils.files.indexer;

/** An exception thrown by {@link DirectoryIndexer}s if a file or folder cannot be found. */
public class ResourceNotFoundException extends RuntimeException {
    /** Constructs a new ResourceNotFoundException exception using the provided error message. */
    public ResourceNotFoundException(String errorMessage) {
        super(errorMessage);
    }

    /** Constructs a new ResourceNotFoundException exception from the provided exception. */
    public ResourceNotFoundException(Exception e) {
        super(e);
    }
}

