
from filescanner.file_scanner import FileScanner
from detectors.newline_detector import NewLineDetector

if __name__ == '__main__':
    java_and_kotlin_scanner = FileScanner(".")
    java_and_kotlin_files = java_and_kotlin_scanner.scan()

    NewLineDetector(java_and_kotlin_files).detect()
