/*
 * This is sample code generated by rpcgen.
 * These are only templates and you can use them
 * as a guideline for developing your own functions.
 */

#include "xat.h"

char * IO_readKeyboard () {

	char * frase;
	char c;
	int i = 0;

	frase = (char *) malloc(sizeof(char));
	read(0, &c, 1);
	while (c == ' '){
		read (0, &c,1);
	}
	while (c != '\n') {
		frase[i] = c;
		i++;
		frase = (char*)realloc(frase, sizeof(char)*(i + 1));
		read(0, &c, 1);
	}
	frase[i] = '\0';

	return frase;
}

int isMine(char * username, char * message) {

	char username2[50];
	int j = 0;

	while(message[j] != ':') {
		username2[j] = message[j];
		j++;
	}
	username2[j] = '\0';

	return strcmp(username, username2);
}

void * threadLector(void * threadInfo) {

	ThreadInfo threadInfoAux = *((ThreadInfo *) threadInfo);
	Xat * xat;
	char *getchat_1_arg;
	int messagesNum = 0, i;

	while (1) {

		xat = getchat_1((void*)&getchat_1_arg, threadInfoAux.clnt);
		if (xat == (Xat *) NULL) {
			clnt_perror (threadInfoAux.clnt, "call failed");
		}

		for (i = messagesNum; i < xat->Xat_len; i++)
			if (isMine(threadInfoAux.username, xat->Xat_val[i])) printf("%s\n", xat->Xat_val[i]);

		if (messagesNum != xat->Xat_len) printf("\n\n%s, type your message below:\n", threadInfoAux.username);

		messagesNum = xat->Xat_len;

		sleep(1);
	}
}

void
programa_xat_1(char *host, char * username)
{
	CLIENT *clnt;
	void  *result_1;
	Message  write_1_arg;
	Xat  *result_2;
	char * keyboard;
	ThreadInfo threadInfo;
	pthread_t llegirThread;

#ifndef	DEBUG
	clnt = clnt_create (host, PROGRAMA_XAT, VERSION_XAT, "udp");
	if (clnt == NULL) {
		clnt_pcreateerror (host);
		exit (1);
	}
#endif	/* DEBUG */

	threadInfo.clnt = clnt;
	threadInfo.username = username;
	pthread_create(&llegirThread, NULL, threadLector, &threadInfo);

	keyboard = IO_readKeyboard();
	write_1_arg.username = username;
	while (strcmp(keyboard, "EXIT")) {

		write_1_arg.text = keyboard;
		result_1 = write_1(&write_1_arg, clnt);
		if (result_1 == (void *) NULL) {
			clnt_perror (clnt, "call failed");
		}
		keyboard = IO_readKeyboard();
	}
#ifndef	DEBUG
	clnt_destroy (clnt);
#endif	 /* DEBUG */
}


int
main (int argc, char *argv[])
{
	char *host;
	char* username;

	if (argc < 3) {
		printf ("usage: %s server_host username\n", argv[0]);
		exit (1);
	}
	host = argv[1];
	username = argv[2];
	programa_xat_1 (host, username);
	exit (0);
}
