set(CMAKE_C_COMPILER_WORKS 1)
set(CMAKE_CXX_COMPILER_WORKS 1)
cmake_minimum_required(VERSION 3.10)
project("videoscout")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -nostdlib++")
include_directories(/data/data/com.tom.rv2ide/files/usr/include/c++/v1)
link_directories(/data/data/com.tom.rv2ide/files/usr/lib)

add_library(videoscout SHARED
    app/src/main/cpp/discovery.cpp
)

set(log-lib "${CMAKE_SOURCE_DIR}/app/src/main/jniLibs/arm64-v8a/liblog.so")
target_link_libraries(videoscout ${log-lib})
