/*
 * pcmprocess.cpp
 *
 *  Created on: 2014-11-10
 *      Author: a33
 */



#include <assert.h>
#include <math.h>
#include "pcmprocess.h"

#ifdef _DEBUG
#include "stdlib.h"
#include "stdio.h"

#endif

#include "../common_log.h"

#define MAKEINT64(a, b) ((((__int64)(a) & 0xFFFFFFFF)) | (((__int64)(b) << 32) & 0xFFFFFFFF00000000))


PcmProcess::PcmProcess()
{
	m_pSt = NULL;
	m_nChannal = 0;
	m_nSampleRateIn = 0;
	m_nSampleRateOut = 0;
}

void PcmProcess::Revise( int nChannal, unsigned int dwInSampleRate, unsigned int dwOutSampleRate )
{
	if(m_pSt && nChannal == m_nChannal && m_nSampleRateIn == dwInSampleRate && m_nSampleRateOut == dwOutSampleRate)
	{
		return;
	}

	Reset(nChannal, dwInSampleRate, dwOutSampleRate);

}

void PcmProcess::Reset( int nChannal, unsigned int dwInSampleRate, unsigned int dwOutSampleRate )
{
	if(m_pSt)
	{
		speex_resampler_destroy(m_pSt);
	}
	else
	{
		m_pSt = speex_resampler_init(nChannal, dwInSampleRate, dwOutSampleRate, 10, NULL);
	}


	m_nChannal = nChannal;
	m_nSampleRateIn = dwInSampleRate;
	m_nSampleRateOut = dwOutSampleRate;
}


PcmProcess::~PcmProcess()
{
	if(m_pSt)
	{
		 speex_resampler_destroy(m_pSt);
	}
}



//assume bitrate = 16
int PcmProcess::mix( unsigned char* pbyteDest, unsigned int& dwDestLen/*in, out*/, unsigned int dwDestCap, const unsigned char* pbyteSrc, unsigned int dwSrcLen )
{
	assert(dwDestLen <= dwDestCap && dwSrcLen <= dwDestCap);
	assert(!(dwDestLen % 2 || dwSrcLen % 2));

	if(dwDestCap < dwSrcLen)
		return false;

	unsigned int mixLen = dwDestLen < dwSrcLen ? dwDestLen : dwSrcLen;


	if(/* DISABLES CODE */ (0))
	{
		unsigned char* lpSrcData = (unsigned char*)pbyteSrc;
		unsigned char* lpDstData = pbyteDest;


		float gain = log10(20.0f);
		for(unsigned int i = 0; i < mixLen; i++)
		{
			*lpDstData = (unsigned char)(((*lpSrcData + *lpDstData)>>1)*gain);
			lpSrcData++;
			lpDstData++;
		}

		for(unsigned int i = mixLen; i <  dwSrcLen; i++)
		{
			*(lpDstData++) = *(lpSrcData++);
		}
	}
	else
	{
		unsigned short* lpSrcData = (unsigned short*)pbyteSrc;
		unsigned short* lpDstData = (unsigned short*)pbyteDest;

		for (unsigned int i = 0; i < mixLen / 2; i++)
		{
			float sample1 = (*lpSrcData - 32768) / 32768.0f;
			float sample2 = (*lpDstData - 32768) / 32768.0f;
			if (fabs(sample1*sample2) > 0.25f)
				*lpDstData = (unsigned short)(*lpSrcData + *lpDstData);
			else
				*lpDstData = fabs(sample1) < fabs(sample2) ? *lpSrcData : *lpDstData;
			lpSrcData++;
			lpDstData++;
		}

		for(unsigned int i = mixLen / 2; i <  dwSrcLen / 2; i++)
		{
			*(lpDstData++) = *(lpSrcData++);
		}
	}

	dwDestLen = dwDestLen > dwSrcLen ? dwDestLen : dwSrcLen;

	return mixLen;

}

//assume bitrate = 16
bool PcmProcess::stereo_to_mono( const unsigned char* pbyteInBuffer, unsigned int dwInLength, unsigned char* pbyteOutBuffer, unsigned int& dwOutLength /*in, out*/ )
{
	if(!pbyteInBuffer || !pbyteOutBuffer || dwOutLength < dwInLength / 2 || dwInLength % 2)
		return false;

	for(int i = 0; i < dwInLength / 4; i++)
	{
		*(short*)(pbyteOutBuffer + 2 * i) = ((*(short*)(pbyteInBuffer + i * 4)) + (*(short*)(pbyteInBuffer + i * 4 + 2))) / 2;
	}

	dwOutLength = dwInLength / 2;
	return true;
}

