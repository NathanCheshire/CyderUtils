package com.github.natche.cyderutils.font;

import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.files.FileUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/** Utilities related to fonts. */
public final class FontUtil {
    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private FontUtil() {
        throw new IllegalMethodException("Instances of FontUtil are not allowed");
    }

    /**
     * Registers the provided true-type font files with {@link GraphicsEnvironment#getLocalGraphicsEnvironment()}.
     *
     * @param fonts the list of true-type font files
     * @throws NullPointerException     if the provided list is null
     * @throws IllegalArgumentException if the provided list is empty or does not contain only true-type fonts
     */
    public static void registerTrueTypeFonts(ImmutableList<File> fonts) {
        Preconditions.checkNotNull(fonts);
        Preconditions.checkArgument(!fonts.isEmpty());
        Preconditions.checkArgument(fonts.stream().allMatch((font) -> font.getAbsolutePath().endsWith(".ttf")));

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        fonts.forEach((font) -> {
            try {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, font));
            } catch (FontFormatException e) {
                throw new FontException("Invalid font " + font);
            } catch (IOException e) {
                throw new FontException("Failed to load font " + font);
            }
        });
    }

    /**
     * Registers the true-type fonts contained in the provided directory.
     *
     * @param directory the directory
     * @throws NullPointerException     if the provided directory is null
     * @throws IllegalArgumentException if the provided directory does not exist or is not a directory
     */
    public static void registerTrueTypeFonts(File directory) {
        Preconditions.checkNotNull(directory);
        Preconditions.checkArgument(directory.exists());
        Preconditions.checkArgument(directory.isDirectory());

        ImmutableList<File> files = FileUtil.getFiles(directory, false);
        registerTrueTypeFonts(files.stream().filter((file) -> file.getAbsolutePath().endsWith(".ttf"))
                .collect(ImmutableList.toImmutableList()));
    }
}
