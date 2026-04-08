cmake_minimum_required(VERSION 3.10.2)
project("videoscout")

# Dodajemy bibliotekę z Twojego pliku discovery.cpp
add_library(videoscout SHARED
            app/src/main/cpp/discovery.cpp)

# Szukamy standardowej biblioteki logowania Androida
find_library(log-lib log)

# Łączymy wszystko. NIE dodajemy żadnych flag -nostdlib!
target_link_libraries(videoscout
                      ${log-lib})
