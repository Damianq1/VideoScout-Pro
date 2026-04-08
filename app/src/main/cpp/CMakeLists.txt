
cmake_minimum_required(VERSION 3.10)

# WYMUSZENIE POMINIĘCIA TESTÓW
set(CMAKE_C_COMPILER_WORKS 1)
set(CMAKE_CXX_COMPILER_WORKS 1)
set(CMAKE_C_ABI_COMPILED 1)
set(CMAKE_CXX_ABI_COMPILED 1)
project(videoscout)

add_library(videoscout SHARED discovery.cpp)

find_library(log-lib log)
target_link_libraries(videoscout ${log-lib})
