package cyder.utilities;

import com.google.gson.Gson;
import cyder.handler.ErrorHandler;
import cyder.obj.User;
import cyder.ui.ConsoleFrame;

import java.io.*;
import java.lang.reflect.Method;

public class UserUtil {

    public static String getUserData(String name) {
        if (ConsoleFrame.getConsoleFrame().getUUID() == null)
            throw new IllegalArgumentException("UUID not yet set");
        File userJsonFile = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID()
                                    + "/userdata.json");

        if (!userJsonFile.exists())
            throw new IllegalArgumentException("userdata.json does not exist");

        User user = extractUser(userJsonFile);
        return extractUserData(user, name);
    }

    public static void setUserData(String name, String value) {
       if (ConsoleFrame.getConsoleFrame().getUUID() == null)
           throw new IllegalArgumentException("UUID is null");

       File userJsonFile = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID()
                                    + "/userdata.json");

       if (!userJsonFile.exists())
           throw new IllegalArgumentException("userdata.json does not exist");

       setUserData(userJsonFile, name, value);
    }

    public static <T> void setUserData(File f, String name, T value) {
        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("File is not a json type");

        User user = extractUser(f);

        try {
            for (Method m : user.getClass().getMethods()) {
                if (m.getName().startsWith("set")
                        && m.getParameterTypes().length == 1
                        && m.getName().toLowerCase().contains(name.toLowerCase())) {
                    m.invoke(user, value);
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter(f)) {
            gson.toJson(user, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function called upon UUID being set for consoleFrame to attempt to fix any user data
     * in case it was corrupted. If we fail to correct any corrupted data, then we corrupt the user and exit
     */
    public static void fixUser() {
        String user = ConsoleFrame.getConsoleFrame().getUUID();

        if (user == null)
            return;

        if (!new File("users/" + user + "/userdata.json").exists())
            IOUtil.corruptedUser();

        //todo read in default user data
        // for any null or empty data, add the default version
        // if any needed data such as password or name and such is null or blank or whatever then corrupted user
        // so ensure data has all pairs from a GenesisShare.getPrefs
    }

    /**
     * Extracts the user from the provided json file
     * @param f - the json file to extract a user object from
     * @return - teh resulting user object
     */
    public static User extractUser(File f) {
        if (!f.exists())
            throw new IllegalArgumentException("Provided file does not exist");
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("Provided file is not a json");

        User ret = null;
        Gson gson = new Gson();

        try (Reader reader = new FileReader(f)) {
            ret = gson.fromJson(reader, User.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return ret;
        }
    }

    /**
     * Extracts the requested data from the provided json file
     * @param f - the json file to extract data from
     * @param data - the data type to extract from the file
     * @return - the requested data
     */
    public static String extractUserData(File f, String data) {
        if (!StringUtil.getExtension(f).equals(".json"))
            throw new IllegalArgumentException("File is not a json type");
        if (!f.exists())
            throw new IllegalArgumentException("File does not exist");

        return extractUserData(extractUser(f), data);
    }

    /**
     * Assuming the corresponding getter and setter functions exist in User.java,
     * this method will call the getter method that matches the provided data
     * @param u - the initialized user containing the data we want to obtain
     * @param data - the data id for which to return
     * @return - the requested data
     */
    public static String extractUserData(User u, String data) {
        String ret = null;

        try {
            for (Method m : u.getClass().getMethods()) {
                if (m.getName().startsWith("get")
                        && m.getParameterTypes().length == 0
                        && m.getName().toLowerCase().contains(data.toLowerCase())) {
                    final Object r = m.invoke(u);
                    ret = (String) r;
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }
}
