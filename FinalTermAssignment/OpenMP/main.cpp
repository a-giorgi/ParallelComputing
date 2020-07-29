//
// Created by Edoardo Cagnes on 17/06/20.
//

#include "DesEncryptor.h"
#include <iostream>
#include <string>
#include <stdio.h>
#include <math.h>
#include <algorithm>
#include <omp.h>
#include <unistd.h>

using namespace std;

int main(int argc, char *argv[]) {
    //snippet to allow dynamic environment variable set up
    char *hasCancel = getenv("OMP_CANCELLATION");
    if (hasCancel == nullptr) {
        printf("Setting up environment variable to allow for cancellation...");
        setenv("OMP_CANCELLATION", "true", 1);
        // Restart the program here
        int output = execvp(argv[0], argv);
        // Execution should not continue past here
        printf("This process failed with code %d\n",output);
        exit(1);
    } else {
        puts("Environment variable successfully set");
    }

    //key
    string key = "AkeyZT31";
    //Converting key to hex
    string hkey = DesEncryptor::string2hex(key);
    //cout << "Hexadecimal key; " << hkey << endl;

    string plaintext = "aaaQoKqs";
    plaintext = DesEncryptor::string2hex(plaintext);

    DesEncryptor* desEncryptor = new DesEncryptor(hkey);
    string hexCiphertext = desEncryptor->encrypt(plaintext,false);

    int threads = 100;
    double t0 = omp_get_wtime();
    double time_tot;
    cout << "Starting bruteforce attack on DES\nEncrypted text is: "<< hexCiphertext << endl;
    cout << "Binary: "<< DesEncryptor::hex2bin(hexCiphertext) << endl;
    cout << "Trying with all possible combinations..."<< endl;

    int length = 8;
    long possiblePlaintexts = (long)pow((double)DesEncryptor::numChars,(double)length);

    #pragma omp parallel num_threads(threads)
    {
        //printf("Starting thread %d\n", omp_get_thread_num());
        char password[8];
        #pragma omp for schedule(static)
        for (int i = 0; i < possiblePlaintexts; i++) {
            //int tid = omp_get_thread_num();
            //printf("Encryption from omp thread %d\n", tid);
            DesEncryptor::generatePassword(password, length, i);
            string str(password);
            string enrcyptedPassword = desEncryptor->encrypt(DesEncryptor::string2hex(str), false);
            if (!enrcyptedPassword.compare(hexCiphertext)) {
                //cout << enrcyptedPassword << "=?" << hexCiphertext << endl;
                cout << "\nCiphertext decrypted: " << str << endl;
                double t1 = omp_get_wtime();
                time_tot = t1 - t0;
                cout << "Total time required: " << time_tot << endl;
                #pragma omp cancel for
            } else {
               // cout << "Trying with: "<< str << endl;
            }
            #pragma omp cancellation point for
        }


    }
}
