package com.github.natche.cyderutils.files;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.github.natche.cyderutils.constants.CyderRegexPatterns;
import com.github.natche.cyderutils.enumerations.Extension;
import com.github.natche.cyderutils.exceptions.FatalException;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.network.NetworkUtil;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.strings.StringUtil;
import com.github.natche.cyderutils.threads.CyderThreadFactory;
import com.github.natche.cyderutils.utils.ArrayUtil;
import com.github.natche.cyderutils.utils.OsUtil;
import net.lingala.zip4j.ZipFile;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Static utilities having to do with files, their names, properties, and attributes. */
public final class FileUtil {
    //todo could we generate these maybe?
    /**
     * The invalid COM names which files may not contain substrings of on Windows.
     * These exist in Windows for backwards compatibility.
     */
    public static final ImmutableList<String> invalidWindowsComNames = ImmutableList.of(
            "COM1", "COM2", "COM3",
            "COM4", "COM5", "COM6",
            "COM7", "COM8", "COM9"
    );

    /**
     * The invalid LPT (Line Printer Terminal) names which files may not contain substrings of on Windows.
     * These exist in Windows for backwards compatibility.
     */
    public static final ImmutableList<String> invalidWindowsLptNames = ImmutableList.of(
            "LPT1", "LPT2", "LPT3",
            "LPT4", "LPT5", "LPT6",
            "LPT7", "LPT8", "LPT9"
    );

    /**
     * The additional invalid Windows filenames aside from the COM and LPT names.
     * These exist in Windows for backwards compatibility.
     */
    public static final ImmutableList<String> otherInvalidWindowsNames = ImmutableList.of(
            "CON", "PRN", "AUX", "NUL"
    );

    /**
     * A list of the restricted windows filenames due to backwards
     * compatibility and the nature of "APIs are forever".
     */
    public static final ImmutableList<String> invalidWindowsFilenames = new ImmutableList.Builder<String>()
            .addAll(invalidWindowsComNames)
            .addAll(invalidWindowsLptNames)
            .addAll(otherInvalidWindowsNames)
            .build();

    /** The list of invalid characters for a file name on unix based systems. */
    public static final ImmutableList<String> invalidUnixFilenameChars = ImmutableList.of(
            "/", "<", ">", "|", "&", ":"
    );

    /** The metadata signature for a png file. */
    public static final ImmutableList<Integer> PNG_SIGNATURE
            = ImmutableList.of(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);

    /** The metadata signature for a jpg file. */
    public static final ImmutableList<Integer> JPG_SIGNATURE = ImmutableList.of(0xFF, 0xD8, 0xFF);

    /** The metadata signature for a wav file (RIFF). */
    public static final ImmutableList<Integer> WAV_SIGNATURE = ImmutableList.of(0x52, 0x49, 0x46, 0x46);

    /** The metadata signature for an mp3 file. */
    public static final ImmutableList<Integer> MP3_SIGNATURE = ImmutableList.of(0x49, 0x44, 0x33);

    /** The audio formats Cyder supports. */
    public static final ImmutableList<String> SUPPORTED_AUDIO_EXTENSIONS
            = ImmutableList.of(Extension.WAV.getExtension(), Extension.MP3.getExtension());

    /** The image formats Cyder supports. */
    public static final ImmutableList<String> SUPPORTED_IMAGE_EXTENSIONS = ImmutableList.of(
            Extension.PNG.getExtension(), Extension.JPG.getExtension(), Extension.JPEG.getExtension());

    /** Supported font types that are loaded upon Cyder's start. */
    public static final ImmutableList<String> SUPPORTED_FONT_EXTENSIONS
            = ImmutableList.of(Extension.TTF.getExtension());

    /** The signature for true-type font file formats. */
    public static final ImmutableList<Integer> TTF_SIGNATURE = ImmutableList.of(0x00, 0x01, 0x00, 0x00, 0x00);

