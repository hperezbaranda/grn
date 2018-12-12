#include <iostream>
#include <cmath>
#include <stdio.h>
#include "./common.h"
#include <cuda_runtime.h>
#include <curand_kernel.h>
#include <assert.h>
#include <fstream>
#include <string>
#include <omp.h>

using namespace std;

#define TABLE_SIZE 2048
#define BUCKET_SIZE 200
#define TAMANHO_VETOR 3
#define PESOS_GPU 200

struct HashTable
{
    unsigned long int *atratores;
    unsigned long int *count;
    int period;
};

HashTable * junta_atratores(HashTable &tabela_atratores,  int *pesos,  int *posIni,  int*eqSize,  int *T,  int nEq)
{
    HashTable * resultado;
    size_t nBytes = TABLE_SIZE*(sizeof(HashTable));
    resultado = (HashTable *)malloc(nBytes);
 

    unsigned long int MAX_ESTADO = (unsigned long int)pow(2,nEq);

    //aloca memoria para o resultado
    for(int i = 0; i < TABLE_SIZE; i++)
    {
        resultado[i].atratores = (unsigned long int *)malloc(sizeof(unsigned long int) * BUCKET_SIZE);
        resultado[i].count = (unsigned long int *)malloc(sizeof(unsigned long int));
        resultado[i].count[0] = 0;
        resultado[i].period = 0;
    }

    int var,peso;
    //garantindo o ciclo(alguns estados calculados pela GPU estao fora do ciclo, porque eu nao sei)
    for(int i = 0; i < TABLE_SIZE; i++ )
    {
        if(tabela_atratores.count[i] != 0)
        {
            int hash = 0;
            //pega os dados dos estado
            unsigned long int s0 = tabela_atratores.atratores[i], s1 = tabela_atratores.atratores[i];
            unsigned long int numEstados  = tabela_atratores.count[i];
            //zera a posição da tabela
            tabela_atratores.atratores[i] = 0;
            tabela_atratores.count[i] = 0; 

            //garantir o ciclo
            do
            {
                unsigned long int newEstado = 0;

                for(int j = 0; j < nEq; j++)
                {
                    int cal_new = nEq-1-j;
                    int repre_var = 0;
                    int sum_prod = 0, pos = posIni[j] , eqsize = eqSize[j], Teq = T[j] ;
                    //aplicando a tlf
                    for(int z = 0; z < eqsize;  z++, pos += 2)
                    {
                        var = pesos[pos];
                        repre_var = (nEq-1)-var;
                        peso = pesos[pos+1];
                        sum_prod += ((s0>>repre_var)%2)*peso;
                    }
                    
                    newEstado |= (sum_prod >= Teq) << cal_new;
                } 
                //extrai primeiro atrator do ciclo
                s0 = newEstado;

                newEstado = 0;
                for(int j = 0; j < nEq; j++)
                {
                    int cal_new = nEq-1-j;
                    int repre_var = 0;
                    int sum_prod = 0, pos = posIni[j] , eqsize = eqSize[j], Teq = T[j] ;
                    //aplicando a tlf
                    for(int z = 0; z < eqsize;  z++, pos += 2)
                    {
                        var = pesos[pos];
                        repre_var = (nEq-1)-var;
                        peso = pesos[pos+1];
                        sum_prod += ((s1>>repre_var)%2)*peso;
                    }
                    
                    newEstado |= (sum_prod >= Teq) << cal_new;
                } 
                //extrai primeiro atrator do ciclo
                s1 = newEstado;

                newEstado = 0;
                for(int j = 0; j < nEq; j++)
                {
                    int cal_new = nEq-1-j;
                    int repre_var = 0;
                    int sum_prod = 0, pos = posIni[j] , eqsize = eqSize[j], Teq = T[j] ;
                    //aplicando a tlf
                    for(int z = 0; z < eqsize;  z++, pos += 2)
                    {
                        var = pesos[pos];
                        repre_var = (nEq-1)-var;
                        peso = pesos[pos+1];
                        sum_prod += ((s1>>repre_var)%2)*peso;
                    }
                    
                    newEstado |= (sum_prod >= Teq) << cal_new;
                } 
                //extrai primeiro atrator do ciclo
                s1 = newEstado;


            }while(s0 != s1);

            //calcula o hash do estado
            unsigned long int auxEstado = s0;
            int upperBit = -1, lowerBit = -1;
            hash = 0;
            for(int j = 0; j < nEq; j++)
            {
                if(lowerBit == -1 && (auxEstado%2 == 1))
                    lowerBit = j+1;
                
                if(auxEstado%2 == 1)
                {
                    upperBit = j + 1;
                    hash += upperBit;
                }
                auxEstado=auxEstado>>1;
            }
            hash += (upperBit - lowerBit);

            //procura um balde vazio desde que o estado encontrado nao seja igual ao dos baldes encontrados no caminho
            if(hash < TABLE_SIZE)    
                while(hash < TABLE_SIZE)
                {
                    if(tabela_atratores.atratores[hash] == s0 || tabela_atratores.count[hash] == 0) break;
                    hash++;
                }
            else
            {
                hash = 0;
                while(hash < TABLE_SIZE)
                {
                    if(tabela_atratores.atratores[hash] == s0 || tabela_atratores.count[hash] == 0) break;
                    hash++;
                }
            }
            
            tabela_atratores.atratores[hash] = s0;
            // tabela_atratores.count[hash] = (numEstados >= tabela_atratores.count[hash]) ? numEstados : tabela_atratores.count[hash];
            tabela_atratores.count[hash] +=numEstados;
        }

    }
  

    for(int i = 0; i < TABLE_SIZE; i++)
    {
        if(tabela_atratores.count[i] != 0)
        {
            //extrai os dados do atrator atual
            unsigned long int estado = tabela_atratores.atratores[i];
            unsigned long int numEstados  = tabela_atratores.count[i];

            unsigned long int aux = 0;

            //da um passo com estado para garantir o ciclo
            unsigned long int newEstado = 0; 

            int k = 0;
            resultado[i].atratores[k++] = estado;
            resultado[i].period++;
            tabela_atratores.count[i] = 0; // já contamos o atrator inicial 
            
            //aplica um passo com aux
            for(int j = 0; j < nEq; j++)
            {
                int cal_new = nEq-1-j;
                int repre_var = 0;
                int sum_prod =0, pos = posIni[j] , eqsize = eqSize[j], Teq = T[j] ;
                //aplicando a tlf
                for(int z = 0; z < eqsize;  z++, pos += 2)
                {
                    var = pesos[pos];
                    repre_var = (nEq-1)-var;
                    peso = pesos[pos+1];
                    sum_prod += ((estado>>repre_var)%2)*peso;
                }
                
                aux |= (sum_prod >= Teq) << cal_new;
            }

            //enquanto aux for diferente acha todos os estados do ciclo e armazena o atrator na tabela de resultado
            while(aux != estado)
            {
                //calcula o hash do estado atual
                unsigned long int auxEstado = estado;
                int upperBit = -1, lowerBit = -1, hash = 0;
                for(int j = 0; j < nEq; j++)
                {
                    if(lowerBit == -1 && (auxEstado%2 == 1))
                        lowerBit = j+1;
                    
                    if(auxEstado%2 == 1)
                    {
                        upperBit = j + 1;
                        hash += upperBit;
                    }
                    auxEstado=auxEstado>>1;
                }
                hash += (upperBit - lowerBit);

                //procurando se o estado encontrado no ciclo esta na tabela
                if(hash < TABLE_SIZE) //se o estado estiver na tabela extraimos seus dados
                {
                    while(hash < TABLE_SIZE)
                    {
                        if(tabela_atratores.atratores[hash] == aux) break;
                        hash++;
                    } 
                }
                else
                {
                    hash = 0;
                    while(hash < TABLE_SIZE)
                    {
                        if(tabela_atratores.atratores[hash] == estado) break;
                        hash++;
                    }
                }

                resultado[i].atratores[k++] = aux;
                resultado[i].period++;

                if(hash < TABLE_SIZE) //se o estado estiver na tabela extraimos seus dados
                {
                    numEstados += tabela_atratores.count[hash];
                    tabela_atratores.count[hash] = 0; // já contamos esse estado em algum atrator 
                }
                

                //da um passo com aux
                newEstado = 0; 
                for(int j = 0; j < nEq; j++)
                {
                    int cal_new = nEq-1-j;
                    int repre_var = 0;
                    int sum_prod = 0, pos = posIni[j] , eqsize = eqSize[j], Teq = T[j] ;
                    //aplicando a tlf
                    for(int z = 0; z < eqsize;  z++, pos += 2)
                    {
                        var = pesos[pos];
                        repre_var = (nEq-1)-var;
                        peso = pesos[pos+1];
                        sum_prod += ((aux>>repre_var)%2)*peso;
                    }
                    
                    newEstado |= (sum_prod >= Teq) << cal_new;
                }
                
                aux = newEstado;
                //printf("aux : %lu\n",aux);
            }

            

            resultado[i].count[0] = numEstados;
        }
    }

    return resultado;   
};


//versao CPU
void sincrono_TabelaCPU(const int * pesos, const int *posIni, const int*eqSize, const int *T,const int nEq, HashTable &tabela_atratores, const unsigned long long MIN_ESTADO, const unsigned long long MAX_ESTADO)
{
    #pragma omp parallel private(tabela_atratores,s0,s1,posIni,pesos,eqSize,T)
    #pragma omp for schedule(static)
    for(unsigned long long estado = MIN_ESTADO; estado < MAX_ESTADO; estado++)
    {  
        unsigned long long s0 = estado, s1 = estado;
        int var,peso;
        // cout << "ESTADO: "<<estado <<endl<<endl;
        do
        {
            //da um passo com s0
            unsigned long long newEstado = 0; 
            for(int j = 0; j < nEq; j++)
            {   
                int cal_new = nEq-1-j;
                int repre_var = 0;
                int sum_prod = 0, pos = posIni[j] , eqsize = eqSize[j], Teq = T[j] ;
                //aplicando a tlf
                for(int z = 0; z < eqsize;  z++, pos += 2)
                {
                    var = pesos[pos];
                    repre_var = (nEq-1)-var;
                    peso = pesos[pos+1];
                    // cout << "Var--Peso "<<var<<" "<<peso<< endl;
                    // cout<< "Rep "<< repre_var<<endl;
                    sum_prod += ((s0>>repre_var)%2)*peso;
                }
                    
                
                newEstado |= (sum_prod >= Teq) << cal_new;
                // cout << "New Estado tmp "<<newEstado<<endl;
            }
            
            s0 = newEstado;
            // cout << "ESTADO FINAL "<<s0<<endl;

            //da dois apssos com s1
            newEstado = 0; 
            for(int j = 0; j < nEq; j++)
            {
                int cal_new = nEq-1-j;
                int repre_var = 0;
                int sum_prod = 0, pos = posIni[j] , eqsize = eqSize[j], Teq = T[j] ;
                //aplicando a tlf
                for(int z = 0; z < eqsize;  z++, pos += 2)
                {
                    var = pesos[pos];
                    repre_var = (nEq-1)-var;
                    peso = pesos[pos+1];
                    sum_prod += ((s1>>repre_var)%2)*peso;
                }
                
                newEstado |= (sum_prod >= Teq) << cal_new;
            }
            
            s1 = newEstado;

            newEstado = 0; 
            for(int j = 0; j < nEq; j++)
            {
                int cal_new = nEq-1-j;
                int repre_var = 0;
                int sum_prod = 0, pos = posIni[j] , eqsize = eqSize[j], Teq = T[j] ;
                //aplicando a tlf
                for(int z = 0; z < eqsize;  z++, pos += 2)
                {
                    var = pesos[pos];
                    repre_var = (nEq-1)-var;
                    peso = pesos[pos+1];
                    sum_prod += ((s1>>repre_var)%2)*peso;
                }
                
                newEstado |= (sum_prod >= Teq) << cal_new;
            }
            
            s1 = newEstado;

        }while(s0 != s1);

        //achou o estado em que s0 e s1 se encontram, salva o estado
        //Neste ponto s1 = s0

        //variaveis auxiliares
        unsigned long long estadoAtr = 0;
        int upperBit = -1, lowerBit = -1, hash = 0;
        unsigned long long auxEstado = 0;

        //extrai o estado
        estadoAtr = s1;
        //printf("%lu\n",s0);

        //calcula o hash do estado
        upperBit = 0; lowerBit = 0; hash = 0;
        auxEstado = estadoAtr;
        #pragma unroll
        for(int i = 0; i < nEq; i++)
        {
            if(lowerBit == -1 && (auxEstado%2 == 1))
                lowerBit = i+1;
            
            if(auxEstado%2 == 1)
            {
                upperBit = i + 1;
                hash += upperBit;
            }
            auxEstado=auxEstado>>1;
        }
        hash += (upperBit - lowerBit);
        
        //insere o estado na tabela hash :
        if(hash >= TABLE_SIZE || hash < 0){
            printf("Estado : %llu Erro ao calcular o hash : %d\n",estadoAtr,hash);
            return;
        }

        //confere se o balde já está cheio e acha um balde vazio
        if(tabela_atratores.atratores[hash] != 0 && tabela_atratores.count[hash]  == estadoAtr)
            tabela_atratores.count[hash]++;//se dois estados caem no mesmo balde, soma mais um no estado
        else
        {
            //procura um balde vazio desde que o estado encontrado nao seja igual ao dos baldes encontrados no caminho
            while(tabela_atratores.count[hash] != 0 && tabela_atratores.atratores[hash] != estadoAtr) hash++;

            if(hash >= TABLE_SIZE) hash = 13;
            tabela_atratores.atratores[hash] = estadoAtr;
            tabela_atratores.count[hash]++;
        }
    }
}



