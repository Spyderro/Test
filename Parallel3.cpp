#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>

int main(int argc, char** argv) {
    MPI_Init(&argc, &argv);

    int world_rank, world_size;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);
    MPI_Comm_size(MPI_COMM_WORLD, &world_size);

    if (world_size != 16) {
        if (world_rank == 0) printf("Program wymaga 16 procesow.\n");
        MPI_Finalize();
        return 0;
    }

    int* A = NULL;
    if (world_rank == 0) {
        A = (int*)malloc(16 * sizeof(int));
        srand(time(NULL));
        for (int i = 0; i < 16; i++) {
            A[i] = rand() % 11;
        }
    }

    int b;
    MPI_Scatter(A, 1, MPI_INT, &b, 1, MPI_INT, 0, MPI_COMM_WORLD);

    int dims[2] = {4, 4};
    int periods[2] = {0, 0};
    
    MPI_Comm cart_comm;
    MPI_Cart_create(MPI_COMM_WORLD, 2, dims, periods, 1, &cart_comm);

    int cart_rank, coords[2];
    MPI_Comm_rank(cart_comm, &cart_rank);
    MPI_Cart_coords(cart_comm, cart_rank, 2, coords);

    printf("Id globalne %d id w topologii %d moje wspolrzedne to %d %d kawalek macierzy b = %d\n", 
           world_rank, cart_rank, coords[0], coords[1], b);
           
    MPI_Barrier(cart_comm);

    MPI_Comm col_comm;
    int free_coords[2] = {1, 0}; 
    MPI_Cart_sub(cart_comm, free_coords, &col_comm);

    int col_rank, col_coords[1];
    MPI_Comm_rank(col_comm, &col_rank);
    MPI_Cart_coords(col_comm, col_rank, 1, col_coords);

    int col_sum = 0;
    MPI_Reduce(&b, &col_sum, 1, MPI_INT, MPI_SUM, 0, col_comm);

    if (cart_rank % 4 == 0 && col_rank == 0) {
        printf("Sumowanie w kolumnach: Id globalne %d moje wspolrzedne %d %d moje id w kolumnie %d Suma w mojej kolumnie = %d\n", 
               world_rank, coords[0], coords[1], col_rank, col_sum);
    }

    MPI_Comm row_comm;
    int q = 4;
    int my_row = cart_rank / q;
    
    MPI_Comm_split(cart_comm, my_row, cart_rank, &row_comm);

    int row_rank;
    MPI_Comm_rank(row_comm, &row_rank);

    int row_sum = 0;
    MPI_Reduce(&b, &row_sum, 1, MPI_INT, MPI_SUM, 0, row_comm);

    if (cart_rank % q == 0 && row_rank == 0) {
        printf("Sumowanie w wierszach: Id globalne %d moje wspolrzedne %d %d moje id w wierszu %d suma w moim wierszu = %d\n", 
               world_rank, coords[0], coords[1], row_rank, row_sum);
    }

    MPI_Comm_free(&col_comm);
    MPI_Comm_free(&row_comm);
    MPI_Comm_free(&cart_comm);
    
    if (world_rank == 0) free(A);

    MPI_Finalize();
    return 0;
}
