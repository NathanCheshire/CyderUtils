package com.github.natche.cyderutils.bounds;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.font.CyderFonts;
import com.github.natche.cyderutils.constants.HtmlTags;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;
import com.github.natche.cyderutils.strings.StringUtil;
import com.github.natche.cyderutils.utils.HtmlUtil;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

/** Utility methods to calculate the needed space for a String of text. */
public final class BoundsUtil {
    /** The default maximum width for returned bounds strings. */
    private static final int DEFAULT_MAX_WIDTH = 1200;

    /** The opening HTML tag char. */
    public static final char openingHtmlTagChar = '<';

    /** The closing HTML tag char. */
    public static final char closingHtmlTagChar = '>';

    /** The number of chars to look back or forward in a string to attempt to find a space to replace with a break tag. */
    private static final int numLookAroundForSpaceChars = 7;

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private BoundsUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Calculates the needed width and height necessary to display the provided string using
     * the {@link CyderFonts#DEFAULT_FONT_SMALL} font. The object returned dictates the html-styled
     * string to use which is guaranteed to fit within a max width of {@link #DEFAULT_MAX_WIDTH}.
     * Callers should check to ensure the height is acceptable to their purpose.
     *
     * @param text the string to display
     * @return an object composed of the width, height, and the html-styled text with break tags inserted if needed
     * @throws NullPointerException if the provided text is null
     */
    public static BoundsString widthHeightCalculation(String text) {
        Preconditions.checkNotNull(text);

        return widthHeightCalculation(text, CyderFonts.DEFAULT_FONT_SMALL, DEFAULT_MAX_WIDTH);
    }

    /**
     * Calculates the needed width and height necessary to display the provided string. The object returned
     * dictates the html-styled string to use which is guaranteed to fit within a max width of
     * {@link #DEFAULT_MAX_WIDTH}. Callers should check to ensure the height is acceptable to their purpose.
     *
     * @param text the string to display
     * @param font the font to be used
     * @return an object composed of the width, height, and the html-styled text with break tags inserted if needed
     * @throws NullPointerException if the provided text or font is null
     */
    public static BoundsString widthHeightCalculation(String text, Font font) {
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(font);

        return widthHeightCalculation(text, font, DEFAULT_MAX_WIDTH);
    }

    /**
     * Calculates the needed width and height necessary to display the provided string. The object returned
     * dictates the html-styled string to use which is guaranteed to fit within the provided maximum width.
     * Callers should check to ensure the height is acceptable to their purpose.
     *
     * @param text     the string to display
     * @param maxWidth the maximum width allowed
     * @param font     the font to be used
     * @return an object composed of the width, height, and the html-styled text with break tags inserted if needed
     * @throws NullPointerException if the text or font is null
     * @throws IllegalArgumentException if the provided text is empty or max width is less than or equal to zero
     */
    public static BoundsString widthHeightCalculation(String text, Font font, int maxWidth) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty());
        Preconditions.checkNotNull(font);
        Preconditions.checkArgument(maxWidth > 0);

        int widthAddition = 5;

        FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(),
                font.isItalic(), true);
        int lineHeightForFont = StringUtil.getMinHeight(text, font);