    /** The regex string for extracting the filename from the extension. */
    private static final String filenameRegex = "\\.([^.]+)$";

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private FileUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns whether the provided file is a supported image file by validating
     * the file extension and the file byte signature.
     *
     * @param file the file to determine if it is a supported image type
     * @return whether the provided file is a supported image file
     */
    @Deprecated
    public static boolean isSupportedImageExtension(File file) {
        Preconditions.checkNotNull(file);

        String name = file.getName();
        if (StringUtil.isNullOrEmpty(name)) return false;

        String extension = getExtension(name);
        if (StringUtil.isNullOrEmpty(extension)) return false;

        boolean fileSignatureMatches =
                fileMatchesSignature(file, PNG_SIGNATURE) || fileMatchesSignature(file, JPG_SIGNATURE);
        boolean extensionValid = StringUtil.in(extension, true, SUPPORTED_IMAGE_EXTENSIONS);
        return extensionValid && fileSignatureMatches;
    }

    /**
     * Returns whether the provided file is a supported audio file by validating
     * the file extension and the file byte signature.
     *
     * @param file the filename to determine if it is a supported audio type
     * @return whether the provided file is a supported audio file
     */
    @Deprecated
    public static boolean isSupportedAudioExtension(File file) {
        Preconditions.checkNotNull(file);

        String name = file.getName();
        if (StringUtil.isNullOrEmpty(name)) return false;

        String extension = getExtension(name);
        if (StringUtil.isNullOrEmpty(extension)) return false;

        return StringUtil.in(extension, true, SUPPORTED_AUDIO_EXTENSIONS)
                && (fileMatchesSignature(file, WAV_SIGNATURE) || fileMatchesSignature(file, MP3_SIGNATURE));
    }

    /**
     * Returns whether the provided file is a supported font file.
     *
     * @param file the file to validate
     * @return whether the provided file is a supported font file
     */
    @Deprecated
    public static boolean isSupportedFontExtension(File file) {
        Preconditions.checkNotNull(file);

        String extension = getExtension(file.getName());
        if (StringUtil.isNullOrEmpty(extension)) return false;

        return StringUtil.in(extension, true, SUPPORTED_FONT_EXTENSIONS)
                && fileMatchesSignature(file, TTF_SIGNATURE);
    }

    /**
     * Returns whether the given file matches the provided signature.
     * Example: passing a png image and an integer array of "89 50 4E 47 0D 0A 1A 0A"
     * would return true.
     *
     * @param file              the file to validate
     * @param expectedSignature the expected file signature bytes
     * @return whether the given file matches the provided signature
     */
    public static boolean fileMatchesSignature(File file, ImmutableList<Integer> expectedSignature) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkNotNull(expectedSignature);
        Preconditions.checkArgument(!expectedSignature.isEmpty());

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            for (int n = 0 ; n < expectedSignature.size() ; n++) {
                int nthByte = inputStream.read();
                if (nthByte != expectedSignature.get(n)) return false;
            }
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    // todo FileReadingUtils

    /**
     * Returns a new {@link BufferedReader} for reading from the provided url.
     *
     * @param url the url to read from
     * @return a BufferedReader for reading from the provided url
     * @throws IOException              if an IOException occurs when opening the URLs stream
     * @throws NullPointerException     if the provided url is null
     * @throws IllegalArgumentException if the provided url is empty or invalid
     */
    public static BufferedReader bufferedReaderForUrl(String url) throws IOException {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.trim().isEmpty());
        Preconditions.checkArgument(CyderRegexPatterns.urlFormationPattern.matcher(url).matches());

