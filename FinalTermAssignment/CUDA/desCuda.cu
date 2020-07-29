/*
 ============================================================================
 Name        : DES_CUDA.cu
 Author      : Andrea Giorgi
 Version     :
 Copyright   : 
 Description : CUDA compute reciprocals
 ============================================================================
 */

#include <iostream>
#include <numeric>
#include <stdlib.h>
#include "des.h"
#include <cuda_runtime_api.h>
#include <sys/time.h>

//cudaCharset must be equal to charset in des.h
__device__ char cudaCharset[] = {'a','b','c','d','e','f','g','h',
                         'i','j','k','l','m','n','o','p',
                         'q','r','s','t','u','v','w','x',
                         'y','z','A','B','C','D','E','F',
                         'G','H','I','J','K','L','M','N',
                         'O','P','Q','R','S','T','U','V',
                         'W','X','Y','Z','1','2','3','4',
                         '5','6','7','8','9','0','.','/'};

__device__ int cudaNumChars = *(&cudaCharset + 1) - cudaCharset;

// Initial Permutation Table
__device__ int cuda_initial_perm[] = {
        58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17,  9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7
};


// Expansion D-box Table
__device__ int cuda_exp_d[48] = {
        32, 1, 2, 3, 4, 5, 4, 5,
        6, 7, 8, 9, 8, 9, 10, 11,
        12, 13, 12, 13, 14, 15, 16, 17,
        16, 17, 18, 19, 20, 21, 20, 21,
        22, 23, 24, 25, 24, 25, 26, 27,
        28, 29, 28, 29, 30, 31, 32, 1
};

// S-box Table
__device__ int cuda_s[8][4][16] = {
        {
                14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7,
                0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8,
                4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0,
                15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13
        },
        {
                15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10,
                3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5,
                0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15,
                13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9
        },
        {
                10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8,
                13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1,
                13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7,
                1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12
        },
        {
                7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15,
                13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9,
                10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4,
                3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14
        },
        {
                2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9,
                14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6,
                4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14,
                11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3
        },
        {
                12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11,
                10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8,
                9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6,
                4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13
        },
        {
                4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1,
                13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6,
                1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2,
                6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12
        },
        {
                13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7,
                1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2,
                7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8,
                2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11
        }
};

// Straight Permutation Table
__device__ int cuda_per[32] = {
        16, 7, 20,21,
        29, 12, 28, 17,
        1, 15, 23, 26,
        5, 18, 31, 10,
        2, 8, 24, 14,
        32, 27, 3, 9,
        19, 13, 30, 6,
        22, 11, 4, 25
};

// Final Permutation
__device__ int cuda_final_perm[] = {
        40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41, 9, 49, 17, 57, 25
};

