package cyder.files;

/**
 * An enum representing various units of digital information.
 * This provides a way to work with data units ranging from a single bit up to an exabyte,
 * allowing for easy conversion and calculation of data sizes.
 */
public enum DataUnit {
    /**
     * A single bit.
     */
    BIT(1),

    /**
     * A nibble, equivalent to 4 bits.
     */
    NIBBLE(4),

    /**
     * A byte, equivalent to 8 bits.
     */
    BYTE(8),

    /**
     * A kilobyte, traditionally considered as 1024 bytes.
     */
    KILOBYTE(1024 * BYTE.value),

    /**
     * A megabyte, traditionally considered as 1024 kilobytes.
     */
    MEGABYTE(1024 * KILOBYTE.value),

    /**
     * A gigabyte, traditionally considered as 1024 megabytes.
     */
    GIGABYTE(1024 * MEGABYTE.value),

    /**
     * A terabyte, traditionally considered as 1024 gigabytes.
     */
    TERABYTE(1024 * GIGABYTE.value),

    /**
     * A petabyte, traditionally considered as 1024 terabytes.
     */
    PETABYTE(1024 * TERABYTE.value),

    /**
     * An exabyte, traditionally considered as 1024 petabytes.
     */
    EXABYTE(1024 * PETABYTE.value);

    private final long value;

    DataUnit(long value) {
        this.value = value;
    }

    /**
     * Returns the value of this data unit.
     * This number is the number of immediately lower units which make up this unit.
     *
     * @return the value of this data unit
     */
    public long getValue() {
        return value;
    }

    /**
     * Returns the value of the unit multiplied by the specified factor.
     *
     * @param factor the factor to multiply the unit's value by
     * @return the multiplied value
     */
    public long get(long factor) {
        return this.value * factor;
    }

    /**
     * Returns a new byte array of the length specified in this unit's magnitude.
     * For example, MEGABYTE.getByteArray(4) will return a byte array that can hold 4 megabytes.
     *
     * @param factor the factor to multiply the unit's value by to determine the byte array's size
     * @return a byte array of the specified size
     * @throws IllegalArgumentException If the calculated size exceeds the maximum array size
     */
    public byte[] getByteArray(int factor) {
        long size = get(factor);
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Requested array size exceeds maximum array size.");
        }
        return new byte[(int) size];
    }

    /**
     * Formats the given byte count into a human-readable string.
     * For example, 1500 will be formatted as "1.46 KB".
     *
     * @param bytes the number of bytes
     * @return a formatted string representing the byte count
     */
    public static String formatBytes(long bytes) {
        if (bytes < DataUnit.KILOBYTE.value) {
            return bytes + " B";
        } else if (bytes < DataUnit.MEGABYTE.value) {
            return String.format("%.2f KB", bytes / (double) DataUnit.KILOBYTE.value);
        } else if (bytes < DataUnit.GIGABYTE.value) {
            return String.format("%.2f MB", bytes / (double) DataUnit.MEGABYTE.value);
        } else if (bytes < DataUnit.TERABYTE.value) {
            return String.format("%.2f GB", bytes / (double) DataUnit.GIGABYTE.value);
        } else if (bytes < DataUnit.PETABYTE.value) {
            return String.format("%.2f TB", bytes / (double) DataUnit.TERABYTE.value);
        } else if (bytes < DataUnit.EXABYTE.value) {
            return String.format("%.2f PB", bytes / (double) DataUnit.PETABYTE.value);
        } else {
            return String.format("%.2f EB", bytes / (double) DataUnit.EXABYTE.value);
        }
    }

    /**
     * Determines the most appropriate DataUnit for the given number of bytes.
     * The next DataUnit is returned if the byte size is halfway or more to the next unit.
     * For example, 512 will result in {@link #BYTE} being returned but {@link #KILOBYTE} will
     * be returned for 513 and higher until the halfway point between {@link #KILOBYTE} and
     * {@link #MEGABYTE} at which point {@link #MEGABYTE} will be returned.
     *
     * @param bytes The number of bytes.
     * @return The DataUnit that best represents the scale of the given byte size.
     */
    public static DataUnit closestDataUnit(long bytes) {
        if (bytes >= PETABYTE.value / 2) {
            return EXABYTE;
        } else if (bytes >= TERABYTE.value / 2) {
            return PETABYTE;
        } else if (bytes >= GIGABYTE.value / 2) {
            return TERABYTE;
        } else if (bytes >= MEGABYTE.value / 2) {
            return GIGABYTE;
        } else if (bytes >= KILOBYTE.value / 2) {
            return MEGABYTE;
        } else if (bytes >= BYTE.value * (DataUnit.KILOBYTE.value / 2)) {
            return KILOBYTE;
        } else {
            return BYTE;
        }
    }
}

