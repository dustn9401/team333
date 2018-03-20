#include "header.h"
char bedroomLED;
extern void
hnd_get_LED(coap_context_t *ctx,
		struct coap_resource_t *resource,
		coap_session_t *session,
		coap_pdu_t *request,
		str* token,
		str* query,
		coap_pdu_t *response);
extern void
hnd_put_LED(coap_context_t *ctx UNUSED_PARAM,
		struct coap_resource_t *resource,
		coap_session_t *session UNUSED_PARAM,
		coap_pdu_t *request,
		str *token UNUSED_PARAM,
		str *query UNUSED_PARAM,
		coap_pdu_t *response);
extern void
init_resources(coap_context_t *ctx);

int main(int argc, char **argv) {
	coap_context_t*  ctx;
	coap_address_t   serv_addr;
	unsigned wait_ms;

	coap_address_init(&serv_addr);
	serv_addr.addr.sin6.sin6_family      = AF_INET6;
	serv_addr.addr.sin6.sin6_addr 	    = in6addr_any;
	serv_addr.addr.sin6.sin6_port        = htons(5683); //default port
	serv_addr.addr.sin6.sin6_scope_id = if_nametoindex("bt0");
	ctx                                 = coap_new_context(&serv_addr);
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
