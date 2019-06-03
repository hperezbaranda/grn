#! /bin/python
import sys
import re
import os
from time import sleep
import numpy as np
from py2cytoscape.data.cyrest_client import CyRestClient
from py2cytoscape.data.util_network import NetworkUtil as util
from py2cytoscape.data.style import StyleUtil as s_util
import pandas as pd
import argparse


def tabelaVerdade(func=None):
	txt=''
	l = [i.split('v')[1] for i in func.split() if 'v' in i ]
	lst2=[]
	[lst2.append(key) for key in l if key not in lst2]
	# print(lst2)
	cant = len(lst2)
	tabela = [[None]*(cant+1) for i in range(2**cant)]

	for i in range(2**cant):

		var = (bin(i).split('b')[1]).zfill(cant)
		txt = ''
		point = 0

		for z in func.split():

			if 'not' == z:
				txt += ' '+z
				#pass
			elif 'v' in z:
				num = z.split('v')[1]
				#print 'var['+i.split('v')[1]+']'
				txt +=' '+var[int(lst2.index(num))]
				point+=1
			else:
				txt += ' '+z

		for j in range(len(var)):
			#print i
			#print j
			tabela[i][j] = int(var[j])
		tabela[i][-1]=int(1 if eval(txt) else 0 )
#		print(txt)
		#print (var+'---->'+str(1 if eval(txt) else 0 ))
	#print func.replace('not', '')
#	print tabela
	#for i in tabela:
	#	print i
	#print func
	#print('\n')
	return tabela,lst2



def TLF(table):
	tlf=[]
	for i in range(len(table[1])-1):
		weights = 0
		for line in table:
			if line[i] == 1 and line[-1] == 1:
				weights+=1
			elif line[i] == 0 and line[-1] == 1:
				weights -=1
		tlf.append(weights * 2)
	mini = min([sum([line[i]*tlf[i] for i in range(len(table[1])-1)]) for line in table if line[-1]])
	tlf.append(mini)

	# for line in table:
	# 	suma=0
	# 	for i in range(len(table[0])-1):
	# 		suma = line[i]+tlf[i]
	# 	print(suma)

	return tlf

def TesteTlf(tlf):
	cant1 = len(tlf)-1
	table1= [[None]*(cant1+1) for i in range(2**cant1)]
	for i in range(2**cant1):
		var1 = (bin(i).split('b')[1]).zfill(cant1)
		suma =0
		for j in range(len(var1)):
			table1[i][j] = int(var1[j])
			suma += tlf[j]*int(var1[j])
		table1[i][-1] = 1 if suma >= tlf[-1] else 0
	return table1

