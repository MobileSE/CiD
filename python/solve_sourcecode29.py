import os
import re


class API_Project:
    def __init__(self, class_name, rtn_type, method_name, parameters, api, pkg_level):
        self.class_name = class_name
        self.rtn_type = rtn_type
        self.method_name = method_name
        self.parameters = parameters
        self.para_num = len(parameters)
        self.api = api
        self.class_name_list = class_name.split('.')
        self.pkg_level = pkg_level
        self.len_pkg = len(self.class_name_list)


def save_lst(lst, filename):
    with open(filename, "w") as fw:
        for item in lst:
            fw.write(item)
            fw.write("\n")


def load_file_neat(filename):
    res = {}
    with open(filename, "r") as fr:
        lines = fr.readlines()
        for line in lines:
            s = line.strip()
            res[s] = 1
    return res


def solve_by_pkg(pkg_level, METHODS_or_FILEDS, sourcecode_30_discard, sourcecode_29, SAVE_NEW_29):

    # remove discard
    sourcecodediscard_pkg = load_file_neat(sourcecode_30_discard)

    res = load_file_neat(sourcecode_29)

    new = []

    for item in res:

        if METHODS_or_FILEDS == "M":
            match = re.search(r'<(\S+):\s(\S+)\s(\S+)\((.*)\)>', item)
        else:
            match = re.search(r'<(\S+):\s(\S+)\s(\S+)>', item)

        if match:
            class_name = match.group(1)
            class_name_len = len(class_name.split("."))
            if class_name_len >= pkg_level:
                pkg_name = ".".join(class_name.split(".")[:pkg_level])
            else:
                pkg_name = class_name
            if pkg_name not in sourcecodediscard_pkg:
                new.append(item)

    print(len(res), len(new))

    save_lst(new, SAVE_NEW_29)


def solve_by_Signature(sourcecode_30_discard, sourcecode_29, SAVE_NEW_29):

    # remove discard
    sourcecodediscard_API = load_file_neat(sourcecode_30_discard)

    res = load_file_neat(sourcecode_29)

    new = []

    for item in res:
        if item not in sourcecodediscard_API:
            new.append(item)

    print(len(res), len(new))

    save_lst(new, SAVE_NEW_29)


if __name__ == '__main__':

    pkg_level = 4

    METHODS_or_FILEDS = "M"  # or "F"   methods or fields
    PKG_or_API = "P"  # or "P"  filter by pkgs or API signatures

    # input file
    sourcecode_29 = "methods-official-29.txt"
    sourcecode_30_discard_pkg = "selected/sourcecode_pkg_discard.txt"
    sourcecode_30_discard_API = "selected/sourcecode_discard.txt"

    # output
    SAVE_NEW_29 = "methods-official-29-new.txt"

    if PKG_or_API == "A":
        solve_by_Signature(sourcecode_30_discard_API, sourcecode_29, SAVE_NEW_29)
    else:
        solve_by_pkg(pkg_level, METHODS_or_FILEDS, sourcecode_30_discard_pkg, sourcecode_29, SAVE_NEW_29)

