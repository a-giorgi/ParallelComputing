cmake_minimum_required(VERSION 3.16)
project(des)

set(CMAKE_CXX_STANDARD 17)

add_executable(des main.cpp des.cpp des.h DesEncryptor.cpp DesEncryptor.h)