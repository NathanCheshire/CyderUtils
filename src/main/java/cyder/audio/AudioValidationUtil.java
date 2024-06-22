package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.audio.exceptions.AudioException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.DataUnit;
import cyder.strings.CyderStrings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Utility class for audio file validation.
 * This class provides methods to validate specific audio file formats.
 */
public final class AudioValidationUtil {
    /**
     * The signature for the FTYP box in MP4 files.
     */
    private static final ImmutableList<Byte> FTYP_BOX
            = ImmutableList.of((byte) 0x66, (byte) 0x74, (byte) 0x79, (byte) 0x70);

    /**
     * The list of valid brands within the FTYP box for M4A files.
     */
    private static final ImmutableList<ImmutableList<Byte>> VALID_BRANDS = ImmutableList.of(
            ImmutableList.of((byte) 0x4D, (byte) 0x34, (byte) 0x41, (byte) 0x20), // M4A
            ImmutableList.of((byte) 0x69, (byte) 0x73, (byte) 0x6F, (byte) 0x6D), // isom
            ImmutableList.of((byte) 0x6D, (byte) 0x70, (byte) 0x34, (byte) 0x32), // mp42
            ImmutableList.of((byte) 0x4D, (byte) 0x34, (byte) 0x42, (byte) 0x20), // M4B
            ImmutableList.of((byte) 0x4D, (byte) 0x34, (byte) 0x50, (byte) 0x20), // M4P
            ImmutableList.of((byte) 0x4D, (byte) 0x34, (byte) 0x56, (byte) 0x20), // M4V
            ImmutableList.of((byte) 0x4D, (byte) 0x34, (byte) 0x52, (byte) 0x20)  // M4R
    );

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private AudioValidationUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Validates that the provided file is a valid M4A file.
     * <p>
     * The method for validating involves reading the initial part of the file and looking for the FTYP box,
     * a common element in MP4 files. If present, a brand identifier within the FTYP box is attempted to be found
     * and validated against a typical identifier associated with M4A files.
     *
     * @param file the file to validate
     * @return whether the provided file is a valid M4A file
     * @throws NullPointerException     if the provided file is null
     * @throws IllegalArgumentException if the provided file does not exist or is not a file
     */
    public static boolean isValidM4aFile(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isFile());

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = DataUnit.KILOBYTE.getByteArray(4);
            int bytesRead = fis.read(buffer);
            AtomicInteger byteOffset = new AtomicInteger();

            while (byteOffset.get() < bytesRead - (int) DataUnit.NIBBLE.getValue()) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, byteOffset.get(), (int) DataUnit.NIBBLE.getValue());
                byteBuffer.order(ByteOrder.BIG_ENDIAN);
                int boxLength = byteBuffer.getInt();

                if (matchesByteArray(FTYP_BOX, buffer, byteOffset.get() + (int) DataUnit.NIBBLE.getValue())) {
                    if (VALID_BRANDS.stream().anyMatch(brand -> matchesByteArray(brand, buffer,
                            (int) (byteOffset.get() + DataUnit.BYTE.getValue())))) return true;
                }

                byteOffset.addAndGet(boxLength);
                if (boxLength == 0) break;
            }
        } catch (IOException e) {
            throw new AudioException(e);
        }

        return false;
    }

    /**
     * Returns whether the provided byte array and buffer match.
     *
     * @param byteArray the byte array
     * @param buffer the buffer
     * @param offset the buffer offset for comparison
     * @return whether the provided byte array matches the provided buffer at the provided offset
     */
    private static boolean matchesByteArray(ImmutableList<Byte> byteArray, byte[] buffer, int offset) {
        return IntStream.range(0, byteArray.size()).allMatch(i -> buffer[offset + i] == byteArray.get(i));
    }
}
