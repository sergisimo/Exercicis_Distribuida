/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#ifndef _XAT_H_RPCGEN
#define _XAT_H_RPCGEN

#include <rpc/rpc.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>

#ifdef __cplusplus
extern "C" {
#endif


typedef char *Word;

typedef struct {
	u_int Xat_len;
	Word *Xat_val;
} Xat;

struct Message {
	Word username;
	Word text;
};

typedef struct Message Message;

typedef struct {
	CLIENT * clnt;
	char * username;
} ThreadInfo;

#define PROGRAMA_XAT 0x20000001
#define VERSION_XAT 1

#if defined(__STDC__) || defined(__cplusplus)
#define write 1
extern  void * write_1(Message *, CLIENT *);
extern  void * write_1_svc(Message *, struct svc_req *);
#define getChat 2
extern  Xat * getchat_1(void *, CLIENT *);
extern  Xat * getchat_1_svc(void *, struct svc_req *);
extern int programa_xat_1_freeresult (SVCXPRT *, xdrproc_t, caddr_t);

#else /* K&R C */
#define write 1
extern  void * write_1();
extern  void * write_1_svc();
#define getChat 2
extern  Xat * getchat_1();
extern  Xat * getchat_1_svc();
extern int programa_xat_1_freeresult ();
#endif /* K&R C */

/* the xdr functions */

#if defined(__STDC__) || defined(__cplusplus)
extern  bool_t xdr_Word (XDR *, Word*);
extern  bool_t xdr_Xat (XDR *, Xat*);
extern  bool_t xdr_Message (XDR *, Message*);

#else /* K&R C */
extern bool_t xdr_Word ();
extern bool_t xdr_Xat ();
extern bool_t xdr_Message ();

#endif /* K&R C */

#ifdef __cplusplus
}
#endif

#endif /* !_XAT_H_RPCGEN */
