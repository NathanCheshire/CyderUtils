
from detectors.word_filter_detector import WordFilterDetector
from filescanner.file_scanner import FileScanner
from detectors.newline_detector import NewLineDetector
from color.colors import *


if __name__ == '__main__':
    java_and_kotlin_scanner = FileScanner(".")
    java_and_kotlin_files = java_and_kotlin_scanner.scan()
    NewLineDetector(java_and_kotlin_files).detect_and_print_violations()

    most_files_scanner = FileScanner(
        ".", ['.java', '.kt', '.txt', '.md', '.py'])
    most_files = most_files_scanner.scan()
    WordFilterDetector(most_files).detect_and_print_violations()
