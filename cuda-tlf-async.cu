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


void printData(const char *msg, int *in,  const int size)
{
    printf("%s: ", msg);

    for (int i = 0; i < size; i++)
    {
        printf("%5d", in[i]);
        fflush(stdout);
    }

    printf("\n");
    return;
}

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
  for (unsigned int i = 0; i < size; i++) {
    vet[size-1-i] = (valor & 1) != 0;
    valor >>= 1;
  }
}

__device__ void calculateState(bool* state,int num){
  // for (size_t i = 0; i < SIZE; i++) {
  //   printf("\n%d\n", equ[i]);
  // }
  // printf("recive: %d\n", num);
  bool equ[SIZE];
  equ[0] = ( state[0]  | !  state[2] );
  equ[1] = ( state[0]  &   state[2] );
  equ[2] = state[1];

  // bool change = true;
  // // if (c_fix > 0){
  //   for (size_t i = 0; i < c_fix; i++) {
  //     if(num == fix[i]){
  //       change = false;
  //     }
  //   }
  // }
  // if(change){
  //   1+1;
  // }
    state[num] = equ[num];

}

__global__ void initSharedMem(int c_fix, float *d_fix){
  for (unsigned int i = 0; i < c_fix+1; i++) {
    printf("%f\n",d_fix[i] );
  }
}

__global__ void findAttractor()
{

  bool state[SIZE];
  uint idx = blockDim.x * blockIdx.x + threadIdx.x;
  initialState(idx,state,SIZE);

  printf("thread number: %d\n", idx);
  // printf("My number: %d\n", number);
  curandState_t a_number;
  curand_init(idx+clock(), 0, 3,  &a_number);
  unsigned int aleatory = curand(&a_number);
  printf("aleatory %d\n", aleatory%SIZE );


  calculateState(state,aleatory%SIZE);
  __syncthreads();
// //  printf("saida: " );
  printf("%d\n", getDecValue(state));
}

__global__ void setRowReadRow(int *out)
{
    // static shared memory
    __shared__ int tile[3][3];

    // mapping from thread index to global memory index
    // unsigned int idx = threadIdx.y * blockDim.x + threadIdx.x;
    unsigned int idx = blockDim.x * blockIdx.x + threadIdx.x;;

    // shared memory store operation
    tile[threadIdx.y][threadIdx.x] = idx;

    // wait for all threads to complete
    // __syncthreads();

    // shared memory load operation
    out[idx] = tile[threadIdx.y][threadIdx.x];
}

int main(int argc, char **argv)
{
  uint seed = (uint) time(NULL);
  srand(seed);
  uint suffle = rand() %SIZE;
  // std::cout <<   << std::endl;
  size_t numSimu = 1 << 0;
  size_t numState = 1 << 3;
  size_t numBlock =1;
  size_t numThreads =1;
  if (numState > 1024){
    numBlock = numState/1024;
    numThreads = 1024;
  }else{
    numThreads = numState;
  }

  for (size_t i = 0; i < numSimu; i++) {
    findAttractor<<<numBlock, numThreads>>>();
  }
  CHECK(cudaDeviceReset());
  // cudaDeviceReset();

  return 0;
}