        if (HtmlUtil.containsHtmlStyling(text)) {
            boolean inHtmlTag = false;
            StringBuilder htmlBuilder = new StringBuilder();
            StringBuilder currentLineBuilder = new StringBuilder();
            for (char c : text.toCharArray()) {
                if (c == openingHtmlTagChar && !inHtmlTag) {
                    htmlBuilder.append(currentLineBuilder);
                    currentLineBuilder = new StringBuilder(String.valueOf(c));
                    inHtmlTag = true;
                    continue;
                } else if (c == closingHtmlTagChar && inHtmlTag) {
                    inHtmlTag = false;
                    currentLineBuilder.append(c);

                    htmlBuilder.append(currentLineBuilder);
                    currentLineBuilder = new StringBuilder();

                    continue;
                }

                if (inHtmlTag) {
                    currentLineBuilder.append(c);
                    continue;
                }

                if (currentLineBuilder.toString().endsWith(HtmlTags.breakTag)) {
                    htmlBuilder.append(currentLineBuilder);
                    currentLineBuilder = new StringBuilder(String.valueOf(c));
                    continue;
                }

                int currentLineWidth = StringUtil.getMinWidth(currentLineBuilder + String.valueOf(c), font);
                if (currentLineWidth > maxWidth) {
                    // Sweet, we can just replace the current char with the break tag
                    if (c == ' ') {
                        htmlBuilder.append(currentLineBuilder).append(HtmlTags.breakTag);
                        currentLineBuilder = new StringBuilder();
                    } else {
                        String currentLine = currentLineBuilder.toString();
                        // Ensure we don't look back farther than we can
                        int numLookBack = Math.min(currentLine.length(), numLookAroundForSpaceChars);

                        boolean insertedSpace = false;
                        for (int i = currentLine.length() - 1 ; i > currentLine.length() - numLookBack - 1 ; i--) {
                            if (currentLine.charAt(i) == ' ') {
                                htmlBuilder.append(currentLine, 0, i)
                                        .append(HtmlTags.breakTag)
                                        .append(currentLine.substring(i + 1 >= currentLine.length() ? i : i + 1));
                                currentLineBuilder = new StringBuilder(String.valueOf(c));
                                insertedSpace = true;
                                break;
                            }
                        }

                        // Unfortunately have to break up a word
                        if (!insertedSpace) {
                            htmlBuilder.append(currentLineBuilder).append(HtmlTags.breakTag);
                            currentLineBuilder = new StringBuilder(String.valueOf(c));
                        }
                    }
                } else {
                    currentLineBuilder.append(c);
                }
            }

            htmlBuilder.append(currentLineBuilder);

            String[] lines = htmlBuilder.toString().split(HtmlTags.breakTag);
            int necessaryHeight = lineHeightForFont * lines.length;

            int necessaryWidth = 0;
            for (String line : lines) {
                necessaryWidth = Math.max(necessaryWidth, StringUtil.getMinWidth(
                        Jsoup.clean(line, Safelist.none()), font));
            }

            if (!htmlBuilder.toString().startsWith(HtmlTags.openingHtml)) {
                htmlBuilder.insert(0, HtmlTags.openingHtml);
            }
            if (!htmlBuilder.toString().endsWith(HtmlTags.closingHtml)) {
                htmlBuilder.append(HtmlTags.closingHtml);
            }

            return new BoundsString(htmlBuilder.toString(), necessaryWidth, necessaryHeight);
        } else {
            // Non-html so we don't have to worry about where break tags fall
            // Preferably they are not in the middle of words

            String[] lines = text.split(HtmlTags.breakTag);
            StringBuilder nonHtmlBuilder = new StringBuilder();

            for (int i = 0 ; i < lines.length ; i++) {
                int fullLineWidth = StringUtil.getMinWidth(lines[i], font) + widthAddition;
                if (fullLineWidth > maxWidth) {
                    // Number of lines to split the current line into
                    int neededLines = (int) Math.ceil(fullLineWidth / (double) maxWidth);
                    neededLines = Math.max(2, neededLines);

                    nonHtmlBuilder.append(insertBreaks(lines[i], neededLines));
                } else {
                    nonHtmlBuilder.append(lines[i]);
                }

                if (i != lines.length - 1) {
                    nonHtmlBuilder.append(HtmlTags.breakTag);
                }
            }

            String nonHtml = nonHtmlBuilder.toString();
            String[] nonHtmlLines = nonHtml.split(HtmlTags.breakTag);

            int w = 0;
            for (String line : nonHtmlLines) {
                int currentWidth = (int) (font.getStringBounds(line, fontRenderContext).getWidth() + widthAddition);
                w = Math.max(w, currentWidth);
            }

            int h = lineHeightForFont * nonHtmlLines.length;

            if (!nonHtml.startsWith(HtmlTags.openingHtml)) {
                nonHtml = HtmlTags.openingHtml + nonHtml;
            }
            if (!nonHtml.endsWith(HtmlTags.closingHtml)) {
                nonHtml += HtmlTags.closingHtml;
            }

            return new BoundsString(nonHtml, w, h);
        }
    }

    /**
     * Inserts {@link HtmlTags#breakTag}s into the text based on the amount of required lines.
     * Note that HTML tags should NOT exist in this string and should be parsed
     * away prior to invoking this method.
     *
     * @param text          the raw text
     * @param requiredLines the number of lines required
     * @return the text with html line breaks inserted
     * @throws NullPointerException     if the provided text is null
     * @throws IllegalArgumentException if the provided text contains html tags or
     *                                  if the number of required lines is less than 1
     */
    public static String insertBreaks(String text, int requiredLines) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!HtmlUtil.containsHtmlStyling(text), text);
        Preconditions.checkArgument(requiredLines > 0);

        if (requiredLines == 1) return text;

        String ret = text;

        int splitFrequency = (int) Math.floor(text.length() / (float) requiredLines);
        int numChars = text.length();

        int currentLines = 1;

        for (int i = splitFrequency ; i < numChars ; i += splitFrequency) {
            if (currentLines == requiredLines) break;

            if (ret.charAt(i) == ' ') {
                StringBuilder builder = new StringBuilder(ret);
                builder.deleteCharAt(i);
                builder.insert(i, HtmlTags.breakTag);
                ret = builder.toString();
            } else {
                boolean breakInserted = false;

                // Check left for a space
                for (int j = i ; j > i - numLookAroundForSpaceChars ; j--) {
                    if (j > 0 && ret.charAt(j) == ' ') {
                        StringBuilder sb = new StringBuilder(ret);
                        sb.deleteCharAt(j);
                        sb.insert(j, HtmlTags.breakTag);

                        ret = sb.toString();

                        breakInserted = true;
                        break;
                    }
                }

                if (breakInserted) {
                    currentLines++;
                    i += HtmlTags.breakTag.length();
                    continue;
                }

                // Check right for a space
                for (int j = i ; j < i + numLookAroundForSpaceChars ; j++) {
                    if (j < numChars && ret.charAt(j) == ' ') {
                        StringBuilder sb = new StringBuilder(ret);
                        sb.deleteCharAt(j);
                        sb.insert(j, HtmlTags.breakTag);

                        ret = sb.toString();

                        breakInserted = true;
                        break;
                    }
                }

                if (!breakInserted) {
                    // Unfortunately have to just insert at the current index
                    StringBuilder sb = new StringBuilder(ret);
                    sb.insert(i, HtmlTags.breakTag);
                    ret = sb.toString();
                }
            }

            currentLines++;
            i += HtmlTags.breakTag.length();
        }

        if (currentLines != requiredLines) {
            int i = ret.length() - 1;
            while (currentLines != requiredLines) {
                if (ret.charAt(i) == ' ') {
                    StringBuilder builder = new StringBuilder(ret);

                    // this will push to the right so our iterating index is still okay to insert at if needed
                    builder.deleteCharAt(i);
                    builder.insert(i, HtmlTags.breakTag);
                    ret = builder.toString();

                    currentLines++;
                }

                i--;
            }
        }

        return ret;
    }
}
