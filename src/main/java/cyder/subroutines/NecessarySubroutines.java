package cyder.subroutines;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.enumerations.CyderInspection;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.utils.ArrayUtil;
import cyder.utils.ReflectionUtil;
import cyder.utils.StaticUtil;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Subroutines which must complete in order for Cyder to start.
 */
public final class NecessarySubroutines {
    /**
     * The test key word to validate tests.
     */
    private static final String TEST = "test";

    /**
     * The vanilla developer names.
     */
    private static final ImmutableList<String> DEVELOPER_NAMES = ImmutableList.of(
            "Nathan Cheshire",
            "Nate Cheshire",
            "Natche",
            "Cypher"
    );

    /**
     * The list of triggers for GuiTest methods.
     */
    private static final ArrayList<String> guiTestTriggers = new ArrayList<>();

    /**
     * The font directory name to load the true-type fonts from.
     */
    private static final String fonts = "fonts";

    /**
     * A list of the discovered triggers from handle annotations.
     */
    private static final ArrayList<String> handleTriggers = new ArrayList<>();

    /**
     * Suppress default constructor.
     *
     * @throws IllegalMethodException if invoked
     */
    private NecessarySubroutines() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Executes the necessary subroutines on the main thread.
     */
    public static void executeSubroutines() {
        for (Subroutine subroutine : subroutines) {
            Boolean result = subroutine.getRoutine().get();

            if (result == null || !result) {
                throw new FatalException(subroutine.getOnFailureMessage());
            }
        }
    }

    /**
     * The list of necessary subroutines which must complete successfully before builds of Cyder are released.
     */
    public static final ImmutableList<Subroutine> subroutines = ImmutableList.of(
            new Subroutine(() -> {
                File[] fontFiles = StaticUtil.getStaticDirectory(fonts).listFiles();

                if (ArrayUtil.nullOrEmpty(fontFiles)) return false;

                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

                for (File fontFile : fontFiles) {
                    if (FileUtil.isSupportedFontExtension(fontFile)) {
                        try {
                            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
                            // todo on font registered hook
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                }

                return true;
            }, "Registering fonts", "Registering fonts failed"),

            new Subroutine(NecessarySubroutines::validateVanillaAnnotations,
                    "Validating vanilla classes", "Validation of vanilla classes failed")
    );

    /**
     * Validates all widget classes annotated with {@link cyder.annotations.Vanilla} annotation.
     *
     * @return whether all vanilla annotations are valid
     */
    private static boolean validateVanillaAnnotations() {
        boolean ret = true;

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> clazz = classInfo.load();

            if (clazz.isAnnotationPresent(Vanilla.class)) {
                if (!validateVanillaAnnotation(clazz)) {
                    ret = false;
                }
            }
        }

        return ret;
    }

    /**
     * Validates the provided vanilla class.
     * A vanilla class is valid if the following are true
     * <ul>
     *     <li>The CyderAuthor annotation is present</li>
     *     <li>The CyderAuthor annotation credits Nate Cheshire as the author</li>
     * </ul>
     *
     * @param clazz the vanilla class to validate
     * @return whether the vanilla class is valid
     */
    private static boolean validateVanillaAnnotation(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkArgument(clazz.isAnnotationPresent(Vanilla.class));

        if (clazz.isAnnotationPresent(SuppressCyderInspections.class)
                && ArrayUtil.toList(clazz.getAnnotation(
                SuppressCyderInspections.class).value()).contains(CyderInspection.VanillaInspection)) {
            return true;
        }

        if (!clazz.isAnnotationPresent(CyderAuthor.class)) {
            // todo on vanilla found without CyderAuthor annotation
            return false;
        }

        String author = clazz.getAnnotation(CyderAuthor.class).author();

        if (!StringUtil.in(author, true, DEVELOPER_NAMES)) {
            // todo on vanilla validation failure hook
            return false;
        }

        return true;
    }
}
