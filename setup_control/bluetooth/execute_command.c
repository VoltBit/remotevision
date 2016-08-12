#include "execute_command.h"

void write_data (FILE * stream)
{
//	int i;

	// for (i = 0; i < 100; i++)
	// 	fprintf (stream, "%d\n", i);
	// if (ferror (stream)) {
	// 	fprintf (stderr, "Output to stream failed.\n");
	// 	exit (EXIT_FAILURE);
	// }
	
}

FILE *execute_command(char *command)
{
	FILE *output;

	if (command == NULL) {
		perror("command is NULL");
		exit(EXIT_FAILURE);
	} else if (strlen(command) == 0) {
		perror("invalid command");
		exit(EXIT_FAILURE);
	}

	output = popen (command, "r");
	if (!output) {
		fprintf(stderr, "incorrect parameters or too many files.\n");
		exit(EXIT_FAILURE);
	}

	// write_data (output);
	// if (pclose (output) != 0) {
	// 	fprintf (stderr, "Could not run more or other error.\n");
	// }
	return output;
	// return EXIT_SUCCESS;
}

// int main(void)
// {
	/*
	hciconfig -a + get devid
	hciconfig hci0 up
	hciconfig hci0 piscan
	*/
	// execute_command("hciconfig -a");
	// execute_command()

// 	return 0;
// }
