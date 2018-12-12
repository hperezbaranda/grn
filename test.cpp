#include <iostream>
#include <math.h>
#include <omp.h>
#include <map>
#include <string>
#include <stdlib.h>
#include <sstream>
#include <fstream>
int x;
//#pragma omp threadprivate(x)

void foo() {
    printf("%p\n", &x);
}

void este(int &num){
    printf("%d\n",num >> 1);
}

int *eq;

using namespace std;

int main(int argc, char **argv) {
    omp_set_num_threads(8);

    filebuf fb;
    if(!fb.open(argv[1],ios::in))
    {
        cerr << "Erro ao abrir arquivo de entrada " << argv[1] <<endl;
        exit(0);
    }
    istream is(&fb);

    eq = (int *)malloc(sizeof(int *)*4);
    //#pragma omp parallel
    #pragma omp parallel private(x)
    {
        printf("%p\n", &x);
        foo();
    }

    is >> eq[0];
    
    
    for(size_t i = 0; i < 4; i++)
    {
        printf("%d\n",eq[i]);
    }
    
    int number = 2;
    printf("\n%d\n", number);
    este(number);
    printf("%d\n",number);
    
}