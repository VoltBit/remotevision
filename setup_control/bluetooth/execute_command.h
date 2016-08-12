#ifndef EXEC_COMM_H_
#define EXEC_COMM_H_

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>

void write_data (FILE * stream);
FILE *execute_command(char *command);

#endif