if __name__ == '__main__':
	parser = argparse.ArgumentParser()
	parser.add_argument("input", help="Input file equations")
	parser.add_argument("ops", help="Number options 0= first time, 1= otherelse")
	args = parser.parse_args()

	pattern =re.compile(r'(?<=v)\d+')

	if int(sys.argv[2]) == 0:
		# Create Client
		cy = CyRestClient()
		# Clear current session
		cy.session.delete()

	# try:
	print(int(sys.argv[2]))
	if int(sys.argv[2]) == 3:
		print("Abrendo saida.txt")
		if os.path.exists("grn_gpu/saida.txt"):
			lines = open("grn_gpu/saida.txt").readlines()
			if len(lines) == 0:
				command = "saida.txt"
				print(command)
				os.remove("saida145.txt")
			posdel =[]
			for u in range(len(lines[1:])):
				elem = lines[u+1].split()[1]
				rep = [x for x in list(map(lambda x,y : y if x in y.split() else None, [elem]*len(lines[u+2:]),lines[u+2:])) if x is not None]
				if len(rep) > 0:
					print("Modificando um erro...")
					line = lines[u+1].split()
					if line[0] == 0:
						line[0] = str(len(line)-2)
					sumvalor = 0
					for r in rep:
						sumvalor += int(r.split()[-1].strip())
						posdel.append(lines.index(r))
					line[-1] = str(int(line[-1])+sumvalor)
					newline = " ".join(line)+'\n'
					lines[u+1] = newline
			posdel =list(set(posdel))
			for e in posdel[::-1]:
				lines.pop(e)
			save = open("grn_gpu/saida.txt","w")
			save.writelines(lines)
	else:
		line = [ i for i in open(sys.argv[1],'r').readlines() if (len(i.strip().split())>3 or (len(i.strip().split())==3 and not i.strip().split()[2].isdigit()))]

		line.sort()
		external = [(i.strip().split()[0],0) for i in open(sys.argv[1],'r').readlines() if (len(i.strip().split())==1)]
		external += [(i.strip().split()[0],int(i.strip().split()[2])) for i in open(sys.argv[1],'r').readlines() if (len(i.strip().split())==3 and i.strip().split()[2].isdigit())]

		print(external)
		node_ext = [i[0] for i in external]

		list_ext = [i.strip().split()[0] for i in line ]
		print(list_ext)

		print("Len: "+str(len(list_ext))+"\n")
		print(node_ext)
		print("Len: "+str(len(node_ext))+"\n")
		#creating graph with all numbers of nodes
		tam=len(list_ext)+len(node_ext)
		# graph = np.empty((tam,3),dtype=str)

		graph = []
		orig=[]
		modif = []
		lst_tlf=[]
		num_ext_id=[]
		cont =0
		for i in line:

			equal_pass = False
			new_value = False
			loc_x = None
			loc_y = None
			count=0
			txt = ''
			for j in i.split():

				if j in list_ext:

					txt += ' v'+str(list_ext.index(j))
					if(equal_pass):
						loc_x = list_ext.index(j)
						new_value = True


					else:

						loc_y = list_ext.index(j)
						new_value = True
						# print(loc_y)
						# print(list_ext)
						# print("Len: "+str(len(list_ext)))
					count+=1
				else:
					#Agregue esto
					#extra = pattern.findall(j)

					#if len(extra)>0:

					if(j == "="):

						equal_pass =True
					if j in node_ext:

						list_ext.append(j)
						txt += ' v'+str(list_ext.index(j))
						num_ext_id.append((j,str(list_ext.index(j))))
						loc_x = list_ext.index(j)
						new_value = True
						#pass
						#txt += ' const'
					else:
						txt += ' '+j
						new_value = False

				if(loc_x != None and loc_y != None and new_value ):
					graph.append([list_ext[loc_x],list_ext[loc_y],list_ext[loc_x]+" (interacts with) "+list_ext[loc_y]])


			if count > 0:
				print(txt)
				table,l = tabelaVerdade(txt.split('=')[1])
				if(txt.split('=')[0] == 'v77'):
					print("sdsa")
					[print(i) for i in table]
				tlf=TLF(table)
				# print("TLF: "+str(tlf))
				modif.append(l)
				lst_tlf.append(tlf)
				orig.append(txt)
			else:
				orig.append(txt.split(' =')[0]+' = const')

		if int(sys.argv[2]) == 0:
			data = pd.DataFrame(graph, columns=['source','target','interaction'])
			net_name=os.path.basename(sys.argv[1]).split(".")[0]
			print(sys.argv[1])
			net1 = cy.network.create_from_dataframe(data, collection="TLF",name =net_name )
			cy.layout.apply(network=net1)
			cy.layout.fit(network=net1)
			my_style = cy.style.create('default')
			cy.style.apply(style=my_style, network=net1)
			sleep(2)
			my_style.update_defaults({'EDGE_TARGET_ARROW_SHAPE':'ARROW_SHORT'})
			cy.style.apply(style=my_style, network=net1)
			eq=[]
			bool = []
			for i in line:
				tmp_monted = []
				tmp_monted.append(i.strip().split(" = ")[0])
				tmp_monted.append(i.strip().split(" = ")[1])
				l_pesos = lst_tlf[line.index(i)]
				l_pos =modif[line.index(i)]
				l_name = list(map(lambda x: list_ext[int(x)],l_pos))
				string = " + ".join(list(map(lambda x,y: str("( "+str(y)+' * '+str(x)+" )"),l_pesos[:-1],l_name)))
				string +=" >= "+str(l_pesos[-1])
				tmp_monted.append(string)
				# print(tmp_monted)
				eq.append(tmp_monted)
				# eq.append(i.strip().split(" = "))
				bool.append(i.strip().split(" = ")[0])
			update_eq = pd.DataFrame(eq,columns=['id','equation','eq. TLF'])
			# update_eq = pd.DataFrame(eq,columns=['id','equation'])
			# sleep(3)
			net1.update_node_table(update_eq,data_key_col="id")


			tmp_data=list(map(lambda x: [x,False,False],bool))+list(map(lambda x: [x,True,False],node_ext))
			update_eq = pd.DataFrame(tmp_data,columns=['id','fixed','observe'])
			net1.update_node_table(update_eq,data_key_col="id")

		print(orig)
		print('\n')
		print("Nodes: "+str(list_ext))
		print(str(len(list_ext))+'\n')
		print('\n')
		#for i in orig:
		#	print i.replace('or','|')
		#print (orig)
		print('\n')
		orig = [i.replace('or', '|') for i in orig]
		orig = [i.replace('and', '&') for i in orig]
		orig = [i.replace('not', '!') for i in orig]

		const =  [j for i in orig if pattern.findall(i) !=None for j in pattern.findall(i)]

		orig = [pattern.sub(lambda x : '['+x.group(0)+']',j) for j in orig]
		# for i in const:
		# # 	orig = [j.replace("v"+i+" ","v["+i+"]") if int(i)< len(list_ext)-1 else j.replace("v"+i,"const")  for j in orig]
		# 	orig = [pattern.sub(lambda x : '['+x.group(0)+']',j) if int(i)< len(list_ext)-1 else pattern.sub('const',j)  for j in orig]
		for i in orig:
			print(i)


		print('\n')
		print("Vetores: "+str(modif))
		print("tamanho: "+str(len(modif)))
		print('\n')
		print("TLF: "+str(lst_tlf))
	#	print("\n")
		print("tamanho: "+str(len(lst_tlf)))
		print("\n")

		# cont = 0
		# txt="pair<int, int> equacoe["+str(len(lst_tlf))+"] = {"
		# for  i in lst_tlf:
		# 	txt+="make_pair("+str(cont)+","+str(len(i))+"), "
		# 	cont +=len(i)
		# txt +="};"
		# print(txt)
		# print("\n")
		# # c_tlf = [j for i in lst_tlf for j in i]
		# c_tlf="pair<int, int> values["+str(cont)+"] = {"
		# for i in range(len(lst_tlf)):
		# 	for j in range(len(lst_tlf[i])-1):
		# 		if int(modif[i][j]) > (len(modif)-1):
		# 			print(modif[i][j])
		# 			# id_ext = [z[0] for z in num_ext_id if z[1]==modif[i][j] ][0]
		# 			# v_id_ext = [y[1] for y in external if y[0] == id_ext][0]
		# 			# if v_id_ext:
		# 			# 	c_tlf+="make_pair(-3,"+str(lst_tlf[i][j])+"), "
		# 			# else:
		# 			# 	c_tlf+="make_pair(-2,"+str(lst_tlf[i][j])+"), "
		# 		else:
		# 			c_tlf+="make_pair("+modif[i][j]+","+str(lst_tlf[i][j])+"), "
		# 	c_tlf+="make_pair(-1,"+str(lst_tlf[i][-1])+"), "
		# c_tlf +="};"
		# print(c_tlf)
		# print("tamanho: "+str(cont))
		# print('\n')
		# #for i in range(len([i for i in orig if i.split("= ")[1] != "const"])):
		print(external)
		print()
		print(num_ext_id)
		num_ext = [i[1] for i in num_ext_id]

		if int(sys.argv[2]) == 1:
			entry =  open('grn_gpu/pesosTabela.txt','w')
			entry.write(str(len(lst_tlf)+len(external))+'\n')
			eqtam =""
			for i in modif:
				eqtam +=str(len(i))+" "
			if len(external) != 0:
				for cnt in range(len(external)):
					eqtam += str(1)+" "
			entry.write(eqtam.strip()+'\n')
			# entry.close()

			for i in range(len(orig)):
				eq = ""
				for j in range(len(modif[i])):
					eq += str(modif[i][j])+ " "+str(lst_tlf[i][j])+" "
				eq += str(lst_tlf[i][-1])
				entry.write(eq.strip()+'\n')
			for cnt in range(len(external)):
				eq = ""
				eq += str(num_ext[cnt])+ " "+str(external[cnt][1])+" "+('1' if (external[cnt][1] == 0) else '0' )
				entry.write(eq.strip()+'\n')
			entry.close()


		for i in range(len(orig)):
			txt =orig[i].split('= ')[0]+"= TLF("
			txt = txt.replace("v","aux")


			if(orig[i].split('= ')[1]) != "( const )":

				for j in range(len(modif[i])):
					#print "*****************"+modif[i][j]
					if modif[i][j] in num_ext:
						#print "-----------"+modif[i][j]
						id_ext = [z[0] for z in num_ext_id if z[1]==modif[i][j] ][0]
						#print"id_ext: "+str(id_ext)
						v_id_ext = [y[1] for y in external if y[0] == id_ext][0]

						txt +=" "+str(v_id_ext)+" * "+ str(lst_tlf[i][j])+" +"
					else:
						txt += ' vet['+modif[i][j]+"] * "+ str(lst_tlf[i][j])+" +"
				txt += ", "+str(lst_tlf[i][-1])+" );"
			else:
				#modif.pop(i)
				#modif.insert(i,[])
				#lst_tlf.pop(i)
				#lst_tlf.insert(i,[])
				txt = orig[i].split('= ')[0]+"= 0;"
				txt = txt.replace("v","aux")
			print(txt)

	# except Exception as e:
	# 	print("Error: "+ str(e))
	# 	sys.exit(1)

	# t,l = tabelaVerdade('( v1 and v2 ) or ( v1 and v3 ) ')
	# tlf = TLF(t)
	# for i in TesteTlf(tlf):
	# 	print i
	# print tlf
sys.exit(0)
