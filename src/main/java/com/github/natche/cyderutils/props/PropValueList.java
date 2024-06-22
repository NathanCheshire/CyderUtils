package com.github.natche.cyderutils.props;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;

import java.util.List;

/**
 * A list type for the value of a prop.
 */
@Immutable
public final class PropValueList {
    /**
     * The list containing the values for a prop.
     */
    public final ImmutableList<String> propValues;

    /**
     * Constructs a new prop value list object.
     *
     * @param propValues the values list.
     */
    public PropValueList(List<String> propValues) {
        Preconditions.checkNotNull(propValues);

        this.propValues = ImmutableList.copyOf(propValues);
    }

    /**
     * Returns the list containing the values for a prop.
     *
     * @return the list containing the values for a prop
     */
    public ImmutableList<String> getPropValues() {
        return propValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof PropValueList)) {
            return false;
        }

        PropValueList other = (PropValueList) o;
        return other.propValues.equals(propValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return propValues.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PropValueList{propValues=" + propValues + "}";
    }
}
