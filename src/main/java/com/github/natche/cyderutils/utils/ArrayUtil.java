package com.github.natche.cyderutils.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.strings.CyderStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/** Utilities related to arrays. */
public final class ArrayUtil {
    /** Suppress default constructor. */
    private ArrayUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns whether the provided array is empty.
     *
     * @param array the array.
     * @param <T>   the type of element contained in the array
     * @return whether the provided array is empty
     */
    public static <T> boolean isEmpty(T[] array) {
        return array.length == 0;
    }

    /**
     * Returns whether the provided array is empty.
     *
     * @param array the array.
     * @return whether the provided array is empty
     */
    public static boolean isEmpty(char[] array) {
        return array.length == 0;
    }

    /**
     * Returns whether the provided array is empty.
     *
     * @param array the array.
     * @return whether the provided array is empty
     */
    public static boolean isEmpty(boolean[] array) {
        return array.length == 0;
    }

    /**
     * Returns whether the provided array is empty.
     *
     * @param array the array.
     * @return whether the provided array is empty
     */
    public static boolean isEmpty(byte[] array) {
        return array.length == 0;
    }

    /**
     * Returns whether the provided array is empty.
     *
     * @param array the array.
     * @return whether the provided array is empty
     */
    public static boolean isEmpty(short[] array) {
        return array.length == 0;
    }

    /**
     * Returns whether the provided array is empty.
     *
     * @param array the array.
     * @return whether the provided array is empty
     */
    public static boolean isEmpty(float[] array) {
        return array.length == 0;
    }

    /**
     * Returns whether the provided array is empty.
     *
     * @param array the array.
     * @return whether the provided array is empty
     */
    public static boolean isEmpty(double[] array) {
        return array.length == 0;
    }

    /**
     * Returns whether the provided array is empty.
     *
     * @param array the array.
     * @return whether the provided array is empty
     */
    public static boolean isEmpty(int[] array) {
        return array.length == 0;
    }

    /**
     * Returns whether the provided array is empty.
     *
     * @param array the array.
     * @return whether the provided array is empty
     */
    public static boolean isEmpty(long[] array) {
        return array.length == 0;
    }

    /**
     * Returns the last element of the provided array.
     *
     * @param array the array
     * @param <T>   the type of element contained in the array
     * @return the last element of the array.
     */
    public static <T> T getLastElement(T[] array) {
        Preconditions.checkNotNull(array);
        Preconditions.checkArgument(array.length > 0);

        return array[array.length - 1];
    }

    /**
     * Performs the provided action on each element of the provided list except the last element.
     *
     * @param list the list
     * @param <T>  the type of element contained in the list
     */
    public static <T> void forEachElementExcludingLast(Consumer<T> action, List<T> list) {
        Preconditions.checkNotNull(action);
        Preconditions.checkNotNull(list);
        Preconditions.checkArgument(!list.isEmpty());

        for (int i = 0 ; i < list.size() ; i++) {
            if (i != list.size() - 1) {
                action.accept(list.get(i));
            }
        }
    }

    /**
     * Creates and returns a {@link ImmutableList} with the contents of the provided array.
     *
     * @param array the array
     * @param <T>   the type of element contained in the array
     * @return a  {@link ImmutableList} with the contents of the provided array
     */
    public static <T> ImmutableList<T> toList(T[] array) {
        Preconditions.checkNotNull(array);

        return ImmutableList.copyOf(new ArrayList<>(Arrays.asList(array)));
    }

    /**
     * Converts the provided character array to a list of characters.
     *
     * @param chars the char array to convert
     * @return the list of characters
     */
    public static ImmutableList<Character> toList(char[] chars) {
        ArrayList<Character> ret = new ArrayList<>();

        for (char c : chars) {
            ret.add(c);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Converts the provided boolean array to a list of booleans.
     *
     * @param bools the boolean array to convert
     * @return the list of booleans
     */
    public static ImmutableList<Boolean> toList(boolean[] bools) {
        ArrayList<Boolean> ret = new ArrayList<>();

        for (boolean b : bools) {
            ret.add(b);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Converts the provided byte array to a list of bytes.
     *
     * @param bytes the byte array to convert
     * @return the list of bytes
     */
    public static ImmutableList<Byte> toList(byte[] bytes) {
        ArrayList<Byte> ret = new ArrayList<>();

        for (byte b : bytes) {
            ret.add(b);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Converts the provided short array to a list of shorts.
     *
     * @param shorts the short array to convert
     * @return the list of shorts
     */
    public static ImmutableList<Short> toList(short[] shorts) {
        ArrayList<Short> ret = new ArrayList<>();

        for (short s : shorts) {
            ret.add(s);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Converts the provided int array to a list of ints.
     *
     * @param ints the int array to convert
     * @return the list of ints
     */
    public static ImmutableList<Integer> toList(int[] ints) {
        ArrayList<Integer> ret = new ArrayList<>();

        for (int i : ints) {
            ret.add(i);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Converts the provided long array to a list of longs.
     *
     * @param longs the long array to convert
     * @return the list of longs
     */
    public static ImmutableList<Long> toList(long[] longs) {
        ArrayList<Long> ret = new ArrayList<>();

        for (long l : longs) {
            ret.add(l);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Converts the provided float array to a list of floats.
     *
     * @param floats the float array to convert
     * @return the list of floats
     */
    public static ImmutableList<Float> toList(float[] floats) {
        ArrayList<Float> ret = new ArrayList<>();

        for (float f : floats) {
            ret.add(f);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Converts the provided double array to a list of doubles.
     *
     * @param doubles the double array to convert
     * @return the list of doubles
     */
    public static ImmutableList<Double> toList(double[] doubles) {
        ArrayList<Double> ret = new ArrayList<>();

        for (double d : doubles) {
            ret.add(d);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns a copy of the provided array reversed
     *
     * @param array the array to reverse
     * @param <T>   the type contained in the array
     * @return a copy of the array reversed
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] reverseArray(T[] array) {
        Preconditions.checkNotNull(array);

        ImmutableList<T> reversedList = ImmutableList.copyOf(array).reverse();

        T[] reversedAArray = (T[]) java.lang.reflect.Array.newInstance(
                reversedList.get(0).getClass(), reversedList.size());
        for (int i = 0 ; i < reversedList.size() ; i++) reversedAArray[i] = reversedList.get(i);

        return reversedAArray;
    }

    /**
     * Returns a random element from the provided array.
     *
     * @param array the array
     * @param <T>   the type contained in the array
     * @return a random element from the provided array
     */
    public static <T> T getRandomElement(T[] array) {
        Preconditions.checkNotNull(array);

        return Arrays.stream(array).skip((int) (array.length * Math.random())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Failed to get random element of collection"));
    }

    /**
     * Returns a random element from the provided collection.
     *
     * @param collection the collection
     * @param <T>        the type contained in the collection
     * @return a random element from the provided collection
     */
    public static <T> T getRandomElement(Collection<T> collection) {
        Preconditions.checkNotNull(collection);

        return collection.stream().skip((int) (collection.size() * Math.random())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Failed to get random element of collection"));
    }

    /**
     * Returns whether the provided array is null or empty.
     *
     * @param array the array
     * @param <T>   the type contained in the array
     * @return whether the provided array is null or empty
     */
    public static <T> boolean nullOrEmpty(T[] array) {
        return array == null || array.length == 0;
    }
}
