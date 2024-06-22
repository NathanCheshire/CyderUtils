import math
import os

from preconditions.preconditions import Preconditions
from util.utils import read_lines


class ScannedFile():
    """
    A file which was picked up from a FileScanner.
    """

    __BYTE_SIZE = 1024

    def __init__(self, file_path: str):
        """
        Constructs a new ScannedFile from the provided file path.

        Raises:
            - ValueError: if the provided file_path is None, empty, or not a file
        """
        Preconditions.check_not_none(file_path)
        Preconditions.check_argument(len(file_path.strip()) > 0)
        Preconditions.check_argument(os.path.exists(file_path))
        Preconditions.check_argument(os.path.isfile(file_path))

        self._file_path = file_path

    def get_path(self) -> str:
        """
        Returns the path of this file.
        """
        return self._file_path

    def get_lines(self) -> list[str]:
        """
        Returns a list of the stripped lines contained by this file.
        """
        return read_lines(self._file_path)

    def get_size(self) -> str:
        """
        Returns a human readable size of this file, ex: 5.1 MB.
        """
        size_bytes = os.path.getsize(self._file_path)
        return self._convert_size(size_bytes)

    def _convert_size(self, size_bytes: int) -> str:
        if size_bytes == 0:
            return "0B"
        
        size_name = ("B", "KB", "MB", "GB", "TB")
        i = int(math.log(size_bytes, 1024))
        p = math.pow(1024, i)
        s = round(size_bytes / p, 2)
        
        return f"{s} {size_name[i]}"
