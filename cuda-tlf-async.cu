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

__device__ bool state[SIZE];
//__shared__ bool equ[SIZE];

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
  }
}

__device__ void calculateState(bool* state,int num){
  // for (size_t i = 0; i < SIZE; i++) {
  //   printf("\n%d\n", equ[i]);
  // }
  bool equ[SIZE];
  equ[0] = ( state[0]  | !  state[2] );
  equ[1] = ( state[0]  &   state[2] );
  equ[2] = state[1];
  state[num] = equ[num];

}

__global__ void initSharedMem(){
  for (size_t i = 0; i < SIZE; i++) {
    printf("\n%d\n", state[i]);
  }
  // equ[0]=( state[0]  | !state[2] );
  // equ[1]=( state[0]  &  state[2] );
  // equ[2]=state[1];
}

__global__ void findAttractor(int number)
{

  bool sta[SIZE];
  uint thread = blockDim.x * blockIdx.x + threadIdx.x;
  initialState(thread,sta,SIZE);

  printf("Hello World from GPU! number: %d\n", thread);
  printf("My number: %d\n", number);

  calculateState(sta,number);

//  printf("saida: " );
  printf("%d\n", getDecValue(sta));
}

int main(int argc, char **argv)
{
  uint seed = (uint) time(NULL);
  srand(seed);
  uint suffle = rand() %SIZE;
  // std::cout <<   << std::endl;
  size_t numThreads =2;
  //initSharedMem<<<1,1>>>();
  findAttractor<<<1, numThreads>>>(suffle);
  cout << endl;
  // CHECK(cudaFree(d_equation));
  CHECK(cudaDeviceReset());
  // cudaDeviceReset();

  return 0;
}
