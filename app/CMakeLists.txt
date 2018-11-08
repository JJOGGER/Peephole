cmake_minimum_required(VERSION 3.4.1)
add_library( # Sets the name of the library.
# 设置so文件名称.
peephole_jni
SHARED
src/main/jni/dspatch.c)

find_library(
log-lib
log)
target_link_libraries(
peephole_jni
${log-lib} )