
from filescanner.scanned_file import ScannedFile
from preconditions.preconditions import Preconditions


class Dectector():
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

    def scan(self):
        """
        Performs the scan on the encapsulated files.
        """
        pass