//versao GPU
__device__ __constant__ int pesosGPU[PESOS_GPU];

__global__ void sincrono_Tabela(const int *posIni ,const int*eqSize,const int *T, unsigned long int *atratores, unsigned long int *count ,const int nEq,const unsigned long long MIN_ESTADO,  const unsigned long long MAX_ESTADO)
{
    //idx da thread também será o estado do grafo
    unsigned long int idx = blockDim.x*blockIdx.x + threadIdx.x + MIN_ESTADO;

    if(idx < MAX_ESTADO)
    {
        //definicao dos vetores em shared memory
        __shared__ int sh_posIni[TAMANHO_VETOR];
        __shared__ int sh_eqSize[TAMANHO_VETOR];
        __shared__ int sh_T[TAMANHO_VETOR];

        __shared__ unsigned long long stable[2][TABLE_SIZE];

        for(unsigned int j = 0; j < (TABLE_SIZE/blockDim.x); j++)
        {
            stable[0][threadIdx.x*(TABLE_SIZE/blockDim.x)+j] = 0;
            stable[1][threadIdx.x*(TABLE_SIZE/blockDim.x)+j] = 0;
        }

        if(threadIdx.x < nEq)
        {
            sh_posIni[threadIdx.x] = posIni[threadIdx.x];
            sh_eqSize[threadIdx.x] = eqSize[threadIdx.x];
            sh_T[threadIdx.x] = T[threadIdx.x];
        }
            
        __syncthreads();
    
        //cada thread faz uma simulação por vez
        unsigned long int s0=idx, s1=idx;

        unsigned long int aux = 0, newEstado = 0;
        
        do
        {
            //guarda o estado em aux
            aux = s0;
            newEstado = 0;

            //um passo em s0
            for(int i = 0; i < nEq; i++)
            {
                int cal_new = nEq-1-i;
                int repre_var = 0;
                int sum_prod =0, pos = sh_posIni[i] , eqsize = sh_eqSize[i], Teq = sh_T[i] ;
                //aplicando a tlf
                for(int j = 0; j < eqsize;  j++, pos += 2){
                    repre_var = (nEq-1)-pesosGPU[pos];
                    sum_prod += ((aux>>repre_var)%2)*pesosGPU[pos+1];
                }
                
                newEstado |= (sum_prod >= Teq) << cal_new;
            }
            s0 = newEstado;
            
            //dois passos em s1
            aux = s1;
            newEstado = 0;

            for(int i = 0; i < nEq; i++)
            {
                int cal_new = nEq-1-i;
                int repre_var = 0;
                int sum_prod =0, pos = sh_posIni[i] , eqsize = sh_eqSize[i], Teq= sh_T[i] ;
                //aplicando a tlf
                for(int j = 0; j < eqsize;  j++, pos += 2){
                    repre_var = (nEq-1)-pesosGPU[pos];
                    sum_prod += ((aux>>repre_var)%2)*pesosGPU[pos+1];
                }
                
                newEstado |= (sum_prod >= Teq) << cal_new;
            }

            aux = newEstado;
            newEstado = 0;
            for(int i = 0; i < nEq; i++)
            {
                int cal_new = nEq-1-i;
                int repre_var = 0;
                int sum_prod =0, pos = sh_posIni[i] , eqsize = sh_eqSize[i], Teq= sh_T[i] ;
                //aplicando a tlf
                for(int j = 0; j < eqsize;  j++, pos += 2){
                    repre_var = (nEq-1)-pesosGPU[pos];
                    sum_prod += ((aux>>repre_var)%2)*pesosGPU[pos+1];
                }
                
                newEstado |= (sum_prod >= Teq) << cal_new;
            }

            s1 = newEstado;

        }while(s0 != s1);

        __syncthreads();

        //Neste ponto s1 = s0

        //variaveis auxiliares
        unsigned long int estado = 0;
        int upperBit = -1, lowerBit = -1, hash = 0;
        unsigned long int auxEstado = 0;

        //extrai o estado
        estado = s1;
        //printf("%lu\n",s0);

        //calcula o hash do estado
        upperBit = 0; lowerBit = 0; hash = 0;
        auxEstado = estado;
        #pragma unroll
        for(int i = 0; i < nEq; i++)
        {
            if(lowerBit == -1 && (auxEstado%2 == 1))
                lowerBit = i+1;
            
            if(auxEstado%2 == 1)
            {
                upperBit = i + 1;
                hash += upperBit;
            }
            auxEstado=auxEstado>>1;
        }
        hash += (upperBit - lowerBit);
        
        //insere o estado na tabela hash :
        if(hash >= TABLE_SIZE || hash < 0){
            printf("Estado : %lu Erro ao calcular o hash : %d\n",estado,hash);
            return;
        }

        //confere se o balde já está cheio e acha um balde vazio
        if(stable[1][hash] != 0 && stable[0][hash] == estado)
            atomicAdd((unsigned long long *)&(stable[1][hash]),(unsigned long long)1);//se dois estados caem no mesmo balde, soma mais um no estado
        else
        {
            //procura um balde vazio desde que o estado encontrado nao seja igual ao dos baldes encontrados no caminho
            while(stable[1][hash] != 0 && stable[0][hash] != estado) hash++;

            if(hash >= TABLE_SIZE) hash = 13;
            atomicExch((unsigned long long *)&(stable[0][hash]),(unsigned long long)estado);
            atomicAdd((unsigned long long *)&(stable[1][hash]),(unsigned long long)1); 
        }
        __syncthreads();

        if(threadIdx.x == 0)
        {
            #pragma unroll
            for(int i = 0; i < TABLE_SIZE; i++)
            {
                atomicAdd((unsigned long long *)&(atratores[i]), (unsigned long long)stable[0][i]);
                atomicAdd((unsigned long long *)&(count[i]), (unsigned long long)stable[1][i]);
            } 
        }
        __syncthreads();

    }

}

