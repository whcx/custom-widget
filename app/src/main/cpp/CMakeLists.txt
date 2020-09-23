
cmake_minimum_required(VERSION 3.4.1)

aux_source_directory(. SOURCE)

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${SOURCE} )

find_library( # Sets the name of the path variable.
              log-lib
              log )

target_include_directories(native-lib PUBLIC ${CMAKE_SOURCE_DIR}/include)

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )