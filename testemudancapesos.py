#! /bin/python
import os
from random import *
import sys
import time

def hamming(n1,n2):
    x = n1 ^ n2
    setBits = 0
    while(x > 0):
        setBits += x & 1
        x >>= 1
    return setBits

# @with_goto
if __name__ == '__main__':
    sim = 1000
    if sys.argv[2] == '1':

        for i in range(sim):
            try:
                lines = open(sys.argv[1]).readlines()
                if sys.argv[3] == '2':
                    linees = [lines.index(x) for x in lines if len(x.split())>3]
                    # print(linees)
                    eq = randint(2,len(linees)-1)
                    # print("Eq" +str(eq))
                    # print(linees[eq])
                    line = lines[linees[eq]].split()
                else:
                    # print(lines)
                    eq = randint(2,int(lines[0])+1)
                    line = lines[eq].split()
                oldline = lines[eq]
                # print(line)
                tam = len(line)-1
                # print(tam)
                if sys.argv[3]=='2':
                    pospeso1 = randrange(1,tam,2)
                    pospeso2 = randrange(1,tam,2)
                    if pospeso1 != pospeso2:
                        # print("pPeso1 "+str(pospeso1))
                        # print("pPeso2 "+str(pospeso2))
                        peso1 = int(line[pospeso1])
                        peso2 = int(line[pospeso2])
                    else:
                        raise Exception("mismo pos")
                    if(peso1>0):
                        newpeso1 = randint(peso1*(-1),peso1)
                        # print("newp "+str(newpeso1))
                    else:
                        newpeso1 = randint(peso1,peso1*(-1))
                        # print("newp1 "+str(newpeso1))
                    if(peso2>0):
                        newpeso2 = randint(peso2*(-1),peso2)
                        # print("newp "+str(newpeso2))
                    else:
                        newpeso2= randint(peso2,peso2*(-1))
                        # print("newp1 "+str(newpeso2))
                    if newpeso1 == peso1 or newpeso2 == peso2:
                        raise Exception("No mudou")
                    else:
                        line[pospeso1] = str(newpeso1)
                        line[pospeso2] = str(newpeso2)
                elif sys.argv[3] == '1':
                    pospeso = randrange(1,tam,2)
                    # print("Peso "+str(pospeso))
                    peso = int(line[pospeso])
                    newpeso = 0
                    if(peso>0):
                        newpeso = randint(peso*(-1),peso)
                        # print("newp "+str(newpeso))
                    else:
                        newpeso = randint(peso,peso*(-1))
                        # print("newp1 "+str(newpeso))
                    # print(newpeso)
                    line[pospeso] = str(newpeso)
                else:
                    # print("Aqui")
                    pospeso = randrange(1,tam,2)
                    posvar = randrange(0,tam,2)
                    # print(pospeso)
                    # print(posvar)
                    peso = int(line[pospeso])
                    newpeso = 0
                    if(peso>0):
                        newpeso = randint(peso*(-1),peso)
                        # print("newp "+str(newpeso))
                    else:
                        newpeso = randint(peso,peso*(-1))
                        # print("newp1 "+str(newpeso))
                    # print(newpeso)
                    line[pospeso] = str(newpeso)

                    newvar = randint(0,int(line[0])-1)
                    # print(newvar)
                    line[posvar] = str(newvar)
                    pass
                newline = " ".join(line)+'\n'
                if oldline == newline:
                    # print("No")
                    pass
                else:
                    # print(line)
                    if sys.argv[3] == '2':
                        lines[linees[eq]] = newline
                    else:
                        lines[eq] = newline
                    # print(lines)
                    save = open("pesosTabela"+str(i)+".txt","w")
                    save.writelines(lines)
            except Exception as ex:
                print(ex)
                pass
        print("Executing simulation...")
        for i in range(sim):
            try:
                string = "./multicore-cpu-tlf pesosTabela"+str(i)+".txt  "+str(2**21)+" > saida"+str(i)+".txt"
                print(string)
                os.system(string)
            except Exception as ex:
                string = "rm saida"+str(i)+".txt"
                print(string)
                os.system(string)
    elif sys.argv[2] == '2':
        for i in range(sim):
            try:
                print("Abrendo saida"+str(i)+".txt")
                if os.path.exists("saida"+str(i)+".txt"):
                    lines = open("saida"+str(i)+".txt").readlines()
                    # lines = open("saida.txt").readlines()
                    posdel =[]
                    for u in range(len(lines[1:])):
                        # print(lines[u+1])
                        elem = lines[u+1].split()[1]
                        rep = [x for x in list(map(lambda x,y : y if x in y.split() else None, [elem]*len(lines[u+2:]),lines[u+2:])) if x is not None]
                        # print(rep)
                        if len(rep) > 0:
                            print("Modificando um erro...")
                            line = lines[u+1].split()
                            sumvalor = 0
                            for r in rep:
                                sumvalor += int(r.split()[-1].strip())
                                posdel.append(lines.index(r))
                            line[-1] = str(int(line[-1])+sumvalor)
                            newline = " ".join(line)+'\n'
                            lines[u+1] = newline
                    posdel =list(set(posdel))
                    # print(list(posdel))
                    for e in posdel[::-1]:
                        lines.pop(e)
                    save = open("saida"+str(i)+".txt","w")
                    # save = open("saida.txt","w")
                    save.writelines(lines)
            except Exception as identifier:
                print(identifier)
                pass
    elif sys.argv[2] == '3':
        count=0
        total=0
        rep = []
        lines1 = open("saida.txt").readlines()[1:]
        for j in range(sim):
            print("Abrendo Arquivo original com o arquivo "+str(j)+"\n")
            try:
                lines2 = open("saida"+str(j)+".txt").readlines()[1:]
                # lines1 = open("saida.txt").readlines()[1:]
                # lines2 = open("saida.txt").readlines()[1:]
                for u in lines1:
                    attcomp = " ".join(u.split()[:-1])
                    for j in lines2:
                        line_tmp = " ".join(j.split()[:-1])
                        if attcomp in line_tmp:
                            rep.append(u)
            except Exception as identifier:
                pass
        t=0
        for i in range(sim):
            try:
                lines1 = open("saida"+str(i)+".txt").readlines()[1:]
                # lines1 = open("saida.txt").readlines()[1:]
                total+=len(lines1)
            except Exception as ex:
                pass
        #print("De nuevo "+str(t))
        print("Iguais: "+str(len(rep)))
        print("Total: "+str(total))
    else:
        hammingminig2 = 0
        hammingmax2 =0
        for i in range(sim):
            try:
                lines = open("saida"+str(i)+".txt").readlines()[1:]
                #lines = open("saida0.txt").readlines()[1:]
                # print("-------------------------------------------------------")
                lista= []
                cantlines = len(lines)
                for j in range(cantlines-1):
                    tmpl =[]
                    for m in lines[j].split()[1:-1]:

                        for k in range(j+1,cantlines):
                            l2 = lines[k].split()[1:-1]
                            #print("as "+str(l2))

                            # tmpl += (list(map(lambda x,y : int(x)^int(y), [m]*len(l2),l2)))
                            tmpl += (list(map(lambda x,y : hamming(int(x),int(y)), [m]*len(l2),l2)))
                            #print(tmpl)
                    lista.append(min(tmpl))
                #print(lista)
                hammingminig2 +=(len([x for x in lista if x <= 2]))
                hammingmax2 +=(len([x for x in lista if x > 2]))
            except Exception as ex:
                pass
        print("Hamming menor igual a 2: "+str(hammingminig2))
        print("Hamming maior a 2: "+str(hammingmax2))
