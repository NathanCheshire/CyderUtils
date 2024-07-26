package com.github.natche.cyderutils.files.indexer;

import com.github.natche.cyderutils.files.FileUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.File;

/** A directory indexer for quickly accessing files and directories based on names or partial paths. */
public final class DirectoryIndexer {
    /** The top level directory of which the internal files and directories, recursively, are indexed from. */
    private final File topLevelDirectory;

    /** The map of static files located. */
    private ImmutableMap<String, File> files = ImmutableMap.of();

    /** The map of static folders located. */
    private ImmutableMap<String, File> folders = ImmutableMap.of();

    /**
     * Constructs a new DirectoryIndexer using the provided directory as the top level directory.
     *
     * @param directory the top level directory
     * @throws NullPointerException     if the provided directory is null
     * @throws IllegalArgumentException if the provided directory does not exist or is not a directory
     */
    public DirectoryIndexer(File directory) {
        Preconditions.checkNotNull(directory);
        Preconditions.checkArgument(directory.exists());
        Preconditions.checkArgument(directory.isDirectory());

        this.topLevelDirectory = directory;
        refreshIndices();
    }

    /**
     * Constructs a new DirectoryIndexer using the folder at the provided path as the top level directory.
     *
     * @param directoryPath the path to the directory
     * @return a new DirectoryIndexer
     * @throws NullPointerException     if the provided string is null
     * @throws IllegalArgumentException if the provided string is empty
     */
    public static DirectoryIndexer from(String directoryPath) {
        Preconditions.checkNotNull(directoryPath);
        Preconditions.checkArgument(!directoryPath.isEmpty());

        return new DirectoryIndexer(new File(directoryPath));
    }

    /**
     * Constructs a new DirectoryIndexer using the provided directory as the top level directory.
     * This method is equivalent to {@link DirectoryIndexer#DirectoryIndexer(File)}
     *
     * @param directory the top level directory
     * @return a new DirectoryIndexer
     * @throws NullPointerException     if the provided directory is null
     * @throws IllegalArgumentException if the provided directory does not exist or is not a directory
     */
    public static DirectoryIndexer from(File directory) {
        Preconditions.checkNotNull(directory);
        Preconditions.checkArgument(directory.exists());
        Preconditions.checkArgument(directory.isDirectory());

        return new DirectoryIndexer(directory);
    }

    /** Refreshes the hash maps for indexing files based on names, paths, or partial paths. */
    public void refreshIndices() {
        ImmutableList<File> files = FileUtil.getFiles(topLevelDirectory, true);
        ImmutableMap.Builder<String, File> fileMap = ImmutableMap.builder();
        files.forEach((file) -> fileMap.put(formatPath(file), file));
        this.files = fileMap.build();

        ImmutableList<File> folders = FileUtil.getFolders(topLevelDirectory, true);
        ImmutableMap.Builder<String, File> folderMap = ImmutableMap.builder();
        folders.forEach((folder) -> fileMap.put(formatPath(folder), folder));
        this.folders = folderMap.build();
    }

    /**
     * Returns the indexed file with the provided filename/path/partial path.
     *
     * @param filename the filename
     * @return the indexed file if found
     * @throws ResourceNotFoundException if a file cannot be found
     * @throws NullPointerException      if the provided string is null
     * @throws IllegalArgumentException  if the provided string is empty
     */
    public File getFile(String filename) throws ResourceNotFoundException {
        Preconditions.checkNotNull(filename);
        Preconditions.checkArgument(!filename.trim().isEmpty());

        if (files.containsKey(filename)) return files.get(filename);

        for (String key : files.keySet()) {
            if (key.contains(filename)) return files.get(filename);
        }

        throw ResourceNotFoundException.throwFromMessage(
                "File not found matching or partially matching " + filename);
    }

    /**
     * Returns the indexed folder with the provided folder name/path/partial path.
     *
     * @param folderName the folder name
     * @return the indexed folder if found
     * @throws ResourceNotFoundException if a folder cannot be found
     * @throws NullPointerException      if the provided string is null
     * @throws IllegalArgumentException  if the provided string is empty
     */
    public File getFolder(String folderName) {
        Preconditions.checkNotNull(folderName);
        Preconditions.checkArgument(!folderName.trim().isEmpty());

        if (folders.containsKey(folderName)) return folders.get(folderName);

        for (String key : folders.keySet()) {
            if (key.contains(folderName)) return folders.get(folderName);
        }

        throw new ResourceNotFoundException("Folder not found matching or partially matching " + folderName);
    }

    /**
     * Returns a list of files indexed with the provided extension.
     *
     * @param extension the extension such as ".mp3" or "mp3". Truthfully, the provided string is used for a suffix check
     * @return the list of files, if any, with the provided extension
     * @throws NullPointerException     if the provided extension is null
     * @throws IllegalArgumentException if the provided extension is empty or only whitespace
     */
    public ImmutableList<File> getFilesOfExtension(String extension) {
        Preconditions.checkNotNull(extension);
        Preconditions.checkArgument(!extension.trim().isEmpty());

        ImmutableList.Builder<File> fileBuilder = new ImmutableList.Builder<>();
        files.forEach((path, file) -> {
            if (path.endsWith(extension)) fileBuilder.add(file);
        });

        return fileBuilder.build();
    }

    /**
     * Returns the path to the resource with the provided name.
     *
     * @param filename the name of the file such as "TheOffice.mp4"
     * @return the path to the file with the provided filename
     * @throws NullPointerException     if the provided filename is null
     * @throws IllegalArgumentException if the provided filename is empty or only whitespace
     */
    public String getFilePath(String filename) {
        Preconditions.checkNotNull(filename);
        Preconditions.checkArgument(!filename.trim().isEmpty());

        return getFile(filename).getAbsolutePath();
    }

    /**
     * Returns the path to the folder with the provided name.
     *
     * @param folderName the name of the folder such as "OfficeSupplies"
     * @return the path to the folder with the provided name
     * @throws NullPointerException     if the provided folder name is null
     * @throws IllegalArgumentException if the provided folder name is empty or only whitespace
     */
    public String getFolderPath(String folderName) {
        Preconditions.checkNotNull(folderName);
        Preconditions.checkArgument(!folderName.trim().isEmpty());

        return getFolder(folderName).getAbsolutePath();
    }

    /**
     * Formats the provided file or directory to the key format we expect, that of paths
     * separated by forward slashes such as "path/to/my/file.txt" or "path/to/my/directory".
     *
     * @param fileOrDirectory the file or directory
     * @return the formatted path to use a key
     */
    private static String formatPath(File fileOrDirectory) {
        return fileOrDirectory.getAbsolutePath().replaceAll("\\\\+", "/").toLowerCase();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof DirectoryIndexer)) {
            return false;
        }

        DirectoryIndexer other = (DirectoryIndexer) o;
        return other.topLevelDirectory.equals(topLevelDirectory)
                && other.folders.size() == folders.size()
                && other.files.size() == files.size();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "DirectoryIndexer{"
                + "topLevelDirectory=\"" + topLevelDirectory + "\", "
                + "files=\"" + files.size() + "\", "
                + "folders=\"" + folders.size() + "\", "
                + "}";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = topLevelDirectory.hashCode();
        ret = 31 * ret + Integer.hashCode(files.size());
        ret = 31 * ret + Integer.hashCode(folders.size());
        return ret;
    }
}
