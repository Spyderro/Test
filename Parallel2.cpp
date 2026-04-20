#include <mpi.h>
#include <stdio.h>

int main(int argc, char** argv) {
    MPI_Init(&argc, &argv);

    int p, id;
    MPI_Comm_size(MPI_COMM_WORLD, &p);
    MPI_Comm_rank(MPI_COMM_WORLD, &id);

    if (p != 12) {
        if (id == 0) printf("Program wymaga dokladnie 12 procesow.\n");
        MPI_Finalize();
        return 0;
    }

    int dims[2] = {4, 3};
    int periods[2] = {1, 0}; 
    int reorder = 1;

    MPI_Comm cart_comm;
    MPI_Cart_create(MPI_COMM_WORLD, 2, dims, periods, reorder, &cart_comm);

    int cart_rank;
    MPI_Comm_rank(cart_comm, &cart_rank);

    int coords[2];
    MPI_Cart_coords(cart_comm, cart_rank, 2, coords);

    int left, right, up, down;
    MPI_Cart_shift(cart_comm, 0, 1, &left, &right);
    MPI_Cart_shift(cart_comm, 1, 1, &up, &down);

    printf("Proces P: %d, moje wspolrzedne to %d %d\n", cart_rank, coords[0], coords[1]);
    printf("Proces P: %d Moi sasiedzi to: prawo %d, lewo %d, gora %d, dol %d\n", 
           cart_rank, right, left, up, down);

    MPI_Comm_free(&cart_comm);
    MPI_Finalize();
    return 0;
}
