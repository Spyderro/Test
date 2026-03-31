#include <mpi.h>
#include <iostream>
#include <stdlib.h>
#include <time.h>

using namespace std;

// Funkcja scalająca dwie posortowane tablice na surowych wskaźnikach
int* merge(int* v1, int n1, int* v2, int n2) {
    int* res = new int[n1 + n2];
    int i = 0, j = 0, k = 0;
    
    while (i < n1 && j < n2) {
        if (v1[i] <= v2[j]) {
            res[k++] = v1[i++];
        } else {
            res[k++] = v2[j++];
        }
    }
    while (i < n1) res[k++] = v1[i++];
    while (j < n2) res[k++] = v2[j++];
    
    return res;
}

int main(int argc, char** argv) {
    MPI_Init(&argc, &argv);

    int rank, size;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    int N = 1000000; // Rozmiar całkowity tablicy (do Zadania 2 zmień na 1 000 000)
    int n_local = N / size; // Zakładamy, że N dzieli się bez reszty przez liczbę procesów
    int* chunk = new int[n_local];

    // 1. Inicjalizacja tablicy losowymi liczbami
    srand(time(NULL) + rank);
    for (int i = 0; i < n_local; i++) {
        chunk[i] = rand() % 1000;
    }

    // Sortowanie bąbelkowe lokalnego kawałka (Twoja wersja bez algorytmów z STL)
    for (int i = n_local - 2; i >= 0; i--) {
        for (int j = 0; j <= i; j++) {
            if (chunk[j] > chunk[j + 1]) {
                int temp = chunk[j];
                chunk[j] = chunk[j + 1];
                chunk[j + 1] = temp;
            }
        }
    }

    // Start pomiaru czasu (po wygenerowaniu i początkowym posortowaniu, 
    // lub przed - zależnie od interpretacji instrukcji prowadzącego)
    double start = MPI_Wtime();

    // 2. Równoległe scalanie w strukturze drzewa
    int krok = 1;
    int current_n = n_local;

    while (krok < size) {
        // Warunek 1: Odbijamy procesy, które wysyłają
        if (rank % (2 * krok) != 0) {
            int target = rank - krok;
            MPI_Send(&current_n, 1, MPI_INT, target, 0, MPI_COMM_WORLD);
            MPI_Send(chunk, current_n, MPI_INT, target, 0, MPI_COMM_WORLD);
            break; // Po wysłaniu danych proces wychodzi z pętli
        } 
        // Warunek 2: Proces o mniejszej randze odbiera i scala (jeśli ma od kogo)
        else if (rank + krok < size) {
            int remote_n;
            MPI_Recv(&remote_n, 1, MPI_INT, rank + krok, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
            
            int* remote_chunk = new int[remote_n];
            MPI_Recv(remote_chunk, remote_n, MPI_INT, rank + krok, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

            // Wywołanie funkcji scalającej
            int* merged = merge(chunk, current_n, remote_chunk, remote_n);
            
            // Czyszczenie starej pamięci, żeby uniknąć wycieków
            delete[] chunk;
            delete[] remote_chunk;
            
            // Podpięcie nowej, większej tablicy pod zmienną chunk
            chunk = merged;
            current_n += remote_n;
        }
        krok *= 2; // Zgodnie z instrukcją zmienna krok podwaja się
    }

    double end = MPI_Wtime();

    // 3. Wypisanie wyników przez proces root
    if (rank == 0) {
        cout << "Wielkosc odebranej tablicy: " << current_n << endl;
        cout << "Liczba procesow: " << size << endl;
        cout << "Czas obliczeniowy: " << end - start << " s" << endl;
        
        // Zgodnie z instrukcją wypisywanie zawartości (dla N=1000000 lepiej to zakomentować)
        // cout << "Zawartosc tablicy: " << endl;
        // for(int i=0; i<current_n; i++) cout << chunk[i] << " ";
        // cout << endl;
    }

    // Zwolnienie ostatecznej pamięci
    delete[] chunk;
    
    MPI_Finalize();
    return 0;
}