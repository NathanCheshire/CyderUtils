package cyder.files;

/**
 * Enum representing various units of digital information.
 * Provides a way to work with data units ranging from a single bit up to an exabyte,
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
     * Returns the value of the unit multiplied by the specified factor.
     *
     * @param factor The factor to multiply the unit's value by.
     * @return The multiplied value.
     */
    public long get(long factor) {
        return this.value * factor;
    }

    /**
     * Overloaded method to accept an integer factor.
     *
     * @param factor The factor to multiply the unit's value by.
     * @return The multiplied value.
     */
    public long get(int factor) {
        return get((long) factor);
    }
}

