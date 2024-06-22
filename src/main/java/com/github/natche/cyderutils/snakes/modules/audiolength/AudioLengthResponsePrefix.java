package com.github.natche.cyderutils.snakes.modules.audiolength;

import com.google.common.base.Preconditions;

import java.time.Duration;

/**
 * Possible responses from the audio length python script.
 */
public enum AudioLengthResponsePrefix {
    ERROR("Error: "),
    RESULT("Audio Length: ");

    private final String prefix;

    AudioLengthResponsePrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Parses a response from the audio length program and returns the duration.
     *
     * @param response the response output from the python script
     * @return the duration of the audio file
     * @throws NullPointerException     if the provided response is null
     * @throws IllegalArgumentException if the provided response is empty or
     *                                  does not start with any of the prefix values
     * @throws NumberFormatException    if the found matching prefix, after being stripped
     *                                  away, does not contain a parsable float
     */
    public static Duration parseResponse(String response) {
        Preconditions.checkNotNull(response);
        Preconditions.checkArgument(!response.trim().isEmpty());

        for (AudioLengthResponsePrefix prefix : values()) {
            if (response.startsWith(prefix.prefix)) {
                String audioLength = response.substring(prefix.prefix.length());
                float millis = Float.parseFloat(audioLength) * 1000f;
                return Duration.ofMillis(Math.round(millis));
            }
        }

        throw new IllegalArgumentException("Provided response does not start with any"
                + " AudioLengthResponsePrefixes, response: " + response);
    }
}
