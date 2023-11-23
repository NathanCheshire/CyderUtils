import argparse
from mutagen import File

def get_audio_length(file_path):
    audio = File(file_path)
    return audio.info.length

def main():
    parser = argparse.ArgumentParser(description='Get the length of an audio file.')
    parser.add_argument('-i', '--input', type=str, required=True, help='Absolute path to the audio file')

    args = parser.parse_args()

    try:
        length = get_audio_length(args.input)
        print(f"Audio Length: {length}")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()
