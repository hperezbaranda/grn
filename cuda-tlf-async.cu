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

  bool state[SIZE];
  uint idx = blockDim.x * blockIdx.x + threadIdx.x;
  initialState(idx,state,SIZE);

  printf("Hello World from GPU! number: %d\n", idx);
  printf("My number: %d\n", number);
  curandState_t a_number;
  curand_init(idx+clock(), 0, 3,  &a_number);
  unsigned int aleatory = curand(&a_number);
  printf("aleatory %d\n", aleatory%SIZE );

  calculateState(state,number);
  // __syncthreads();
//  printf("saida: " );
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
  size_t numSimu = 1 << 20;
  size_t numState = 1 << 3;
  size_t numBlock =0;
  if (numState > 1024){
    numBlock = numState/1024;
  }else{
    numBlock = numState;
  }


  size_t numThreads =1024;
  //initSharedMem<<<1,1>>>();
  findAttractor<<<numBlock, numThreads>>>(suffle);

  // int thread = 64;
  // int block = 20;
  // size_t nBytes = thread * sizeof(int)*block;
  // int *d_C;
  // CHECK(cudaMalloc((int**)&d_C, nBytes));
  // int *gpuRef  = (int *)malloc(nBytes);
  //
  // CHECK(cudaMemset(d_C, 0, nBytes));
  // setRowReadRow<<<block, thread>>>(d_C);
  // CHECK(cudaMemcpy(gpuRef, d_C, nBytes, cudaMemcpyDeviceToHost));
  //
  // printData("set row read row   ", gpuRef, thread*block);


  cout << endl;
  // CHECK(cudaFree(d_equation));
  CHECK(cudaDeviceReset());
  // cudaDeviceReset();

  return 0;
}