__device__ unsigned int* cudaDesEncrypt(int blockSize, unsigned int* bin, unsigned int* key, unsigned int* binIP, unsigned int* left,
		unsigned int* right, unsigned int* backupR, unsigned int* xorbin, unsigned int* expanded,
		unsigned int* sBoxResult, unsigned int* ciphertext, unsigned int* sBoxResultFull,unsigned int* sPermResult){
	int length = 64, i,j, round = 0, sbRow, sFullResIndex, sbColumn, value, sResIndex;
	unsigned long int index = blockIdx.x*blockSize+threadIdx.x;

    // Initial Permutation
	for(i=0;i<64;i++){
		binIP[(index*64)+i] = bin[index*64+cuda_initial_perm[i]-1];
	}

	//Now we divide in left and right
	for(i=0; i<length/2; i++){
	    left[(index*32)+i] = binIP[(index*64)+i];
	    right[(index*32)+i] = binIP[(index*64)+i+(length/2)];
	}

	while(round < 16) {
		for (i = 0; i < length / 2; i++) {
		    backupR[(index*32)+i] = right[(index*32)+i];
		}
		sFullResIndex = 0;
		//using the expansionBox
		for(i = 0; i < 48; i++){
		    expanded[(index*48)+i] = right[(index*32)+cuda_exp_d[i]-1];
		}
		//Key XOR Expansion(right)
		for (i = 0; i < 48; i++) {
		    xorbin[(index*48)+i] = (expanded[(index*48)+i]+key[48*round+i]) % 2;
		}
		//SBOX
		for (i = 0; i < 48; i += 6) {
			sbRow = (xorbin[(index*48)+i + 5]*1) + (xorbin[(index*48)+i] * 2); //from 0 to 3
			sbColumn = (xorbin[(index*48)+i + 1]* 8) + (xorbin[(index*48)+i + 2] * 4) + (xorbin[(index*48)+i + 3] *2)
					+ (xorbin[(index*48)+i + 4]*1); //from 0 to 15
			value = cuda_s[i/6][sbRow][sbColumn];
			//converting decimal value inside sBox to binary
			sResIndex = 0;
			while(value>0){
				sBoxResult[(index*4)+sResIndex]= value % 2;
				sResIndex++;
				value/=2;
			}
			while(sResIndex < 4){
				sBoxResult[(index*4)+sResIndex] = 0;
				sResIndex++;
			}
			for (j = sResIndex - 1; j >= 0; j--) {
				sBoxResultFull[(index*48)+sFullResIndex] = sBoxResult[(index*4)+j];
				sFullResIndex++;
			}
		}
		//Now we have the full output from sbox
		for (i = 0; i < 32; i++){
		    sPermResult[(index*32)+i] = sBoxResultFull[(index*48)+cuda_per[i] - 1];
		}
		//Xor with left and sbox output
		for (i = 0; i < 32; i++){
			right[(index*32)+i] = (sPermResult[(index*32)+i] + left[(index*32)+i])%2;
		}
		for (i = 0; i < 32; i++){
		    left[(index*32)+i] = backupR[(index*32)+i];
		}
		round++;
	}

	for(i=0; i<32; i++){
		binIP[(index*64)+i]=right[(index*32)+i];
		binIP[(index*64)+i+32]=left[(index*32)+i];
	}

	for(i=0; i<64; i++){
		ciphertext[(index*64)+i]=binIP[(index*64)+cuda_final_perm[i]-1];
	}

	return ciphertext;

}
//used for debug purpose
__global__ void helloGPU(int blockSize){
	int index = blockIdx.x*blockSize+threadIdx.x;
	printf("Hello from thread: %d\n",index);
}

__device__ char* cudaString2hex(int blockSize, const char input[],unsigned int len, char output[])
{
	unsigned long int index = blockIdx.x*blockSize+threadIdx.x;
    static const char hex_digits[] = "0123456789ABCDEF";
    int count = 0;
    for (int i=0; i<len; ++i) {
        const char c = input[index*8+i];
        output[index*16+count] = (hex_digits[c >> 4]);
        count++;
        output[index*16+count] = (hex_digits[c & 15]);
        count++;

    }
    return output;
}

__device__ unsigned  int* cudaHex2bin(int blockSize, const char input[], int length, unsigned int output[]) //hexadecimal to binary
{
	unsigned long int index = blockIdx.x*blockSize+threadIdx.x;
	const unsigned short int map[64]=
	{
			0, 0, 0, 0,
			0, 0, 0, 1,
			0, 0, 1, 0,
			0, 0, 1, 1,
			0, 1, 0, 0,
			0, 1, 0, 1,
			0, 1, 1, 0,
			0, 1, 1, 1,
			1, 0, 0, 0,
			1, 0, 0, 1,
			1, 0, 1, 0,
			1, 0, 1, 1,
			1, 1, 0, 0,
			1, 1, 0, 1,
			1, 1, 1, 0,
			1, 1, 1, 1
	};
	int arrIndex = 0;

	for(int i = 0; i<length; i++){
		int mapIndex = 0;
		if(input[index*16+i]>='0'&& input[index*16+i]<='9'){
			for(int j = 0; j < 4; j++){
				mapIndex = input[index*16+i] - '0'; //convert char to int maintaining numbers
				output[index*64+arrIndex]= map[mapIndex*4+j];
				arrIndex++;
			}
		}else{
			switch(input[index*16+i]) {
				case 'A':
					mapIndex = 10;
				break;
				case 'B':
					mapIndex = 11;
				break;
				case 'C':
					mapIndex = 12;
				break;
				case 'D':
					mapIndex = 13;
				break;
				case 'E':
					mapIndex = 14;
				break;
				case 'F':
					mapIndex = 15;
				break;
			}
			for(int j = 0; j < 4; j++){
				output[index*64+arrIndex]=map[mapIndex*4+j];
				arrIndex++;
			}
		}
	}

    return output;
}

