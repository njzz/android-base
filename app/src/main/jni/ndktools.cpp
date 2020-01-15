//
// Created by Administrator on 2019/10/16.
//



#include <unistd.h>
#include <csignal>
#include <cstdlib>
#include <sys/resource.h>
#include <cstdio>
#include <cerrno>
#include <sys/stat.h>
#include <sys/wait.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include<arpa/inet.h>
#include <pthread.h>
#include <cstring>
#include "com_librarys_tools_NDKTools.h"

#define MAX_PROCESS_NAME    128
#define MAIN_SOCKET_PORT    62111
#define HELP_SOCKET_PORT    62112
char pname[MAX_PROCESS_NAME];

void ExecuteCommandWithPopen(char *command, char *out_result,
                             int resultBufferSize) {
    out_result[resultBufferSize - 1]='\0';
    FILE *fp = popen(command, "r");
    if (fp) {
        fgets(out_result, resultBufferSize - 1, fp);
        out_result[resultBufferSize - 1] = '\0';
        pclose(fp);
    } else {
        exit(0);
    }
}

bool checkMain(){
    int   sockfd, n;
    char  recvline[10];
    struct sockaddr_in  socketaddr;

    if( (sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0){
        return false;
    }

    memset(&socketaddr, 0, sizeof(socketaddr));
    socketaddr.sin_family = AF_INET;
    socketaddr.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
    socketaddr.sin_port = htons(HELP_SOCKET_PORT);

    if( bind(sockfd, (struct sockaddr*)&socketaddr, sizeof(socketaddr)) == -1){
        exit(0);//已经存在守护进程了。
    }

    //服务器端口
    socketaddr.sin_port = htons(MAIN_SOCKET_PORT);
    if( connect(sockfd, (struct sockaddr*)&socketaddr, sizeof(socketaddr)) < 0){
        return true;//连接不上
    }

    recv(sockfd, recvline, 10, 0);//会阻塞，因为不会发送任何东西

    return true;
}

void check_and_restart_service() {
    char cmdline[200];
    checkMain();//连接
    sprintf(cmdline, "am startservice –user 0 -n %s", pname);
    char tmp[200];
    sprintf(tmp, "cmd=%s", cmdline);
    ExecuteCommandWithPopen(cmdline, tmp, 200);
}

void goLoop() {
    int testCount=0;
    while(1){
        check_and_restart_service(); // 应该要去判断service状态，这里一直restart 是不足之处
        if(++testCount==999999){
            exit(0);
        }
    }
}

int start() {
    struct rlimit r;
    int pid = fork();
    if (pid < 0) {
        exit(0);
    } else if (pid != 0) {
        //exit(0);
    } else { //  第一个子进程
        setsid();
        umask(0); //为文件赋予更多的权限，因为继承来的文件可能某些权限被屏蔽

        pid = fork();
        if (pid == 0) { // 第二个子进程
            // 这里实际上为了防止重复开启线程，应该要有相应处理
            chdir("/"); //修改进程工作目录为根目录
            //关闭不需要的从父进程继承过来的文件描述符。
            if (r.rlim_max == RLIM_INFINITY) {
                r.rlim_max = 1024;
            }
            int i;
            for (i = 0; i < r.rlim_max; i++) {
                close(i);
            }
            umask(0);
            //signal(SIGPIPE,SIG_IGN);
            signal(SIGINT,SIG_IGN);
            signal(SIGTERM,SIG_IGN);
//            chmod("/proc/self/cmdline", S_IRUSR|S_IWUSR| S_IRGRP| S_IROTH);
//            FILE *f =fopen("/proc/self/cmdline","rb+");
//            if(f!=NULL){
//                fwrite("a.out.",6,1,f);
//                fclose(f);
//            }
            goLoop();

        } else {
            exit(0);
        }
    }
    return 0;
}

void *ThreadMain(void *fd){

    int listenfd=(int)(long long)(int*)fd,connfd;
    char rev[10];
    while(1){
        if( (connfd = accept(listenfd, (struct sockaddr*)NULL, NULL)) == -1){
            continue;
        }
        int n = recv(connfd, rev, 10, 0);//会阻塞，因为他们之间不会发送任何东西

        if(n==5) break;
        close(connfd);
    }
    close(listenfd);
    return NULL;
}

bool bindServerRun(){
    int  listenfd;
    struct sockaddr_in  servaddr;
    char  buff[4096];

    if( (listenfd = socket(AF_INET, SOCK_STREAM | SOCK_CLOEXEC, 0)) == -1 ){
        return false;
    }

    memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(MAIN_SOCKET_PORT);

    if( bind(listenfd, (struct sockaddr*)&servaddr, sizeof(servaddr)) == -1){
        return false;
    }

    if( listen(listenfd, 2) == -1){
        return false;
    }

    pthread_t tid;
    pthread_create(&tid,NULL,ThreadMain,(void*)listenfd);//主进程的socket

    return true;
}

JNIEXPORT jstring JNICALL Java_com_librarys_tools_NDKTools_getStringFromNDK
  (JNIEnv *env, jclass,jstring str1,jstring str2){

    //printf("Java_com_yyh_fork_NativeRuntime_startService run….ProcessName:%s", rtn);

    //start(1, rtn, sd);

    const char *pStr1=env->GetStringUTFChars(str1,NULL);
    const char *pStr2=env->GetStringUTFChars(str2,NULL);

    memset(pname,0,MAX_PROCESS_NAME);
    strncpy(pname,pStr1,strlen(pStr1));
    ////
    env->ReleaseStringUTFChars(str1,pStr1);
    env->ReleaseStringUTFChars(str2,pStr2);

    bool b=bindServerRun();//绑定主进程存活，以便被检测
    start();//开启检查进程
    return env->NewStringUTF(b?"success":strerror(errno));
  }