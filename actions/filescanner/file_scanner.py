import os
from filescanner.scanned_file import ScannedFile
from preconditions.preconditions import Preconditions
from util.utils import find_files


class FileScanner():
    """
    A base class for a utility scanner which looks through a set of files
    """

    def __init__(self, starting_directory: str, extensions: list[str] = ['.java', '.kt'], recursive: bool = True):
        """
        Constructs a new FileScanner objectusing the provided parameters.

        Raises:
            - ValueError: if any argument is None, empty, or the provided
              starting directory does not exist or is not a directory
        """
        Preconditions.check_not_none(starting_directory)
        Preconditions.check_not_none(extensions)
        Preconditions.check_not_none(recursive)
        Preconditions.check_argument(starting_directory.strip())
        Preconditions.check_argument(len(extensions) > 0)
        Preconditions.check_argument(os.path.exists(starting_directory))
        Preconditions.check_argument(os.path.isdir(starting_directory))

        self._starting_directory = starting_directory
        self._recursive = recursive
        self._extensions = extensions

    def scan(self) -> list[ScannedFile]:
        files = find_files(self._starting_directory,
                           self._extensions, self._recursive)
        return [ScannedFile(file_path) for file_path in files]
