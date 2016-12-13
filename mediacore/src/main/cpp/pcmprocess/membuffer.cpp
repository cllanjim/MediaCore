/*
 * membuffer.cpp
 *
 *  Created on: 2014-11-10
 *      Author: a33
 */

#include "membuffer.h"

int GetTickCount()
{
	return 0;
}

void ZeroMemory(void * p, size_t size)
{
    memset(p, 0, size);
}


ReadBuffer::ReadBuffer(const unsigned char* pSource, int nSourceLen):m_pSource(pSource),m_nCurrent(0),m_nSourceLen(nSourceLen)
{

}

int ReadBuffer::Read(unsigned char* pData, int nLen)
{
	int nReal = nLen;
	if(m_nCurrent + nLen > m_nSourceLen)
	{
		nReal = m_nSourceLen - m_nCurrent;
	}

	memcpy(pData, m_pSource + m_nCurrent, nReal);
	m_nCurrent += nReal;
	return nReal;
}

bool ReadBuffer::Eof()
{
	return m_nCurrent == m_nSourceLen;
}

void ReadBuffer::Reset()
{
	m_nCurrent = 0;
}


bool ReadBuffer::Advance( int offset /*= 0*/ )
{
	if(m_nCurrent + offset > m_nSourceLen)
		return false;
	else
		m_nCurrent += offset;
	return true;
}

unsigned char* ReadBuffer::GetCurBuffer()
{
	if(Eof())
		return NULL;
	else
		return (unsigned char*)(m_pSource + m_nCurrent);
}

WriteBuffer::WriteBuffer(unsigned char* pDest/* = 0*/, int nDestLen/* = 0*/):m_pDest(pDest),m_nCurrent(0),m_nDestCapability(nDestLen)
{

}

int WriteBuffer::Write(const unsigned char* pData, int nLen)
{
	int nReal = nLen;
	if(m_nCurrent + nLen > m_nDestCapability)
	{
		nReal = m_nDestCapability - m_nCurrent;
	}

	memcpy(m_pDest + m_nCurrent, pData, nReal);
	m_nCurrent += nReal;
	return nReal;
}

bool WriteBuffer::Eof()
{
	return m_nCurrent == m_nDestCapability;
}

int WriteBuffer::GetLength()
{
	return m_nCurrent;
}

unsigned char* WriteBuffer::GetBuffer()
{
	return m_pDest;
}

void WriteBuffer::Reset(unsigned char* pDest, int nDestLen)
{
	m_nCurrent = 0;
	m_pDest = pDest;
	m_nDestCapability = nDestLen;
}

bool WriteBuffer::Write2Full( const unsigned char* pData, int nDataSize, int& writeSize )
{
	if(nDataSize <= 0)
	{
		writeSize = 0;
		return false;
	}

	if(m_nDestCapability - m_nCurrent < nDataSize)
	{
		writeSize = m_nDestCapability - m_nCurrent;
	}
	else
	{
		writeSize = nDataSize;
	}

	Write(pData,writeSize);

	return Eof();
}

void WriteBuffer::Tell( int offset /*= 0*/ )
{
	m_nCurrent = offset;
}
