#include <cstdio> // Zmieniono iostream na cstdio
#include <algorithm>
#include <ctime>
#include <omp.h>

using namespace std;

#define N 100000
#define X 200
#define CUTOFF 1000 

void quicksort(int *, int, int);
int partition(int *, int, int);
int choosePivot(int *, int, int);

int main(int argc, char ** argv) {
  srand(time(NULL));
  int * arr = new int[N];

  for (int i = 0; i < N; i++) arr[i] = rand()%X;

  printf("Rozpoczynam sortowanie...\n");

  double start_time = omp_get_wtime();

  #pragma omp parallel
  {
      #pragma omp single nowait
      {
          quicksort(arr, 0, N-1);
      }
  }

  double end_time = omp_get_wtime();
  printf("Czas sortowania (OpenMP): %f s\n", end_time - start_time);

  delete[] arr;
  return 0;
}

void quicksort(int * arr, int lo, int hi) {
  if(lo < hi) {
    int p = partition(arr, lo, hi);
    
    if (hi - lo < CUTOFF) {
        quicksort(arr, lo, p - 1);
        quicksort(arr, p + 1, hi);
    } else {
        #pragma omp task shared(arr)
        quicksort(arr, lo, p - 1);
        
        #pragma omp task shared(arr)
        quicksort(arr, p + 1, hi);
        
        #pragma omp taskwait
    }
  }
}

int partition(int * arr, int lo, int hi) {
  int pivotIdx = choosePivot(arr, lo, hi);
  int pivotVal = arr[pivotIdx];
  swap(arr[pivotIdx], arr[hi]);

  int storeIdx = lo;
  for(int i = lo; i < hi; i++) {
    if(arr[i] < pivotVal) {
      swap(arr[i], arr[storeIdx]);
      storeIdx++;
    }
  }

  swap(arr[storeIdx], arr[hi]);
  return storeIdx;
}

int choosePivot(int * arr, int lo, int hi) {
  int mid = (lo+hi)/2;
  if(arr[lo] > arr[hi]) swap(arr[lo], arr[hi]);
  if(arr[mid] < arr[lo]) swap(arr[mid], arr[lo]);
  if(arr[hi] < arr[mid]) swap(arr[mid], arr[hi]);
  return mid;
}
