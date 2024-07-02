package com.github.natche.cyderutils.network.ipdataco.models;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import com.google.gson.annotations.SerializedName;

/** A currency object representing a user's home currency. */
@Immutable
public final class Currency {
    /** The full name of the currency. */
    private final String name;

    /** The ISO4217 currency code. */
    private final String code;

    /** The symbol of the currency. */
    private final String symbol;

    /** The native symbol of the currency. */
    @SerializedName("native")
    private final String nativeSymbol;

    /** The plural version of the currency. */
    private final String plural;

    /**
     * Constructs a new Currency object.
     *
     * @param name         the full name of the currency
     * @param code         the ISO4217 currency code
     * @param symbol       the symbol of the currency
     * @param nativeSymbol the native symbol of the currency
     * @param plural       the plural version of the currency
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if any parameter is empty
     */
    public Currency(String name, String code, String symbol, String nativeSymbol, String plural) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(code);
        Preconditions.checkNotNull(symbol);
        Preconditions.checkNotNull(nativeSymbol);
        Preconditions.checkNotNull(plural);
        Preconditions.checkArgument(!name.trim().isEmpty());
        Preconditions.checkArgument(!code.trim().isEmpty());
        Preconditions.checkArgument(!symbol.trim().isEmpty());
        Preconditions.checkArgument(!nativeSymbol.trim().isEmpty());
        Preconditions.checkArgument(!plural.trim().isEmpty());

        this.name = name;
        this.code = code;
        this.symbol = symbol;
        this.nativeSymbol = nativeSymbol;
        this.plural = plural;
    }

    /**
     * Returns the full name of the currency.
     *
     * @return the full name of the currency
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the ISO4217 currency code.
     *
     * @return the ISO4217 currency code
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the symbol of the currency.
     *
     * @return the symbol of the currency
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the native symbol of the currency.
     *
     * @return the native symbol of the currency
     */
    public String getNativeSymbol() {
        return nativeSymbol;
    }

    /**
     * Returns the plural version of the currency.
     *
     * @return the plural version of the currency
     */
    public String getPlural() {
        return plural;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Currency{"
                + "name=\"" + name + "\""
                + ", code=\"" + code + "\""
                + ", symbol=\"" + symbol + "\""
                + ", nativeSymbol=\"" + nativeSymbol + "\""
                + ", plural=\"" + plural + "\""
                + "}";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = name.hashCode();
        ret = 31 * ret + code.hashCode();
        ret = 31 * ret + symbol.hashCode();
        ret = 31 * ret + nativeSymbol.hashCode();
        ret = 31 * ret + plural.hashCode();
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Currency)) {
            return false;
        }

        Currency other = (Currency) o;
        return name.equals(other.name)
                && code.equals(other.code)
                && symbol.equals(other.symbol)
                && nativeSymbol.equals(other.nativeSymbol)
                && plural.equals(other.plural);
    }
}

