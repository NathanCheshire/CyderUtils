from collections import defaultdict
from detectors.detector import Detector
from color.colors import *
from util.file_and_location import FileAndLocation
from util.utils import write_lines


class ThreeLineJavadocDetector(Detector):
    DOC_COMMENT_START = "/**"
    DOC_COMMENT_END = "*/"

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

            lines = file.get_lines()
            for line_index, line in enumerate(lines):
                if line.lstrip().startswith(self.DOC_COMMENT_START) and not in_comment:
                    in_comment = True
                    num_comment_lines += 1
                elif line.lstrip().startswith(self.DOC_COMMENT_END) and in_comment:
                    in_comment = False
                    num_comment_lines += 1

                    if num_comment_lines == 3:
                        if not self._fix_javadoc:
                            ret.append(FileAndLocation(
                                file.get_path(), file.get_path(), line_index, line_index, lines))
                        else:
                            ret.append((file, line_index))

                    num_comment_lines = 0
                elif in_comment:
                    num_comment_lines += 1

        # fixing Java docs should result in no failures
        if self._fix_javadoc:
            file_lines_map = defaultdict(list)
            for file, line_index in ret:
                file_lines_map[file].append(line_index)

            for file, line_indices in file_lines_map.items():
                lines = file.get_lines()

                line_indices.sort(reverse=True)

                for line_index in line_indices:
                    start_line = line_index - 2
                    end_line = line_index

                    content = lines[start_line + 1].strip().strip('*').strip()
                    leading_spaces = lines[start_line][:len(
                        lines[start_line]) - len(lines[start_line].lstrip())]

                    lines[start_line] = f"{leading_spaces}{self.DOC_COMMENT_START} {content} {self.DOC_COMMENT_END}\n"
                    del lines[start_line + 1:end_line + 1]

                write_lines(file.get_path(), lines)

            return []
        else:
            self._num_failures = len(ret)
            return ret
