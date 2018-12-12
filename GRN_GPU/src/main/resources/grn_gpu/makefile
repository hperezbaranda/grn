program = multicore-tlf-tabela
program_cpu = multicore-cpu-tlf
arch = sm_30
metrics =--metrics all#--metrics shared_load_transactions_per_request --metrics shared_store_transactions_per_request

simN =2097152
input = pesosTabela.txt 
tecnologia =  GPU

all: $(tecnologia)

GPU:
	nvcc -arch=$(arch) $(program).cu -o $(program)
	./$(program) $(input) $(simN) $(tecnologia) > saida.txt

CPU:
	g++ -o multicore-cpu-tlf multicore-cpu-tlf.cpp -fopenmp
	./$(program_cpu) $(input) $(simN)> saida.txt
	
analise:
	nvprof --unified-memory-profiling off ./$(program) $(input) $(simN)

clean:
	rm $(program) multicore saidaCPU.txt saidaGPU.txt
