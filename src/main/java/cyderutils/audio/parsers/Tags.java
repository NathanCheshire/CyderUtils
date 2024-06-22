package cyderutils.audio.parsers;

/**
 * The tags object contained within a {@link Stream} object.
 */
public class Tags {
    /**
     * The encoder for this audio file.
     */
    private String encoder;

    /**
     * Constructs a new Tags object.
     */
    public Tags() {}

    /**
     * Returns the encoder for this audio file.
     *
     * @return the encoder for this audio file
     */
    public String getEncoder() {
        return encoder;
    }

    /**
     * Sets the encoder for this audio file.
     *
     * @param encoder the encoder for this audio file
     */
    public void setEncoder(String encoder) {
        this.encoder = encoder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Tags{"
                + "encoder=\"" + encoder + "\""
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return encoder.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Tags)) {
            return false;
        }

        Tags other = (Tags) o;
        return encoder.equals(other.encoder);
    }
}
