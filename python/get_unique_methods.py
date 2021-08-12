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
            res[s] = 1
    return res


def filter_methods(FrameworkOriginalFile, FrameworkFilteredFile):
    res, lst, all = load_file(FrameworkOriginalFile)
    new_lst = []
    new_lst_2 = []

    for i in range(len(lst)):
        item = lst[i]
        if "'" in item:
            continue
        match = re.search(r'<(\S+):\s(\S+)\s(\S+)\((.*)\)>', item)
        if match:
            class_name = match.group(1)
            rtn_type = match.group(2)
            method_name = match.group(3)
            parameters = match.group(4)

            match1 = re.search(r'\S+\.\d+$', class_name)
            if match1:
                # print(all[i])
                continue

            hash = ".".join(class_name.split(".")[:3])
            if hash == "com.android.framework":
                # print(all[i])
                continue

            match2 = re.search(r'\S+\.\d+$', method_name)
            if match2:
                # print(all[i])
                continue

            if "." in method_name:  # 可以删
                # print(lst[i])
                continue

            # if ".stub." in class_name:
            #     # print(lst[i])
            #     continue

            match4 = re.search(r'\.V\d+_\d+\.', class_name)
            if match4:
                # print(all[i])
                continue

            flag1 = 0
            for paramter in parameters:
                match3 = re.search(r'\S+\.\d+$', paramter.strip())
                if match3:
                    flag1 = 1
                    break
            if flag1 == 1:
                continue

        modifiers = res[item]
        flag = 0
        for m in modifiers:
            if m != "":
                flag = 1
            if m.strip() == "private" or m.strip() == "default":
                flag = 0
                break

        if flag == 0:
            # print(all[i])
            continue

        new_lst.append(all[i])
        new_lst_2.append(lst[i])

    save_lst(new_lst_2, FrameworkFilteredFile)
    print("framework original: ", len(all), " filtered: ", len(new_lst))


def get_unique_methods(framework_30_discard, MiuiCodeFilterFile, FrameworkFilteredFile,
                       SAVE_intersection, SAVE_source_selected, SAVE_framework_selected,
                       SAVE_SOURCECODE_discard, SAVE_FRAMEWORK_discard,
                       SAVE_SOUCECODE_PKG_discard, SAVE_FRAMEWORK_PKG_discard):

    res1 = load_file_neat(MiuiCodeFilterFile)
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
                if hash1 in pkgs_1[pkg_level]:
                    pkgs_1[pkg_level][hash1] += 1
                else:
                    pkgs_1[pkg_level][hash1] = 1
            else:
                hash1 = class_name
                if hash1 in pkgs_1[api_pro1.len_pkg]:
                    pkgs_1[api_pro1.len_pkg][hash1] += 1
                else:
                    pkgs_1[api_pro1.len_pkg][hash1] = 1


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
                if hash2 in pkgs_2[pkg_level]:
                    pkgs_2[pkg_level][hash2] += 1
                else:
                    pkgs_2[pkg_level][hash2] = 1
            else:
                hash2 = class_name
                if hash2 in pkgs_2[api_pro2.len_pkg]:
                    pkgs_2[api_pro2.len_pkg][hash2] += 1
                else:
                    pkgs_2[api_pro2.len_pkg][hash2] = 1

    OnlyinSC = []  # set * pkg_level
    OnlyinFrame = []
    deleted_pkg1 = []  # string list
    deleted_pkg2 = []
    for i in range(0, pkg_level + 1):
        OnlyinSC.append(pkgs_1[i].keys() - pkgs_2[i].keys())
        OnlyinFrame.append(pkgs_2[i].keys() - pkgs_1[i].keys())
        deleted_pkg1.extend(list(OnlyinSC[i]))
        deleted_pkg2.extend(list(OnlyinFrame[i]))

    new_res1 = []
    for api in pro1:
        if pro1[api].len_pkg < pkg_level:
            class_name = pro1[api].class_name
            length: int = pro1[api].len_pkg
            if class_name not in OnlyinSC[length]:
                new_res1.append(api)
        else:
            class_name = pro1[api].class_name
            short_name = ".".join(class_name.split(".")[:pkg_level])
            if short_name not in OnlyinSC[pkg_level]:
                new_res1.append(api)

    new_res2 = []
    for api in pro2:
        if pro2[api].len_pkg < pkg_level:
            class_name = pro2[api].class_name
            length: int = pro2[api].len_pkg
            if class_name not in OnlyinFrame[length]:
                new_res2.append(api)
        else:
            class_name = pro2[api].class_name
            short_name = ".".join(class_name.split(".")[:pkg_level])
            if short_name not in OnlyinFrame[pkg_level]:
                new_res2.append(api)

    api_inter = set(new_res1).intersection(set(new_res2))
    print("selected APIs: ", len(new_res1), len(new_res2), "intersection: ", len(api_inter))

    save_lst(list(api_inter), SAVE_intersection)
    save_lst(new_res1, SAVE_source_selected)
    save_lst(new_res2, SAVE_framework_selected)

    discard1 = []
    discard2 = pro2.keys() - set(new_res2)

    # remove discard
    framworkdiscard_pkg = load_file_neat(framework_30_discard)

    for item in pro1.keys() - set(new_res1):
        if pro1[item].len_pkg >= pkg_level:
            tmp_short_name = pro1[item].class_name[:pkg_level]
        else:
            tmp_short_name = pro1[item].class_name
        if tmp_short_name not in framworkdiscard_pkg and item not in discard1:
            discard1.append(item)

    print("discarded APIs: ", len(discard1), len(discard2))
    save_lst(discard1, SAVE_SOURCECODE_discard)
    save_lst(discard2, SAVE_FRAMEWORK_discard)

    new_deleted_pkg1 = []
    for item in deleted_pkg1:
        if item not in framworkdiscard_pkg:
            new_deleted_pkg1.append(item)

    save_lst(new_deleted_pkg1, SAVE_SOUCECODE_PKG_discard)
    save_lst(deleted_pkg2, SAVE_FRAMEWORK_PKG_discard)
    print("discarded pkgs: ", len(deleted_pkg1), len(deleted_pkg2))


