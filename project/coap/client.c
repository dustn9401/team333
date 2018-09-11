#include "header.h"

int main(int argc, char **argv) {
	coap_context_t*  ctx;
	coap_address_t   serv_addr;
	unsigned wait_ms;

	coap_address_init(&serv_addr);
	serv_addr.addr.sin6.sin6_family      = AF_INET6;
	//serv_addr.addr.sin6.sin6_addr 	     = in6addr_any;
	inet_pton(AF_INET6, argv[1], serv_addr.addr.sin6.sin6_addr);
	serv_addr.addr.sin6.sin6_port        = htons(5683); //default port
	serv_addr.addr.sin6.sin6_scope_id    = if_nametoindex("wlan0");
	ctx                                  = coap_new_context(&serv_addr);
	if (!ctx) exit(EXIT_FAILURE);
	init_resources(ctx);

	wait_ms = COAP_RESOURCE_CHECK_TIME * 1000;

	while (1) {
		int result = coap_run_once(ctx, wait_ms);
		if ( result < 0 ){
			break;
		}
		else if((unsigned)result < wait_ms){
			wait_ms -= result;
		}
		else{
			wait_ms = COAP_RESOURCE_CHECK_TIME * 1000;
		}
	}    

	coap_free_context(ctx);
	coap_cleanup();

	return 0;
}
