CC=gcc
CFLAGS=
OBJS=main.o handler.o
LIBS=-lcoap-1
all:server

server:$(OBJS)
	$(CC) $(CFLAGS) -o server $(OBJS) $(LIBS)

main.o:main.c
	$(CC) $(CFLAGS) -c main.c

handler.o:handler.c
	$(CC) $(CFLAGS) -c handler.c

clean:
	rm -f $(OBJS) server core main.o handler.o
