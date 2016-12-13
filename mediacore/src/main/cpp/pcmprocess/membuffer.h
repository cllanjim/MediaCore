/*
 * membuffer.h
 *
 *  Created on: 2014-11-10
 *      Author: a33
 */

#ifndef MEMBUFFER_H_
#define MEMBUFFER_H_

#include <string.h>

int GetTickCount();
void ZeroMemory(void * p, size_t size);


struct ReadBuffer
{
	ReadBuffer(const unsigned char* pSource, int nSourceLen);

	int Read(unsigned char* pData, int nLen);

	bool Eof();

	void Reset();

	bool Advance(int offset = 0);

	unsigned char * GetCurBuffer();

	int m_nCurrent; //zero base
	int m_nSourceLen;
	const unsigned char* m_pSource;
};

struct WriteBuffer
{
	WriteBuffer(unsigned char* pDest = 0, int nDestLen = 0);

	int Write(const unsigned char* pData, int nLen);

	bool Write2Full(const unsigned char* pData, int nDataSize, int& writeSize);

	bool Eof();

	int GetLength();

	unsigned char * GetBuffer();

	void Reset(unsigned char* pDest, int nDestLen);

	void Tell(int offset = 0);


	int m_nCurrent;			//zero base
	int m_nDestCapability;
	unsigned char* m_pDest;
};






#endif /* MEMBUFFER_H_ */
