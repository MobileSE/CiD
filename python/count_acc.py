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


def load_file(filename):
    lst = []
    all = []
    res = {}
    with open(filename, "r") as fr:
        lines = fr.readlines()
        for line in lines:
            s = line.split(":<")
            api_sig = s[0].strip()

            modifiers = re.split('[ :]', s[1].strip("<>"))

            res[api_sig] = list(set(modifiers))
            lst.append(api_sig)
            all.append(line.strip())
    return res, lst, all


def load_file_neat(filename):
    res = {}
    with open(filename, "r") as fr:
        lines = fr.readlines()
        for line in lines:
            s = line.strip()
            if "void <init>()>" in s:
                print(s)
                continue
            if ".Stub:" in s:
                print(s)
                continue
            if ".Proxy:" in s:
                print(s)
                continue
            if ".Default:" in s:
                print(s)
                continue
            if "void <clinit>()>" in s:
                print(s)
                continue
            res[s] = 1
    return res


def main(SourceCodeFile, SourceCodeHideFile, FrameworkFilteredFile, Filter_file):
    pkgs = load_file_neat(Filter_file)
    res1 = load_file_neat(SourceCodeFile)
    res1_1 = load_file_neat(SourceCodeHideFile)
    count111 = 0
    for item in res1_1:
        if item not in res1:
            res1[item] = 1
        else:
            count111 += 1

    res2 = load_file_neat(FrameworkFilteredFile)

    pkgs_1 = []
    pkgs_2 = []
    for i in range(0, pkg_level + 1):
        tmp1 = {}
        tmp2 = {}
        pkgs_1.append(tmp1)
        pkgs_2.append(tmp2)

    pro1 = {}
    pro2 = {}

    new_res1 = {}
    new_res2 = {}

    for api in res1:
        match = re.search(r'<(\S+):\s(\S+)\s(\S+)\((.*)\)>', api)
        if match:
            class_name = match.group(1)
            rtn_type = match.group(2)
            method_name = match.group(3)
            parameters = match.group(4)

            api_pro1 = API_Project(class_name, rtn_type, method_name, parameters, api, pkg_level)
            pro1[api] = api_pro1

            if api_pro1.len_pkg >= pkg_level:
                hash1 = ".".join(class_name.split(".")[:pkg_level])
            else:
                hash1 = class_name
            if hash1 not in pkgs:
                new_res1[api] = 1

    for api in res2:
        match = re.search(r'<(\S+):\s(\S+)\s(\S+)\((.*)\)>', api)
        if match:
            class_name = match.group(1)
            rtn_type = match.group(2)
            method_name = match.group(3)
            parameters = match.group(4)

            api_pro2 = API_Project(class_name, rtn_type, method_name, parameters, api, pkg_level)
            pro2[api] = api_pro2

            if api_pro2.len_pkg >= pkg_level:
                hash2 = ".".join(class_name.split(".")[:pkg_level])
            else:
                hash2 = class_name
            if hash2 not in pkgs:
                new_res2[api] = 1

    inter = set(new_res1.keys()).intersection(new_res2.keys())
    all = set(new_res1.keys()).union(new_res2.keys())

    print(len(inter) * 1.0 / len(all))

    print(new_res1.keys() - new_res2.keys())


if __name__ == '__main__':

    # run official-framework-30 and sourcecode-30, get two discard files

    level = 30
    pkg_level = 4
    print("pkg length: ", pkg_level)

    # input files
    SourceCodeFile = "sourcecode/framework-" + str(level) + "/methods.txt"
    SourceCodeHideFile = "sourcecode/framework-" + str(level) + "/methods.hide.txt"
    FrameworkFilteredFile = "official/framework-" + str(level) + "/methods-filtered.txt"
    Filter_file = "filter_pkg.txt"

    # select
    main(SourceCodeFile, SourceCodeHideFile, FrameworkFilteredFile, Filter_file)




