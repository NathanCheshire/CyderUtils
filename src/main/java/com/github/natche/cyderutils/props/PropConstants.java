package com.github.natche.cyderutils.props;

import com.github.natche.cyderutils.enumerations.Extension;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.utils.OperatingSystem;
import com.github.natche.cyderutils.utils.OsUtil;

import java.io.File;

/**
 * Constants necessary for the {@link PropLoader}.
 */
final class PropConstants {
    /**
     * The possible annotations for props.
     */
    enum Annotation {
        /**
         * An annotation used to indicate the prop's value should not be logged.
         */
        NO_LOG("no_log");

        /**
         * The annotation name for this annotation without the @.
         */
        private final String annotation;

        Annotation(String annotation) {
            this.annotation = annotation;
        }

        /**
         * Returns this annotation.
         *
         * @return this annotation
         */
        public String getAnnotation() {
            return "@" + annotation;
        }
    }

    /**
     * The character a line must end with to interpret the next line as being the same prop.
     */
    static final String multiLinePropSuffix = "\\";

    /**
     * Lines which start with this are marked as a comment and not parsed as props.
     */
    static final String commentPrefix = "#";

    /**
     * The local propositional argument directory.
     */
    static final File localPropsDirectory = new File("props");

    /**
     * The extension for prop files.
     */
    static final String propExtension = Extension.INI.getExtension();

    /**
     * The prefix prop files must have.
     */
    static final String propFilePrefix = "prop";

    /**
     * The separator for prop keys and values.
     */
    static final String keyValueSeparator = ":";

    /**
     * The escape character for a comma.
     */
    static final String escapeSequence = "\\";

    /**
     * The string to split a prop file contents at to separate the raw file master string into separate lines.
     */
    static final String splitPropFileContentsAt = OsUtil.OPERATING_SYSTEM == OperatingSystem.WINDOWS
            ? CyderStrings.carriageReturnChar + CyderStrings.newline : CyderStrings.newline;

    /**
     * The token to split a string into a list of strings at.
     */
    static final String splitListsAtChar = ",";

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private PropConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
