import os
import fnmatch


def is_valid_file(file_name: str, extensions: list[str]) -> bool:
    """
    Returns whether the provided file is of one of the provided extensions.
    """
    return any(fnmatch.fnmatch(file_name, f"*{ext}") for ext in extensions)


def find_files(starting_directory: str, extensions: list[str], recursive: bool = False) -> list[str]:
    """
    Finds all files within the provided directory that end in one of the provided extensions.

    :param starting_directory: the directory to start search from
    :param extensions: a list of valid extensions such as [".java"]
    :param recursive: whether to recurse through subdirectories
    :return: a list of discovered files
    """

    if not extensions:
        raise ValueError('Error: must provide valid extensions')

    if recursive:
        return [os.path.join(root, file)
                for root, _, files in os.walk(starting_directory)
                for file in files if is_valid_file(file, extensions)]
    else:
        return [os.path.join(starting_directory, file)
                for file in os.listdir(starting_directory)
                if os.path.isfile(os.path.join(starting_directory, file)) and is_valid_file(file, extensions)]


def strip_lines(lines: list[str]) -> list[str]:
    """
    Returns a new list containing the original lines having each been stripped.
    """
    return [line.strip() for line in lines]


def read_lines(file_path: str) -> list[str]:
    """
    Reads the lines from the provided file path and returns
      them after stripping leading and trailing whitespace.
    """
    return strip_lines(open(file_path, 'r').readlines())


def write_lines(file_path: str, lines: list[str]) -> None:
    """
    Writes the provided lines to the provided file.
    """
    with open(file_path, 'w', encoding='utf-8') as file:
        file.writelines(lines)