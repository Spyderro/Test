#include <cstdio> // Zmieniono iostream na cstdio
#include <algorithm>
#include <ctime>
#include <mpi.h>

using namespace std;

#define N 100000 
#define X 200

void quicksort(int *, int, int);
int partition(int *, int, int);
int choosePivot(int *, int, int);

void mergeBlokow(int* docelowa, int* zrodlowa, int rozmiar, int liczbaProcesow) {
    std::copy(zrodlowa, zrodlowa + rozmiar, docelowa);
    std::sort(docelowa, docelowa + rozmiar); 
}

int main(int argc, char ** argv) {
    int rank, size;
    
    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    int *global_arr = nullptr;
    int local_size = N / size;
    int *local_arr = new int[local_size];

    if (rank == 0) {
        global_arr = new int[N];
        srand(time(NULL));
        for (int i = 0; i < N; i++) global_arr[i] = rand() % X;
    }

    MPI_Scatter(global_arr, local_size, MPI_INT, 
                local_arr, local_size, MPI_INT, 
                0, MPI_COMM_WORLD);

    quicksort(local_arr, 0, local_size - 1);

    int *gathered_arr = nullptr;
    if (rank == 0) {
        gathered_arr = new int[N];
    }

    MPI_Gather(local_arr, local_size, MPI_INT, 
               gathered_arr, local_size, MPI_INT, 
               0, MPI_COMM_WORLD);

    if (rank == 0) {
        mergeBlokow(global_arr, gathered_arr, N, size);
        printf("Sortowanie MPI zakonczone sukcesem.\n"); // Użycie printf
        
        delete[] global_arr;
        delete[] gathered_arr;
    }

    delete[] local_arr;
    MPI_Finalize();
    return 0;
}

void quicksort(int * arr, int lo, int hi) {
  if(lo < hi) {
    int p = partition(arr, lo, hi);
    quicksort(arr, lo, p - 1);
    quicksort(arr, p + 1, hi);
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
