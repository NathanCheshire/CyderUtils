package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.enums.Direction;
import cyder.enums.Dynamic;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.Logger;
import cyder.user.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities regarding a user, their json file, and IO to/from that json file.
 */
public class UserUtil {
    /**
     * Instantiation of util method not allowed.
     */
    private UserUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * The semaphore to use when reading or writing user data.
     */
    private static final Semaphore userIOSemaphore = new Semaphore(1);

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
    public static Semaphore getUserIOSemaphore() {
        return userIOSemaphore;
    }

    /**
     * Blocks any future user IO by acquiring the semaphore and never releasing it.
     * This method blocks until the IO semaphore can be acquired.
     */
    public static synchronized void blockFutureIO() {
        try {
            userIOSemaphore.acquire();
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
                Logger.log(Logger.Tag.SYSTEM_IO, "[JSON WRITE] [Levenshtein = "
                        + currentLevenshteinDistance + "] User was written to file: "
                        + OSUtil.buildPath(cyderUserFile.getParentFile().getName(), cyderUserFile.getName()));

                // validate the user is still valid
                getterSetterValidator(cyderUserFile);

                // backup the file
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

        File jsonFile = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(), uuid, UserFile.USERDATA.getName());

        Preconditions.checkArgument(jsonFile.exists(), "File does not exist");
        Preconditions.checkArgument(FileUtil.getExtension(jsonFile).equals(".json"),
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
     * @param f the file to write to
     * @param u the user object to write to the file
     */
    public static void setUserData(File f, User u) {
        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");
        if (!FileUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("File is not a json type");

        Gson gson = new Gson();

        try {
            FileWriter writer = new FileWriter(f);
            userIOSemaphore.acquire();
            gson.toJson(u, writer);

            String currentSerializedUser = gson.toJson(u);

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
            userIOSemaphore.release();
        }
    }

    /**
     * The backup directory.
     */
    public static final File backupDirectory =
            new File(OSUtil.buildPath(Dynamic.PATH,
                    Dynamic.BACKUP.getDirectoryName()));

    /**
     * Saves the provided jsonFile to the backup directory in case
     * restoration is required for the next Cyder instance.
     * Upon successfully saving the json, any past jsons for the user linked
     * to the uuid are deleted.
     *
     * @param jsonFile the current user json file to backup
     */
    public static void backupUserJsonFile(File jsonFile) {
        try {
            // ensure save directory exists
            if (!backupDirectory.exists()) {
                backupDirectory.mkdir();
            }

            // timestamp to mark this backup
            long timestamp = System.currentTimeMillis();
            String uuid = FileUtil.getFilename(jsonFile.getParentFile());
            String newFilename = uuid + "_" + timestamp + ".json";

            // find most recent file
            File[] backups = backupDirectory.listFiles();
            checkNotNull(backups);
            long currentMaxTimestamp = 0;

            // find most recent timestamp that matches our uuid
            for (File backup : backups) {
                String filename = FileUtil.getFilename(backup);

                // ensure in valid format
                if (filename.contains("_")) {
                    String[] parts = filename.split("_");

                    // ensure like "uuid_timestamp"
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

            // build file from uuid and the found most recent timestamp
            File mostRecentFile = null;

            // found one if the timestamp isn't the initial value
            if (currentMaxTimestamp != 0) {
                mostRecentFile = OSUtil.buildFile(
                        Dynamic.PATH,
                        Dynamic.BACKUP.getDirectoryName(), uuid + "_" + currentMaxTimestamp);
            }

            // if no files in directory or current is different from previous
            if (mostRecentFile == null || !FileUtil.fileContentsEqual(jsonFile, mostRecentFile)) {
                // copy file contents from jsonFile to newBackup
                File newBackup = OSUtil.buildFile(Dynamic.PATH,
                        Dynamic.BACKUP.getDirectoryName(), newFilename);
                if (!newBackup.createNewFile()) {
                    Logger.log(Logger.Tag.DEBUG, "Failed to create backup file: "
                            + newBackup.getAbsolutePath() + ", for user: " + uuid);
                    return;
                }

                BufferedReader jsonReader = new BufferedReader(new FileReader(jsonFile));
                String serializedUser = jsonReader.readLine();
                jsonReader.close();

                BufferedWriter jsonWriter = new BufferedWriter(new FileWriter(newBackup));
                jsonWriter.write(serializedUser);
                jsonWriter.close();

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
                                OSUtil.deleteFile(backup);
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
                if (!FileUtil.getExtension(backup).equals(".json"))
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
                File mostRecentBackup = OSUtil.buildFile(
                        Dynamic.PATH,
                        Dynamic.BACKUP.getDirectoryName(),
                        uuid + "_" + mostRecentTimestamp + ".json");

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

            File currentUserFile = OSUtil.buildFile(
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
                    Logger.log(Logger.Tag.SYSTEM_IO,
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
    @SuppressWarnings("UnusedAssignment")
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
                    if (getterMethod.getName().startsWith("get")) {
                        Object getter = getterMethod.invoke(user);

                        if (!(getter instanceof String)) {
                            // invalid getter result so find default value and set

                            // find the preference associated with this getter
                            Preference preference = null;
                            for (Preference pref : Preferences.getPreferences()) {
                                if (pref.getID().equalsIgnoreCase(getterMethod.getName()
                                        .replace("get", ""))) {
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
                                        .equalsIgnoreCase(getterMethod.getName().replace("get", ""))) {

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

                if (exes != null && !exes.isEmpty()) {
                    for (MappedExecutable me : exes) {
                        if (!nonDuplicates.contains(me)) {
                            nonDuplicates.add(me);
                        }
                    }

                    // set exes
                    user.setExecutables(nonDuplicates);
                }
                // somehow null so just make an empty list
                else if (exes == null) {
                    exes = new LinkedList<>();
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
     */
    public static void validateUsers() {
        // we use all user files here since we are determining if they are corrupted or not
        File users = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName());

        File[] files = users.listFiles();

        if (files != null && files.length > 0) {
            for (File userFile : files) {
                //file userdata
                File json = new File(OSUtil.buildPath(
                        userFile.getAbsolutePath(), UserFile.USERDATA.getName()));

                if (json.exists()) {
                    if (!getterSetterValidator(json)) {
                        userJsonCorruption(userFile.getName());
                    }
                }
            }
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
            getUserIOSemaphore().acquire();

            File currentUserBackgrounds = OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(), uuid,
                    UserFile.BACKGROUNDS.getName());

            if (!currentUserBackgrounds.exists())
                return;

            File[] files = currentUserBackgrounds.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    boolean valid = true;

                    try (FileInputStream fi = new FileInputStream(f)) {
                        ImageIO.read(fi).getWidth();
                    } catch (Exception e) {
                        valid = false;
                        ExceptionHandler.silentHandle(e);
                    }

                    if (!valid) {
                        OSUtil.deleteFile(f);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            getUserIOSemaphore().release();
        }
    }

    /**
     * Extracts the user from the provided json file.
     *
     * @param f the json file to extract a user object from
     * @return the resulting user object
     */
    public static User extractUser(File f) {
        Preconditions.checkArgument(f.exists(), "Provided file does not exist");
        Preconditions.checkArgument(FileUtil.validateExtension(f, ".json"),
                "Provided file is not a json");

        User ret = null;
        Gson gson = new Gson();

        try {
            Reader reader = new FileReader(f);
            ret = gson.fromJson(reader, User.class);
            reader.close();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

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
                if (m.getName().startsWith("set")
                        && m.getParameterTypes().length == 1
                        && m.getName().replace("set", "").equalsIgnoreCase(name)) {
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
     * Returns the requested data from the currently logged-in user.
     * This method exists purely for when indexing the preferences and user data
     * is required. The direct getter should be used if possible.
     *
     * @param id the ID of the data we want to obtain
     * @return the resulting data
     */
    public static String getUserDataById(String id) {
        Preconditions.checkArgument(!StringUtil.isNull(id),
                "Invalid id argument: " + id);

        String ret = null;

        String[] ignoreDatas = PropLoader.getString("ignore_data").split(",");
        boolean shouldIgnore = StringUtil.in(id, true, ignoreDatas);

        if (!shouldIgnore) {
            Logger.log(Logger.Tag.SYSTEM_IO, "Userdata requested: " + id);
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
     * @return a user object with all the default
     * {@link Preferences} found in {@code GenesisShare}.
     */
    public static User buildDefaultUser() {
        User ret = new User();

        //for all the preferences
        for (Preference pref : Preferences.getPreferences()) {
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
                            // :/ not sure what happened here
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
     * Deleting non audio files from the Music/ directory
     * Removing album art not linked to an audio file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void cleanUsers() {
        File users = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName());

        if (!users.exists()) {
            users.mkdir();
        } else {
            File[] uuids = users.listFiles();

            if (uuids != null && uuids.length > 0) {
                for (File user : uuids) {
                    if (!user.isDirectory())
                        continue;
                    // take care of void users
                    if (user.isDirectory() && user.getName().contains("VoidUser")) {
                        OSUtil.deleteFile(user);
                    } else {
                        File musicDir = new File(OSUtil.buildPath(user.getAbsolutePath(), "Music"));

                        if (!musicDir.exists()) {
                            continue;
                        }

                        File[] files = musicDir.listFiles();
                        ArrayList<String> validMusicFileNames = new ArrayList<>();

                        if (files != null && files.length > 0) {
                            for (File musicFile : files) {
                                if (!FileUtil.isSupportedAudioExtension(musicFile) && !musicFile.isDirectory()) {
                                    OSUtil.deleteFile(musicFile);
                                } else {
                                    validMusicFileNames.add(FileUtil.getFilename(musicFile));
                                }
                            }
                        }

                        File albumArtDirectory = new File
                                (OSUtil.buildPath(user.getAbsolutePath(), "Music", "AlbumArt"));

                        if (!albumArtDirectory.exists())
                            continue;

                        File[] albumArtFiles = albumArtDirectory.listFiles();

                        if (albumArtFiles != null && albumArtFiles.length > 0) {
                            for (File albumArt : albumArtFiles) {

                                // if the album art file name does not match to a music file name, delete it
                                if (!StringUtil.in(FileUtil.getFilename(albumArt),
                                        true, validMusicFileNames)) {
                                    OSUtil.deleteFile(albumArt);
                                }
                            }
                        }
                    }
                }
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
            File userJson = OSUtil.buildFile(
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

                    Gson gson = new Gson();

                    // ensure the backup is parsable as a user object
                    Reader reader = new FileReader(restore);
                    User backupUser = gson.fromJson(reader, User.class);
                    reader.close();

                    // write user to current user json
                    Writer writer = new FileWriter(userJson);
                    gson.toJson(backupUser, writer);
                    writer.close();

                    // log success
                    Logger.log(Logger.Tag.CORRUPTION,
                            "[BACKUP SUCCESS] Successfully restored "
                                    + uuid + " from: " + FileUtil.getFilename(userJsonBackup.get().getName()));

                    // success in restoring user from backup so exit method
                    return;
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                // exception above so proceed as normal
                Logger.log(Logger.Tag.CORRUPTION,
                        "[BACKUP FAILURE] attempted restoration of " + uuid + " failed");
            }

            // no recovery so add uuid to the list of invalid users
            addInvalidUuid(uuid);

            // create parent directory
            File userDir = OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(), uuid);
            File json = OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(), uuid, UserFile.USERDATA.getName());

            // check for empty content
            if (json.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(json));
                if (StringUtil.isNull(reader.readLine())) {
                    reader.close();
                    OSUtil.deleteFile(json);
                }
            }

            File[] files = userDir.listFiles();

            //if there's nothing left in the user
            if (files != null && files.length == 0) {
                OSUtil.deleteFile(userDir);
            } else {
                // otherwise, we need to figure out all the file names in each sub-dir,
                // not recursive, and inform the user that a json was deleted
                // and tell them which files are remaining

                String informString = "Unfortunately a user's data file was corrupted and had to be deleted. "
                        + "The following files still exists and are associated with the user at the following "
                        + "path:<br/><b>"
                        + OSUtil.buildPath(Dynamic.PATH,
                        Dynamic.USERS.getDirectoryName(), uuid) + "</b><br/>Files:";

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

                //log the corruption
                Logger.log(Logger.Tag.CORRUPTION, "[Resulting Popup]\n" + informString);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the provided user file after creating it if it did not exist.
     *
     * @param fileName the file name of the user file to return a reference to
     * @return the provided user file
     */
    public static File getUserFile(String fileName) {
        Preconditions.checkArgument(ConsoleFrame.INSTANCE.getUUID() != null,
                "ConsoleFrame uuid is not yet set");

        boolean in = false;

        for (UserFile f : UserFile.values()) {
            if (fileName.equalsIgnoreCase(f.getName())) {
                in = true;
                break;
            }
        }

        if (!in) {
            throw new IllegalArgumentException("Provided user file does not exists as standard enum type");
        }

        File ret = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
                ConsoleFrame.INSTANCE.getUUID(), fileName);

        if (!ret.exists()) {
            if (ret.mkdir()) {
                return ret;
            } else {
                throw new FatalException("Failed to create user file: " + fileName);
            }
        }

        return ret;
    }

    /**
     * Returns the number of valid users associated with Cyder.
     *
     * @return the number of valid users associated with Cyder
     */
    public static int getUserCount() {
        return getUserUUIDs().size();
    }

    /**
     * Returns a list of valid uuids associated with Cyder users.
     *
     * @return a list of valid uuids associated with Cyder users
     */
    public static ArrayList<String> getUserUUIDs() {
        ArrayList<String> uuids = new ArrayList<>();

        File usersDir = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName());
        File[] users = usersDir.listFiles();

        if (users != null && users.length > 0) {
            for (File user : users) {
                File json = new File(OSUtil.buildPath(user.getAbsolutePath(), UserFile.USERDATA.getName()));

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

        File usersDir = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName());
        File[] users = usersDir.listFiles();

        if (users != null && users.length > 0) {
            for (File user : users) {
                File json = new File(OSUtil.buildPath(user.getAbsolutePath(), UserFile.USERDATA.getName()));

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
            u.setLoggedin("0");
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
            if (extractUser(userJSON).getLoggedin().equals("1"))
                return Optional.of(FileUtil.getFilename(userJSON.getParentFile().getName()));
        }

        return Optional.empty();
    }

    /**
     * Creates the default background inside the user's Backgrounds/ directory.
     *
     * @param uuid the user's uuid to save the default background to
     * @return a reference to the file created
     */
    public static File createDefaultBackground(String uuid) {
        //default background is creating an image gradient
        Image img = CyderIcons.defaultBackground.getImage();

        BufferedImage bi;

        //try to get default image that isn't bundled with Cyder
        try {
            bi = ImageIO.read(new URL(CyderUrls.DEFAULT_BACKGROUND_URL));
        } catch (Exception e) {
            ExceptionHandler.handle(e);

            bi = new BufferedImage(img.getWidth(null),
                    img.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
        }

        File backgroundFile = OSUtil.buildFile(
                Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
                uuid, UserFile.BACKGROUNDS.getName(), "Default.png");

        File backgroundFolder = OSUtil.buildFile(
                Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
                uuid, UserFile.BACKGROUNDS.getName());

        try {
            if (!backgroundFolder.exists()) {
                if (!backgroundFolder.mkdir()) {
                    throw new FatalException("Could not create user's background directory at: "
                            + backgroundFolder.getAbsolutePath());
                }
            }

            ImageIO.write(bi, "png", backgroundFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return backgroundFile;
    }

    /**
     * Saves the provided file in the current user's files/ directory.
     *
     * @param name the filename + extension to create in the files/ directory
     * @return a File object representing the file that was created
     * @throws IllegalStateException if the file could not be created at this time
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createFileInUserSpace(String name) {
        if (!StringUtil.isNull(ConsoleFrame.INSTANCE.getUUID())) {
            File saveDir = OSUtil.buildFile(Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(),
                    ConsoleFrame.INSTANCE.getUUID(), UserFile.FILES.getName());
            File createFile = new File(saveDir, name);

            if (createFile.exists()) {
                Logger.log(Logger.Tag.SYSTEM_IO, "File already existed in userspace: " + name);
                return createFile;
            }

            try {
                if (!saveDir.exists()) {
                    saveDir.mkdir();
                }

                if (OSUtil.createFile(createFile, true)) {
                    Logger.log(Logger.Tag.SYSTEM_IO, "Created file in userspace: " + name);
                    return createFile;
                }
            } catch (Exception ignored) {
            }
        }

        throw new IllegalStateException("File could not be created at this time: " + name);
    }
}