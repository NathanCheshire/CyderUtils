import sys
from detectors.detector import Detector
from color.colors import *


class NewLineDetector(Detector):
    def detect(self):
        max_len = max(len(file._file_path) for file in self._files)

        num_failures = 0
        for file in self._files:
            print(
                f"{file.get_path():<{max_len}} {file.get_size():>10}")

            current_running_new_lines = 0
            anchored = False
            originally_anchored = False
            last_anchor = None
            last_anchor_line_number = 0

            for line_index, line in enumerate(file.get_lines()):
                line_empty = self._is_line_empty(line)

                if line_empty and not originally_anchored:
                    # line is empty and not encountered starting file non whitespace content so continue
                    continue
                elif not line_empty and not anchored:
                    # we were counting newlines and now we have encountered a non-newline
                    if current_running_new_lines > 1:
                        self.print_stats(current_running_new_lines, last_anchor_line_number,
                                         last_anchor, line_index + 1, line)
                        num_failures += 1

                    originally_anchored = True
                    anchored = True
                    last_anchor = line
                    current_running_new_lines = 0
                elif line_empty:
                    # line is empty so increment or start counting
                    if current_running_new_lines == 0:
                        last_anchor_line_number = line_index
                    anchored = False
                    current_running_new_lines += 1

        if not num_failures:
            print(
                f"\n{green}{bold}No newlines found in {len(self._files)} searched files{reset}\n")
        else:
            print(
                f"\n{red}{bold}Found {num_failures} violation{'' if num_failures == 1 else 's'}{reset}\n")

        sys.exit(1 if num_failures else 0)

    def _is_line_empty(self, line: str) -> bool:
        """
        Returns whether this line is empty after stripping.
        """
        return len(line.strip()) == 0

    def print_stats(self, num_unnecessary_new_lines: int, starting_line_num: int,
                    starting_line: str, ending_list_num: int, ending_line: str):
        """
        Prints the statistics found for the current file.
        """

        print(f"{blue}{bold}{sep}")
        print(
            f"Found {red}{num_unnecessary_new_lines}{blue} new lines between\n")
        print(f"{red}{starting_line_num}{reset} {bold}: {blue}{starting_line}")
        print(f"{red}{ending_list_num}{reset} {bold}: {blue}{ending_line}")
        print(f"{sep}{reset}")
