package cyder.utilities;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import cyder.annotations.ManualTest;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Widget;
import cyder.constants.CyderStrings;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderShare;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadFactory;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.utilities.objects.WidgetDescription;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utilities for methods regarding reflection.
 */
@SuppressWarnings("ConstantConditions") /* Guava Reflection */
public class ReflectionUtil {
    /**
     * Prevent illegal class instantiation.
     */
    private ReflectionUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Returns a String representation of the provided object
     * using all public get() methods found.
     *
     * @param obj the object to build into a String
     * @return the string representation of the object
     */
    private static String toStringFromGetters(Object obj) {
        StringBuilder ret = new StringBuilder();

        ret.append(obj.getClass().getName());
        ret.append("(");

        for (Method m : obj.getClass().getMethods()) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
                try {
                    ret.append(m.getName());
                    ret.append(" = ");
                    ret.append(m.invoke(obj));
                    ret.append(", ");
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }
        }

        String retString = ret.toString();
        return retString.substring(0, retString.length() - 3) + ")";
    }

    /**
     * A common method utilized by near all top-level Cyder classes
     * as the overridden logic for their toString() methods.
     *
     * @param obj the obj to return a String representation for
     * @return the String representation for the provided object
     * detailing the classname, hashcode, and reflected data
     */
    public static String commonCyderToString(Object obj) {
        String superName = obj.getClass().getName();
        int hash = obj.hashCode();

        String reflectedFields = toStringFromGetters(obj);

        if (reflectedFields == null || reflectedFields.isEmpty())
            reflectedFields = "No reflection data acquired";

        //remove anything after the $int if superName contains a $
        if (superName.contains("$")) {
            superName = superName.split("\\$")[0];
        }

        return superName + ",hash = " + hash + ", reflection data = " + reflectedFields;
    }

    /**
     * A toString() replacement method used by most Cyder classes.
     *
     * @param obj the object to invoke toString() on
     * @return a custom toString() representation of the provided object
     */
    public static String commonCyderUIReflection(Component obj) {
        CyderFrame topFrame = (CyderFrame) SwingUtilities.getWindowAncestor(obj);
        String frameRep;

        if (topFrame != null) {
            frameRep = topFrame.getTitle();
        } else {
            if (obj instanceof CyderFrame) {
                frameRep = "Object itself is the top level frame";
            } else {
                frameRep = "No associated frame";
            }
        }

        String superName = obj.getClass().getName();
        int hash = obj.hashCode();

        //remove anything after the $int if superName contains a $
        if (superName.contains("$")) {
            superName = superName.split("\\$")[0];
        }

        String getTitleResult = "No getTitle() method found";
        String getTextResult = "No getText() method found";
        String getTooltipResult = "No getTooltipText() method found";

        try {
           for (Method method : obj.getClass().getMethods()) {
               if (method.getName().startsWith("getText") && method.getParameterCount() == 0) {
                   Object locGetText = method.invoke(obj);

                   if (locGetText instanceof String) {
                       String locGetTextString = (String) locGetText;

                       if (locGetTextString != null && !locGetTextString.isEmpty()
                               && !locGetTextString.equalsIgnoreCase("null")) {
                           getTextResult = locGetTextString;
                       }
                   }
               } else if (method.getName().startsWith("getTooltipText") && method.getParameterCount() == 0) {
                   Object locGetTooltipText = method.invoke(obj);

                   if (locGetTooltipText instanceof String) {
                       String locGetTooltipTextString = (String) locGetTooltipText;

                       if (locGetTooltipTextString != null && !locGetTooltipTextString.isEmpty()
                               && !locGetTooltipTextString.equalsIgnoreCase("null")) {
                           getTooltipResult = locGetTooltipTextString;
                       }
                   }
               } else if (method.getName().startsWith("getTitle") && method.getParameterCount() == 0) {
                   Object locGetTitle = method.invoke(obj);

                   if (locGetTitle instanceof String) {
                       String locGetTitleString = (String) locGetTitle;

                       if (locGetTitleString != null && !locGetTitleString.isEmpty()
                               && !locGetTitleString.equalsIgnoreCase("null")) {
                           getTitleResult = locGetTitleString;
                       }
                   }
               }
           }
        } catch (Exception e) {
           ExceptionHandler.handle(e);
        }

        return "Component name = [" + superName + "], bounds = [(" + obj.getX() + ", "
                + obj.getY() + ", " + obj.getWidth() + ", " + obj.getHeight() + ")], hash = [" + hash + "], " +
                "parentFrame = [" + frameRep + "], associated text = [" + getTextResult + "], tooltip text = [" +
                getTooltipResult + "], title = [" + getTitleResult + "]";
    }

    /**
     * The top level package for Cyder.
     */
    public static final String TOP_LEVEL_PACKAGE = "cyder";

    /**
     * A set of all classes contained within Cyder starting at {@link ReflectionUtil#TOP_LEVEL_PACKAGE}.
     */
    public static ImmutableSet<ClassPath.ClassInfo> cyderClasses;

    // load cyder classes at runtime
    static {
        try {
            cyderClasses = ClassPath
                    .from(Thread.currentThread().getContextClassLoader())
                    .getTopLevelClassesRecursive(TOP_LEVEL_PACKAGE);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Finds all widgets within Cyder by looking for methods annotated with {@link Widget}.
     * The annotated method MUST take no parameters, be named "showGUI()",
     * contain a valid description, and contain at least one trigger.
     *
     * @throws IllegalMethodException if an invalid {@link Widget} annotation is located
     */
    public static void validateWidgets() {
        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> classer = classInfo.load();

            for (Method m : classer.getMethods()) {
                if (m.isAnnotationPresent(Widget.class)) {
                    String[] triggers = m.getAnnotation(Widget.class).triggers();
                    String description = m.getAnnotation(Widget.class).description();

                    String[] values = null;

                    if (m.isAnnotationPresent(SuppressCyderInspections.class))
                        values = m.getAnnotation(SuppressCyderInspections.class).values();

                    if (!m.getName().equals("showGUI")) {
                        if (values != null && StringUtil.in("WidgetInspection", false, values))
                            continue;

                        Logger.log(LoggerTag.DEBUG, "Method annotated with @Widget is not named" +
                                " showGUI(); name: " + m.getName());
                    }

                    if (StringUtil.isNull(description)) {
                        throw new IllegalMethodException("Method annotated with @Widget has empty description");
                    }

                    if (triggers.length == 0) {
                        throw new IllegalMethodException("Method annotated with @Widget has empty triggers");
                    }

                    for (String trigger : triggers) {
                        if (StringUtil.isNull(trigger)) {
                            throw new IllegalMethodException("Method annotated with @Widget has an empty trigger");
                        } else if (trigger.contains(" ")) {
                            throw new IllegalMethodException("Method annotated with " +
                                    "@Widget has triggers which contain spaces: \"" + trigger + "\"");
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds all manual tests within Cyder by looking for methods annotated with {@link ManualTest}.
     * The annotated method MUST take no parameters, and contain a valid, unique trigger.
     *
     * @throws IllegalMethodException if an invalid {@link ManualTest} annotation is located
     */
    public static void validateTests() {
        LinkedList<String> foundTriggers = new LinkedList<>();

        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> classer = classInfo.load();

            for (Method m : classer.getMethods()) {
                if (m.isAnnotationPresent(ManualTest.class)) {
                    String trigger = m.getAnnotation(ManualTest.class).trigger();

                    String[] values = null;

                    if (m.isAnnotationPresent(SuppressCyderInspections.class))
                        values = m.getAnnotation(SuppressCyderInspections.class).values();

                    if (!m.getName().toLowerCase().endsWith("test")) {
                        if (values != null && StringUtil.in("TestInspection", false, values))
                            continue;

                        Logger.log(LoggerTag.DEBUG, "Method annotated with @ManualTest does not end" +
                                " with test; name: " + m.getName());
                    }

                    if (StringUtil.isNull(trigger)) {
                        throw new IllegalMethodException("Method annotated with @ManualTest has no trigger");
                    }

                    if (StringUtil.in(trigger, true, foundTriggers)) {
                        throw new IllegalArgumentException("Method annotation with @ManualTest " +
                                "has a trigger which has already been used; method: " + m.getName()
                                + ", trigger: " + trigger);
                    }

                    foundTriggers.add(trigger);
                }
            }
        }
    }

    /**
     * Returns a list of names, descriptions, and triggers of all the widgets found within Cyder.
     *
     * @return a list of descriptions of all the widgets found within Cyder
     */
    public static ArrayList<WidgetDescription> getWidgetDescriptions() {
        ArrayList<WidgetDescription> ret = new ArrayList<>();

        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> classer = classInfo.load();

            for (Method m : classer.getMethods()) {
                if (m.isAnnotationPresent(Widget.class)) {
                    String[] triggers = m.getAnnotation(Widget.class).triggers();
                    String description = m.getAnnotation(Widget.class).description();
                    ret.add(new WidgetDescription(classer.getName(), description, triggers));
                }
            }
        }

        return ret;
    }

    /**
     * Opens the widget with the same trigger as the one provided.
     * If for some reason someone was an idiot and put a duplicate trigger
     * for a widget in Cyder, the first occurrence will be invoked.
     *
     * @param trigger the trigger for the widget to open
     * @return whether a widget was opened
     */
    public static boolean openWidget(String trigger) {
        for (ClassPath.ClassInfo classInfo : cyderClasses) {
            Class<?> classer = classInfo.load();

            for (Method m : classer.getMethods()) {
                if (m.isAnnotationPresent(Widget.class)) {
                    String[] widgetTriggers = m.getAnnotation(Widget.class).triggers();

                    for (String widgetTrigger : widgetTriggers) {
                        if (widgetTrigger.equalsIgnoreCase(trigger)) {
                            String shortWidgetName = classer.getName()
                                    .split("\\.")[classer.getName().split("\\.").length - 1];
                            ConsoleFrame.getConsoleFrame().getInputHandler().println("Opening widget: " + shortWidgetName);
                            try {
                                if (m.getParameterCount() == 0) {
                                    m.invoke(classer);
                                    
                                    StringBuilder triggerBuilder = new StringBuilder();
                                    
                                    for (int i = 0 ; i < widgetTriggers.length ; i++) {
                                        triggerBuilder.append(widgetTriggers[i]);
                                        
                                        if (i != widgetTrigger.length() - 1)
                                            triggerBuilder.append(", ");
                                    }
                                    
                                    Logger.log(LoggerTag.WIDGET_OPENED,
                                            shortWidgetName + ", trigger = "
                                                    + trigger + ", triggers = [" + triggerBuilder + "]");
                                    return true;
                                } else throw new IllegalStateException("Found widget showGUI()" +
                                        " method with parameters: " + m.getName() + ", class: " + classer);
                            } catch (Exception e) {
                                ExceptionHandler.handle(e);
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Executor service used to find a similar command utilizing commandFinder.py.
     */
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(
            new CyderThreadFactory("Similar Command Finder"));

    /**
     * Finds the most similar command to the unrecognized one the user provided.
     *
     * @param command the command to find a similar one to
     * @return the most similar command to the one provided
     */
    public static Future<Optional<String>> getSimilarCommand(String command) {
        return executor.submit(() -> {
            Optional<String> ret = Optional.empty();

            try {
                Runtime rt = Runtime.getRuntime();
                String[] commands = {"python",
                        OSUtil.buildPath("cyder","src","cyder","python","commandFinder.py"),
                        command, String.valueOf(CyderShare.JAR_MODE)};
                //noinspection CallToRuntimeExec
                Process proc = rt.exec(commands);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String output = stdInput.readLine();
                ret = Optional.of(output);
            } catch (Exception e) {
                ExceptionHandler.silentHandle(e);
            }

            return ret;
        });
    }
}
