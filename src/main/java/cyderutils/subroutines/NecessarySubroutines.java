package cyderutils.subroutines;

import com.google.common.collect.ImmutableList;
import cyderutils.exceptions.FatalException;
import cyderutils.exceptions.IllegalMethodException;
import cyderutils.files.FileUtil;
import cyderutils.strings.CyderStrings;
import cyderutils.utils.ArrayUtil;
import cyderutils.utils.StaticUtil;

import java.awt.*;
import java.io.File;

/**
 * Subroutines which must complete in order for Cyder to start.
 */
public final class NecessarySubroutines {
    /**
     * The font directory name to load the true-type fonts from.
     */
    private static final String fonts = "fonts";

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
            }, "Registering fonts", "Registering fonts failed")
    );
}
