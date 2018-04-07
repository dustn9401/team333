CC=gcc
CFLAGS=
OBJS=main.o handler.o
LIBS=-lcoap-1 -L/usr/local/lib
INCLUDE=-I /usr/local/include/coap/
all:server

server:$(OBJS)
	$(CC) $(CFLAGS) -o server $(OBJS) $(INCLUDE) $(LIBS)

main.o:main.c
	$(CC) $(CFLAGS) -c main.c

handler.o:handler.c
	$(CC) $(CFLAGS) -c handler.c

clean:
	rm -f $(OBJS) server core main.o handler.o
