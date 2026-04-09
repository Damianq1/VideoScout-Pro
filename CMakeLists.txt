cmake_minimum_required(VERSION 3.10.2)
project("videoscout")

# Konfiguracja biblioteki głównej
add_library(videoscout SHARED
            app/src/main/cpp/discovery.cpp)

# Szukanie biblioteki logowania Androida
find_library(log-lib log)

# Łączenie zależności
target_link_libraries(videoscout ${log-lib})
