program = multicore-tlf-tabela
program_cpu = multicore-cpu-tlf
arch = sm_30
metrics =--metrics all#--metrics shared_load_transactions_per_request --metrics shared_store_transactions_per_request

simN =10
input = pesosTabela.txt 
tecnologia = CPUs
saida = saida_S10.txt

all: $(tecnologia)

GPUs:
	nvcc -arch=$(arch) $(program).cu -o $(program)
	./$(program) $(input) $(simN) $(tecnologia) > $(saida)

CPUs:
	g++ -o multicore-cpu-tlf multicore-cpu-tlf.cpp -fopenmp
	./$(program_cpu) $(input) $(simN) > $(saida)
	
analise:
	nvprof --unified-memory-profiling off ./$(program) $(input) $(simN)

clean:
	rm $(program) multicore saidaCPU.txt saidaGPU.txt
