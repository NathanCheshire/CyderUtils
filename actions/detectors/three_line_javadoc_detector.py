from detectors.detector import Detector
from color.colors import *
from util.file_and_location import FileAndLocation
from util.utils import read_lines


class ThreeLineJavadocDetector(Detector):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._fix_javadoc = False

    def set_should_fix_javadoc(self, should_fix: bool) -> "ThreeLineJavadocDetector":
        """
        Sets whether this detector should correct any three-line Javadoc or KDoc comments found.

        :param should_fix: whether this detector should fix

        :return: this instance
        """
        self._fix_javadoc = should_fix
        return self

    def detect(self):
        self._num_failures = 0

        if self._fix_javadoc:
            print(f"{bold}{red}Fix Javadoc enabled{reset}")

        ret = []
        for file in self._files:
            in_comment = False
            num_comment_lines = 0
            last_line = ""

            lines = file.get_lines()
            for line_index, line in enumerate(lines):
                if line == "/**" and not in_comment:
                    in_comment = True
                    num_comment_lines += 1
                elif line == "*/" and in_comment:
                    in_comment = False
                    num_comment_lines += 1

                    if num_comment_lines == 3:
                        ret.append(FileAndLocation(
                            file.get_path(), file.get_path(), line_index, line_index, lines))
                        new_line = last_line[1:].strip()
                        # TODO: write new line in place of the old three

                    num_comment_lines = 0
                elif in_comment:
                    num_comment_lines += 1

                last_line = line

        self._num_failures = len(ret)
        return ret
