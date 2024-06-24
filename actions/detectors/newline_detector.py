from detectors.detector import Detector
from color.colors import *
from util.file_and_location import FileAndLocation
import os


class NewLineDetector(Detector):
    def detect(self):
        num_failures = 0
        ret = []
        for file in self._files:
            current_running_new_lines = 0
            in_newline_content = True
            past_starting_newlines = False
            last_anchor_line_number = 0

            lines = file.get_lines()
            for line_index, line in enumerate(lines):
                line_empty = self._is_line_empty(line)

                if line_empty and not past_starting_newlines:
                    # line is empty and not encountered starting file non whitespace content so continue
                    continue
                elif not line_empty and in_newline_content:
                    # we were counting newlines and now we have encountered a non-newline
                    if current_running_new_lines > 1:
                        ret.append(FileAndLocation(os.path.basename(file.get_path()), file.get_path(
                        ), last_anchor_line_number, line_index + 1, lines))
                        num_failures += 1

                    past_starting_newlines = True
                    in_newline_content = False
                    current_running_new_lines = 0
                elif line_empty:
                    # line is empty so increment or start counting
                    if current_running_new_lines == 0:
                        last_anchor_line_number = line_index
                    in_newline_content = True
                    current_running_new_lines += 1

        self._num_failures = num_failures
        return ret
