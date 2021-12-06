#include <jni.h>
#include <cstdlib>
#include <string>
#include <pthread.h>
#include <unistd.h>
#include <android/log.h>
#include <tun2socks/tun2socks.h>
#include <pegasocks/pegas.h>

// Start threads to redirect stdout and stderr to logcat.
int pipe_stdout[2];
int pipe_stderr[2];
pthread_t thread_stdout;
pthread_t thread_stderr;
const char *ADBTAG = "native-libs";

void *thread_stderr_func(void *) {
    ssize_t redirect_size;
    char buf[2048];
    while ((redirect_size = read(pipe_stderr[0], buf, sizeof buf - 1)) > 0) {
        //__android_log will add a new line anyway.
        if (buf[redirect_size - 1] == '\n') {
            --redirect_size;
        }
        buf[redirect_size] = 0;
        __android_log_write(ANDROID_LOG_ERROR, ADBTAG, buf);
    }
    return 0;
}

void *thread_stdout_func(void *) {
    ssize_t redirect_size;
    char buf[2048];
    while ((redirect_size = read(pipe_stdout[0], buf, sizeof buf - 1)) > 0) {
        //__android_log will add a new line anyway.
        if (buf[redirect_size - 1] == '\n') {
            --redirect_size;
        }
        buf[redirect_size] = 0;
        __android_log_write(ANDROID_LOG_INFO, ADBTAG, buf);
    }
    return 0;
}

int start_redirecting_stdout_stderr() {
    //set stdout as unbuffered.
    setvbuf(stdout, 0, _IONBF, 0);
    pipe(pipe_stdout);
    dup2(pipe_stdout[1], STDOUT_FILENO);

    //set stderr as unbuffered.
    setvbuf(stderr, 0, _IONBF, 0);
    pipe(pipe_stderr);
    dup2(pipe_stderr[1], STDERR_FILENO);

    if (pthread_create(&thread_stdout, 0, thread_stdout_func, 0) == -1) {
        return -1;
    }
    pthread_detach(thread_stdout);

    if (pthread_create(&thread_stderr, 0, thread_stderr_func, 0) == -1) {
        return -1;
    }
    pthread_detach(thread_stderr);

    return 0;
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_hexyoungs_pegasocks_NativeLibs_getPegasVersion(JNIEnv *env, jobject thiz) {
    char ver[32] = "";
    pgs_get_version(ver);
    jstring result = env->NewStringUTF(ver); // C style string to Java String
    return result;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_hexyoungs_pegasocks_NativeLibs_getTun2SocksVersion(JNIEnv *env, jobject thiz) {
    char ver[32] = "";
    tun2socks_get_version(ver);
    jstring result = env->NewStringUTF(ver); // C style string to Java String
    return result;
}