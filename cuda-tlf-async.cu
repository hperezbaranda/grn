#include "common.h"
#include <iostream>
#include <stdio.h>
#include <cstdlib>
#include <curand.h>
#include <curand_kernel.h>

#define SIZE 3

/*
 * A simple introduction to programming in CUDA. This program prints "Hello
 * World from GPU! from 10 CUDA threads running on the GPU.
 */
using namespace std;

__device__ int getDecValue(bool* v){
  int num=0;
  for (size_t i = 0; i < SIZE; i++) {
    //printf("%sd ", (1 << i-1) * v[i-1] );
    num += (1 << i) * v[SIZE-i-1];
  }
    return num;
}
__device__ void initialState(unsigned long int valor, bool *vet, int size) {
    // std::cout <<" initialState: "<< std::endl;
    for (int i = 0; i < size; i++) {
        vet[size-1-i] = (valor & 1) != 0;
        valor >>= 1;
        // printf("%d     ",vet[(size-i)-1] );
            // cout << vet[i] << " ";
    }
    // __syncthreads();
    // cout << endl;
}

__device__ void calculateState(bool* vet, int num){
  bool aux[SIZE]={  ( vet[0] ) | ! ( vet[2] )  , ( vet[0] ) &  ( vet[2] ) ,vet[1]};

  vet[num] = aux[num];
}

__global__ void findAttractor(int number)
{

    bool grafo[SIZE];
    uint thread = blockDim.x * blockIdx.x + threadIdx.x;
    initialState(thread,grafo,SIZE);
    // printf("%d\n", SIZE );

    printf("Hello World from GPU! number: %d\n", thread);
    // for (size_t i = 0; i < SIZE; i++) {
    //   printf("%d ",grafo[i] );
    // }
    //
    // printf("\n" );


    // curandState state;
    // uint seed = (uint) clock64();
    // curand_init(seed+thread,0,1,&state);
    // printf("State: %d\n", state);
    printf("My number: %d\n", number);
    calculateState(grafo,number);

    for (size_t i = 0; i < SIZE; i++) {
      printf("%d ",grafo[i] );
    }
    printf("\n%d", getDecValue(grafo));


}

int main(int argc, char **argv)
{
    printf("Hello World from CPU!\n");
    uint seed = (uint) time(NULL);
    srand(seed);
    uint suffle = rand() %SIZE;
    // std::cout <<   << std::endl;
    size_t numThreads =8;
    findAttractor<<<1, numThreads>>>(suffle);
    cout << endl;
    CHECK(cudaDeviceReset());
    // cudaDeviceReset();
    return 0;
}