bool PcmProcess::mono_to_stereo( const unsigned char* pbyteInBuffer, unsigned int dwInLength, unsigned char* pbyteOutBuffer, unsigned int& dwOutLength /*in, out*/ )
{
	if(!pbyteInBuffer || !pbyteOutBuffer || dwOutLength < dwInLength * 2 )
		return false;

	for(int i = 0; i < dwInLength / 2; i++)
	{
		*(short*)(pbyteOutBuffer + 4 * i) = *(short*)(pbyteInBuffer + 2 * i);
		*(short*)(pbyteOutBuffer + 4 * i+2) = *(short*)(pbyteInBuffer + 2 * i);
	}

	dwOutLength = dwInLength * 2;
	return true;
}

//assume bitrate = 16
bool PcmProcess::pcm_convert( const unsigned char* pbyteInBuffer, unsigned int dwInLength, unsigned int dwInSampleRate,  unsigned int dwChannal,
							  unsigned char* pbyteOutBuffer, unsigned int& dwOutLength, /*in, out*/ unsigned int dwOutSampleRate ,unsigned int dwOutChannal)
{
	if(!pbyteInBuffer || !pbyteOutBuffer)
		return false;

    //	mono & stereo convert
	if(dwChannal==1 && dwOutChannal==2){
		unsigned char* stereoBuffer = (unsigned char *) malloc(sizeof(unsigned char*) * dwInLength * 2);
		unsigned int stereoLen = dwInLength*2;
		mono_to_stereo(pbyteInBuffer,dwInLength,stereoBuffer,stereoLen);
		pbyteInBuffer = stereoBuffer;
		dwInLength = stereoLen;
		dwChannal = dwOutChannal;
	}else if(dwChannal==2 && dwOutChannal==1){
		unsigned char* monoBuffer = (unsigned char *) malloc(sizeof(unsigned char*) * dwInLength /2);
		unsigned int monoLen = dwInLength/2;
        stereo_to_mono(pbyteInBuffer,dwInLength,monoBuffer,monoLen);
		pbyteInBuffer = monoBuffer;
		dwInLength = monoLen;
		dwChannal = dwOutChannal;
	}

	assert(dwOutLength >= dwInLength * (float(dwOutSampleRate) / dwInSampleRate));

	//perform optical
	if(dwInSampleRate == dwOutSampleRate)
	{
		for(int i = 0; i < dwInLength; i++)
		{
			*(pbyteOutBuffer + i) = *(pbyteInBuffer + i);
		}

		dwOutLength = dwInLength;
		return true;
	}

	Revise(dwChannal, dwInSampleRate, dwOutSampleRate);

	spx_uint32_t out_len = dwOutLength  / 2 / dwChannal;
	spx_uint32_t in_len = dwInLength / 2 / dwChannal;

	speex_resampler_process_interleaved_int(m_pSt, (spx_int16_t*)pbyteInBuffer, &in_len, (spx_int16_t *)pbyteOutBuffer, &out_len);
	dwOutLength = out_len * 2 * dwChannal;

	return true;

}





PcmRealTimeMixer::PcmRealTimeMixer(unsigned int nOutputSize, unsigned int nSampleRate, unsigned int nChannal):m_nPckSize(nOutputSize),m_SampleRate(nSampleRate),m_nChannal(nChannal)
{
	for(int i = 0; i < MAX_MIXER_SOURCE; i++)
	{
		m_IDSet.insert(i);
		SourceInfo info;
		m_VecSrcInfo.push_back(info);
	}

	m_nLastOutputTime = 0;
	m_nCurMixLen = 0;

	ZeroMemory(m_MixBuffer, MAX_MIXER_BUFFER*2);

}

bool PcmRealTimeMixer::AddSource( unsigned int& ID, unsigned int nGap, unsigned int nSampleRate, unsigned int nChannal)
{
	if(m_IDSet.size() > 0)
	{
		ID = *(m_IDSet.begin());
		m_IDSet.erase(ID);
		m_IDWorkSet.insert(ID);
		SourceInfo info;
		info.nIdealGap = nGap;
		info.nSampleRate = nSampleRate;
		info.nChannal = nChannal;
		m_VecSrcInfo[ID] = info;
		return true;
	}
	return false;
}