        return new BufferedReader(new InputStreamReader(new URL(url).openStream()));
    }

    /**
     * Creates a new {@link BufferedInputStream} for the provided file to be read from.
     *
     * @param file the file
     * @return the buffered input stream
     * @throws FileNotFoundException    if the FileInputStream fails to find the provided file
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist
     */
    public static BufferedInputStream bisForFile(File file) throws FileNotFoundException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        return new BufferedInputStream(new FileInputStream(file));
    }

    /**
     * Returns a new {@link BufferedInputStream} for reading from the provided url.
     *
     * @param urlString the url string
     * @return the buffered input stream
     * @throws NullPointerException     if the provided urlString is null
     * @throws IllegalArgumentException if the provided urlString is empty or not a valid URL
     * @throws IOException              if an IOException occurs while opening the URLs stream
     */
    public static BufferedInputStream bisForUrl(String urlString) throws IOException {
        Preconditions.checkNotNull(urlString);
        Preconditions.checkArgument(!urlString.isEmpty());
        Preconditions.checkArgument(NetworkUtil.isValidUrl(urlString));

        return bisForUrl(new URL(urlString));
    }

    /**
     * Creates a new {@link BufferedInputStream} for reading from the provided url.
     *
     * @param url the url to read from
     * @return the buffered input stream
     * @throws NullPointerException if the provided url is null
     * @throws IOException          if an IOException occurs while opening the URLs stream
     */
    public static BufferedInputStream bisForUrl(URL url) throws IOException {
        Preconditions.checkNotNull(url);

        return new BufferedInputStream(url.openStream());
    }

    /**
     * Returns the first n number of bytes from the provided file.
     * If the file is less than numBytes, this method will break early and return
     *
     * @param file     the file to return n bytes from
     * @param numBytes the number of bytes to return
     * @return the requested number of bytes
     */
    public static ImmutableList<Integer> getBytes(File file, int numBytes) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(!file.isDirectory());
        Preconditions.checkArgument(numBytes > 0);

        ArrayList<Integer> bytes = new ArrayList<>(numBytes);

        try (BufferedInputStream bis = bisForFile(file)) {
            for (int i = 0 ; i < numBytes ; i++) {
                int b = bis.read();
                if (b == -1) break;
                bytes.add(b);
            }

            while (bytes.size() < numBytes) bytes.add(0x00);
            return ImmutableList.copyOf(bytes);
        } catch (IOException e) {
            throw new FatalException(e.getMessage());
        }
    }

    /**
     * Uses a regex to get the file name of the provided file, does not return the period.
     *
     * @param file the file of which to return the name of (this does not include the
     *             extension; use {@link File#getName()} )} to get the full filename + extension)
     * @return the file name requested
     */
    public static String getFilename(String file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(!file.isEmpty());

        return file.replaceAll(filenameRegex, "");
    }

    /**
     * Uses a regex to get the file extension of the provided file, returns the period too.
     *
     * @param file the name of the file of which to return the extension of
     * @return the file extension requested
     */
    public static String getExtension(String file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(!file.isEmpty());

        return file.replace(getFilename(file), "");
    }

    /**
     * Uses a regex to get the file name of the provided file, does not return the period.
     *
     * @param file the name of the file of which to return the name of (this does not include the
     *             extension; use {@link File#getName()})} to get the full filename + extension)
     * @return the file name requested
     */
    public static String getFilename(File file) {
        Preconditions.checkNotNull(file);

        return file.getName().replaceAll(filenameRegex, "");
    }

    /**
     * Uses a regex to get the file extension of the provided file, returns the period too.
     *
     * @param file the file of which to return the extension of
     * @return the file extension requested
     */
    public static String getExtension(File file) {
        Preconditions.checkNotNull(file);

        String filename = getFilename(file);

        return file.getName().substring(filename.length());
    }

    /**
     * Uses a regex to get the file extension of the provided file.
     *
     * @param file the file of which to return the extension of
     * @return the file extension requested
     */
    public static String getExtensionWithoutPeriod(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(!file.getName().isEmpty());

        String filename = getFilename(file);
        String withoutFilename = file.getName().replace(filename, "");
        if (withoutFilename.length() > 1) {
            return withoutFilename.substring(1);
        }

        return "";
    }

    /**
     * Returns whether the provided file ends in the expected extension.
     *
     * @param file              the file to validate the extension again
     * @param expectedExtension the expected extension such as ".json"
     * @return whether the provided file ends in the expected extension
     */
    public static boolean validateExtension(File file, String expectedExtension) {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(expectedExtension);
        Preconditions.checkArgument(!expectedExtension.isEmpty());

        return getExtension(file).equalsIgnoreCase(expectedExtension);
    }

    /**
     * Returns whether the provided file ends in one of the expected extensions.
     *
     * @param file               the file to validate the extension again
     * @param expectedExtensions the expected extensions such as ".json", ".mp3", ".png", etc
     * @return whether the provided file ends in one of the expected extension
     */
    public static boolean validateExtension(File file, String expectedExtension, String... expectedExtensions) {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(expectedExtension);
        Preconditions.checkArgument(!expectedExtension.isEmpty());
        Preconditions.checkNotNull(expectedExtensions);
        Preconditions.checkArgument(expectedExtensions.length > 0);

        ImmutableList<String> extensions = new ImmutableList.Builder<String>()
                .add(expectedExtension)
                .addAll(ArrayUtil.toList(expectedExtensions)).build();

        return StringUtil.in(getExtension(file), false, extensions);
    }

    /**
     * Returns whether the provided file ends in one of the expected extensions.
     *
     * @param file               the file to validate the extension again
     * @param expectedExtensions the expected extensions such as ".json", ".mp3", ".png", etc
     * @return whether the provided file ends in one of the expected extension
     */
    public static boolean validateExtension(File file, Collection<String> expectedExtensions) {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(expectedExtensions);
        Preconditions.checkArgument(expectedExtensions.size() > 0);

        return StringUtil.in(getExtension(file), false, expectedExtensions);
    }

    /**
     * Returns whether the file's name without the extension matches the expected name.
     *
     * @param file         the file
     * @param expectedName the expected name
     * @return whether the file's name without the extension matches the expected name
     */
    public static boolean validateFileName(File file, String expectedName) {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(expectedName);
        Preconditions.checkArgument(!expectedName.isEmpty());

        return getFilename(file).equals(expectedName);
    }

    /**
     * Returns whether the contents of the two files are equal.
     * If either of the files does not exist then false is returned.
     *
     * @param file    the first file
     * @param fileTwo the second file
     * @return whether the contents of the two file are equal
     * @throws NullPointerException iof either of the files are null
     */
    public static boolean fileContentsEqual(File file, File fileTwo) {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(fileTwo);

        if (!file.exists() || !fileTwo.exists()) return false;

        try {
            // todo static
            return com.google.common.io.Files.equal(file, fileTwo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // todo zip file util

    // todo this should be files obviously right?

    /**
     * Zips the provided file/folder and deletes the original if successful and requested.
     *
     * @param source      the file/dir to zip
     * @param destination the destination of the zip archive
     * @return whether the zip operation was successful
     */
    @CanIgnoreReturnValue
    public static boolean zip(String source, String destination) {
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(destination);
        Preconditions.checkArgument(!source.isEmpty());
        Preconditions.checkArgument(!destination.isEmpty());

        try {
            FileOutputStream fos = new FileOutputStream(destination);
            ZipOutputStream zos = new ZipOutputStream(fos);

            File fileToZip = new File(source);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zos.putNextEntry(zipEntry);

            byte[] bytes = DataUnit.KILOBYTE.getByteArray(1);
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

            closeIfNotNull(zos);
            closeIfNotNull(fis);
            closeIfNotNull(fos);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Unzips the provided zip directory to the provided directory.
     *
     * @param sourceZip         the source zip archive
     * @param destinationFolder the folder to extract the contents of the zip archive to
     * @return the file or list of files that were extracted
     */
    @CanIgnoreReturnValue
    public static ImmutableList<File> unzip(File sourceZip, File destinationFolder) {
        Preconditions.checkNotNull(sourceZip);
        Preconditions.checkNotNull(destinationFolder);
        Preconditions.checkArgument(sourceZip.exists());
        Preconditions.checkArgument(destinationFolder.exists());

        try (ZipFile zipFile = new ZipFile(sourceZip)) {
            zipFile.extractAll(destinationFolder.getAbsolutePath());
        } catch (IOException e) {}

        return null; // todo fix me, will probably need to use a directory watcher?
    }

    // todo could be some kind of a chained op like
    // ClosableUtil.closeIfOpen(myClosable)
    // .elseIfAlreadyClosed(() -> {})
    // .onError((error) -> {});

    /**
     * Closes the provided object which implements {@link Closeable}.
     *
     * @param closable the object to close and free
     */
    public static void closeIfNotNull(Closeable closable) {
        if (closable == null) return;
        try {
            closable.close();
        } catch (IOException ignored) {}
    }

    /**
     * Constructs a unique file with which the caller can create, and write to, safely.
     * The provided filenameAndExtension may be void of an extension if desired.
     *
     * @param filenameAndExtension the proposed original filename and extension
     *                             (if applicable) such as "file.txt" or "file"
     * @param directory            the directory the file will be created in
     * @return a File object representing a unique File object which can be safely
     * created and written to in the provided directory
     */
    public static File constructUniqueName(String filenameAndExtension, File directory) {
        Preconditions.checkNotNull(filenameAndExtension);
        Preconditions.checkArgument(!filenameAndExtension.trim().isEmpty());
        Preconditions.checkNotNull(directory);
        Preconditions.checkArgument(directory.exists());
        Preconditions.checkArgument(directory.isDirectory());

        File[] files = directory.listFiles();
        if (ArrayUtil.nullOrEmpty(files)) {
            return OsUtil.buildFile(directory.getAbsolutePath(), filenameAndExtension);
        }

        ImmutableList<String> filenames = ImmutableList.copyOf(
                Arrays.stream(files).map(File::getName).collect(Collectors.toList()));

        int lastPeriodIndex = filenameAndExtension.lastIndexOf(".");
        if (lastPeriodIndex == -1) {
            String ret = filenameAndExtension;
            int number = 1;
            while (StringUtil.in(ret, true, filenames)) {
                ret = filenameAndExtension + "_" + number;
                number++;
            }

            return OsUtil.buildFile(directory.getAbsolutePath(), ret);
        }

        String name = filenameAndExtension.substring(0, lastPeriodIndex);
        String extension = filenameAndExtension.substring(lastPeriodIndex);

        String ret = filenameAndExtension;
        int number = 1;
        while (StringUtil.in(ret, true, filenames)) {
            ret = name + "_" + number + extension;
            number++;
        }

        return OsUtil.buildFile(directory.getAbsolutePath(), ret);
    }

    /**
     * Returns an immutable list of files found within the provided directory and all sub-directories.
     *
     * @param topLevelDirectory the top level directory to search for files in
     * @return an immutable list of files found within the provided directory and all sub-directories
     */
    public static ImmutableList<File> getFiles(File topLevelDirectory) {
        return getFiles(topLevelDirectory, true);
    }

    /*
     * Returns an immutable list of files found within the provided directory.
     *
     * @param topLevelDirectory the top level directory to search for files in
     * @param recursive whether to find files recursively starting from the provided directory
     * @return an immutable list of files found within the provided directory
     */
    public static ImmutableList<File> getFiles(File topLevelDirectory, boolean recursive) {
        return getFiles(topLevelDirectory, recursive, "");
    }

    /**
     * Returns an immutable list of files found within the provided directory.
     *
     * @param topLevelDirectory the top level directory to search for files in
     * @param recursive         whether to find files recursively starting from the provided directory
     * @param extensionRegex    the regex to match extensions for such as "(txt|jpg)". leave blank for all extensions
     * @return an immutable list of files found within the provided directory
     */
    public static ImmutableList<File> getFiles(File topLevelDirectory, boolean recursive, String extensionRegex) {
        Preconditions.checkNotNull(topLevelDirectory);
        Preconditions.checkArgument(topLevelDirectory.exists());
        Preconditions.checkArgument(topLevelDirectory.isDirectory());
        Preconditions.checkNotNull(extensionRegex);

        File[] topLevelFiles = topLevelDirectory.listFiles();
        if (topLevelFiles == null || ArrayUtil.isEmpty(topLevelFiles)) return ImmutableList.of();

        ArrayList<File> ret = new ArrayList<>();

        Arrays.stream(topLevelFiles).forEach(file -> {
            if (file.isFile()) {
                String extension = FileUtil.getExtension(file).replace("\\.", "");
                if (extensionRegex.isEmpty() || extension.matches(extensionRegex)) ret.add(file);
            } else if (recursive && file.isDirectory()) {
                ret.addAll(getFiles(file, true, extensionRegex));
            }
        });

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns a list of folders found within the provided directory.
     *
     * @param topLevelDirectory the directory to find folders within
     * @return a list of folders found within the provided directory
     */
    public static ImmutableList<File> getFolders(File topLevelDirectory) {
        return getFolders(topLevelDirectory, true);
    }

    /**
     * Returns a list of folders found within the provided directory.
     *
     * @param topLevelDirectory the directory to find folders within
     * @param recursive         whether to recurse from the top level directory
     * @return a list of folders found within the provided directory
     */
    public static ImmutableList<File> getFolders(File topLevelDirectory, boolean recursive) {
        Preconditions.checkNotNull(topLevelDirectory);
        Preconditions.checkArgument(topLevelDirectory.exists());
        Preconditions.checkArgument(topLevelDirectory.isDirectory());

        ArrayList<File> ret = new ArrayList<>();

        File[] topLevelFiles = topLevelDirectory.listFiles();

        if (topLevelFiles != null && topLevelFiles.length > 0) {
            Arrays.stream(topLevelFiles).forEach(file -> {
                if (!file.isDirectory()) return;
                ret.add(file);
                if (recursive) ret.addAll(getFolders(file, true));
            });
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Reads the contents of the provided file and returns the collective contents in a singular string.
     *
     * @param file the file whose contents to read
     * @return the contents of the file
     * @throws IOException if reading the file fails
     */
    public static String readFileContents(File file) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        return Files.readString(Path.of(file.getAbsolutePath()));
    }

    /**
     * Returns a hex string for the provided binary file.
     *
     * @param file the binary file of pure binary contents
     * @return the String of hex data from the file
     */
    public static String getHexString(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.BIN.getExtension()));

        try {
            BufferedReader fis = new BufferedReader(new FileReader(file));
            String[] stringBytes = fis.readLine().split("(?<=\\G........)");
            StringBuilder sb = new StringBuilder();

            for (String stringByte : stringBytes) {
                sb.append(Integer.toString(Integer.parseInt(stringByte, 2), 16));
            }

            fis.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new FatalException("Could not read binary file");
    }

    /**
     * Returns a binary string for the provided binary file.
     *
     * @param file the binary file of pure binary contents
     * @return the String of binary data from the file
     */
    public static String getBinaryString(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.BIN.getExtension()));

        try {
            BufferedReader fis = new BufferedReader(new FileReader(file));
            String stringBytes = fis.readLine();
            fis.close();
            return stringBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new FatalException("Could not read binary file");
    }

    /**
     * Opens the provided resource.
     *
     * @param resource           the resource to open
     * @param allowCyderHandlers whether to attempt to open the resource using a Cyder handler if possible
     * @return whether the file opening process was successful
     */
    @CanIgnoreReturnValue
    public static Future<Boolean> openResource(String resource, boolean allowCyderHandlers) {
        Preconditions.checkNotNull(resource);
        Preconditions.checkArgument(!resource.isEmpty());

        String threadName = "Resource opener: " + resource;
        return Executors.newSingleThreadExecutor(new CyderThreadFactory(threadName)).submit(() -> {
            File referenceFile = new File(resource);
            boolean referenceFileExists = referenceFile.exists();

            try {
                if (referenceFileExists && allowCyderHandlers) {
                    for (CyderFileHandler handler : CyderFileHandler.values()) {
                        if (handler.shouldUseForFile(referenceFile)) {
                            return handler.open(referenceFile);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return openResourceUsingNativeProgram(resource);
        });
    }

    /**
     * Opens the provided resource using the native {@link Desktop}.
     * This could be a file, directory, url, link, etc.
     *
     * @param resource the resource to open
     * @return whether the file opening process was successful
     */
    @CanIgnoreReturnValue
    public static boolean openResourceUsingNativeProgram(String resource) {
        Preconditions.checkNotNull(resource);
        Preconditions.checkArgument(!resource.isEmpty());

        try {
            File filePointer = new File(resource);

            if (filePointer.exists()) {
                Desktop.getDesktop().open(filePointer);
            } else {
                Desktop.getDesktop().browse(new URI(resource));
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Returns a list of lines from the provided file.
     *
     * @param file a file
     * @return a list of lines from the provided file
     */
    public static ImmutableList<String> getFileLines(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());

        ArrayList<String> ret = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ret.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Writes the provided lines to the provided file with newlines after each written line.
     *
     * @param file   the file to write the lines too
     * @param lines  the lines to write
     * @param append whether to append the lines or overwrite the existing content, if any, with the new content
     */
    public static void writeLinesToFile(File file, List<String> lines, boolean append) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());
        Preconditions.checkNotNull(lines);
        Preconditions.checkArgument(!lines.isEmpty());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the size of the file/directory in bytes.
     * If the provided file is a directory, the sum of contained files is returned recursively.
     * Empty directories are reported as length 0, files which do not exist are also reported as 0.
     *
     * @param file the file
     * @return the size of the file in bytes
     * @throws NullPointerException if the provided file is null
     * @throws IOException          if an IOException occurs when calling {@link Files#size(Path)}
     */
    public static long getFileSize(File file) throws IOException {
        Preconditions.checkNotNull(file);
        if (!file.exists()) return 0L;
        if (file.isFile()) return Files.size(Paths.get(file.getAbsolutePath()));

        AtomicLong ret = new AtomicLong(0L);
        File[] files = file.listFiles();
        if (files != null) Arrays.stream(files).forEach(child -> {
            try {
                long childSize = getFileSize(child);
                ret.addAndGet(childSize);
            } catch (IOException ignored) {}
        });
        return ret.get();
    }

    /**
     * Returns the total bytes of the file if found. Zero else.
     *
     * @param file the file to find the total bytes of
     * @return the total bytes of the file
     * @throws IOException if an exception occurs when reading the file
     */
    public static long getTotalBytes(File file) throws IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        try (FileInputStream fis = new FileInputStream((file))) {
            return fis.available();
        }
    }

    /**
     * Returns whether the provided filename is valid for the operating system
     * Cyder is currently running on.
     *
     * @param filename the desired filename
     * @return whether the provided filename is valid for the operating system
     * Cyder is currently running on
     */
    public static boolean isValidFilename(String filename) {
        Preconditions.checkNotNull(filename);
        Preconditions.checkArgument(!filename.isEmpty());

        final String finalFilename = filename.trim();

        switch (OsUtil.OPERATING_SYSTEM) {
            case MAC -> {
                return !filename.contains("/")
                        && !filename.contains(CyderStrings.nullChar);
            }
            case WINDOWS -> {
                if (filename.matches(CyderRegexPatterns.windowsInvalidFilenameChars.pattern())) return false;
                if (invalidWindowsFilenames.stream().anyMatch(finalFilename::equalsIgnoreCase)) return false;
                if (!filename.contains(".")) return !filename.endsWith(".");

                return Arrays.stream(filename.split("\\."))
                        .anyMatch(part -> invalidWindowsFilenames.stream().noneMatch(part::equalsIgnoreCase));
            }
            case GNU_LINUX -> {
                if (invalidUnixFilenameChars.stream().anyMatch(filename::contains)) return false;
            }
            case UNKNOWN ->
                    throw new UnsupportedOsException("Unknown operating system: " + OsUtil.OPERATING_SYSTEM_NAME);
        }

        return true;
    }

    /**
     * Creates a new file pointer from the provided file pointer with the new extension.
     * Note, the file is not automatically created if it does not exist.
     *
     * @param file         the file
     * @param newExtension the new extension
     * @return a new file pointer with all the same information regarding the
     * path and filename but with the new extension
     * @throws NullPointerException if the provided file or extension are null
     * @throws IllegalArgumentException if the provided file is not a file or the newExtension is empty
     */
    public static File swapExtension(File file, String newExtension) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.isFile());
        Preconditions.checkNotNull(newExtension);
        Preconditions.checkArgument(!newExtension.trim().isEmpty());

        String oldFilePath = file.getAbsolutePath();
        String fileNameWithoutExtension = oldFilePath.replaceFirst("[.][^.]+$", "");
        String newFileName = fileNameWithoutExtension + "." + newExtension;

        return new File(newFileName);
    }

    /**
     * Adds the provided suffix to the end of the filename before the extension.
     * Note, the file is not automatically created if it does not exist.
     *
     * @param file the file to add a suffix to the name of
     * @param suffix the suffix to append
     * @return a new file pointer to the suffixed filename
     * @throws NullPointerException if the provided file or string are null
     * @throws IllegalArgumentException if the provided file is not a file or the suffix is empty
     * @throws IllegalStateException if the calculated new filename is invalid
     */
    public static File addSuffixToFilename(File file, String suffix) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.isFile());
        Preconditions.checkNotNull(suffix);
        Preconditions.checkArgument(!suffix.trim().isEmpty());

        String fileName = file.getName();
        String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        String newFileName = nameWithoutExtension + suffix + extension;
        if (!isValidFilename(newFileName))
            throw new IllegalStateException("Proposed new filename is invalid: " + newFileName);
        return new File(file.getParent(), newFileName);
    }
}
