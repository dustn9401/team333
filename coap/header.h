#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include <net/if.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <errno.h>
#include <signal.h>
#include <unistd.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <dirent.h>
#include <coap/coap.h>
#include <coap/coap_dtls.h>
#include <sys/wait.h>
#ifdef __GNUC__
#define UNUSED_PARAM __attribute__ ((unused))
#else
#define UNUSED_PARAM
#endif
#define BUF 255
#define ON 1
#define OFF 0
