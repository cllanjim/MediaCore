package com.zxzx74147.mediacore.components.video.filter;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;

import com.zxzx74147.mediacore.components.video.filter.base.gpuimage.GPUImageFilter;
import com.zxzx74147.mediacore.components.video.filter.helper.MagicFilterFactory;
import com.zxzx74147.mediacore.components.video.filter.helper.MagicFilterType;
import com.zxzx74147.mediacore.components.video.util.OpenGlUtils;
import com.zxzx74147.mediacore.components.video.util.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public abstract class MagicDisplay implements Renderer {
	/**
	 * 所选择的滤镜，类型为MagicBaseGroupFilter
	 * 1.mCameraInputFilter将SurfaceTexture中YUV数据绘制到FrameBuffer
	 * 2.mFilters将FrameBuffer中的纹理绘制到屏幕中
	 */
	protected GPUImageFilter mFilter;
	
	/**
	 * 所有预览数据绘制画面
	 */
	protected final GLSurfaceView mGLSurfaceView;
	
	/**
	 * SurfaceTexure纹理id
	 */
	protected int mTextureId = OpenGlUtils.NO_TEXTURE;
	
	/**
	 * 顶点坐标
	 */
	protected final FloatBuffer mGLCubeBuffer;
	
	/**
	 * 纹理坐标
	 */
	protected final FloatBuffer mGLTextureBuffer;

	
	/**
	 * GLSurfaceView的宽高
	 */
	protected int mSurfaceWidth, mSurfaceHeight;
	
	/**
	 * 图像宽高
	 */
	protected int mImageWidth, mImageHeight;
	
	protected Context mContext;
	
	public MagicDisplay(Context context, GLSurfaceView glSurfaceView){
		mContext = context;
		mGLSurfaceView = glSurfaceView;  
		
//		mFilter = MagicFilterFactory.initFilters(MagicFilterType.NONE);
		
		mGLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.put(TextureRotationUtil.TEXTURE_NO_ROTATION).position(0);

		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setRenderer(this);
		mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	/**
	 * 设置滤镜
	 * @param
	 */
	public void setFilter(final MagicFilterType filterType) {
		mGLSurfaceView.queueEvent(new Runnable() {
       		
            @Override
            public void run() {
            	if(mFilter != null)
            		mFilter.destroy();
            	mFilter = null;
            	mFilter = MagicFilterFactory.initFilters(filterType);
            	if(mFilter != null)
	            	mFilter.init();
            	onFilterChanged();
            }
        });
		mGLSurfaceView.requestRender();
    }
	
	protected void onFilterChanged(){
		if(mFilter == null)
			return;
		mFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);

	}
	
	protected void onResume(){
		
	}
	
	protected void onPause(){
	}
	
	protected void onDestroy(){
		
	}
	

}
