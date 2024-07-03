package com.github.natche.cyderutils.image;

import com.github.natche.cyderutils.files.FileUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.Arrays;

/**
 * The image file types supported by Cyder.
 */
public enum SupportedImageFileType {
    /**
     * The PNG image extension.
     */
    PNG(".png", ImmutableList.of(0x89, 0x50, 0x4E, 0x47)),

    /**
     * The JPG image extension.
     */
    JPG(".jpg", ImmutableList.of(0xFF, 0xD8, 0xFF)),

    /**
     * The JPEG image extension. {@link #JPG} is equivalent and should be preferred over this.
     */
    JPEG(".jpeg", ImmutableList.of(0xFF, 0xD8, 0xFF));

    private final String extension;
    private final ImmutableList<Integer> signature;

    SupportedImageFileType(String extension, ImmutableList<Integer> signature) {
        this.extension = extension;
        this.signature = signature;
    }

    /**
     * Returns the extension including the period.
     *
     * @return the extension including the period
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Returns the file signature for this extension.
     *
     * @return the file signature for this extension
     */
    public ImmutableList<Integer> getSignature() {
        return signature;
    }

    /**
     * Returns whether the provided file is supported.
     *
     * @param imageFile the image file
     * @return whether the provided file is supported
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist or is not a file
     */
    public static boolean isSupported(File imageFile) {
        Preconditions.checkNotNull(imageFile);
        Preconditions.checkArgument(imageFile.exists());
        Preconditions.checkArgument(imageFile.isFile());

        return Arrays.stream(values()).anyMatch(supportedImageExtension -> {
            boolean extensionMatches = FileUtil.validateExtension(imageFile, supportedImageExtension.getExtension());
            boolean noSignature = supportedImageExtension.getSignature().isEmpty();
            if (noSignature) return extensionMatches;
            boolean fileSignatureMatches =
                    FileUtil.fileMatchesSignature(imageFile, supportedImageExtension.getSignature());
            return extensionMatches && fileSignatureMatches;
        });
    }

    /**
     * Returns whether the provided file is of this type.
     *
     * @param imageFile the image file
     * @return whether the provided file is of this type
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist or is not a file
     */
    public boolean isOfType(File imageFile) {
        Preconditions.checkNotNull(imageFile);
        Preconditions.checkArgument(imageFile.exists());
        Preconditions.checkArgument(imageFile.isFile());

        boolean extensionMatches = FileUtil.validateExtension(imageFile, extension);
        boolean fileSignatureMatches = FileUtil.fileMatchesSignature(imageFile, signature);
        return extensionMatches && fileSignatureMatches;
    }
}
