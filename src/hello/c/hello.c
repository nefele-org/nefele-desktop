#include <jni.h>
#include <stdio.h>


JNIEXPORT void JNICALL Java_Program_print(JNIEnv* env, jobject obj) {
    printf("Hello World\n");
}