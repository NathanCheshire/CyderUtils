package cyder.handlers.internal.enums;

/**
 * Hashes used for AutoCyphering upon Cyder entry.
 */
public enum DebugHash {
    /**
     * Nathan's hash.
     */
    Nathan("Nathan", "571b100a3b69f7b09828c0b61d325b15e52c512c14fdcf6d248f7ca733d0a7f6");

    /**
     * The name for the debug hash to use for the username.
     */
    private final String name;

    /**
     * The password for the debug hash to use for the password.
     */
    private final String pass;

    /**
     * Creates a new DebugHash.
     *
     * @param name the username to use
     * @param pass the already once hashed password to use
     */
    DebugHash(String name, String pass) {
        this.name = name;
        this.pass = pass;
    }

    /**
     * Returns the username associated with this DebugHash.
     *
     * @return the username associated with this DebugHash
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the password associated with this DebugHash.
     *
     * @return the password associated with this DebugHash
     */
    public String getPass() {
        return pass;
    }
}
