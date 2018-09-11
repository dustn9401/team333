#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <net/if.h>
#include <pthread.h>
#define BUF_SIZE 256

char client_buf[BUF_SIZE];
char server_buf[BUF_SIZE];
struct sockaddr_in6 svrsin6, clisin6;
int client_socket, server_socket, len;

void *write_to_client();
void *read_from_client();
void *write_to_server();
void *read_from_server();

void *write_to_client()
{
	while (1) {
		if (server_buf[0]) {
			sendto(server_socket, server_buf, BUF_SIZE, 0, (struct sockaddr *)&svrsin6, sizeof(svrsin6));
			printf("write to client : %s\n", server_buf);
			memset(server_buf, 0, BUF_SIZE);
		}
	}
	exit(0);
}
void *read_from_client()
{
	while (1) {
		recvfrom(server_socket, client_buf, BUF_SIZE, 0, (struct sockaddr *)&svrsin6, &len);
		printf("read from client : %s\n", client_buf);
	}
	exit(0);
}
void *write_to_server()
{
	while (1) {
		if (client_buf[0]) {
			sendto(client_socket, client_buf, BUF_SIZE, 0, (struct sockaddr *)&clisin6, sizeof(clisin6));
			printf("write to server : %s\n", client_buf);
			memset(client_buf, 0, BUF_SIZE);
		}
	}
	exit(0);
}
void *read_from_server()
{
	while (1) {
		recvfrom(client_socket, server_buf, BUF_SIZE, 0, (struct sockaddr *)&clisin6, &len);
		printf("read from server : %s\n", server_buf);
	}
	exit(0);
}
int main(int argc, char **argv)
{
	if (argc < 3) {
		printf("Usage : %s <ip> <port>\n", argv[0]);
		exit(1);
	}
	char ipv6_addr[16];
	char addr6_str[40];


	client_socket = socket(PF_INET6, SOCK_DGRAM, 0);
	if (client_socket < 0)
	{
		perror("write socket create error:");
		exit(0);
	}
	memset(&clisin6, 0, sizeof(svrsin6));
	clisin6.sin6_family = AF_INET6;
	clisin6.sin6_flowinfo = 0;
	clisin6.sin6_port = htons(atoi(argv[2]));
	clisin6.sin6_scope_id = if_nametoindex("bt0");

	inet_pton(AF_INET6, argv[1], (void *)&ipv6_addr);
	memcpy((void *)&clisin6.sin6_addr, (void *)&ipv6_addr, 16);
	inet_ntop(AF_INET6, (void *)&clisin6.sin6_addr, addr6_str, 40);


	len = sizeof(svrsin6);
	server_socket = socket(PF_INET6, SOCK_DGRAM, 0);
	if (server_socket < 0)
	{
		perror("read socket create error:");
		exit(0);
	}
	memset(&svrsin6, 0, sizeof(svrsin6));
	svrsin6.sin6_family = AF_INET6;
	svrsin6.sin6_flowinfo = 0;
	svrsin6.sin6_port = htons(atoi(argv[2]));
	svrsin6.sin6_scope_id = if_nametoindex("wlan0");
	svrsin6.sin6_addr = in6addr_any;

	if (bind(server_socket, (struct sockaddr *)&svrsin6, sizeof(svrsin6)) < 0) {
		perror("bind");
		exit(1);
	}

	pthread_t rcthread, wcthread, rsthread, wsthread;
	pthread_create(&rcthread, NULL, read_from_client, NULL);
	pthread_create(&rsthread, NULL, read_from_server, NULL);
	pthread_create(&wcthread, NULL, write_to_client, NULL);
	pthread_create(&wsthread, NULL, write_to_server, NULL);
	pthread_exit(NULL);

	close(client_socket);
	close(server_socket);
	return 0;
}
