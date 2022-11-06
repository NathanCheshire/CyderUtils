package cyder.user;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.enums.Direction;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.genesis.CyderSplash;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.network.NetworkUtil;
import cyder.props.PropLoader;
import cyder.utils.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities regarding a user, their json file, and IO to/from that json file.
 */
public final class UserUtil {
    /**
     * The default background to use for account creation when a network connection is unavailable.
     */
    public static final BufferedImage DEFAULT_USER_SOLID_COLOR_BACKGROUND
            = ImageUtil.toBufferedImage(ImageUtil.imageIconFromColor(Color.black, 800, 800));

    /**
     * Suppress default constructor.
     */
    private UserUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The semaphore to use when reading or writing user data.
     */
    private static final Semaphore userIoSemaphore = new Semaphore(1);

    /**
     * The current Cyder user stored in memory and written to the
     * current user file whenever time data changes.
     */
    private static User cyderUser;

    /**
     * The corresponding file for cyderUser.
     */
    private static File cyderUserFile;

    /**
     * Returns the semaphore used for IO to/from the user's JSON file.
     *
     * @return the semaphore used for IO to/from the user's JSON file
     */
    public static Semaphore getUserIoSemaphore() {
        return userIoSemaphore;
    }

    /**
     * Blocks any future user IO by acquiring the semaphore and never releasing it.
     * This method blocks until the IO semaphore can be acquired.
     */
    public static synchronized void blockFutureIo() {
        try {
            userIoSemaphore.acquire();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * The last serialized string that was written to the current user file.
     */
    private static String previousSerializedUser = "";

    /**
     * The current levenshtein distance between the last and current write to the user json file.
     */
    private static int currentLevenshteinDistance;

    /**
     * Writes the current User, {@link UserUtil#cyderUser},
     * to the user's json if the json exists AND the provided user
     * object contains all the data required by a user object.
     * Upon a successful serialization/de-serialization, the json
     * is backed up and placed in dynamic/backup.
     */
    public static synchronized void writeUser() {
        if (cyderUserFile == null || !cyderUserFile.exists() || cyderUser == null)
            return;

        try {
            // write to user data file
            setUserData(cyderUserFile, cyderUser);

            // don't bother with other actions if the written value was no different than the previous
            if (currentLevenshteinDistance > 0) {
                // log the write since we know the user is valid
                Logger.log(LogTag.SYSTEM_IO, "[JSON WRITE] [Levenshtein = "
                        + currentLevenshteinDistance + "] User was written to file: "
                        + OsUtil.buildPath(cyderUserFile.getParentFile().getName(), cyderUserFile.getName()));

                getterSetterValidator(cyderUserFile);
                backupUserJsonFile(cyderUserFile);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Sets the given user to the current Cyder user.
     * This method should only be called if the current contents
     * of the user, meaning possible writes within the past 100ms,
     * can be discarded.
     * <p>
     * This method should only be called when setting
     * the user due to a Cyder login event.
     * <p>
     * If you are trying to set data for the current cyder user,
     * call {@link UserUtil#getCyderUser()}.
     * <p>
     * Common usages of this, such as setting an object such
     * as the screen stat would look like the following:
     *
     * <pre>{@code UserUtil.getCyderUser().setScreenStat(myScreenStat);}</pre>
     *
     * @param uuid the user's uuid
     */
    public static void setCyderUser(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(!uuid.isEmpty());

        File jsonFile = OsUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(), uuid, UserFile.USERDATA.getName());

        Preconditions.checkArgument(jsonFile.exists(), "File does not exist");
        Preconditions.checkArgument(FileUtil.getExtension(jsonFile).equals(Extension.JSON.getExtension()),
                "File is not a json type");

        cyderUserFile = jsonFile;
        cyderUser = extractUser(jsonFile);
    }

    /**
     * Returns the currently set Cyder user.
     * If not set, a default user is generated and returned.
     *
     * @return the currently set Cyder user
     */
    public static User getCyderUser() {
        if (cyderUser == null) {
            return buildDefaultUser();
        }

        return cyderUser;
    }

    /**
     * Writes the provided user to the provided file.
     *
     * @param file the file to write to
     * @param user the user object to serialize and write to the file
     */
    public static void setUserData(File file, User user) {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(user);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.JSON.getExtension()));

        try {
            FileWriter writer = new FileWriter(file);
            userIoSemaphore.acquire();
            SerializationUtil.toJson(user, writer);

            String currentSerializedUser = SerializationUtil.toJson(user);

            if (previousSerializedUser.isEmpty()) {
                currentLevenshteinDistance = currentSerializedUser.length();
            } else {
                currentLevenshteinDistance = StringUtil.levenshteinDistance(
                        currentSerializedUser, previousSerializedUser);
            }

            previousSerializedUser = currentSerializedUser;

            writer.close();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            userIoSemaphore.release();
        }
    }

    /**
     * The backup directory.
     */
    public static final File backupDirectory = new File(
            OsUtil.buildPath(Dynamic.PATH, Dynamic.BACKUP.getDirectoryName()));

    /**
     * Saves the provided jsonFile to the backup directory in case
     * restoration is required for the next Cyder instance.
     * Upon successfully saving the json, any past jsons for the user linked
     * to the uuid are deleted.
     *
     * @param jsonFile the current user json file to backup
     * @throws FatalException if the backup directory cannot be created
     */
    public static void backupUserJsonFile(File jsonFile) {
        Preconditions.checkNotNull(jsonFile);

        try {
            if (!backupDirectory.exists()) {
                if (!backupDirectory.mkdir()) {
                    throw new FatalException("Failed to create backup directory");
                }
            }

            long backupTimestamp = System.currentTimeMillis();
            String uuid = FileUtil.getFilename(jsonFile.getParentFile());
            String backupFilename = uuid + "_" + backupTimestamp + Extension.JSON.getExtension();

            File[] backups = backupDirectory.listFiles();
            checkNotNull(backups);
            long currentMaxTimestamp = 0;

            // find most recent timestamp that matches our uuid
            for (File backup : backups) {
                String filename = FileUtil.getFilename(backup);

                if (filename.contains("_")) {
                    String[] parts = filename.split("_");

                    if (parts.length == 2) {
                        String foundUuid = parts[0];
                        long foundTimestamp = Long.parseLong(parts[1]);

                        // if uuids match and timestamp is better
                        if (uuid.equals(foundUuid) && foundTimestamp > currentMaxTimestamp) {
                            currentMaxTimestamp = foundTimestamp;
                        }
                    }
                }
            }

            File mostRecentBackup = null;
            if (currentMaxTimestamp != 0) {
                mostRecentBackup = OsUtil.buildFile(Dynamic.PATH,
                        Dynamic.BACKUP.getDirectoryName(), uuid + "_" + currentMaxTimestamp);
            }

            if (mostRecentBackup == null || !FileUtil.fileContentsEqual(jsonFile, mostRecentBackup)) {
                File newBackup = OsUtil.buildFile(Dynamic.PATH, Dynamic.BACKUP.getDirectoryName(), backupFilename);
                if (!newBackup.createNewFile()) {
                    Logger.log(LogTag.DEBUG, "Failed to create backup file: "
                            + newBackup.getAbsolutePath() + ", for user: " + uuid);
                    return;
                }

                String backupSerializeUser = SerializationUtil.toJson(SerializationUtil.fromJson(jsonFile, User.class));
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(newBackup))) {
                    writer.write(backupSerializeUser);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                backups = backupDirectory.listFiles();
                checkNotNull(backups);

                for (File backup : backups) {
                    String filename = FileUtil.getFilename(backup);

                    if (filename.contains("_")) {
                        String[] parts = filename.split("_");

                        if (parts.length == 2) {
                            // if uuid of this backup is the user we just
                            // backed up and not the file we just made
                            if (parts[0].equals(uuid) && !FileUtil.getFilename(backup)
                                    .equals(FileUtil.getFilename(newBackup))) {
                                OsUtil.deleteFile(backup);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the most recent userdata.json backup for the provided user uuid.
     * If none is found, and empty optional is returned.
     *
     * @param uuid the uuid for the backup json to return
     * @return the most recent backup file for the user if found
     */
    public static Optional<File> getUserJsonBackup(String uuid) {
        Optional<File> ret = Optional.empty();

        // get backups
        File[] backups = backupDirectory.listFiles();

        // if backups were found
        if (backups != null && backups.length > 0) {
            long mostRecentTimestamp = 0;

            for (File backup : backups) {
                // not sure how this would happen but still check
                if (!FileUtil.getExtension(backup).equals(Extension.JSON.getExtension()))
                    continue;

                String name = FileUtil.getFilename(backup);

                // if backup is properly named
                if (name.contains("_")) {
                    String[] parts = name.split("_");

                    if (parts.length == 2) {
                        if (parts[0].equals(uuid)) {
                            long unixTimestamp = Long.parseLong(parts[1]);

                            if (unixTimestamp > mostRecentTimestamp)
                                mostRecentTimestamp = unixTimestamp;
                        }
                    }
                }
            }

            // if a recent backup was found for the user
            if (mostRecentTimestamp != 0) {
                File mostRecentBackup = OsUtil.buildFile(
                        Dynamic.PATH,
                        Dynamic.BACKUP.getDirectoryName(),
                        uuid + "_" + mostRecentTimestamp + Extension.JSON.getExtension());

                // should always be true...
                if (mostRecentBackup.exists()) {
                    ret = Optional.of(mostRecentBackup);
                }
            }
        }

        return ret;
    }

    /**
     * The maximum number of times to attempt to create a file/directory.
     */
    public static final int MAX_CREATION_ATTEMPTS = 1000;

    /**
     * Creates all the user files in {@link UserFile} for the user with the provided uuid.
     *
     * @param uuid the user uuid
     */
    public static void ensureUserFilesExist(String uuid) {
        checkNotNull(uuid);

        for (UserFile val : UserFile.values()) {
            if (val.getName().equals(UserFile.USERDATA.getName()))
                continue;

            File currentUserFile = OsUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(),
                    uuid, val.getName());

            if (!currentUserFile.exists()) {
                int attempts = 0;

                while (attempts < MAX_CREATION_ATTEMPTS) {
                    try {
                        boolean success;

                        if (currentUserFile.isFile()) {
                            success = currentUserFile.createNewFile();
                        } else {
                            success = currentUserFile.mkdir();
                        }

                        // if created, break
                        if (success)
                            break;
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);

                        // couldn't create so try again
                        attempts++;
                    }
                }

                if (attempts == MAX_CREATION_ATTEMPTS) {
                    // log the failure
                    Logger.log(LogTag.SYSTEM_IO,
                            "Unable to create all user files for user [" + uuid
                                    + "] after " + MAX_CREATION_ATTEMPTS + " attempts");
                }
            }
        }
    }

    /**
     * The maximum number of times to attempt to invoke the getter/setter validator on a file.
     */
    public static final int MAX_GETTER_SETTER_VALIDATION_ATTEMPTS = 10;

    /**
     * Attempts to fix any user data via GSON serialization
     * and invoking all setters with default data for corresponding
     * getters which returned null.
     *
     * @param userJson the json file to validate and fix if needed
     * @return whether the file could be handled correctly as a user
     * and was fixed if it was incorrect at first
     */
    public static boolean getterSetterValidator(File userJson) {
        Preconditions.checkArgument(userJson != null);

        // user doesn't have json so ignore it during Cyder instance
        if (!userJson.exists()) {
            return false;
        }

        // ensure all the user files are created
        ensureUserFilesExist(userJson.getParentFile().getName());

        // serialize the user, if this fails we're screwed from the start
        User user;
        try {
            user = extractUser(userJson);
        } catch (Exception ignored) {
            return false;
        }

        // if somehow GSON messed up then we're screwed
        if (user == null) {
            return false;
        }

        // master return val
        boolean ret = false;

        // attempt to validate MAX_GETTER_SETTER_VALIDATION_ATTEMPTS times
        int iterations = 0;
        while (iterations < MAX_GETTER_SETTER_VALIDATION_ATTEMPTS) {
            try {
                // begin getter setter restoration routine

                // for all getters (primitive values)
                for (Method getterMethod : user.getClass().getMethods()) {
                    if (getterMethod.getName().startsWith(GET)) {
                        Object getter = getterMethod.invoke(user);

                        if (!(getter instanceof String)) {
                            // invalid getter result so find default value and set

                            // find the preference associated with this getter
                            Preference preference = null;
                            for (Preference pref : Preference.getPreferences()) {
                                if (pref.getID().equalsIgnoreCase(getterMethod.getName()
                                        .replace(GET, ""))) {
                                    preference = pref;
                                    break;
                                }
                            }

                            // this skips for non primitive values
                            if (preference == null) {
                                continue;
                            }

                            // cannot attempt to restore objects who's tooltip is IGNORE
                            if (preference.getTooltip().equalsIgnoreCase("IGNORE")) {
                                return false;
                            }

                            // attempt to restore by using default value

                            // find setter
                            for (Method setterMethod : user.getClass().getMethods()) {
                                // if the setter matches our getter
                                if (setterMethod.getName().startsWith("set")
                                        && setterMethod.getParameterTypes().length == 1
                                        && setterMethod.getName().replace("set", "")
                                        .equalsIgnoreCase(getterMethod.getName().replace(GET, ""))) {

                                    // invoke setter method with default value
                                    setterMethod.invoke(user, preference.getDefaultValue());
                                    break;
                                }
                            }
                        }
                    }
                }

                // validate and remove possibly duplicate exes
                LinkedList<MappedExecutable> exes = user.getExecutables();
                LinkedList<MappedExecutable> nonDuplicates = new LinkedList<>();

                if (exes == null) {
                    user.setExecutables(new LinkedList<>());
                } else if (!exes.isEmpty()) {
                    for (MappedExecutable me : exes) {
                        if (!nonDuplicates.contains(me)) {
                            nonDuplicates.add(me);
                        }
                    }

                    user.setExecutables(nonDuplicates);
                }

                if (user.getScreenStat() == null) {
                    // screen stat restoration
                    user.setScreenStat(new ScreenStat(0, 0,
                            0, 0, 0, false, Direction.TOP));
                }

                // success in parsing so break out of loop
                ret = true;
                setUserData(userJson, user);
                break;
            } catch (Exception ignored) {
                iterations++;
            }
        }

        return ret;
    }

    /**
     * Attempts getter/setter validation for all users.
     * If this fails for a user, they become corrupted
     * for the current session meaning it is not usable.
     * Also ensures no users with a duplicate name exist.
     */
    public static void validateUsers() {
        // we use all user files here since we are determining if they are corrupted
        File users = OsUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName());

        File[] files = users.listFiles();

        if (files != null && files.length > 0) {
            for (File userFile : files) {
                //file userdata
                File json = new File(OsUtil.buildPath(
                        userFile.getAbsolutePath(), UserFile.USERDATA.getName()));

                if (json.exists()) {
                    if (!getterSetterValidator(json)) {
                        userJsonCorruption(userFile.getName());
                    }
                }
            }
        }

        LinkedList<String> usernames = new LinkedList<>();

        for (File userFile : getUserJsons()) {
            User user = extractUser(userFile);
            String username = user.getName();

            if (StringUtil.in(username, true, usernames)) {
                throw new FatalException("Duplicate username found: " + username
                        + ", second uuid: " + userFile.getParentFile().getName());
            }

            usernames.add(username);
        }
    }

    /**
     * Attempts to read backgrounds that Cyder would use for a user.
     * If failure, the image is corrupted, so we delete it in the calling function.
     *
     * @param uuid the uuid of the user whose backgrounds to validate
     */
    public static void deleteInvalidBackgrounds(String uuid) {
        try {
            //acquire sem so that any user requested exit will not corrupt the background
            getUserIoSemaphore().acquire();

            File currentUserBackgrounds = OsUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(), uuid,
                    UserFile.BACKGROUNDS.getName());

            if (!currentUserBackgrounds.exists())
                return;

            File[] files = currentUserBackgrounds.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    boolean valid = true;

                    try (FileInputStream fis = new FileInputStream(f)) {
                        ImageUtil.read(fis).getWidth();
                    } catch (Exception ignored) {
                        valid = false;
                    }

                    if (!valid) {
                        OsUtil.deleteFile(f);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            getUserIoSemaphore().release();
        }
    }

    /**
     * Extracts the user from the provided json file.
     *
     * @param file the json file to extract a user object from
     * @return the resulting user object
     */
    public static User extractUser(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.JSON.getExtension()));

        User ret = null;

        try {
            Reader reader = new FileReader(file);
            ret = SerializationUtil.fromJson(reader, User.class);
            reader.close();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * The method prefix to locate mutator methods reflectively.
     */
    private static final String SET = "set";

    /**
     * The method prefix to locate accessor methods reflectively.
     */
    private static final String GET = "get";

    /**
     * Sets the {@link UserUtil#cyderUser}'s data to the provided value.
     * This method exists purely for when indexing the preferences and user data
     * is required. The direct setter should be used if possible.
     *
     * @param name  the name of the data to set
     * @param value the new value
     */
    public static void setUserDataById(String name, String value) {
        try {
            for (Method m : cyderUser.getClass().getMethods()) {
                if (m.getName().startsWith(SET)
                        && m.getParameterTypes().length == 1
                        && m.getName().replace(SET, "").equalsIgnoreCase(name)) {
                    m.invoke(cyderUser, value);
                    writeUser();
                    break;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * The list of userdata to ignore when logging.
     */
    private static final ImmutableList<String> IGNORE_USER_DATA;

    /**
     * Returns the list of user data keys to ignore when logging.
     *
     * @return the list of user data keys to ignore when logging
     */
    public static ImmutableList<String> getIgnoreUserData() {
        return IGNORE_USER_DATA;
    }

    /**
     * The key to obtain the user data keys to ignore when logging.
     */
    private static final String IGNORE_DATA = "ignore_data";

    /**
     * The comma splitter.
     */
    private static final Splitter commaSplitter = Splitter.on(",");

    static {
        IGNORE_USER_DATA = ImmutableList.copyOf(commaSplitter.splitToList(PropLoader.getString(IGNORE_DATA)));
    }

    /**
     * Returns the requested data from the currently logged-in user.
     * This method exists purely for when indexing the preferences and user data
     * is required. The direct getter should be used if possible.
     *
     * @param id the ID of the data we want to obtain
     * @return the resulting data
     */
    public static String getUserDataById(String id) {
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(id));

        String ret = null;
        boolean shouldIgnore = StringUtil.in(id, true, IGNORE_USER_DATA);

        if (!shouldIgnore) {
            Logger.log(LogTag.SYSTEM_IO, "Userdata requested: " + id);
        }

        try {
            for (Method m : cyderUser.getClass().getMethods()) {
                if (m.getName().startsWith("get")
                        && m.getParameterTypes().length == 0
                        && m.getName().toLowerCase().contains(id.toLowerCase())) {
                    Object r = m.invoke(cyderUser);
                    ret = (String) r;
                    break;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns a user with all the default values set.
     * Note some default values are empty strings and others
     * are objects that should not be cast to strings.
     * <p>
     * Due to the mutability of a User, this method exist to create
     * a brand new object with default values each time as a static final
     * user cannot be created and returned safely.
     *
     * @return a user object with all the default {@link Preference}s
     */
    public static User buildDefaultUser() {
        User ret = new User();

        //for all the preferences
        for (Preference pref : Preference.getPreferences()) {
            //get all methods of user
            for (Method m : ret.getClass().getMethods()) {
                //make sure it's a setter with one parameter
                if (m.getName().startsWith("set") && m.getParameterTypes().length == 1) {
                    //parse away set from method name and find default preference from list above
                    String methodName = m.getName().replace("set", "");

                    //find default value to match
                    if (methodName.equalsIgnoreCase(pref.getID())) {
                        try {
                            Class<?> castTo = m.getParameterTypes()[0];
                            if (castTo.isPrimitive()) {
                                m.invoke(ret, pref.getDefaultValue());
                            } else {
                                m.invoke(ret, castTo.cast(pref.getDefaultValue()));
                            }

                            //we've invoked this setter with the preference so next preference
                            break;
                        } catch (Exception e) {
                            ExceptionHandler.silentHandle(e);
                        }
                    }
                }
            }
        }

        //external things stored in a user aside from preferences
        ret.setExecutables(null);

        return ret;
    }

    /**
     * Clean the user directories meaning the following actions are taken:
     *
     * <ul>
     *     <li>Ensuring the users directory is created</li>
     *     <li>Deleting non audio files from the Music/ directory</li>
     *     <li>Removing album art not linked to an audio file</li>
     *     <li>Removing backup json files which are not linked to any users</li>
     * </ul>
     */
    public static void cleanUsers() {
        File users = Dynamic.buildDynamic(Dynamic.USERS.getDirectoryName());
        if (!users.exists()) {
            if (!users.mkdirs()) {
                throw new FatalException("Failed to create users directory");
            }

            return;
        }

        File[] uuids = users.listFiles();
        if (uuids != null && uuids.length > 0) {
            for (File user : uuids) {
                if (!user.isDirectory()) {
                    throw new FatalException("Found non-directory in users directory: " + user.getAbsolutePath());
                }

                File musicDir = new File(OsUtil.buildPath(user.getAbsolutePath(), UserFile.MUSIC.getName()));
                if (musicDir.exists()) {
                    CyderSplash.INSTANCE.setLoadingMessage("Cleaning user music directory: "
                            + FileUtil.getFilename(user));
                    cleanUserMusicDirectory(musicDir, user);
                } else {
                    if (!OsUtil.createFile(musicDir, false)) {
                        throw new FatalException("Failed to create user's music directory: "
                                + musicDir.getAbsolutePath());
                    }
                }

                File backgroundsDir = new File(OsUtil.buildPath(
                        user.getAbsolutePath(), UserFile.BACKGROUNDS.getName()));
                if (backgroundsDir.exists()) {
                    CyderSplash.INSTANCE.setLoadingMessage("Resizing user backgrounds: "
                            + FileUtil.getFilename(user));
                    resizeUserBackgroundFiles(backgroundsDir);
                } else {
                    if (!OsUtil.createFile(backgroundsDir, false)) {
                        throw new FatalException("Failed to create user's backgrounds directory: "
                                + backgroundsDir.getAbsolutePath());
                    }
                }
            }
        }

        cleanBackupJsons();
    }

    /**
     * Resizes all the valid backgrounds in the provided user backgrounds directory.
     *
     * @param backgroundsDir the user backgrounds directory
     */
    private static void resizeUserBackgroundFiles(File backgroundsDir) {
        Preconditions.checkNotNull(backgroundsDir);
        Preconditions.checkArgument(backgroundsDir.exists());
        Preconditions.checkArgument(backgroundsDir.isDirectory());
        Preconditions.checkArgument(backgroundsDir.getName().equals(UserFile.BACKGROUNDS.getName()));

        File[] backgroundFiles = backgroundsDir.listFiles();
        if (backgroundFiles == null || backgroundFiles.length == 0) return;

        ArrayList<File> validBackgroundFiles = new ArrayList<>();
        Arrays.stream(backgroundFiles)
                .filter(FileUtil::isSupportedImageExtension)
                .forEach(validBackgroundFiles::add);

        Dimension maximumDimension = new Dimension(UiUtil.getDefaultMonitorWidth(), UiUtil.getDefaultMonitorHeight());

        for (File backgroundFile : validBackgroundFiles) {
            BufferedImage image = null;
            try {
                CyderSplash.INSTANCE.setLoadingMessage("Reading background: " + FileUtil.getFilename(backgroundFile));
                image = ImageUtil.read(backgroundFile);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            if (image == null) continue;
            CyderSplash.INSTANCE.setLoadingMessage("Resizing background: " + FileUtil.getFilename(backgroundFile));
            image = ImageUtil.ensureFitsInBounds(image, maximumDimension);

            try {
                if (!ImageIO.write(image, FileUtil.getExtensionWithoutPeriod(backgroundFile), backgroundFile)) {
                    throw new FatalException("Failed to downscale image: "
                            + backgroundFile.getAbsolutePath());
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    /**
     * Cleans the provided user music directory by removing non
     * audio files and album art not linked to current audio files.
     *
     * @param musicDirectory the user music directory
     * @param userDirectory  the user directory
     */
    private static void cleanUserMusicDirectory(File musicDirectory, File userDirectory) {
        Preconditions.checkNotNull(musicDirectory);
        Preconditions.checkNotNull(musicDirectory);
        Preconditions.checkArgument(musicDirectory.exists());
        Preconditions.checkArgument(userDirectory.exists());
        Preconditions.checkArgument(musicDirectory.isDirectory());
        Preconditions.checkArgument(userDirectory.isDirectory());
        Preconditions.checkArgument(musicDirectory.getName().equals(UserFile.MUSIC.getName()));

        File[] files = musicDirectory.listFiles();
        ArrayList<String> validMusicFileNames = new ArrayList<>();

        // Remove non audio files from music directory
        if (files != null && files.length > 0) {
            Arrays.stream(files).forEach(musicFile -> {
                if (!FileUtil.isSupportedAudioExtension(musicFile) && !musicFile.isDirectory()) {
                    OsUtil.deleteFile(musicFile);
                } else {
                    validMusicFileNames.add(FileUtil.getFilename(musicFile));
                }
            });
        }

        File albumArtDirectory = new File(OsUtil.buildPath(userDirectory.getAbsolutePath(),
                UserFile.MUSIC.getName(), UserFile.ALBUM_ART));
        if (!albumArtDirectory.exists()) return;
        File[] albumArtFiles = albumArtDirectory.listFiles();

        if (albumArtFiles != null && albumArtFiles.length > 0) {
            Arrays.stream(albumArtFiles).forEach(albumArtFile -> {
                if (!StringUtil.in(FileUtil.getFilename(albumArtFile), true, validMusicFileNames)) {
                    OsUtil.deleteFile(albumArtFile);
                }
            });
        }
    }

    /**
     * Removes any backup jsons from dynamic/backup not liked to current Cyder users.
     */
    private static void cleanBackupJsons() {
        File backupDirectory = OsUtil.buildFile(Dynamic.PATH, Dynamic.BACKUP.getDirectoryName());

        if (!backupDirectory.exists()) {
            return;
        }

        File[] backupFiles = backupDirectory.listFiles();

        if (backupFiles == null || backupFiles.length == 0) {
            return;
        }

        for (File backupFile : backupFiles) {
            String name = backupFile.getName();
            String uuid = name.split("_")[0];

            if (!StringUtil.in(uuid, false, getUserUuids())) {
                Logger.log(LogTag.DEBUG, "Deleting backup file not linked to user: " + name);
                OsUtil.deleteFile(backupFile);
            }
        }
    }

    /**
     * The linked list of invalid users which this instance of Cyder will ignore.
     */
    private static final LinkedList<String> invalidUUIDs = new LinkedList<>() {
        @Override
        public boolean remove(Object o) {
            throw new IllegalMethodException("Removing of invalid uuids not allowed");
        }
    };

    /**
     * Adds the provided uuid to the list of uuids to ignore throughout Cyder.
     *
     * @param uuid the uuid to ignore
     */
    public static void addInvalidUuid(String uuid) {
        if (!StringUtil.in(uuid, false, invalidUUIDs)) {
            invalidUUIDs.add(uuid);
        }
    }

    /**
     * After a user's json file was found to be invalid due to it being
     * un-parsable, null, empty, not there, or any other reason, this
     * method attempts to locate a backup to save the user.
     * If this fails, an information pane is shown saying which user failed to be parsed
     * <p>
     * This method should be utilized anywhere a userdata file is deemed invalid. Never
     * should a userdata file be deleted.
     *
     * @param uuid the uuid of the corrupted user
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void userJsonCorruption(String uuid) {
        try {
            File userJson = OsUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(),
                    uuid, UserFile.USERDATA.getName());

            try {
                // attempt to recovery a backup
                Optional<File> userJsonBackup = getUserJsonBackup(uuid);

                if (userJsonBackup.isPresent()) {
                    File restore = userJsonBackup.get();

                    if (!userJson.exists())
                        userJson.createNewFile();

                    // ensure the backup is parsable as a user object
                    Reader reader = new FileReader(restore);
                    User backupUser = SerializationUtil.fromJson(reader, User.class);
                    reader.close();

                    // write user to current user json
                    Writer writer = new FileWriter(userJson);
                    SerializationUtil.toJson(backupUser, writer);
                    writer.close();

                    // log success
                    Logger.log(LogTag.CORRUPTION,
                            "[BACKUP SUCCESS] Successfully restored "
                                    + uuid + " from: " + FileUtil.getFilename(userJsonBackup.get().getName()));

                    // success in restoring user from backup so exit method
                    return;
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                // exception above so proceed as normal
                Logger.log(LogTag.CORRUPTION,
                        "[BACKUP FAILURE] attempted restoration of " + uuid + " failed");
            }

            // no recovery so add uuid to the list of invalid users
            addInvalidUuid(uuid);

            // create parent directory
            File userDir = OsUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(), uuid);
            File json = OsUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(), uuid, UserFile.USERDATA.getName());

            // check for empty content
            if (json.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(json));
                if (StringUtil.isNullOrEmpty(reader.readLine())) {
                    reader.close();
                    OsUtil.deleteFile(json);
                }
            }

            File[] files = userDir.listFiles();

            //if there's nothing left in the user
            if (files != null && files.length == 0) {
                OsUtil.deleteFile(userDir);
            } else {
                // otherwise, we need to figure out all the file names in each sub-dir,
                // not recursive, and inform the user that a json was deleted
                // and tell them which files are remaining

                String informString = "Unfortunately a user's data file was corrupted and had to be deleted. "
                        + "The following files still exists and are associated with the user at the following "
                        + "path:<br/><b>"
                        + OsUtil.buildPath(Dynamic.PATH, Dynamic.USERS.getDirectoryName(), uuid) + "</b><br/>Files:";

                LinkedList<String> filenames = new LinkedList<>();

                File[] userFiles = userDir.listFiles();

                if (userFiles != null && userFiles.length > 0) {
                    for (File f : userFiles) {
                        if (f.isFile()) {
                            filenames.add(FileUtil.getFilename(f));
                        } else if (f.isDirectory()) {
                            File[] subFiles = f.listFiles();

                            if (subFiles != null && subFiles.length > 0) {
                                for (File file : subFiles) {
                                    filenames.add(FileUtil.getFilename(file));
                                }
                            }
                        }
                    }
                }

                if (filenames.isEmpty()) {
                    informString += "No files found associated with the corrupted user";
                } else {
                    StringBuilder sb = new StringBuilder();

                    for (String filename : filenames) {
                        sb.append("<br/>").append(filename);
                    }

                    informString += sb;
                }

                InformHandler.inform(new InformHandler.Builder(informString).setTitle("Userdata Corruption"));
                Logger.log(LogTag.CORRUPTION, "[Resulting Popup]\n" + informString);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the provided user file after creating it if it did not exist.
     *
     * @param userFile the user file to return the file reference of
     * @return the provided user file reference
     */
    public static File getUserFile(UserFile userFile) {
        Preconditions.checkNotNull(userFile);
        Preconditions.checkArgument(Console.INSTANCE.getUuid() != null);

        File ret = OsUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
                Console.INSTANCE.getUuid(), userFile.getName());

        if (!ret.exists()) {
            if (OsUtil.createFile(ret, false)) {
                return ret;
            } else {
                throw new FatalException("Failed to create user file: " + userFile);
            }
        }

        return ret;
    }

    /**
     * Returns whether there are no users created for Cyder.
     *
     * @return whether there are no users created for Cyder
     */
    public static boolean noUsers() {
        return getUserUuids().isEmpty();
    }

    /**
     * Returns a list of valid uuids associated with Cyder users.
     *
     * @return a list of valid uuids associated with Cyder users
     */
    public static ArrayList<String> getUserUuids() {
        ArrayList<String> uuids = new ArrayList<>();

        File usersDir = OsUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName());
        File[] users = usersDir.listFiles();

        if (users != null && users.length > 0) {
            for (File user : users) {
                File json = new File(OsUtil.buildPath(user.getAbsolutePath(), UserFile.USERDATA.getName()));

                if (json.exists() && !StringUtil.in(user.getName(), false, invalidUUIDs))
                    uuids.add(user.getName());
            }
        }

        return uuids;
    }

    /**
     * Returns a list of valid user jsons associated with Cyder users.
     *
     * @return a list of valid user jsons associated with Cyder users
     */
    public static ArrayList<File> getUserJsons() {
        ArrayList<File> userFiles = new ArrayList<>();

        File usersDir = OsUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName());
        File[] users = usersDir.listFiles();

        if (users != null && users.length > 0) {
            for (File user : users) {
                File json = new File(OsUtil.buildPath(user.getAbsolutePath(), UserFile.USERDATA.getName()));

                if (json.exists() && !StringUtil.in(user.getName(), false, invalidUUIDs)) {
                    userFiles.add(json);
                }
            }
        }

        return userFiles;
    }

    /**
     * Sets the loggedin keys of all users to 0.
     */
    public static void logoutAllUsers() {
        for (File json : getUserJsons()) {
            User u = extractUser(json);
            u.setLoggedIn("0");
            setUserData(json, u);
        }
    }

    /**
     * Searches through the users directory and finds the first logged-in user.
     *
     * @return the uuid of the first logged-in user
     */
    public static Optional<String> getFirstLoggedInUser() {
        for (File userJSON : getUserJsons()) {
            if (extractUser(userJSON).getLoggedIn().equals("1"))
                return Optional.of(FileUtil.getFilename(userJSON.getParentFile().getName()));
        }

        return Optional.empty();
    }

    /**
     * The maximum latency to allow when attempting to download the default user background.
     */
    private static final int MAX_LATENCY = 2000;

    /**
     * Creates the default background inside the user's Backgrounds/ directory.
     *
     * @param uuid the user's uuid to save the default background to
     * @return a reference to the file created
     */
    public static File createDefaultBackground(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(!uuid.isEmpty());

        BufferedImage createMe = DEFAULT_USER_SOLID_COLOR_BACKGROUND;

        int latency = NetworkUtil.latency(MAX_LATENCY);
        if (latency < MAX_LATENCY) {
            try {
                createMe = ImageUtil.read(CyderUrls.DEFAULT_BACKGROUND_URL);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        File backgroundFile = OsUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                uuid, UserFile.BACKGROUNDS.getName(), "Default" + Extension.PNG.getExtension());
        File backgroundFolder = OsUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                uuid, UserFile.BACKGROUNDS.getName());

        try {
            if (!backgroundFolder.exists()) {
                if (!backgroundFolder.mkdir()) {
                    throw new FatalException("Could not create user's background directory at: "
                            + backgroundFolder.getAbsolutePath());
                }
            }

            ImageIO.write(createMe, Extension.PNG.getExtensionWithoutPeriod(), backgroundFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return backgroundFile;
    }

    /**
     * Creates a file with the provided name in the current user's files/ directory.
     *
     * @param name the filename + extension to create in the files/ directory
     * @return a File object representing the file that was created
     * @throws IllegalStateException if the file could not be created at this time
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createFileInUserSpace(String name) {
        if (!StringUtil.isNullOrEmpty(Console.INSTANCE.getUuid())) {
            File saveDir = OsUtil.buildFile(Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(),
                    Console.INSTANCE.getUuid(), UserFile.FILES.getName());
            File createFile = new File(saveDir, name);

            if (createFile.exists()) {
                Logger.log(LogTag.SYSTEM_IO, "File already existed in userspace: " + name);
                return createFile;
            }

            try {
                if (!saveDir.exists()) {
                    saveDir.mkdir();
                }

                if (OsUtil.createFile(createFile, true)) {
                    Logger.log(LogTag.SYSTEM_IO, "Created file in userspace: " + name);
                    return createFile;
                }
            } catch (Exception ignored) {
            }
        }

        throw new IllegalStateException("File could not be created at this time: " + name);
    }

    /**
     * Resets all data/preferences (preferences for which {@link Preference#getIgnoreForUserCreation()} returns true)
     * to their default values.
     *
     * @param user the user to reset to a default state
     */
    public static void resetUser(User user) {
        for (Preference pref : Preference.getPreferences()) {
            if (!pref.getIgnoreForUserCreation()) {
                for (Method m : user.getClass().getMethods()) {
                    if (m.getName().startsWith(SET)
                            && m.getParameterTypes().length == 1
                            && m.getName().replace(SET, "").equalsIgnoreCase(pref.getID())) {
                        try {
                            m.invoke(user, pref.getDefaultValue());
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }

                        break;
                    }
                }
            }
        }
    }

    /**
     * User details are valid.
     */
    private static final String VALID = "Valid details";

    /**
     * No username was provided.
     */
    private static final String NO_USERNAME = "No username";

    /**
     * The username provided contains invalid characters.
     */
    private static final String INVALID_NAME = "Invalid name";

    /**
     * The username provided is already in use.
     */
    private static final String NAME_IN_USE = "Username already in use";

    /**
     * A validation wrapper for whether something is valid and an explanation message.
     */
    public record Validation(boolean valid, String message) {}

    /**
     * Returns whether the provided username is valid.
     *
     * @param username the username to validate
     * @return whether the provided username is valid
     */
    public static Validation validateUsername(String username) {
        Preconditions.checkNotNull(username);

        if (username.isEmpty()) {
            return new Validation(false, NO_USERNAME);
        } else if (!StringUtil.removeNonAscii(username).equals(username)) {
            return new Validation(false, INVALID_NAME);
        } else if (usernameInUse(username)) {
            return new Validation(false, NAME_IN_USE);
        } else {
            return new Validation(true, VALID);
        }
    }

    /**
     * No password was provided.
     */
    private static final String NO_PASSWORD = "No password";

    /**
     * No confirmation password was provided.
     */
    private static final String NO_CONFIRMATION = "No confirmation password";

    /**
     * The provided passwords do not match.
     */
    private static final String PASSWORDS_DO_NOT_MATCH = "Passwords do not match";

    /**
     * The password contains no letter.
     */
    private static final String NO_LETTER = "Password needs a letter";

    /**
     * The password is not of length at least 5.
     */
    private static final String INVALID_LENGTH = "Password is not > 4";

    /**
     * The password does not contain a number.
     */
    private static final String NO_NUMBER = "Password needs a number";

    /**
     * Returns whether the provided passwords match and are valid.
     *
     * @param password             the first password
     * @param passwordConfirmation the password confirmation
     * @return whether the provided passwords match and are valid
     */
    public static Validation validatePassword(char[] password, char[] passwordConfirmation) {
        Preconditions.checkNotNull(password);
        Preconditions.checkNotNull(passwordConfirmation);

        if (password.length == 0) {
            return new Validation(false, NO_PASSWORD);
        } else if (passwordConfirmation.length == 0) {
            return new Validation(false, NO_CONFIRMATION);
        } else if (!Arrays.equals(password, passwordConfirmation)) {
            return new Validation(false, PASSWORDS_DO_NOT_MATCH);
        } else if (password.length < 4) {
            return new Validation(false, INVALID_LENGTH);
        } else if (!StringUtil.containsLetter(password)) {
            return new Validation(false, NO_LETTER);
        } else if (!StringUtil.containsNumber(password)) {
            return new Validation(false, NO_NUMBER);
        } else {
            return new Validation(true, VALID);
        }
    }

    /**
     * Returns whether the provided username is already in use.
     *
     * @param username the username to determine if in use
     * @return whether the provided username is already in use
     */
    public static boolean usernameInUse(String username) {
        Preconditions.checkNotNull(username);
        Preconditions.checkArgument(!username.isEmpty());

        if (noUsers()) return false;

        for (File userFile : getUserJsons()) {
            if (extractUser(userFile).getName().equalsIgnoreCase(username)) {
                return true;
            }
        }

        return false;
    }

    /**
     * The key to use to obtain the ip data key from the props.
     */
    private static final String IP_KEY = "ip_key";

    /**
     * The key to use to obtain the YouTube API v3 key from the props.
     */
    private static final String YOUTUBE_API_3_KEY = "youtube_api_3_key";

    /**
     * Validates the ip key from the propkeys.ini file.
     *
     * @return whether the ip key was valid
     */
    private static boolean validateIpKey() {
        try {
            URL url = new URL(CyderUrls.IPDATA_BASE + PropLoader.getString(IP_KEY));
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            reader.close();
            return true;
        } catch (Exception ex) {
            ExceptionHandler.silentHandle(ex);
        }

        return false;
    }

    /**
     * The header for the url to validate a provided YouTube API 3 key.
     */
    private static final String YOUTUBE_API_3_KEY_VALIDATOR_HEADER = CyderUrls.YOUTUBE_API_V3_SEARCH
            + "?part=snippet&q=" + "gift+and+a+curse+skizzy+mars" + "&type=video&key=";

    /**
     * Validates the youtube key from the propkeys.ini file.
     *
     * @return whether the youtube key was valid
     */
    private static boolean validateYoutubeApiKey() {
        String key = PropLoader.getString(YOUTUBE_API_3_KEY);

        if (!key.isEmpty()) {
            try {
                NetworkUtil.readUrl(YOUTUBE_API_3_KEY_VALIDATOR_HEADER + key);
                return true;
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }

        return false;
    }

    /**
     * Validates the weather key from the propkeys.ini file.
     *
     * @return whether the weather key was valid
     */
    private static boolean validateWeatherKey() {
        String openString = CyderUrls.OPEN_WEATHER_BASE
                + PropLoader.getString("default_weather_location")
                + "&appid=" + PropLoader.getString("weather_key")
                + "&units=imperial";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(openString).openStream()))) {
            reader.readLine();
            return true;
        } catch (Exception ex) {
            ExceptionHandler.silentHandle(ex);
        }

        return false;
    }
}
