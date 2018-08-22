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

  bool equ[SIZE];
  equ[0] = ( state[0]  | !  state[2] );
  equ[1] = ( state[0]  &   state[2] );
  equ[2] = state[1];

    state[num] = equ[num];

}

__global__ void findAttractor(int num, int * fix)
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
  //  printf("saida: " );
  printf("%d\n", getDecValue(state));
}
__global__ void findAttractor(){

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
  //  printf("saida: " );
  printf("%d\n", getDecValue(state));
}

__global__ void setRowReadRow(int *out){
    // static shared memory
    __shared__ int tile[3][3];

    // mapping from thread index to global memory index
    // unsigned int idx = threadIdx.y * blockDim.x + threadIdx.x;
    unsigned int idx = blockDim.x * blockIdx.x + threadIdx.x;

    // shared memory store operation
    tile[threadIdx.y][threadIdx.x] = idx;
    printf("%d\n", tile[threadIdx.y][threadIdx.x]);

    // wait for all threads to complete
    // __syncthreads();

    // shared memory load operation
    out[idx] = tile[threadIdx.y][threadIdx.x];
}

__global__ void testeNum(int num, int * fix){
  for (size_t i = 0; i < num; i++) {
    printf("%d\n",fix[i] );
  }
}

__global__ void testeNum1(){

    printf("%d\n",4 );

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

  // for (size_t i = 0; i < numSimu; i++) {
  //   findAttractor<<<numBlock, numThreads>>>();
  // }
  int numFix=0;
  if(argc > 1){
    numFix= atoi(argv[1]);
    int nBytes = numFix * sizeof(int);
    int *d_C;
    CHECK(cudaMalloc((int**)&d_C, nBytes));
    int *gpuRef  = (int *)malloc(nBytes);
    int count =2;
    for (size_t i = 0; i < numFix; i++) {
      gpuRef[i] =atoi(argv[count++]);
    }
    CHECK(cudaMemcpy(d_C, gpuRef, nBytes, cudaMemcpyHostToDevice));
    // testeNum<<<1, 3>>>(numFix,d_C);
    for (size_t i = 0; i < numSimu; i++) {
      findAttractor<<<numBlock, numThreads>>>(numFix,d_C);
    }
  }else{
    // testeNum1<<<1, 3>>>();
    for (size_t i = 0; i < numSimu; i++) {
      findAttractor<<<numBlock, numThreads>>>();
    }
  }





  //CHECK(cudaMemset(d_C, 0, nBytes));

   // gpuRef[0]=1;
   // gpuRef[1]=2;
   // gpuRef[2]=0;
   // CHECK(cudaMemcpy(d_C, gpuRef, nBytes, cudaMemcpyHostToDevice));
   //
   // testeNum<<<1, 3>>>(numFix,d_C);
   //CHECK(cudaMemcpy(gpuRef, d_C, nBytes, cudaMemcpyDeviceToHost));
   // printData("setRowReadRow       ", gpuRef, 3);


  CHECK(cudaDeviceReset());
  // cudaDeviceReset();

  return 0;
}
