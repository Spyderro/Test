#include <mpi.h>
#include <stdio.h>
#include <math.h>

int main(int argc, char** argv) {
    MPI_Init(&argc, &argv);

    int p, id;
    MPI_Comm_size(MPI_COMM_WORLD, &p);
    MPI_Comm_rank(MPI_COMM_WORLD, &id);

    int q = (int)sqrt(p);
    
    int row_color = id / q;

    MPI_Comm new_comm;
    MPI_Comm_split(MPI_COMM_WORLD, row_color, id, &new_comm);

    int new_id;
    MPI_Comm_rank(new_comm, &new_id);

    int test = 0;
    if (new_id == 0) {
        test = row_color;
    }

    MPI_Bcast(&test, 1, MPI_INT, 0, new_comm);

    printf("Global id: %2d, wiersz: %d, id w nowym komunikatorze: %d, zmienna test: %d\n", 
           id, row_color, new_id, test);

    MPI_Comm_free(&new_comm);
    MPI_Finalize();
    return 0;
}
