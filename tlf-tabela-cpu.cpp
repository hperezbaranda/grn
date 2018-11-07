#include <iostream>
#include <cmath>
#include <stdio.h>
#include <stdlib.h>
#include <fstream>
#include <string>

using namespace std;

#define TABLE_SIZE 1024
#define BUCKET_SIZE 200
#define TAMANHO_VETOR 21
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
    // resultado = (HashTable *)malloc(nBytes);

    resultado = new HashTable [nBytes];
 

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

    

    // ------------------- Sincrono -----------------

     size_t nBytes = sizeof(unsigned long int)*TABLE_SIZE;

    //aloca memoria 
    HashTable cpuTable,gpuTable, cpuTable2;
    cpuTable.atratores = (unsigned long int *)malloc(nBytes);
    cpuTable.count = (unsigned long int *)malloc(nBytes);
    cpuTable2.atratores = (unsigned long int *)malloc(nBytes);
    cpuTable2.count = (unsigned long int *)malloc(nBytes);
    

    //inicializacao da tabela
    for(unsigned int i = 0; i < TABLE_SIZE; i++)
    {
        cpuTable.count[i] = 0;
        cpuTable.atratores[i] = 0;
    }
    
    unsigned long long MIN_ESTADO = 0;
    unsigned long long MAX_ESTADO = 0;

    //pede a quantidade de estados para simular
    string argv2 = argv[2];
    for(int i = 0; i < argv2.size() ; i++)
        MAX_ESTADO += ((unsigned long int)(argv2[i] - '0'))*pow(10,argv2.size()-i-1);
    
    
    //GPU
    /* printf("Modelo Sincrono GPU\n");
    printf("N de estados : %llu\n",MAX_ESTADO - MIN_ESTADO);
    printf("Blocos : %d  Threads : %d \n",grid.x, block.x); */
    HashTable * resultado;
    string tec = argv[3];
    
       
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

    
    free(TCPU);
    free(eqSizeCPU);
    free(pesosCPU);
    free(pesoIniCPU);
    return 0;
}

