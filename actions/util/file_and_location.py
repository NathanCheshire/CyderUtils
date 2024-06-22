class FileAndLocation:
    """ 
    A record type for holding information about a file, its path, and a specific line range.
    """

    def __init__(self, file_name: str, file_path: str, start_line: int, end_line: int, lines: list) -> None:
        """
        Constructs a new FileAndLocation.

        :param file_name: the name of the file.
        :param file_path: the path to the file.
        :param start_line: the starting line number of the specified range (inclusive).
        :param end_line: the ending line number of the specified range (inclusive).
        :param lines: the list of lines within the specified range.
        """

        self._file_name = file_name
        self._file_path = file_path
        self._start_line = start_line
        self._end_line = end_line
        self._lines = lines

    def get_file_name(self) -> str:
        """ 
        Returns the name of the file.
        """
        return self._file_name

    def get_file_path(self) -> str:
        """ 
        Returns the path to the file.
        """
        return self._file_path

    def get_start_line(self) -> int:
        """ 
        Returns the starting line number of the specified range.
        """
        return self._start_line

    def get_end_line(self) -> int:
        """ 
        Returns the ending line number of the specified range.
        """
        return self._end_line

    def get_lines(self) -> list:
        """ 
        Returns the list of lines within the specified range.
        """
        return self._lines
