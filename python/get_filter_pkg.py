import os
import re

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

def getFileList_in(rootDir, pickstr):
    """
    :param rootDir:  root directory of dataset
    :return: A filepath list of sample
    """
    filePath = []
    for parent, dirnames, filenames in os.walk(rootDir):
        for filename in filenames:
            if pickstr in filename:
                file = os.path.join(parent, filename)
                filePath.append(file)
    return filePath

if __name__ == '__main__':

    DIR1 = "selected_field"
    DIR2 = "selected_method"

    ss = "_pkg_discard_"

    files1 = getFileList_in(DIR1, ss)
    files2 = getFileList_in(DIR2, ss)
    files = list(set(files1).union((set(files2))))
    all = set()

    for file in files:
        res = load_file_neat(file)
        all = all.union(res.keys())

    save_lst(all, "filter_pkg.txt")
    print(len(all))