/* __global__ void assincrono_Tabela(curandState * curandstate, const int *posIni ,const int*eqSize,const int *T, unsigned long long * result ,const int nEq,const unsigned long long MIN_ESTADO,  const unsigned long long MAX_ESTADO)
{

    int idx = threadIdx.x+blockDim.x*blockIdx.x;

    //definindo qual equação a thread pega
    int warpID = threadIdx.x / 32;
    int nWarps = blockDim.x / 32;

    int EQ = blockIdx.x*nWarps + warpID;

    //inicia o rand usando o id da tread + estado min como seed
    curand_init(idx+MIN_ESTADO,MAX_ESTADO - MIN_ESTADO,MIN_ESTADO,&curandstate[idx]);

    //so faz a computacao se a equacao carregada é valida
    if(EQ >= 0 && EQ < nEq)
    {
        //printf("Block : %d Warp : %d Thread : %d  EQ : %d\n",blockIdx.x,warpID,threadIdx.x,EQ);
        int pos = posIni[EQ], eqsize = eqSize[EQ], Teq = T[EQ];

        //definindo o numero de simulacoes e valor inicial do contador de quantas vezes a variavel e zero
        unsigned long long nSim = 1000000/32, zero = 0;

        for(unsigned long long i = 0; i < nSim; i++)
        {
            //gerando estado aleatório a partir de uma distribuição uniforme
            float randf = curand_uniform(&(curandstate[idx]));
            randf *= (MAX_ESTADO - MIN_ESTADO  + 0.999999);
            randf += MIN_ESTADO;
            unsigned long long estado = (unsigned long long)truncf(randf);

            int sum_prod =0;
            //aplicando a tlf
            for(int j = 0; j < eqsize;  j++, pos += 2)
                sum_prod += ((estado>>pesosGPU[pos])%2)*pesosGPU[pos+1];
            //se a soma for menor que o treshold o valor da variável é zero
            zero += (sum_prod < Teq) ? 1 : 0;
        }
        __syncthreads();

        //shuffle_xor
        //traz todos os dados para a tread 0 de cada warp e salva no vetor
        for(int mask = 1; mask <= 16; mask*=2 )
            zero += __shfl_xor (zero, mask,32);
        __syncthreads();
        

        int laneID = threadIdx.x%32;
        //retorna os resultados para a CPU
        if(laneID == 0)
            atomicAdd(&result[EQ],zero);
        __syncthreads();

    }

} */


