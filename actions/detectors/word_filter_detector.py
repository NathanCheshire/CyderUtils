import os
from detectors.detector import Detector
from util.file_and_location import FileAndLocation
from util.utils import read_lines


class WordFilterDetector(Detector):
    def detect(self):
        blocked_words_file = os.path.join(
            os.path.dirname(__file__), '../blocked.txt')
        blocked_words = read_lines(blocked_words_file)

        num_failures = 0
        ret = []
        for file in self._files:
            lines = file.get_lines()
            for line_index, line in enumerate(lines):
                line_empty = self._is_line_empty(line)

                if line_empty:
                    continue
                if os.path.abspath(blocked_words_file) == os.path.abspath(file.get_path()):
                    continue

                parts = line.strip().split()
                for part in parts:
                    if part.strip() in blocked_words:
                        ret.append(FileAndLocation(os.path.basename(file.get_path()), file.get_path(),
                                                   line_index + 1, line_index + 1, lines))

        self._num_failures = num_failures
        return ret