if __name__ == '__main__':

    # run miui and official-framework-30

    level = 30
    pkg_level = 4

    # input field discard
    framework_30_discard = "selected/framework_pkg_discard.txt"
    print("pkg length: ", pkg_level)

    # input files
    MiuiFramework = "methods-miui-30.txt"
    MiuiFrameworkFiltered = "methods-miui-30-filtered.txt"

    OfficialFrameworkFile = "official/framework-" + str(level) + "/methods.txt"
    OfficialFrameworkFilteredFile = "official/framework-" + str(level) + "/methods-filtered.txt"

    # output files
    OUTPUT_ROOT = "selected"
    if not os.path.exists(OUTPUT_ROOT):
        os.mkdir(OUTPUT_ROOT)
    SAVE_intersection = OUTPUT_ROOT + "/intersection_methods_" + str(level) + ".txt"
    SAVE_source_selected = OUTPUT_ROOT + "/Miui_selected.txt"
    SAVE_framework_selected = OUTPUT_ROOT + "/Officialframework_selected.txt"
    SAVE_SOURCECODE_discard = OUTPUT_ROOT + "/Miui_discard.txt"
    SAVE_FRAMEWORK_discard = OUTPUT_ROOT + "/Officialframework_discard.txt"
    SAVE_SOUCECODE_PKG_discard = OUTPUT_ROOT + "/Miui_pkg_discard.txt"
    SAVE_FRAMEWORK_PKG_discard = OUTPUT_ROOT + "/Officialframework_pkg_discard.txt"

    # Framework filter first
    filter_methods(MiuiFramework, MiuiFrameworkFiltered)
    filter_methods(OfficialFrameworkFile, OfficialFrameworkFilteredFile)

    # select
    get_unique_methods(framework_30_discard, MiuiFrameworkFiltered, OfficialFrameworkFilteredFile,
                   SAVE_intersection, SAVE_source_selected, SAVE_framework_selected,
                   SAVE_SOURCECODE_discard, SAVE_FRAMEWORK_discard,
                   SAVE_SOUCECODE_PKG_discard, SAVE_FRAMEWORK_PKG_discard)




