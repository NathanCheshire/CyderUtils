package com.github.natche.cyderutils.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import com.github.natche.cyderutils.exceptions.IllegalMethodException;
import com.github.natche.cyderutils.ui.frame.CyderFrame;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import static com.github.natche.cyderutils.strings.CyderStrings.ATTEMPTED_INSTANTIATION;

/**
 * Utilities for Jvm reflection.
 */
public final class ReflectionUtil {
    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private ReflectionUtil() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
    }

    /**
     * The class string.
     */
    private static final String clazz = "class";

    /**
     * The get string.
     */
    private static final String GET = "get";

    /**
     * Returns a String representation of the provided object
     * using all public accessor methods found.
     *
     * @param object the object to build into a String
     * @return the string representation of the object
     */
    public static String buildGetterString(Object object) {
        Preconditions.checkNotNull(object);

        StringBuilder ret = new StringBuilder();

        ret.append(clazz).append(":").append(" ");
        ret.append(getBottomLevelClass(object.getClass()));
        ret.append(",").append(" ");

        for (Method m : object.getClass().getMethods()) {
            if (m.getName().startsWith(GET) && m.getParameterTypes().length == 0) {
                try {
                    ret.append(m.getName());
                    ret.append(":").append(" ");
                    ret.append(m.invoke(object));
                    ret.append(",").append(" ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String retString = ret.toString();
        retString = retString.trim().substring(0, retString.length() - 1).trim();
        return retString;
    }

    /**
     * Finds all getters associated with the provided class and returns a list
     * containing the method names of all the public accessor methods.
     *
     * @param clazz the class to find all getters of
     * @return a list of getter names
     */
    public static ImmutableList<String> getGetterNames(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        ArrayList<String> ret = new ArrayList<>();

        for (Method m : clazz.getMethods()) {
            if (m.getName().startsWith(GET) && m.getParameterTypes().length == 0) {
                try {
                    ret.add(m.invoke(clazz).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * The set string used to locate setter/mutator methods of an object.
     */
    private static final String SET = "set";

    /**
     * Finds all setters associated with the provided class and returns a list
     * containing the method names of all the public mutator methods.
     *
     * @param clazz the class to find all mutators of
     * @return a list of setter names
     */
    public static ImmutableList<String> getSetterNames(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        ArrayList<String> ret = new ArrayList<>();

        for (Method m : clazz.getMethods()) {
            if (m.getName().startsWith(SET) && m.getParameterTypes().length == 1) {
                try {
                    ret.add(m.invoke(clazz).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns the name of the class without all the package info.
     * Example: if {@link CyderFrame} was provided, typically invoking {@link CyderFrame#toString()}
     * would return "cyder.ui.CyderFrame" (with its hashcode appended of course).
     * This method will simply return "CyderFrame".
     *
     * @param clazz the class to find the name of
     * @return the bottom level class name
     */
    public static String getBottomLevelClass(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        String superName = clazz.toString();

        boolean innerClass = superName.contains("$");
        if (innerClass) superName = superName.split("\\$")[0];

        // Remove package info (cyder.ui.CyderFrame)
        if (superName.contains(".")) {
            String[] parts = superName.split("\\.");
            superName = ArrayUtil.getLastElement(parts);
        }

        String ret = superName;
        if (innerClass) {
            ret += " (" + "inner class" + ")";
        }

        return ret.trim();
    }

    /**
     * The top level package for Cyder.
     */
    public static final String TOP_LEVEL_PACKAGE_NAME = "cyderutils";

    /**
     * A set of all classes contained within Cyder starting at {@link ReflectionUtil#TOP_LEVEL_PACKAGE_NAME}.
     */
    private static final ImmutableList<ClassPath.ClassInfo> cyderClasses;

    /**
     * Returns the class info objects of all classes found within the current build of Cyder.
     *
     * @return the class info objects of all classes found within the current build of Cyder
     */
    public static ImmutableList<ClassPath.ClassInfo> getCyderClasses() {
        return cyderClasses;
    }

    static {
        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        ClassPath cyderClassPath = null;

        try {
            cyderClassPath = ClassPath.from(currentThreadClassLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        cyderClasses = cyderClassPath == null
                ? ImmutableList.of()
                : ImmutableList.copyOf(cyderClassPath.getTopLevelClassesRecursive(TOP_LEVEL_PACKAGE_NAME));
    }

    /**
     * Returns whether the provided method is public.
     *
     * @param method the method
     * @return whether the method is public
     */
    public static boolean isPublic(Method method) {
        Preconditions.checkNotNull(method);

        return Modifier.isPublic(method.getModifiers());
    }

    /**
     * Returns whether the provided method is static.
     *
     * @param method the method
     * @return whether the provided method is static
     */
    public static boolean isStatic(Method method) {
        Preconditions.checkNotNull(method);

        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * Returns whether the provided method returns a {@link Boolean} type.
     *
     * @param method the method
     * @return whether the method returns a boolean
     */
    public static boolean returnsBoolean(Method method) {
        Preconditions.checkNotNull(method);

        return method.getReturnType() == boolean.class;
    }

    /**
     * Returns whether the provided method returns a {@link Void} type.
     *
     * @param method the method
     * @return whether the method returns a void type
     */
    public static boolean returnsVoid(Method method) {
        Preconditions.checkNotNull(method);

        return method.getReturnType().equals(Void.TYPE);
    }

    /**
     * Returns whether the provided method is public, static, and returns a boolean type.
     *
     * @param method the method
     * @return whether the method is public, static, and returns a boolean type
     */
    public static boolean isPublicStaticBoolean(Method method) {
        Preconditions.checkNotNull(method);

        return isPublic(method) && isStatic(method) && returnsBoolean(method);
    }
}
