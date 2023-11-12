package cyder.audio;

// todo maybe should have some encapsulated object for a general audio file
//  and then some enum for length extraction choices
public enum DetermineAudioLengthMethod {
    FFPROBE,
    PYTHON_MUTAGEN
}
