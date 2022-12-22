package main.java.cyder.handlers.input;

import com.google.common.base.Preconditions;
import com.google.common.reflect.ClassPath;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import main.java.cyder.annotations.CyderTest;
import main.java.cyder.annotations.Handle;
import main.java.cyder.exceptions.FatalException;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.threads.CyderThreadRunner;
import main.java.cyder.utils.ReflectionUtil;

import java.lang.reflect.Method;

/**
 * A handler for invoking {@link main.java.cyder.annotations.CyderTest}s.
 */
public class TestHandler extends InputHandler {
    /**
     * The name of the default parameter.
     */
    private static final String VALUE = "value";

    /**
     * Suppress default constructor.
     */
    private TestHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Invokes all tests with the default trigger of {@link #VALUE}.
     */
    public static void invokeDefaultTests() {
        Class<?> clazz = CyderTest.class;

        Method method = null;
        try {
            method = clazz.getDeclaredMethod(VALUE);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (method == null) {
            throw new FatalException("Failed reflection when attempting to find CyderTest's default value");
        }
        String value = (String) method.getDefaultValue();
        invokeTestsWithTrigger(value);
    }

    @Handle
    public static boolean handle() {
        boolean testTriggered = false;

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> classer = classInfo.load();

            for (Method method : classer.getMethods()) {
                if (method.isAnnotationPresent(CyderTest.class)) {
                    String trigger = method.getAnnotation(CyderTest.class).value();
                    if (!trigger.equalsIgnoreCase(getInputHandler().commandAndArgsToString())) continue;
                    testTriggered = invokeTestsWithTrigger(trigger);
                    if (testTriggered) break;
                }
            }
        }

        return testTriggered;
    }

    /**
     * Invokes any and all {@link CyderTest}s found with the provided trigger.
     *
     * @param trigger the trigger
     * @return whether a test was invoked
     */
    @CanIgnoreReturnValue
    private static boolean invokeTestsWithTrigger(String trigger) {
        Preconditions.checkNotNull(trigger);
        Preconditions.checkArgument(!trigger.isEmpty());

        boolean ret = false;

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> classer = classInfo.load();

            for (Method method : classer.getMethods()) {
                if (method.isAnnotationPresent(CyderTest.class)) {
                    if (!ReflectionUtil.isStatic(method)) {
                        Logger.log(LogTag.CYDER_TEST_WARNING, "CyderTest method"
                                + " found not static: " + method.getName());
                        continue;
                    }
                    if (!ReflectionUtil.isPublic(method)) {
                        Logger.log(LogTag.CYDER_TEST_WARNING, "CyderTest method"
                                + " found not public: " + method.getName());
                        continue;
                    }
                    if (!ReflectionUtil.returnsVoid(method)) {
                        Logger.log(LogTag.CYDER_TEST_WARNING, "CyderTest method"
                                + " found not void return: " + method.getName());
                        continue;
                    }

                    String testTrigger = method.getAnnotation(CyderTest.class).value();
                    if (trigger.equalsIgnoreCase(testTrigger)) {
                        String threadName = "CyderTest thread runner, method: " + CyderStrings.quote
                                + method.getName() + CyderStrings.quote;
                        CyderThreadRunner.submit(() -> {
                            try {
                                Logger.log(LogTag.DEBUG, "Invoking CyderTest "
                                        + CyderStrings.quote + method.getName() + CyderStrings.quote
                                        + " in " + ReflectionUtil.getBottomLevelClass(classer));
                                method.invoke(classer);
                            } catch (Exception e) {
                                ExceptionHandler.handle(e);
                            }
                        }, threadName);

                        ret = true;
                    }
                }
            }
        }

        return ret;
    }
}