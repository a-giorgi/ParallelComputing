//
// Created by Edoardo Cagnes on 17/06/20.
//

#ifndef DES_DES_H
#define DES_DES_H

#include <string>
#include <vector>

using namespace std;
static char charset[] = {'a','b','c','d','e','f','g','h',
                         'i','j','k','l','m','n','o','p',
                         'q','r','s','t','u','v','w','x',
                         'y','z','A','B','C','D','E','F',
                         'G','H','I','J','K','L','M','N',
                         'O','P','Q','R','S','T','U','V',
                         'W','X','Y','Z','1','2','3','4',
                         '5','6','7','8','9','0','.','/'};
static int numChars = *(&charset + 1) - charset;
string hex2bin(string s);
string bin2hex(string s);
string permute(string k, int* arr, int n);
string shift_left(string k, int shifts);
string xor_(string a, string b);
string string2hex(string& input);
int hex_value(char hex_digit);
string hex2string(const string& input);
string encrypt(string input, string key, bool decrypt);
string binEncrypt(string input, string key, bool decrypt);
string encrypt(string pt, vector<string> rkb, vector<string> rk);
void generatePassword(char v[], int length, int iteration);
void findPlaintext(string hexCiphertext, string key, int length);

#endif //DES_DES_H
