#include <iostream>
#include <math.h>
#include <omp.h>
#include <map>
#include <string>
#include <stdlib.h>
#include <sstream>
#include <fstream>

// #define SIZE 21
#define NUMTHREADS 1024
#define simN 2097152

using namespace std;
typedef map <string,map <int,int> > Tabela;
Tabela atractor;

void pass(unsigned long long &state);
bool equals(bool *vet1, bool *vet2, int size);
void initialState(unsigned long int valor, bool *vet1, bool *vet2, int size);
int binarice(bool *vect);
string boolArraytoString(bool *vet, int size);
unsigned long long int boolArraytoInt(bool *vet, int size);
void runGNR (int inicio, int fim);

int *eqSizeCPU, *threshold, *equations;
int nEq;

int main(int argc, char **argv) {
    
    filebuf fb;
    if(!fb.open(argv[1],ios::in))
    {
        cerr << "Erro ao abrir arquivo de entrada " << argv[1] <<endl;
        exit(0);
    }
    istream is(&fb);
    is >> nEq;

    size_t bytes = sizeof(int)*nEq;
    eqSizeCPU = (int *)malloc(bytes);
    threshold = (int *)malloc(bytes);

    unsigned long int estadosIniciais;
    // estadosIniciais = (unsigned long int) pow(2, nEq)-1;
    stringstream numsimulation;
    numsimulation << argv[2];
    numsimulation >> estadosIniciais;
    //cout << "estados iniciais: "<<estadosIniciais << endl;
    unsigned int period = 0;
    unsigned int transient = 0;
    omp_set_num_threads(NUMTHREADS);
    unsigned long int inicio=0;
    unsigned long int dadosporThread = (estadosIniciais/NUMTHREADS)-1;
    unsigned long int fim = dadosporThread;

    int count = 0;
    for(int i = 0; i < nEq; i++)
    {
        is >> eqSizeCPU[i];
        count +=eqSizeCPU[i];
    }

    equations = (int *)malloc(sizeof(int)*count*2);

    int insert = 0;
    for(size_t i = 0; i < nEq; i++)
    {
        int var, peso;
        for(size_t j = 0; j < eqSizeCPU[i]; j++,insert+=2)
        {
            is >> var >> peso;
            // printf("%d  %d\n", var, peso);
            equations[insert]=var;
            equations[insert+1]=peso;
        }
        is >> threshold[i];        
    }

    runGNR(0,estadosIniciais);
    
    cout << "CPU" << endl;
    for( Tabela::iterator it = atractor.begin(); it != atractor.end(); ++it )
    {
        // cout << it->first << ": ";
        for( map <int,int>::iterator it2=it->second.begin(); it2 != it->second.end(); ++it2){
            cout << it2->first << " "<< it->first <<it2->second << endl;
        }
        
    }

    free(threshold);
    free(eqSizeCPU);
    return 0;
}

void initialState(unsigned long int valor, bool *vet1, bool *vet2, int size) {
    // std::cout <<" initialState: "<< std::endl;
    for (int i = 0; i < size; i++) {
        vet1[size-1-i] = (valor & 1) != 0;
        vet2[size-1-i] = vet1[size-1-i];
        valor >>= 1;
            // std::cout <<vet1[i] << " ";
    }
    //std::cout <<" = "<< boolArraytoInt(vet1,size) << std::endl;
}

bool equals(bool *vet1, bool *vet2, int size) {
    for (int i = 0; i < size; i++) {
        if (vet1[i] != vet2[i]) {
            return false;
        }
    }
    return true;
}

string boolArraytoString(bool *vet, int size) {
    string out;
    for (int i = size - 1; i >= 0; i--) {
        if (vet[i]) {
            out = out + "1";
        } else {
            out = out + "0";
        }
    }
    return out;
}

unsigned long long int boolArraytoInt(bool *vet, int size) {
    int out = 0;
    // std::cout <<"boolArraytoInt:"<< std::endl;
    for (int i = size - 1; i >= 0; i--) {
        //   std::cout << vet[i] << " ";
        if (vet[i]) {
            out |= 1;
        }
        if (i > 0) out <<= 1;
    }
    //  std::cout << " = " << out << std::endl;
    return out;
}

short TLF(int sum_prod, int T){
	return ((sum_prod >= T) ? 1 : 0);
}

void pass (unsigned long long &state){
    int statetmp = state;
    state =0;
    int poseq = 0;
    for(size_t i = 0; i < nEq; i++)
    {
        int sumprod = 0;
        
        int calNew = nEq-1-i;
        for(size_t j = 0; j < eqSizeCPU[i]; j++,poseq+=2)
        {            
            int posreal = nEq-1-equations[poseq];
            sumprod += ((statetmp >> posreal)%2)*equations[poseq+1];
        }
        state |= TLF(sumprod,threshold[i]) << calNew;        
    }   
}

void runGNR(int inicio, int fim) {
    unsigned long long s0;
    unsigned long long s1;
    unsigned long long int period = 0;
    // unsigned long int transient = 0;
    srand((unsigned)time(NULL));
    double tick, tock;
	tick = omp_get_wtime();
    #pragma omp parallel private(s0,s1,period) // Cada thread tem seu próprio s0 e s1 para executar a sua parte do for
    #pragma omp for schedule(static)
    for (unsigned long long int i = inicio; i < fim; i++) {
        string at = "";       
        s0 = s1 = i;
        //s0=s1 = rand() % fim;
        
        period = 0;
        do {
            pass(s0);
            pass(s1);
            pass(s1);
            
        
        } while (s0 != s1);

        stringstream bintostr;
        bintostr << s0;      
        at = bintostr.str()+" ";
        
        size_t found;
        bool already = false;
        int pos_found = -1;
        #pragma omp critical (find)
        {
        for( Tabela::iterator it = atractor.begin(); it != atractor.end(); ++it )
        {
            found = it->first.find(at);
            if(found != string::npos){
                already = true;
                at = it->first;
                period = it->second.begin()->first;
                pos_found = found;
            }
        }
        }
        
        if(!already){
            do {
                pass(s1);
                period++;
                if(s0 != s1){
                    stringstream bintostr;
                    bintostr << s1;
                    at += bintostr.str()+" ";  
                }
            } while (s0 != s1); 
        }
        #pragma omp critical (write)
        {
            atractor[at][period] += 1;
        }
        period = 0;
    }

	tock = omp_get_wtime();;
	// cout << "Time elapse: " << tock-tick << endl;


}
