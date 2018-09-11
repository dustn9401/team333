#include "header.h"
char bedroomLED=OFF;
void
hnd_get_LED(coap_context_t *ctx,
		struct coap_resource_t *resource,
		coap_session_t *session,
		coap_pdu_t *request,
		str* token,
		str* query,
		coap_pdu_t *response){
	unsigned char buf[40];
	const char* response_data = bedroomLED ? "ON" : "OFF";
	response->code = COAP_RESPONSE_CODE(205);
	coap_add_option(response, COAP_OPTION_CONTENT_TYPE, coap_encode_var_bytes(buf, COAP_MEDIATYPE_TEXT_PLAIN), buf);
	coap_add_data(response, strlen(response_data), (unsigned char *)response_data);
	printf("Send data : %s\n", response_data);
}
void
hnd_put_LED(coap_context_t *ctx UNUSED_PARAM,
		struct coap_resource_t *resource,
		coap_session_t *session UNUSED_PARAM,
		coap_pdu_t *request,
		str *token UNUSED_PARAM,
		str *query UNUSED_PARAM,
		coap_pdu_t *response){
	size_t size;
	unsigned char *data;
	char buf[40];

	response->code = COAP_RESPONSE_CODE(204);
	coap_get_data(request, &size, &data);
	strcpy(buf, data);
	buf[size]=0;

	if(size == 0)
		printf("put error\n");
	else 
		printf("Message from client : %s\n", buf);
	
	if(strcmp(buf, "off")==0 || strcmp(buf, "OFF")==0)
		bedroomLED = OFF;
	if(strcmp(buf, "on")==0 || strcmp(buf, "ON")==0)
		bedroomLED = ON;
}

void
init_resources(coap_context_t *ctx) {
	coap_resource_t *r;

	r = coap_resource_init((unsigned char *)"LED", 3, 0);
	coap_register_handler(r, COAP_REQUEST_GET, hnd_get_LED);
	coap_register_handler(r, COAP_REQUEST_PUT, hnd_put_LED);

	coap_add_attr(r, (unsigned char *)"bedroom", 7, (unsigned char *)"0", 1, 0);
	coap_add_resource(ctx, r);
}