int main(int argc, char **argv)
{
    int nEq; // numero de equações
    filebuf fb;
    if(!fb.open(argv[1],ios::in))
    {
        cerr << "Erro ao abrir arquivo de entrada " << argv[1] <<endl;
        exit(0);
    }

    istream is(&fb);
    is >> nEq;

    int * pesosCPU, *pesoIniCPU, *pesoIniGPU, *eqSizeCPU, *eqSizeGPU,*TCPU,*TGPU;
    unsigned long long *resultCPU, *resultGPU; // peso, tamanho das equações e threshold de cada equação
    size_t bytes = sizeof(int)*nEq;

    // Alocando memória do host
    eqSizeCPU = (int *)malloc(bytes);
    TCPU = (int *)malloc(bytes);
    pesoIniCPU = (int *)malloc(bytes);
    resultCPU = (unsigned long long *)malloc(3*sizeof(unsigned long long));

    //alocando memoria device
    cudaMalloc((int **)&eqSizeGPU,bytes);
    cudaMalloc((int**)&TGPU,bytes);
    cudaMalloc((int **)&pesoIniGPU, bytes);
    cudaMalloc((unsigned long long **)&resultGPU, 3*sizeof(unsigned long long));

    //lendo os tamanhos das equações
    int nPesos = 0; // numero de pesos

    for(int i = 0; i < 3; i++)
        resultCPU[i] = 0;

    for(int i = 0; i < nEq; i++)
    {
        is >> eqSizeCPU[i];
        nPesos+= eqSizeCPU[i];
    }
    
    //alocando vetores com pesos e re
    pesosCPU = (int *)malloc(sizeof(int)*nPesos*2);

    int posPeso = 0; //posição dos pesos
    for(int i = 0; i < nEq; i++)
    {
        int var=0, peso=0, T=0; // variavel da equação, peso e threshold
        pesoIniCPU[i] = posPeso;
        for(int j = 0; j < eqSizeCPU[i]; j++,posPeso+=2)
        {
            is >> var >> peso;
            pesosCPU[posPeso] = var;
            pesosCPU[posPeso + 1] = peso;
        }
        is >> T;
        TCPU[i] = T;
    }
    fb.close();

    
     //pesos ficam em memoria const
    cudaMemcpy(TGPU,TCPU,bytes,cudaMemcpyHostToDevice);
    cudaMemcpy(eqSizeGPU,eqSizeCPU,bytes,cudaMemcpyHostToDevice);
    cudaMemcpy(pesoIniGPU,pesoIniCPU,bytes,cudaMemcpyHostToDevice);
    cudaMemcpy(resultGPU,resultCPU,3*sizeof(unsigned long long),cudaMemcpyHostToDevice);

    // ------------------- Sincrono -----------------

     size_t nBytes = sizeof(unsigned long int)*TABLE_SIZE;

    //aloca memoria 
    HashTable cpuTable,gpuTable, cpuTable2;
    cpuTable.atratores = (unsigned long int *)malloc(nBytes);
    cpuTable.count = (unsigned long int *)malloc(nBytes);
    cpuTable2.atratores = (unsigned long int *)malloc(nBytes);
    cpuTable2.count = (unsigned long int *)malloc(nBytes);
    cudaMalloc((unsigned long int **)&gpuTable.atratores, nBytes);
    cudaMalloc((unsigned long int **)&gpuTable.count,nBytes);

    //inicializacao da tabela
    for(unsigned int i = 0; i < TABLE_SIZE; i++)
    {
        cpuTable.count[i] = 0;
        cpuTable.atratores[i] = 0;
    }
    
    //copia tabela do host para tabela da gpu
    cudaMemcpy(gpuTable.atratores, cpuTable.atratores, nBytes, cudaMemcpyHostToDevice);
    cudaMemcpy(gpuTable.count, cpuTable.count, nBytes, cudaMemcpyHostToDevice);

    int threads = 1024;
    dim3 block(threads);

    unsigned long long MIN_ESTADO = 0;
    unsigned long long MAX_ESTADO = 0;

    //pede a quantidade de estados para simular
    string argv2 = argv[2];
    for(int i = 0; i < argv2.size() ; i++)
        MAX_ESTADO += ((unsigned long int)(argv2[i] - '0'))*pow(10,argv2.size()-i-1);
    
    dim3 grid((MAX_ESTADO + block.x -1)/block.x);

    //GPU
    /* printf("Modelo Sincrono GPU\n");
    printf("N de estados : %llu\n",MAX_ESTADO - MIN_ESTADO);
    printf("Blocos : %d  Threads : %d \n",grid.x, block.x); */
    HashTable * resultado;
    string tec = argv[3];
    
    if(tec == "GPU")
    {
        cudaMemcpyToSymbol(pesosGPU,pesosCPU,sizeof(int)*nPesos*2);//copia memoria do host para o device
        sincrono_Tabela<<<grid,block,bytes>>>(pesoIniGPU,eqSizeGPU,TGPU,gpuTable.atratores, gpuTable.count,nEq,MIN_ESTADO,MAX_ESTADO);
           
        //extrai resultado da GPU
        cudaMemcpy(cpuTable.atratores, gpuTable.atratores, nBytes, cudaMemcpyDeviceToHost);
        cudaMemcpy(cpuTable.count, gpuTable.count, nBytes, cudaMemcpyDeviceToHost);

        //estados de todos os atratores separados
        /* for(int i = 0; i < TABLE_SIZE; i++)
            printf("%lu %lu\n",cpuTable.atratores[i], cpuTable.count[i]); */

        //Faz o estograma (HashTable &tabela_atratores, const int *pesos, const int *posIni, const int*eqSize, const int *T, const int nEq)
        resultado = junta_atratores(cpuTable,pesosCPU,pesoIniCPU,eqSizeCPU,TCPU,nEq);
        //imprime resultado
        for(int i = 0; i < TABLE_SIZE; i++)
        {
            if(resultado[i].period != 0 )
            {
                cout<<resultado[i].period<< " ";
                for(int j = 0; j < resultado[i].period; j++)
                    cout<<resultado[i].atratores[j]<< " ";
                cout<<resultado[i].count[0]<<endl;
            }
        }

        //libera memoria alocada
        for(int i = 0; i < TABLE_SIZE; i++)
        {
            free(resultado[i].atratores);
            free(resultado[i].count);
        }
        free(resultado);
        cudaFree(gpuTable.atratores);
        cudaFree(gpuTable.count);
    }

    
    HashTable * resultadoCPU;

    if(tec == "CPU")
    {
        /* printf("Modelo Sincrono CPU\n"); */
        //CPU

        
        sincrono_TabelaCPU(pesosCPU,pesoIniCPU,eqSizeCPU,TCPU,nEq,cpuTable2,MIN_ESTADO,MAX_ESTADO);
        resultadoCPU = junta_atratores(cpuTable2,pesosCPU,pesoIniCPU,eqSizeCPU,TCPU,nEq);
        
        //imprime resultado
        for(int i = 0; i < TABLE_SIZE; i++)
        {
            if(resultadoCPU[i].period != 0 )
            {
                cout << resultadoCPU[i].period<< " ";
                for(int j = 0; j < resultadoCPU[i].period; j++)
                    cout << resultadoCPU[i].atratores[j]<<" ";
                cout<<resultadoCPU[i].count[0]<<endl;
            }
        }

        //libera memoria alocada
        for(int i = 0; i < TABLE_SIZE; i++)
        {
            free(resultadoCPU[i].atratores);
            free(resultadoCPU[i].count);
        }
    

        
        free(resultadoCPU);
        free(cpuTable.atratores);
        free(cpuTable.count);
    }

    //-------------Assincrono----------------

    /* curandState *d_state;
    cudaMalloc(&d_state, sizeof(curandState));

    //limpa a memoria do resultado para reutilizar o vetor
    free(resultCPU);
    cudaFree(resultGPU);

    size_t bytesResult = nEq*sizeof(unsigned long long);

    //aloca vetor de resultado
    resultCPU = (unsigned long long *)malloc(bytesResult);
    cudaMalloc((unsigned long long **)&resultGPU, bytesResult);

    //inicializa valores
    for(int i = 0; i < nEq ; i++)
        resultCPU[i] = 0;
    
    cudaMemcpy(resultGPU, resultCPU, bytesResult, cudaMemcpyHostToDevice);


    printf("Modelo Assincrono\n");
    printf("Estado Min : %llu Estado Max : %llu\n",MIN_ESTADO,MAX_ESTADO);
    printf("Blocos : %d  Threads : %d \n",grid.x, block.x);
    assincrono_Tabela<<<grid,block,bytes>>>(d_state,pesoIniGPU,eqSizeGPU,TGPU,resultGPU,nEq,MIN_ESTADO,MAX_ESTADO);
    CHECK(cudaGetLastError());

    cudaMemcpy(resultCPU,resultGPU,bytesResult,cudaMemcpyDeviceToHost);

    double async_p0[nEq];

    for(int i = 0; i < nEq; i++)
    {
        async_p0[i] = ((double)resultCPU[i]/(double)(MAX_ESTADO-MIN_ESTADO));
        printf("Variavel  %d  0: %2f  1: %2f\n",i,async_p0[i],1.000000000-async_p0[i]);
    } */
    
    free(TCPU);
    free(eqSizeCPU);
    free(pesosCPU);
    free(pesoIniCPU);
    cudaFree(eqSizeGPU);
    cudaFree(TGPU);
    cudaFree(pesoIniGPU);
    
    
    cudaDeviceReset();
    return 0;
}