void PcmRealTimeMixer::RemoveSource( unsigned int ID )
{
	m_IDSet.insert(ID);
	m_IDWorkSet.erase(ID);
	SourceInfo info;
	m_VecSrcInfo[ID] = info;
	if(m_IDWorkSet.size() == 0)
	{
		m_nLastOutputTime = 0;
		m_nCurMixLen = 0;
		ZeroMemory(m_MixBuffer, MAX_MIXER_BUFFER*2);
	}
}

bool PcmRealTimeMixer::InputData( unsigned int ID, const unsigned char* pByInBuffer, unsigned int dwInlength, unsigned int uTick, unsigned char* pbyteOutBuffer, unsigned int& dwOutLength/*in, out*/ )
{
	assert(dwOutLength >= m_nPckSize);
	assert(!(dwInlength % 2));

	//这个外面的时间有问题
	uTick = GetTickCount();
	//check ID valid
	if(m_IDWorkSet.find(ID) == m_IDWorkSet.end())
	{
#ifdef _DEBUG
		printf("\r\n###Error ID %d", ID);
#endif
		return false;
	}

	//-- format convert --
	unsigned char* pSrcBuffer = (unsigned char*)pByInBuffer;
	unsigned int dwSrcLength = dwInlength;

	if(m_VecSrcInfo[ID].nChannal != m_nChannal)
	{
		unsigned int nOutLen = MAX_SOURCE_BUFFER;
		assert(m_VecSrcInfo[ID].nChannal == 2 && m_nChannal == 1);
		if(!PcmProcess::stereo_to_mono((unsigned char*)pSrcBuffer,dwSrcLength, m_SourceBuffer, nOutLen))
		{
			assert(false);
			return false;
		}
		pSrcBuffer = m_SourceBuffer;
		dwSrcLength = nOutLen;
	}

	if(m_VecSrcInfo[ID].nSampleRate != m_SampleRate)
	{
		unsigned int nOutLen = MAX_SOURCE_BUFFER;
		if(!m_ReSample.pcm_convert(pSrcBuffer,
			dwSrcLength,
			m_VecSrcInfo[ID].nSampleRate,
			m_nChannal,
			m_SourceBuffer, nOutLen,
			m_SampleRate,m_nChannal))
		{
			assert(false);
			return false;
		}
		pSrcBuffer = m_SourceBuffer;
		dwSrcLength = nOutLen;
	}
	//-- format convert end --

	if(m_VecSrcInfo[ID].bSync)		// source start
	{
		//adjust mix start point
		if(m_nLastOutputTime == 0)				//mixer start
		{
			m_VecSrcInfo[ID].nCurpos = 0;
			m_nLastOutputTime = uTick - (double)dwSrcLength * 1000 / m_SampleRate / m_nChannal / 2;
		}
		else
		{
			assert(uTick >= m_nLastOutputTime);
			unsigned int span = uTick - m_nLastOutputTime;
			m_VecSrcInfo[ID].nCurpos = 0;
			unsigned int len = Advance(span);

#ifdef _DEBUG
			printf("\r\n###Sync ID %d  Span %d", ID, span);
#endif
			if(len > dwSrcLength)
			{
				if(len <= MAX_MIXER_BUFFER)
				{
					m_VecSrcInfo[ID].nCurpos = len - dwSrcLength;

					if(m_VecSrcInfo[ID].nCurpos > m_nCurMixLen)
					{
						ZeroMemory(m_MixBuffer + m_nCurMixLen, m_VecSrcInfo[ID].nCurpos - m_nCurMixLen);
					}
				}
				else
				{
					 //too old
					Reset();
					m_VecSrcInfo[ID].nCurpos = 0;
					m_nLastOutputTime = uTick - (double)dwSrcLength * 1000 / m_SampleRate / m_nChannal / 2;
				}
			}
			else
			{
				pSrcBuffer += dwSrcLength - len;
				dwSrcLength = len;
			}
		}
		m_VecSrcInfo[ID].bSync = false;
	}
	else if(uTick - m_VecSrcInfo[ID].nLastTick < m_VecSrcInfo[ID].nIdealGap / 2 || uTick - m_VecSrcInfo[ID].nLastTick > m_VecSrcInfo[ID].nIdealGap * 3 / 2)
	{
		//do sth?
#ifdef _DEBUG
		printf("\r\nWarining Change too larger ***ID %d  Span %d", ID, uTick - m_VecSrcInfo[ID].nLastTick);
#endif
	}

	//mix
	unsigned char* pDest = m_MixBuffer;
	pDest += m_VecSrcInfo[ID].nCurpos;
	unsigned int dwDestLen = 0;
	if(m_nCurMixLen > m_VecSrcInfo[ID].nCurpos)
		dwDestLen = m_nCurMixLen - m_VecSrcInfo[ID].nCurpos;

	unsigned int dwDestCap = MAX_MIXER_BUFFER - m_VecSrcInfo[ID].nCurpos;
	if(MAX_MIXER_BUFFER < m_VecSrcInfo[ID].nCurpos + dwSrcLength)  //too fast, discard, avoiding cumulation
	{
		dwSrcLength = 0;
#ifdef _DEBUG
		printf("\r\nWarning Source Mixer Buffer Full");
#endif
	}
	else
	{
		bool b = PcmProcess::mix(pDest, dwDestLen, dwDestCap, pSrcBuffer, dwSrcLength);
		assert(b);
		m_nCurMixLen = m_VecSrcInfo[ID].nCurpos + dwDestLen;
	}

	m_VecSrcInfo[ID].nCurpos += dwSrcLength;
	m_VecSrcInfo[ID].nLastTick = uTick;


	//check something to output
	unsigned int nOutputSize = 0;
	bool bOutput = true;
	for(std::set<unsigned int>::iterator it = m_IDWorkSet.begin(); it != m_IDWorkSet.end(); it++)
	{
		if(m_VecSrcInfo[*it].bSync)
			continue;

		if(m_VecSrcInfo[*it].nCurpos < m_nPckSize)
		{
			if(uTick - m_VecSrcInfo[*it].nLastTick >= 2 * m_VecSrcInfo[*it].nIdealGap)
			{
#ifdef _DEBUG
				printf("\r\n###Source Disable? ID %d  Span %d", ID, uTick - m_VecSrcInfo[*it].nLastTick);
#endif
				m_VecSrcInfo[*it].bSync = true;
				continue;
			}
			else
			{
				bOutput = false;
				break;
			}
		}

		if(nOutputSize == 0)
			nOutputSize = m_VecSrcInfo[*it].nCurpos;
		else if(nOutputSize > m_VecSrcInfo[*it].nCurpos)
		{
			nOutputSize = m_VecSrcInfo[*it].nCurpos;
		}
	}

	//output and adjust position
	if(bOutput && nOutputSize >= m_nPckSize)
	{
		memcpy(pbyteOutBuffer, m_MixBuffer, nOutputSize);
		dwOutLength = nOutputSize;
		memmove(m_MixBuffer, m_MixBuffer + nOutputSize, MAX_MIXER_BUFFER*2 - nOutputSize);

		for(std::set<unsigned int>::iterator it = m_IDWorkSet.begin(); it != m_IDWorkSet.end(); it++)
		{
			if(m_VecSrcInfo[*it].bSync)
				continue;
			assert(m_VecSrcInfo[*it].nCurpos >= nOutputSize);
			m_VecSrcInfo[*it].nCurpos -= nOutputSize;
		}
		assert(m_nCurMixLen >= nOutputSize);
		m_nCurMixLen -= nOutputSize;
		m_nLastOutputTime = uTick;
		return true;
	}

	return false;
}

unsigned int PcmRealTimeMixer::Advance( unsigned int dwSpan )
{
	return (unsigned int)(dwSpan * ((double) m_SampleRate / 1000) * m_nChannal * 2) & 0xFFFE;
}

void PcmRealTimeMixer::Reset()
{
	for(int i = 0; i < MAX_MIXER_SOURCE; i++)
	{
		SourceInfo info;
		m_VecSrcInfo.push_back(info);
	}
	m_nLastOutputTime = 0;
	m_nCurMixLen = 0;
	ZeroMemory(m_MixBuffer, MAX_MIXER_BUFFER*2);

}

PcmRealTimeMixer::~PcmRealTimeMixer()
{

}