__device__ unsigned int* generatePassword(int blockSize, char* password, char * hex_password, unsigned int* bin_password, unsigned int length, unsigned long int iteration){
	unsigned long int index = blockIdx.x*blockSize+threadIdx.x;
    for (int j=0; j<(length-1);j++){
    	password[(index*length)+j] = cudaCharset[iteration / (unsigned long int)(pow((double) cudaNumChars ,(double)((length-1)-j)))];
        iteration = iteration % ( unsigned long int)(pow((double) cudaNumChars ,(double)((length-1)-j)));
    }
    password[(index*length)+length-1] = cudaCharset[iteration % cudaNumChars];
    hex_password = cudaString2hex(blockSize, password, length, hex_password);
    //printf("%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c\n",hex_password[index*16+0],hex_password[index*16+1],hex_password[index*16+2],hex_password[index*16+3],hex_password[index*16+4],hex_password[index*16+5],hex_password[index*16+6],hex_password[index*16+7],hex_password[index*16+8],hex_password[index*16+9],hex_password[index*16+10],hex_password[index*16+11],hex_password[index*16+12],hex_password[index*16+13],hex_password[index*16+14],hex_password[index*16+15]);
    bin_password = cudaHex2bin(blockSize, hex_password, length*2, bin_password);
    return bin_password;
}

__global__ void findPassword(int blockSize, int numThreads, int passwordLength, unsigned int* targetPassword,
		char* password, char * hex_password, unsigned int* bin_password, unsigned int* key, unsigned int* binIP,
		unsigned int* left, unsigned int* right, unsigned int* backupR, unsigned int* xorbin, unsigned int* expanded,
		unsigned int* sBoxResult, unsigned int* ciphertext, unsigned int* sBoxResultFull, unsigned int* sPermResult){
	unsigned int long const index = blockIdx.x*blockSize+threadIdx.x;
	bool found = false;
	unsigned int l = passwordLength;
	unsigned long int n = cudaNumChars;
	unsigned long int plaintexts = n;
	    for (int i = 1; i < l; i++) {
	    	plaintexts = plaintexts * n;
	    }

	unsigned long int threadIterations = plaintexts/numThreads;
	unsigned long int startIteration = index*threadIterations;
	unsigned long int endIteration = startIteration + threadIterations;
	if(index == plaintexts){
		endIteration = plaintexts;
	}
	//printf("\nnumChars: %d, startIteration: %lu, endIteration: %lu, thread: %lu \n",cudaNumChars,startIteration,endIteration,index);
	for(unsigned long int iteration = startIteration; iteration < endIteration; iteration++){
		//printf("iteration %lu from thread %lu,\n",iteration, index);
		generatePassword(blockSize, password, hex_password, bin_password, 8, iteration);
		cudaDesEncrypt(blockSize, bin_password,key,binIP,left,right,backupR,xorbin,expanded,
			sBoxResult,ciphertext,sBoxResultFull, sPermResult);
		for(int i=0; i<64; i++){
			if(ciphertext[index*64+i] != targetPassword[i]){
				break;
			}
			if( i == 64-1){
				found = true;
			}
		}
		if(found){
			 printf("\nPassword decrypted from thread %lu. The plaintext is: \n", index);
			 for(int i=0; i < 64; i++){
			     printf("%d", bin_password[index*64+i]);
			 }
			 printf("\n");
			 asm("trap;");
			 found = false;
		}
	}
	printf("No results found from thread %d\n",index);
}

