package com.github.natche.cyderutils.network.ipdataco.models;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import com.google.gson.annotations.SerializedName;

/**
 * A language entry for the list of languages a user might be native to.
 */
@Immutable
public final class Language {
    /**
     * The name of the language.
     */
    private final String name;

    /**
     * The native name of the language.
     */
    @SerializedName("native")
    private final String nativeName;

    /**
     * The ISO639-1 language code.
     */
    private final String code;

    /**
     * Constructs a new language object.
     *
     * @param name       the name of the language
     * @param nativeName the native name of the language
     * @param code       the ISO639-1 language code
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if any parameter is empty
     */
    public Language(String name, String nativeName, String code) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(nativeName);
        Preconditions.checkNotNull(code);
        Preconditions.checkArgument(!name.trim().isEmpty());
        Preconditions.checkArgument(!nativeName.trim().isEmpty());
        Preconditions.checkArgument(!code.trim().isEmpty());

        this.name = name;
        this.nativeName = nativeName;
        this.code = code;
    }

    /**
     * Returns the name of the language.
     *
     * @return the name of the language
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the native name of the language.
     *
     * @return the native name of the language
     */
    public String getNativeName() {
        return nativeName;
    }

    /**
     * Returns the ISO639-1 language code.
     *
     * @return the ISO639-1 language code
     */
    public String getCode() {
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Language{"
                + "name='" + name + "\""
                + ", nativeName='" + nativeName + "\""
                + ", code='" + code + "\""
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = name.hashCode();
        ret = 31 * ret + nativeName.hashCode();
        ret = 31 * ret + code.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Language)) {
            return false;
        }

        Language other = (Language) o;
        return name.equals(other.name)
                && nativeName.equals(other.name)
                && code.equals(other.code);
    }
}

