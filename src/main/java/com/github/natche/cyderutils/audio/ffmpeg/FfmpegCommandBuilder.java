package com.github.natche.cyderutils.audio.ffmpeg;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder for FFmpeg commands.
 */
public final class FfmpegCommandBuilder {
    /**
     * The command parts for this command.
     */
    private final ArrayList<String> commandParts = new ArrayList<>();

    /**
     * Constructs a new FfmpegCommandBuilder with {@link FfmpegArgument#FFMPEG} as the initial argument.
     */
    public FfmpegCommandBuilder() {
        this(FfmpegArgument.FFMPEG);
    }

    /**
     * Constructs a new FfmpegCommandBuilder with the provided initial argument.
     *
     * @param startingArgument the starting argument such as {@link FfmpegArgument#FFMPEG}
     */
    public FfmpegCommandBuilder(FfmpegArgument startingArgument) {
        commandParts.add(startingArgument.getArgumentName());
    }

    /**
     * Constructs a new FfmpegCommandBuilder with the
     * initial command parts provided. {@link FfmpegArgument#FFMPEG} is added first if startingCommandParts
     * does not start with {@link FfmpegArgument#FFMPEG}.
     *
     * @param startingCommandParts the starting command parts
     */
    public FfmpegCommandBuilder(List<String> startingCommandParts) {
        Preconditions.checkNotNull(startingCommandParts);
        Preconditions.checkArgument(!startingCommandParts.isEmpty());

        if (!startingCommandParts.get(0).equals(FfmpegArgument.FFMPEG.getArgumentName())) {
            commandParts.add(FfmpegArgument.FFMPEG.getArgumentName());
        }

        commandParts.addAll(startingCommandParts);
    }

    /**
     * Adds the provided argument to the encapsulated list.
     *
     * @param argument the argument to add
     * @return the provided argument to the encapsulated list
     * @throws NullPointerException     if the provided argument is null
     * @throws IllegalArgumentException if the provided argument is empty
     */
    @CanIgnoreReturnValue
    public FfmpegCommandBuilder addArgument(String argument) {
        Preconditions.checkNotNull(argument);
        Preconditions.checkArgument(!argument.trim().isEmpty());

        commandParts.add(argument);
        return this;
    }

    /**
     * Adds the provided argument and value to the encapsulated list.
     *
     * @param argument the argument to add
     * @param value    the argument's value to add
     * @return the provided argument to the encapsulated list
     * @throws NullPointerException     if the provided argument or value is null
     * @throws IllegalArgumentException if the provided argument or value is empty
     */
    @CanIgnoreReturnValue
    public FfmpegCommandBuilder addArgument(String argument, String value) {
        Preconditions.checkNotNull(argument);
        Preconditions.checkNotNull(value);
        Preconditions.checkArgument(!argument.trim().isEmpty());
        Preconditions.checkArgument(!value.trim().isEmpty());

        commandParts.addAll(ImmutableList.of(argument, value));
        return this;
    }

    /**
     * Adds all the provided arguments to the encapsulated list.
     *
     * @param arguments the list of arguments
     * @return the arguments to add to the encapsulated list
     * @throws NullPointerException if the provided list is null
     * @throws IllegalArgumentException if the provided list is empty
     */
    public FfmpegCommandBuilder addAllArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        Preconditions.checkArgument(!arguments.isEmpty());

        commandParts.addAll(arguments);
        return this;
    }

    /**
     * Joins all the command parts and returns the constructed command.
     *
     * @return the built command
     */
    public String build() {
        return String.join(" ", commandParts);
    }

    /**
     * Returns the list of command parts.
     *
     * @return the list of command parts
     */
    public ImmutableList<String> list() {
        return ImmutableList.copyOf(commandParts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return commandParts.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "FfmpegCommandBuilder{"
                + "commandParts=" + commandParts
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof FfmpegCommandBuilder)) {
            return false;
        }

        FfmpegCommandBuilder other = (FfmpegCommandBuilder) o;
        return commandParts.equals(other.commandParts);
    }
}
