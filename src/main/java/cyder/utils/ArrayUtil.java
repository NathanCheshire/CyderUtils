package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utilities related to arrays.
 */
public final class ArrayUtil {
    /**
     * Suppress default constructor.
     */
    private ArrayUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
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
    public static ImmutableList<Character> charArrayToList(char[] chars) {
        ArrayList<Character> ret = new ArrayList<>();

        for (char c : chars) {
            ret.add(c);
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
}
