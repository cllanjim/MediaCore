/*
 * pcmprocess.h
 *
 *  Created on: 2014-11-10
 *      Author: a33
 */

#ifndef PCMPROCESS_H_
#define PCMPROCESS_H_



#include <vector>
#include <set>
#include <map>
#include "../libspeex/speex/speex_resampler.h"
#include "membuffer.h"

class PcmProcess
{

public:

	static bool stereo_to_mono(const unsigned char* pbyteInBuffer,
		unsigned int dwInLength,
		unsigned char* pbyteOutBuffer,
		unsigned int& dwOutLength	/*in, out*/
		);
	static int mix(unsigned char* pbyteDest,
		unsigned int& dwDestLen/*in, out*/,
		unsigned int dwDestCap,
		const unsigned char* pbyteSrc,
		unsigned int dwSrcLen);

	PcmProcess();
	~PcmProcess();

	/*
	remark: the dwOutLength may less or large than dwInLength * dwOutSampleRate / dwInSampleRate;
	*/
	bool pcm_convert(const unsigned char* pbyteInBuffer,
		unsigned int dwInLength,
		unsigned int dwInSampleRate,
		unsigned int dwChannal,
		unsigned char* pbyteOutBuffer,
		unsigned int& dwOutLength,		/*in, out*/
		unsigned int dwOutSampleRate);


private:

	void Revise(int nChannal, unsigned int dwInSampleRate, unsigned int dwOutSampleRate);
	void Reset(int nChannal, unsigned int dwInSampleRate, unsigned int dwOutSampleRate);

	int  m_nChannal;
	int  m_nSampleRateIn;
	int  m_nSampleRateOut;

	SpeexResamplerState* m_pSt;


};



#define MAX_SOURCE_BUFFER    (4410 * 8)
#define MAX_MIXER_BUFFER	 (4410 * 16)
#define MAX_MIXER_SOURCE	 3
#define MAX_COUNT_PER_MIXER  (4410 * 40)

class PcmRealTimeMixer {
public:
	PcmRealTimeMixer(unsigned int nOutputSize, unsigned int nSampleRate, unsigned int nChannal);
	~PcmRealTimeMixer();

	bool AddSource(unsigned int& ID, unsigned int nGap, unsigned int nSampleRate, unsigned int nChannal);
	void RemoveSource(unsigned int ID);

	bool InputData(unsigned int ID, const unsigned char* pByInBuffer, unsigned int dwInlength, unsigned int uTick, unsigned char* pbyteOutBuffer, unsigned int& dwOutLength/*in, out*/);

private:
	unsigned int Advance(unsigned int dwSpan);
	void Reset();

	struct SourceInfo
	{
		unsigned int nCurpos;
		unsigned int nLastTick;
		unsigned int nIdealGap;
		unsigned int nSampleRate;
		unsigned int nChannal;
		bool bSync;
		SourceInfo():nCurpos(0), nIdealGap(100),nLastTick(0),bSync(true){}
	};

	std::vector<SourceInfo> m_VecSrcInfo;
	std::set<unsigned int> m_IDSet;
	std::set<unsigned int> m_IDWorkSet;

	unsigned int m_SampleRate;
	unsigned int m_nChannal;
	unsigned int m_nLastOutputTime;
	unsigned int m_nCurMixLen;

	unsigned char m_MixBuffer[MAX_MIXER_BUFFER*2];
	unsigned int m_nPckSize;

	PcmProcess	  m_ReSample;
	unsigned char m_SourceBuffer[MAX_SOURCE_BUFFER];

};



#endif /* PCMPROCESS_H_ */
