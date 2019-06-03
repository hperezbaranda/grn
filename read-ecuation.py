#! /bin/python
import sys
import re



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
	pattern =re.compile(r'(?<=v)\d+')
	#print sys.argv[1]+'expr/expressions.ALL.txt'

	try:
		line = open(sys.argv[1]+'expr/expressions.ALL.txt','r').readlines()

		#external = [i.strip() for i in open(sys.argv[1]+'expr/external_components.ALL.txt','r').readlines()]
		try:
			external = [(i.strip().split()[0],int(i.strip().split()[1])) for i in open(sys.argv[1]+'expr/external_components.ALL.txt','r').readlines()]

		except Exception as e:
			external = [(i.strip().split()[0],0) for i in open(sys.argv[1]+'expr/external_components.ALL.txt','r').readlines()]

		#print external
		node_ext = [i[0] for i in external]

		list = [i.split(' =')[0] for i in line]

		orig=[]
		modif = []
		lst_tlf=[]
		num_ext_id=[]
		for i in line:
			count=0
			txt = ''
			for j in i.split():
				if j in list:
					txt += ' v'+str(list.index(j))
					count+=1
				else:
					#Agregue esto
					#extra = pattern.findall(j)

					#if len(extra)>0:
					if j in node_ext:
						#print j
						list.append(j)
						txt += ' v'+str(list.index(j))
						num_ext_id.append((j,str(list.index(j))))
						#pass
						#txt += ' const'
					else:
						txt += ' '+j

			#print(1 if eval(txt.split('=')[1]) else 0)

			if count > 0:
				#print txt.split('=')[1]
				table,l = tabelaVerdade(txt.split('=')[1])

				tlf=TLF(table)
				#print("TLF: "+tlf)
				# print('\n')
				modif.append(l)
				lst_tlf.append(tlf)
				orig.append(txt)
			else:
				orig.append(txt.split(' =')[0]+' = const')
				#print "Holaaaa"

		print('\n')
		print("Nodes: "+str(list))
		print('\n')
		#for i in orig:
		#	print i.replace('or','|')
		#print (orig)
		print('\n')
		orig = [i.replace('or', '|') for i in orig]
		orig = [i.replace('and', '&') for i in orig]
		orig = [i.replace('not', '!') for i in orig]
		for n in range(len(orig)):
			orig = [i.replace('v'+str(n)+' ', 'v['+str(n)+'] ') for i in orig]
		#orig = [i.replace('v'+str(n)+' ', 'v['+str(n)+'] ') for n in range(len(orig)) for i in orig]
		#orig = [i.replace('v39', 'const') for i in orig]
		#print pattern.match('( v9 )').group(0)
		const =  [pattern.search(i).group(0) for i in orig if pattern.search(i) !=None]
		#print const
		for i in const:
			#print i
			orig = [j.replace("v"+i,"const") for j in orig]

		for i in orig:
			print(i)


		print('\n')
		print(modif)
		print("tamanho: "+str(len(modif)))
		print('\n')
		print("TLF: "+str(lst_tlf))
	#	print("\n")
		print("tamanho: "+str(len(lst_tlf)))
		print("\n")
		cont = 0
		txt="pair<int, int> equacoe["+str(len(lst_tlf))+"] = {"
		for  i in lst_tlf:
			txt+="make_pair("+str(cont)+","+str(len(i))+"), "
			cont +=len(i)
		txt +="};"
		print(txt)
		print("\n")
		# c_tlf = [j for i in lst_tlf for j in i]
		c_tlf="pair<int, int> values["+str(cont)+"] = {"
		for i in range(len(lst_tlf)):
			for j in range(len(lst_tlf[i])-1):
				if int(modif[i][j]) > (len(modif)-1):
					id_ext = [z[0] for z in num_ext_id if z[1]==modif[i][j] ][0]
					v_id_ext = [y[1] for y in external if y[0] == id_ext][0]
					if v_id_ext:
						c_tlf+="make_pair(-3,"+str(lst_tlf[i][j])+"), "
					else:
						c_tlf+="make_pair(-2,"+str(lst_tlf[i][j])+"), "
				else:
					c_tlf+="make_pair("+modif[i][j]+","+str(lst_tlf[i][j])+"), "
			c_tlf+="make_pair(-1,"+str(lst_tlf[i][-1])+"), "
		c_tlf +="};"
		print(c_tlf)
		print("tamanho: "+str(cont))
		print('\n')
		#for i in range(len([i for i in orig if i.split("= ")[1] != "const"])):
		print(external)
		print(num_ext_id)
		num_ext = [i[1] for i in num_ext_id]
		print(num_ext)
		for i in range(len(orig)):
			txt =orig[i].split('= ')[0]+"= TLF("
			txt = txt.replace("v","aux")
	#		print txt
			#print orig[i].split('= ')[1]!= "( const )"
			if(orig[i].split('= ')[1]) != "( const )":
				#print txt
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

	except Exception as e:

		print("Error: "+ str(e))

	# t,l = tabelaVerdade('not ( v1 or v2  or  v3 ) ')
	# tlf = TLF(t)
	# for i in TesteTlf(tlf):
	#     print i
	# print tlf
