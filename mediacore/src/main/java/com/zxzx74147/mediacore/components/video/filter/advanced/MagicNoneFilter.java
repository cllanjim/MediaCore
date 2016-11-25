package com.zxzx74147.mediacore.components.video.filter.advanced;


import com.zxzx74147.mediacore.R;
import com.zxzx74147.mediacore.components.video.filter.base.gpuimage.GPUImageFilter;
import com.zxzx74147.mediacore.components.video.util.OpenGlUtils;

public class MagicNoneFilter extends GPUImageFilter {

	public MagicNoneFilter(){
		super(NO_FILTER_VERTEX_SHADER, OpenGlUtils.readShaderFromRawResource(R.raw.default_no_filter_fragment));
	}
	
	protected void onDestroy() {
        super.onDestroy();

    }

	protected void onInit(){
		super.onInit();
	}
	
	protected void onInitialized(){
		super.onInitialized();
	}
}
