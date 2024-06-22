
from filescanner.file_scanner import FileScanner
from detectors.newline_detector import NewLineDetector
from color.colors import *

def print_running(program: str) -> None:
    print(f"\n{bold}{green}Running {program}{reset}\n")


if __name__ == '__main__':
    java_and_kotlin_scanner = FileScanner(".")
    java_and_kotlin_files = java_and_kotlin_scanner.scan()

    print_running("newline detector")
    NewLineDetector(java_and_kotlin_files).detect()
