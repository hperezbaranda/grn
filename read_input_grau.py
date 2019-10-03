#! /usr/bin/env python

import sys
if __name__ == "__main__":
    file = sys.argv[1]
    lines = open(file).readlines()
    l = [0]*15
    for i in lines[2:]:
        split = i.split(" ")
        grau= 0 if split[1] == '0' and len(split) == 3 else int((len(split)-1)/2)
        l[grau]+=1
    print(l)           