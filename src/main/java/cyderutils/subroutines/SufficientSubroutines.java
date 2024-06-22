package cyderutils.subroutines;

import com.google.common.collect.ImmutableList;
import cyderutils.exceptions.IllegalMethodException;
import cyderutils.process.PythonPackage;
import cyderutils.snakes.PythonUtil;
import cyderutils.strings.CyderStrings;
import cyderutils.threads.CyderThreadRunner;

import java.util.Optional;
import java.util.concurrent.Future;

/**
 * A subroutine for completing startup subroutines which are not necessary for Cyder to run properly.
 */
public final class SufficientSubroutines {
    /**
     * The minimum acceptable Python major version.
     */
    private static final int MIN_PYTHON_MAJOR_VERSION = 3;

    /**
     * The name of the sufficient subroutine to ensure the needed Python dependencies defined in
     * {@link cyderutils.process.PythonPackage} are installed.
     */
    private static final String PYTHON_PACKAGES_INSTALLED_ENSURER = "Python Packages Installed Ensurer";

    /**
     * The name of the sufficient subroutine to check for Python 3 being installed.
     */
    private static final String PYTHON_3_INSTALLED_ENSURER = "Python 3 Installed Ensurer";

    /**
     * The name for the thread which executes the sequential subroutines.
     */
    private static final String SUFFICIENT_SUBROUTINE_EXECUTOR_THREAD_NAME = "Sufficient Subroutine Executor";

    /**
     * Suppress default constructor.
     */
    private SufficientSubroutines() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The subroutines to execute.
     */
    public static final ImmutableList<Subroutine> subroutines = ImmutableList.of(
            new Subroutine(() -> {
                Future<ImmutableList<PythonPackage>> futureMissingPackages =
                        PythonUtil.getMissingRequiredPythonPackages();

                while (!futureMissingPackages.isDone()) Thread.onSpinWait();

                try {
                    ImmutableList<PythonPackage> missingPackages = futureMissingPackages.get();

                    for (PythonPackage missingPackage : missingPackages) {
                        // todo on missing package found
                        //                        Logger.log(LogTag.PYTHON, "Missing required Python package: "
                        //                                + missingPackage.getPackageName());
                    }

                    return missingPackages.isEmpty();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return false;
            }, PYTHON_PACKAGES_INSTALLED_ENSURER),

            new Subroutine(() -> {
                Future<Optional<String>> futureOptionalVersion = PythonUtil.getPythonVersion();
                while (!futureOptionalVersion.isDone()) Thread.onSpinWait();

                Optional<String> optionalVersion = Optional.empty();
                try {
                    optionalVersion = futureOptionalVersion.get();
                } catch (Exception ignored) {}

                if (optionalVersion.isEmpty()) {
                    // todo on failed to find python version
                    return false;
                }

                String versionString = optionalVersion.get();

                int version = -1;
                try {
                    version = Integer.parseInt(String.valueOf(versionString.charAt(0)));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (version == -1) {
                    // todo save as above, hook here though
                    return false;
                }

                if (version >= MIN_PYTHON_MAJOR_VERSION) {
                    // todo on successful and valid version found
                    return true;
                }

                // todo on too old version found hook
                return false;
            }, PYTHON_3_INSTALLED_ENSURER)
    );

    /**
     * Executes the sufficient subroutines in a separate thread.
     */
    public static void executeSubroutines() {
        CyderThreadRunner.submit(() -> {
            for (Subroutine sufficientSubroutine : subroutines) {
                CyderThreadRunner.submitSupplier(sufficientSubroutine.getRoutine(),
                        sufficientSubroutine.getThreadName());
            }
        }, SUFFICIENT_SUBROUTINE_EXECUTOR_THREAD_NAME);
    }
}
