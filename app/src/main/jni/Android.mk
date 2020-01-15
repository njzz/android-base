LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ndktools

LOCAL_SRC_FILES := ndktools.cpp

include $(BUILD_SHARED_LIBRARY)