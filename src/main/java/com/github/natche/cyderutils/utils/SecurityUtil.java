package com.github.natche.cyderutils.utils;

import com.google.common.base.Preconditions;
import com.github.natche.cyderutils.exceptions.FatalException;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.UUID;

/** Static utility class containing methods related to security. */
public final class SecurityUtil {
    /** The hashing algorithms supported by {@link SecurityUtil}. */
    public enum HashingAlgorithm {
        /** The sha256 hashing algorithm. */
        SHA256("SHA-256"),

        /** The sha1 hashing algorithm. */
        SHA1("SHA-1"),

        /** The md5 hashing algorithm. */
        MD5("MD5");

        /** The name of this hashing algorithm. */
        private final String name;

        HashingAlgorithm(String name) {
            this.name = name;
        }

        /**
         * Returns the name of this hashing algorithm.
         *
         * @return the name of this hashing algorithm
         */
        public String getName() {
            return name;
        }
    }

    /** Suppress default constructor. */
    private SecurityUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Converts the given char array to a byte array without using string object.
     * This way any possible security issues that arise from the nature of String pool are avoided.
     * Remember to use Arrays.fill(bytes, (byte) 0) for bytes or Arrays.fill(chars, '\u0000') for chars
     * when finished with the byte/char array.
     *
     * @param chars the char array to be converted to byte array
     * @return the byte array representing the given char array
     */
    private static byte[] toBytes(char[] chars) {
        Preconditions.checkNotNull(chars);
        Preconditions.checkArgument(chars.length > 0);

        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());

        // Clear possible sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    /**
     * Returns a byte array of the provided char array after hashing via the SHA256 algorithm.
     *
     * @param input the input char array
     * @return the hashed character array converted to bytes
     */
    public static byte[] getSha256(char[] input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length > 0);

        try {
            MessageDigest md = MessageDigest.getInstance(HashingAlgorithm.SHA256.getName());
            return md.digest(toBytes(input));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        throw new FatalException("Unable to compute SHA256 of input");
    }

    /**
     * Returns a byte array of the provided char array after hashing via the SHA1 algorithm.
     *
     * @param input the input char array
     * @return the hashed character array converted to bytes
     */
    public static byte[] getSha1(char[] input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length > 0);

        try {
            MessageDigest md = MessageDigest.getInstance(HashingAlgorithm.SHA1.getName());
            return md.digest(toBytes(input));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        throw new FatalException("Unable to compute SHA1 of input");
    }

    /**
     * Returns a byte array of the provided char array after hashing via the MD5 algorithm.
     *
     * @param input the input char array
     * @return the hashed character array converted to bytes
     */
    public static byte[] getMd5(char[] input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length > 0);

        try {
            MessageDigest md = MessageDigest.getInstance(HashingAlgorithm.MD5.getName());
            return md.digest(toBytes(input));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        throw new FatalException("Unable to compute MD5 of input");
    }

    /**
     * Returns a byte array of the provided byte array after hashing via the SHA256 algorithm.
     *
     * @param input the input char array
     * @return the hashed character array converted to bytes
     */
    public static byte[] getSha256(byte[] input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length > 0);

        try {
            MessageDigest md = MessageDigest.getInstance(HashingAlgorithm.SHA256.getName());
            return md.digest(input);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        throw new FatalException("Unable to compute SHA256 of input");
    }

    /**
     * Returns a string representing the byte array.
     *
     * @param hash the array of bytes
     * @return a string representing the byte array
     */
    public static String toHexString(byte[] hash) {
        Preconditions.checkNotNull(hash);
        Preconditions.checkArgument(hash.length > 0);

        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    /**
     * Returns a unique uuid using the sha256 algorithm and the
     * standard {@link UUID#nameUUIDFromBytes(byte[])} method.
     *
     * @return a unique uuid
     */
    public static String generateUuid() {
        try {
            MessageDigest salt = MessageDigest.getInstance(HashingAlgorithm.SHA256.getName());
            salt.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
            return UUID.nameUUIDFromBytes(salt.digest()).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new FatalException("Unable to compute SHA256 of input");
    }

    /**
     * Double hashes the provided password using sha256 and returns the hex string representing the password.
     *
     * @param password the password to double hash
     * @return the double hashed password
     */
    public static String doubleHashToHex(char[] password) {
        Preconditions.checkNotNull(password);

        return toHexString(getSha256(toHexString(getSha256(password)).toCharArray()));
    }

    /**
     * Hashes the provided input using SHA256 and converts the output to hex representation.
     *
     * @param input the input to hash
     * @return the hashed input converted to a hex string
     */
    public static String hashAndHex(String input) {
        Preconditions.checkNotNull(input);

        return toHexString(getSha256(input.toCharArray()));
    }
}
