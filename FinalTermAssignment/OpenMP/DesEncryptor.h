//
// Created by Andrea Giorgi on 20/06/20.
//

#ifndef DES_DESENCRYPTOR_H
#define DES_DESENCRYPTOR_H
#include <string>
#include <vector>

using namespace std;

class DesEncryptor {
public:
    static char charset[];
    static int numChars;
    DesEncryptor(string key);
    static string hex2bin(string s);
    static string bin2hex(string s);
    static string permute(string k, int* arr, int n);
    static string shift_left(string k, int shifts);
    static string xor_(string a, string b);
    static string string2hex(string& input);
    static int hex_value(char hex_digit);
    static string hex2string(const string& input);
    static void generatePassword(char v[], int length, int iteration);
    string encrypt(string pt, bool decrypt);

private:
    static int initial_perm[];
    static int exp_d[48];
    static int s[8][4][16];
    static int per[32];
    static int final_perm[];
    static int keyp[56];
    static int shift_table[16];
    static int key_comp[48];
    vector<string> rk;
    vector<string> rkb;
    vector<string> decryptRk;
    vector<string> decryptRkb;
};


#endif //DES_DESENCRYPTOR_H
