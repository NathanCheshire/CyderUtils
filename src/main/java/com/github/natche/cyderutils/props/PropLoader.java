package com.github.natche.cyderutils.props;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CheckReturnValue;
import com.github.natche.cyderutils.exceptions.FatalException;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.files.FileUtil;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.strings.StringUtil;
import com.github.natche.cyderutils.utils.ArrayUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;

import static com.github.natche.cyderutils.props.PropConstants.*;

/** A class for loading props from prop files from the props directory for usage throughout Cyder. */
public final class PropLoader {
    /** The props map of keys to the string values which require casting. */
    private static ImmutableMap<String, String> props = ImmutableMap.of();

    /** The instant at which the props were last loaded/reloaded. */
    private static Instant loadedInstant = Instant.now();

    /** Whether the props have been loaded. */
    private static boolean propsLoaded;

    /** Suppress default constructor. */
    private PropLoader() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Reloads the props from the found prop files.
     * Note this does not check whether reloading props is permitted.
     * The caller is required to validate that before invoking this method.
     */
    public static void reloadProps() {
        propsLoaded = false;

        props = ImmutableMap.of();
        loadProps();
        loadedInstant = Instant.now();
    }

    /**
     * Returns the props list size.
     *
     * @return the props list
     */
    public static int getPropsSize() {
        return props.size();
    }

    /**
     * Returns the instant at which the props were last loaded.
     *
     * @return the instant at which the props were last loaded
     */
    public static Instant getLoadedInstant() {
        return loadedInstant;
    }

    /**
     * Returns the value string for the prop with the provided key from the props list if found. Empty optional else.
     *
     * @param key the key of the prop to find within the present prop files
     * @return the prop value string from the located prop file is present. Empty optional else
     */
    @CheckReturnValue
    static Optional<String> getPropValueStringFromFile(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(!key.isEmpty());

        if (props.containsKey(key)) {
            String value = props.get(key);
            if (value == null) return Optional.empty();
            return Optional.of(value);
        }

        return Optional.empty();
    }

    /**
     * Loads the props from the {@link PropConstants#localPropsDirectory}.
     * This overwrites any previously loaded props.
     */
    private static void loadProps() {
        loadProps(localPropsDirectory);
    }

    /**
     * Discovers and returns a list of prop files from the provided directory.
     *
     * @param directory the directory to discover prop files in
     * @return a list of prop files from the provided directory
     */
    static ImmutableList<File> discoverPropFiles(File directory) {
        Preconditions.checkNotNull(directory);
        Preconditions.checkArgument(directory.exists());
        Preconditions.checkArgument(directory.isDirectory());

        ArrayList<File> ret = new ArrayList<>();

        File[] files = directory.listFiles();

        if (ArrayUtil.nullOrEmpty(files)) {
            return ImmutableList.of();
        }

        Arrays.stream(files)
                .filter(file -> file.getName().startsWith(propFilePrefix))
                .filter(file -> FileUtil.validateExtension(file, propExtension))
                .forEach(ret::add);

        return ImmutableList.copyOf(ret);
    }

    /**
     * Loads the props from all discovered prop files within the provided directory.
     * This overwrites any previously loaded props.
     *
     * @param propsDirectory the directory to discover prop files from and load
     */
    @SuppressWarnings("SameParameterValue") /* Currently this is always called from loadProps() */
    private static void loadProps(File propsDirectory) {
        Preconditions.checkNotNull(propsDirectory);
        Preconditions.checkArgument(propsDirectory.exists());
        Preconditions.checkArgument(propsDirectory.isDirectory());
        Preconditions.checkState(!propsLoaded);

        props = extractPropsFromDirectory(propsDirectory);
        propsLoaded = true;
    }

    /**
     * Returns a map of all the props loaded from prop files contained in the provided directory.
     *
     * @param propsDirectory the directory to discovery prop files in
     * @return the map of prop keys to values
     */
    static ImmutableMap<String, String> extractPropsFromDirectory(File propsDirectory) {
        Preconditions.checkNotNull(propsDirectory);
        Preconditions.checkArgument(propsDirectory.exists());
        Preconditions.checkArgument(propsDirectory.isDirectory());

        LinkedHashMap<String, String> ret = new LinkedHashMap<>();

        discoverPropFiles(propsDirectory)
                .forEach(propFile -> {
                    // todo Logger.log(LogTag.PROPS_ACTION, "Discovered prop file: " + propFile.getAbsolutePath());
                    ret.putAll(extractPropsFromFile(propFile));
                });

        return ImmutableMap.copyOf(ret);
    }

