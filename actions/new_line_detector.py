

import sys
from stat_generator import find_files


def main():
    failed = False

    files = find_files(starting_dir=".", extensions=['.java'], recursive=True)

    for file in files:
        num_newlines = 0
        anchored = False
        last_anchor = None
        last_anchor_line_number = 0
        originally_anchored = False
        line_number = 0

        file_lines = open(file, 'r').readlines()

        for line in file_lines:
            empty = len(line.strip()) == 0

            line_number = line_number + 1

            if empty and not originally_anchored:
                continue
            elif not empty and not anchored:
                if num_newlines > 1:
                    print("File:", file)
                    print("Found", num_newlines, "new lines between:")
                    print(last_anchor_line_number, ":", last_anchor.strip())
                    print("and")
                    print(line_number, ":", line.strip())
                    print("-------------------------------")

                    failed = True

                originally_anchored = True
                anchored = True
                last_anchor = line
                num_newlines = 0
            elif empty:
                if num_newlines == 0:
                    last_anchor_line_number = line_number - 1

                anchored = False
                num_newlines = num_newlines + 1

    return 1 if failed else 0


if __name__ == '__main__':
    sys.exit(main())