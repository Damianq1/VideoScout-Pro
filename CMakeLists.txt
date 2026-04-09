cmake_minimum_required(VERSION 3.10.2)
project("videoscout")

# Ścieżka musi prowadzić do pliku wewnątrz folderu app
add_library(videoscout SHARED
            app/src/main/cpp/discovery.cpp)

find_library(log-lib log)

target_link_libraries(videoscout ${log-lib})
