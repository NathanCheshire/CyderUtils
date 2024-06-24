
from filescanner.scanned_file import ScannedFile
from preconditions.preconditions import Preconditions
from util.file_and_location import FileAndLocation
from color.colors import *
import sys


class Detector():
    """
    A class for detecting specifics within scanned files.
    """

    def __init__(self, files: list[ScannedFile], exit_on_error: bool = True):
        """
        Constructs a new instance of a detector

        Raises:
            - ValueError: if files is None or empty
        """
        Preconditions.check_not_none(files)
        Preconditions.check_argument(len(files) > 0)

        self._files = files
        self._exit_on_error = exit_on_error
        self._num_failures = 0

    def set_num_failures(self, num_failures: int) -> None:
        self._num_failures = num_failures

    def _is_line_empty(self, line: str) -> bool:
        """
        Returns whether this line is empty after stripping.
        """
        return len(line.strip()) == 0

    def detect(self) -> list[FileAndLocation]:
        """
        Performs the scan on the encapsulated files and returns a list
          of FilesAndLocation objects for locations which did not pass the detection.
        """
        pass

    def detect_and_print_violations(self) -> None:
        """
        Runs the detections and prints violations, exiting on failure if set.
        """

        print(f"{bold}{green}Running detector: {self.__class__.__name__}{reset}")

        files_and_locations = self.detect()

        for file_and_location in files_and_locations:
            file_and_location.print_self()

        if self._num_failures:
            print(f"{bold}{green}Found {self._num_failures} violations {reset}")
            if self._exit_on_error:
                sys.exit(1)
        else:
            print(
                f"{bold}{green}No problems found by: {self.__class__.__name__}{reset}")
