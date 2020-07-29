//
// Created by Edoardo Cagnes on 17/06/20.
//

#include "DesEncryptor.h"
#include <iostream>
#include <string>
#include <stdio.h>
#include <math.h>
#include <algorithm>
#include <sys/time.h>

using namespace std;

int main() {
    string key = "AkeyZT31";
    //Converting key to hex
    string hkey = DesEncryptor::string2hex(key);
    //cout << "Hexadecimal key; " << hkey << endl;

    string plaintext = "aaaaa6aZ";
    plaintext = DesEncryptor::string2hex(plaintext);

    DesEncryptor* desEncryptor = new DesEncryptor(hkey);
    string hexCiphertext = desEncryptor->encrypt(plaintext,false);

    cout << "Starting bruteforce attack on DES\nEncrypted text is: "<< hexCiphertext << endl;
    cout << "Binary: "<< DesEncryptor::hex2bin(hexCiphertext) << endl;
    cout << "Trying with all possible combinations..."<< endl;
    char password[8];
    int length = 8;
    long possiblePlaintexts = (long)pow((double)DesEncryptor::numChars,(double)length);
    struct timeval t0, t1;
    gettimeofday(&t0, NULL);
    for(int i = 0; i < possiblePlaintexts; i++){
        DesEncryptor::generatePassword(password,length,i);
        string str(password);
        string enrcyptedPassword = desEncryptor->encrypt(DesEncryptor::string2hex(str), false);
        if(!enrcyptedPassword.compare(hexCiphertext)){
            cout << "\nCiphertext decrypted: "<< str << endl;
            gettimeofday(&t1, NULL);
            double time_tot = ((t1.tv_sec  - t0.tv_sec) * 1000000u + t1.tv_usec - t0.tv_usec) / 1.e6;
            cout << "Total time required: "<< time_tot << endl;
            break;
        }else{
            //cout << "Trying with: "<< str << endl;
        }
    }
}
