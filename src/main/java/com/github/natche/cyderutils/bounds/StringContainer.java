package com.github.natche.cyderutils.bounds;

import com.google.common.base.Preconditions;

/** A container for some {@link String} content. */
public abstract class StringContainer {
    /** The string contained by this container. */
    protected final String containedString;

    /**
     * Constructs a new string container.
     *
     * @param containedString the contained string
     * @throws NullPointerException if the provided string is null
     */
    protected StringContainer(String containedString) {
        Preconditions.checkNotNull(containedString);
        this.containedString = containedString;
    }

    /**
     * Returns the String contained by this container.
     *
     * @return the String contained by this container
     */
    public String getString() {
        return containedString;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return containedString.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof StringContainer)) {
            return false;
        }

        StringContainer other = (StringContainer) o;
        return containedString.equals(other.getString());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{containedString=\"" + containedString + "\"}";
    }
}
