import sys
from detectors.detector import Dectector


class NewLineDetector(Dectector):
    def detect(self):
        max_len = max(len(file._file_path) for file in self._files)

        num_failures = 0
        for file in self._files:
            print(
                f"Scanning {file.get_path():<{max_len}} {file.get_size():>10}")

            current_running_new_lines = 0

            anchored = False
            originally_anchored = False
            last_anchor = None
            last_anchor_line_number = 0

            file_lines = file.get_lines()

            for line_index, line in enumerate(file_lines):
                line = line.strip()
                line_empty = len(line) == 0

                if line_empty and not originally_anchored:
                    continue
                elif not line_empty and not anchored:
                    if current_running_new_lines > 1:

                        self.print_stats(current_running_new_lines, last_anchor_line_number,
                                         last_anchor, line_index + 1, line)
                        num_failures += 1

                    originally_anchored = True
                    anchored = True
                    last_anchor = line
                    current_running_new_lines = 0
                elif line_empty:
                    if current_running_new_lines == 0:
                        last_anchor_line_number = line_index

                    anchored = False
                    current_running_new_lines = current_running_new_lines + 1

        if not num_failures:
            print(
                f"No newlines found in {len(self._files)} searched files")
        else:
            print(
                f"Found {num_failures} violation{'' if num_failures == 1 else 's'}")

        sys.exit(1 if num_failures else 0)

    def print_stats(self, num_unnecessary_new_lines: int, starting_line_num: int,
                    starting_line: str, ending_list_num: int, ending_line: str):
        """
        Prints the statistics found for the current file.
        """

        print("-------------------------------")
        print("Found", num_unnecessary_new_lines, "new lines between:")
        print(f"{starting_line_num}: {starting_line}")
        print("and")
        print(f"{ending_list_num}: {ending_line}")
        print("-------------------------------")
