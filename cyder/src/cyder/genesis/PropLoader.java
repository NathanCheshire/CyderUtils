package cyder.genesis;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * A class for loading ini props from props.ini used throughout Cyder.
 */
public class PropLoader {
    /**
     * The props files to parse props from.
     */
    private static final ImmutableList<File> propFiles =
            ImmutableList.of(new File("props.ini"), new File("propkeys.ini"));

    /**
     * Lines which start with this are marked as a comment and not parsed as props.
     */
    public static final String commentString = "#";

    /**
     * Suppress default constructor.
     */
    private PropLoader() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * A prop object mapping a key to a value of the props.ini file.
     */
    public static record Prop(String key, String value) {
    }

    /**
     * The props immutable list.
     */
    private static ImmutableList<Prop> props;

    /**
     * Returns the props list.
     *
     * @return the props list
     */
    public static ImmutableList<Prop> getProps() {
        return props;
    }

    /**
     * Whether to log the next prop that is loaded.
     * Props which should not be logged when loaded should be
     * annotated with the @no_log annotation.
     */
    private static boolean logNextProp = true;

    /**
     * Whether the props have been loaded.
     */
    private static boolean propsLoaded = false;

    /**
     * Returns whether the props have been loaded.
     *
     * @return whether the props have been loaded
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean arePropsLoaded() {
        return propsLoaded;
    }

    /**
     * Loads the props from all prop files.
     */
    public static void loadProps() {
        Preconditions.checkArgument(!propsLoaded);

        for (File f : propFiles) {
            Preconditions.checkArgument(f.exists(), "Could not find prop file: " + f.getName());
        }

        try {
            ArrayList<Prop> propsList = new ArrayList<>();

            for (File propFile : propFiles) {
                BufferedReader reader = new BufferedReader(new FileReader(propFile));
                String line;

                while ((line = reader.readLine()) != null) {
                    // comment
                    if (line.trim().startsWith(commentString)) {
                        continue;
                    }
                    // blank line
                    else if (line.trim().length() == 0) {
                        continue;
                    }
                    // hide next prop value
                    else if (line.trim().equals("@no_log")) {
                        logNextProp = false;
                        continue;
                    }

                    String[] parts = line.split(":");

                    Prop addProp;

                    if (parts.length < 2) {
                        throw new IllegalStateException("Could not parse line: " + line);
                    } else if (parts.length == 2) {
                        addProp = new Prop(parts[0].trim(), parts[1].trim());
                    } else {
                        int lastKeyIndex = -1;

                        for (int i = 0 ; i < parts.length - 1 ; i++) {
                            // if it's an escaped comma, continue
                            if (parts[i].endsWith("\\")) {
                                parts[i] = parts[i].substring(0, parts[i].length() - 1);
                                continue;
                            }

                            // should be real comma so ensure not already set
                            if (lastKeyIndex != -1)
                                throw new IllegalStateException("Could not parse line: " + line);

                            // set last index of key parts
                            lastKeyIndex = i;
                        }

                        if (lastKeyIndex == -1) {
                            throw new IllegalStateException("Could not parse line: " + line);
                        }

                        StringBuilder key = new StringBuilder();
                        StringBuilder value = new StringBuilder();

                        for (int i = 0 ; i <= lastKeyIndex ; i++) {
                            key.append(parts[i]);

                            if (i != lastKeyIndex) {
                                key.append(":");
                            }
                        }

                        for (int i = lastKeyIndex + 1 ; i < parts.length ; i++) {
                            value.append(parts[i]);

                            if (i != parts.length - 1) {
                                value.append(":");
                            }
                        }

                        addProp = new Prop(key.toString().trim(), value.toString().trim());
                    }

                    propsList.add(addProp);

                    if (logNextProp) {
                        Logger.log(Logger.Tag.PROP_LOADED, addProp);
                    } else {
                        Logger.log(Logger.Tag.PROP_LOADED, "key = " + addProp.key + ", value = HIDDEN");
                    }

                    logNextProp = true;
                }

                reader.close();
            }

            props = ImmutableList.copyOf(propsList);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            props = ImmutableList.of();
        } finally {
            propsLoaded = true;
        }
    }

    /**
     * Returns the prop value with the provided key.
     *
     * @param key the key to get the prop value of
     * @return the prop value with the provided key
     */
    public static String getString(String key) {
        Preconditions.checkArgument(propsLoaded);

        for (Prop prop : props) {
            if (prop.key.equals(key)) {
                return prop.value;
            }
        }

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + "\"");
    }

    /**
     * Returns the prop value with the provided key.
     *
     * @param key the key to get the prop value of
     * @return the prop value with the provided key
     */
    public static boolean getBoolean(String key) {
        Preconditions.checkArgument(propsLoaded);

        for (Prop prop : props) {
            if (prop.key.equals(key)) {
                return prop.value.equals("1") || prop.value.equalsIgnoreCase("true");
            }
        }

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + "\"");
    }

    /**
     * Returns the prop value with the provided key.
     *
     * @param key the key to get the prop value of
     * @return the prop value with the provided key
     */
    public static int getInteger(String key) {
        Preconditions.checkArgument(propsLoaded);

        for (Prop prop : props) {
            if (prop.key.equals(key)) {
                return Integer.parseInt(prop.value);
            }
        }

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + "\"");
    }

    /**
     * Returns the prop value with the provided key.
     *
     * @param key the key to get the prop value of
     * @return the prop value with the provided key
     */
    public static float getFloat(String key) {
        Preconditions.checkArgument(propsLoaded);

        for (Prop prop : props) {
            if (prop.key.equals(key)) {
                return Float.parseFloat(prop.value);
            }
        }

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + "\"");
    }

    /**
     * Returns the prop value with the provided key.
     *
     * @param key the key to get the prop value of
     * @return the prop value with the provided key
     */
    public static double getDouble(String key) {
        Preconditions.checkArgument(propsLoaded);

        for (Prop prop : props) {
            if (prop.key.equals(key)) {
                return Double.parseDouble(prop.value);
            }
        }

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + "\"");
    }
}