    /**
     * Extracts the props found from the provided prop file into an immutable map.
     *
     * @param propFile the prop file to extract the props from
     * @return an immutable map of key strings to value strings representing the props found in the provided file
     */
    private static ImmutableMap<String, String> extractPropsFromFile(File propFile) {
        Preconditions.checkNotNull(propFile);
        Preconditions.checkArgument(propFile.exists());
        Preconditions.checkArgument(propFile.isFile());
        Preconditions.checkArgument(propFile.getName().startsWith(propFilePrefix));
        Preconditions.checkArgument(FileUtil.validateExtension(propFile, propExtension));

        LinkedHashMap<String, String> ret = new LinkedHashMap<>();

        ImmutableList<String> currentFileLines = ImmutableList.of();

        try {
            String fileContents = FileUtil.readFileContents(propFile);
            currentFileLines = ImmutableList.copyOf(fileContents.split(splitPropFileContentsAt));
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean logNextProp = true;
        StringBuilder previousLinesOfMultilineProp = new StringBuilder();

        for (String line : currentFileLines) {
            if (isComment(line)) {
                continue;
            } else if (StringUtil.isNullOrEmpty(line)) {
                continue;
            } else if (isNoLogAnnotation(line)) {
                logNextProp = false;
                continue;
            } else if (line.endsWith(multiLinePropSuffix)) {
                previousLinesOfMultilineProp.append(line.substring(0, line.length() - 1).trim());
                continue;
            }

            String fullLine = previousLinesOfMultilineProp.toString();
            fullLine += fullLine.isEmpty() ? line : line.stripLeading();

            Pair<String, String> extractedKeyValue = extractPropFromLine(fullLine);
            String key = extractedKeyValue.getKey();
            String value = extractedKeyValue.getValue();

            if (ret.containsKey(key)) {
                throw new FatalException("Duplicate prop found: " + extractedKeyValue);
            }

            ret.put(key, value);

            String logValue = logNextProp ? ", value: " + value : "";
            // todo Logger.log(LogTag.PROPS_ACTION, "key: " + key + logValue);

            logNextProp = true;
            previousLinesOfMultilineProp = new StringBuilder();
        }

        return ImmutableMap.copyOf(ret);
    }

    /**
     * Returns whether provided line is indicative of a comment in a prop file
     *
     * @param line the line to parse a prop from
     * @return whether the provided line is a comment
     */
    static boolean isComment(String line) {
        Preconditions.checkNotNull(line);

        return line.trim().startsWith(commentPrefix);
    }

    /**
     * Returns whether the provided line is a "no log" annotation.
     *
     * @param line the line to parse a prop from
     * @return whether the provided line is a no log annotation
     */
    static boolean isNoLogAnnotation(String line) {
        Preconditions.checkNotNull(line);

        return line.trim().equals(Annotation.NO_LOG.getAnnotation());
    }

    /**
     * Attempts to extract a prop key and value from the provided line.
     * The first unescaped colon is what dictates where the line is split into
     * the key and value.
     *
     * @param line the line to extract the prop from
     * @return a pair containing the prop key and value
     */
    static Pair<String, String> extractPropFromLine(String line) {
        Preconditions.checkNotNull(line);
        Preconditions.checkArgument(!line.isEmpty());
        Preconditions.checkArgument(line.contains(keyValueSeparator),
                "Could not find "
                        + "\"" + keyValueSeparator + "\"" + " in prop line: "
                        + "\"" + line + "\"" + ". Every prop key/value pair must be separated by a "
                        + "\"" + keyValueSeparator + "\"" + " character.");

        String[] parts = line.split(keyValueSeparator);
        Preconditions.checkArgument(parts.length > 1);

        if (parts.length == 2) {
            String key = parts[0].trim();
            String value = parts[1].trim();
            return Pair.of(key, value);
        }

        // Figure out where key ends and value starts
        int firstValueIndex = -1;
        for (int i = 0 ; i < parts.length - 1 ; i++) {
            if (parts[i].endsWith(escapeSequence)) {
                parts[i] = parts[i].substring(0, parts[i].length() - 1);
                continue;
            }

            firstValueIndex = i;
            break;
        }

        if (firstValueIndex == -1) {
            throw new IllegalStateException("Could not parse line: " + line);
        }

        StringBuilder keyBuilder = new StringBuilder();
        for (int i = 0 ; i <= firstValueIndex ; i++) {
            keyBuilder.append(parts[i]);

            if (i != firstValueIndex) {
                keyBuilder.append(keyValueSeparator);
            }
        }

        StringBuilder valueBuilder = new StringBuilder();
        for (int i = firstValueIndex + 1 ; i < parts.length ; i++) {
            valueBuilder.append(parts[i]);

            if (i != parts.length - 1) {
                valueBuilder.append(keyValueSeparator);
            }
        }

        return Pair.of(keyBuilder.toString().trim(), valueBuilder.toString().trim());
    }
}