int main(){
	int i;
	locale locale;
	std::string numberOfThreads, insertedPassword;
	//char input[16] = {'6','1','6','1','6','1','6','1','6','1','3','6','6','1','5','A'};
	char inputStr[8]; //= {'a','a','a','a','a','a','b','b'};
	char keyText[16] = {'4','1','6','B','6','5','7','9','5','A','5','4','3','3','3','1'};

	//fetching password from user
	getPassword:;
	std::cout << "Write your password (must be 8 characters long):\n";
	std::cin >> insertedPassword;
	if(insertedPassword.length()!=8) {
	    cout<<"The password must be 8 characters long\n";
	    goto getPassword;
	}

	for(int i = 0; i < insertedPassword.length(); i++) {
		for(int j = 0; j < numChars; j++){
			if(insertedPassword[i] == charset[j]){
				break;
			}
			if(j == numChars-1){
				std::cout << "Password contains some illegal characters!\n";
				goto getPassword;
			}
		}
	    inputStr[i] = insertedPassword[i];
	}

	//converting input to hex
	char* input = (char*)malloc(16);
	input = string2hex(inputStr,8,input);

	// converting hex to bin
	char* binary = (char*)malloc(16*4);
	binary = hex2bin(input,16,binary);

	//displaying plaintext
	std::cout<< "Binary password: ";
	for(i =0; i<64;i++){
		std::cout<< binary[i];
	}
	std::cout<< endl;
	int* plaintextInt = new int[64];
	for(i=0;i<64;i++){
		plaintextInt[i] = binary[i]-'0';
	}

	char* keyBin = (char*)malloc(16*4);
	keyBin = hex2bin(keyText,16,keyBin);
	std::cout<< "Key: ";
	for(i=0; i<64;i++){
		std::cout<< keyBin[i];
	}
	std::cout<<endl;

	//generating keys
	unsigned int** key = keyGenerator(keyBin);
    int height = 16;
    int width = 48;

    //placing keys into 1D array
    unsigned int* reducedKey = new unsigned int [height*width];
    for (int h = 0; h < height; h++){
        for (int w = 0; w < width; w++)
        	reducedKey[width * h + w] = key[h][w];
    }

    //Ciphertext target
    unsigned int *ciphertextPassword = (unsigned int*)malloc(64*sizeof(unsigned int*));
    ciphertextPassword = binEncrypt(plaintextInt,reducedKey);
    std::cout<<"Ciphertext (encrypted password): ";
    for(i=0; i<64;i++){
    		std::cout<< ciphertextPassword[i];
    }
    std::cout<<endl;

    unsigned int threads;
    // Allowing user to set threads
    setTreads:;
    std::cout << "Choose the number of threads:\n";
    std::cin >> numberOfThreads;
    for(int i = 0; i < numberOfThreads.length(); i++) {
    	if(!isdigit(numberOfThreads[i], locale)) {
    		std::cout << "Wrong value: you must write an integer!\n";
    		goto setTreads;
    	}
    }
    if(atoi(numberOfThreads.c_str()) != 0)
    	threads = atoi(numberOfThreads.c_str());
    if(threads == 0){
    	threads = 1;
    	std::cout<< "Number of threads set to 1"<<endl;
    }

    /** organizing threads to maximize warps */
    int t, b;
    int blockSize = 32;
    if(threads<=32){
    	b = 1;
    	t = threads;
    	printf("Configuration: <<<%d,%d>>>\n",b,t);
    }else if(threads>32 && (threads%128)==0){
    	b = threads/128;
    	t = 128;
    	blockSize = 128;
    	printf("Configuration: <<<%d,%d>>>\n",b,t);
    }else if(threads>32 && (threads%128)!=0 &&(threads%32)==0){
    	b = threads/32;
    	t = 32;
    	printf("Configuration: <<<%d,%d>>>\n",b,t);
    }else{
    	int q = threads/32;
    	int mod = threads % 32;
    	if(mod>16){
    		threads = (q+1)*32;
    	}else{
    		threads = (q)*32;
    	}
    	std::cout << "Thread fixed to "<< threads <<" to improve performances"<<endl;
    	b = threads/32;
    	t = 32;
    	printf("Configuration: <<<%d,%d>>>\n",b,t);
    }
    /** ------------------ */

    /**CudaMalloc for cudaDesEncrypt */
    unsigned int* binIP;
    unsigned int* left;
    unsigned int* right;
    unsigned int* backupR;
    unsigned int* xorbin;
    unsigned int* expanded;
    unsigned int* sBoxResult;
    unsigned int* ciphertext;
    unsigned int* sBoxResultFull;
    unsigned int* sPermResult;
    unsigned int* cudaKey;

    cudaMalloc(&binIP, (threads*64*sizeof(unsigned int)));
    cudaMalloc(&left, (threads*64*sizeof(unsigned int))/2);
    cudaMalloc(&right, (threads*64*sizeof(unsigned int))/2);
    cudaMalloc(&backupR,(threads*64*sizeof(unsigned int))/2);
    cudaMalloc(&xorbin, (threads*48*sizeof(unsigned int)));
    cudaMalloc(&expanded, (threads*48*sizeof(unsigned int)));
    cudaMalloc(&sBoxResult, (threads*4*sizeof(unsigned int)));
    cudaMalloc(&ciphertext, (threads*64*sizeof(unsigned int)));
    cudaMalloc(&sBoxResultFull,(threads*48*sizeof(unsigned int)));
    cudaMalloc(&sPermResult,(threads*32*sizeof(unsigned int)));
    cudaMalloc(&cudaKey,(width*height*sizeof(unsigned int)));
    cudaMemcpy(cudaKey, reducedKey, width*height* sizeof(unsigned int), cudaMemcpyHostToDevice);
    /**-----------------------------*/

    /** CudaMalloc for generatePassword and findPlaintext */
    char* password;
    char* hex_password;
    unsigned int* bin_password;
    unsigned int* targetPassword;

    cudaMalloc(&password,(threads*8*sizeof(char)));
    cudaMalloc(&hex_password,(threads*8*2*sizeof(char)));
    cudaMalloc(&bin_password,(threads*8*2*4*sizeof(unsigned int)));
    cudaMalloc(&targetPassword,(64*sizeof(unsigned int)));
    cudaMemcpy(targetPassword, ciphertextPassword, 64* sizeof(unsigned int), cudaMemcpyHostToDevice);
    /**---------------------------------*/

    struct timeval t0, t1;
    gettimeofday(&t0, NULL);

    int passwordLength = 8;
    unsigned long int possiblePlaintexts = (long)pow((double)numChars,(double)passwordLength);

    printf("Possible plaintexts: %lu\n",possiblePlaintexts);
    printf("Trying every possible combination...\n");
    findPassword<<<b,t>>>(blockSize, threads, 8, targetPassword,password,hex_password,bin_password,
    		cudaKey,binIP,left,right, backupR, xorbin,expanded,sBoxResult,ciphertext,sBoxResultFull,sPermResult);

    cudaDeviceSynchronize();

    gettimeofday(&t1, NULL);
    double time = ((t1.tv_sec  - t0.tv_sec) * 1000000u + t1.tv_usec - t0.tv_usec) / 1.e6;
    std::cout<<"Time passed: "<< time <<endl;

    //releasing GPU memory
    cudaFree(binIP);
    cudaFree(left);
    cudaFree(right);
    cudaFree(backupR);
    cudaFree(xorbin);
    cudaFree(expanded);
    cudaFree(sBoxResult);
    cudaFree(ciphertext);
    cudaFree(sBoxResultFull);
    cudaFree(sPermResult);
    cudaFree(cudaKey);
    cudaFree(password);
    cudaFree(hex_password);
    cudaFree(bin_password);
    cudaFree(targetPassword);

    return 0;

}

