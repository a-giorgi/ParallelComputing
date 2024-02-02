# Parallel Computing Experiments
This repository contains two projects conducted to explore parallel computing technologies, specifically focusing on:

- Java threads
- OpenMP
- CUDA


## Multithread Image Reader

The **Multithread Image Reader** is a Java-based software application. In order to run this software you need the Java Runtime Environment, which can be obtained from [here](https://www.java.com/it/download/).

The development of this software was made using IntelliJ IDEA Community Edition, available [here](https://www.jetbrains.com/idea/).

The primary objective of this software is to conduct a performance comparison between sequential and parallel image loading methodologies. For detailed information regarding performance analysis and development, refer to the paper "Multithread Image Reader with Java Threads.pdf".



## DES Password Decryption

The **DES Password Decryption** software includes both sequential and parallel versions in C++. For the latter, it uses CUDA and OpenMP technologies.

The main purpose of this software is to compare the performances between the sequentially and CPU/GPU parallel DES Password Decryption using a brute force approach. Detailed information about performances and development can be found inside the paper "Des Password Decryption.pdf"
