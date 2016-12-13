LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := libspeex_static

# 遍历目录及子目录的函数
define walk
    $(wildcard $(1)) $(foreach e, $(wildcard $(1)/*), $(call walk, $(e)))
endef

 
# 遍历CDP目录
ALLFILES = $(call walk, $(LOCAL_PATH)/../)

# 从所有文件中提取出所有.cpp和.c文件
FILE_LIST += $(filter %.cpp, $(ALLFILES))
FILE_LIST += $(filter %.c, $(ALLFILES))
FILE_LIST += $(filter %.cc, $(ALLFILES))

LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_C_INCLUDES := ../

LOCAL_CFLAGS := -DUSE_KISS_FFT -DFIXED_POINT -DEXPORT="" -UHAVE_CONFIG_H

ifeq ($(APP_OPTIM), debug)
    LOCAL_CFLAGS += -DANDROID_DEV
else
    LOCAL_CFLAGS += -DANDROID_PROD
endif

include $(BUILD_STATIC_LIBRARY)

