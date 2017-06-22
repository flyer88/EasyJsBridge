package com.dove.easyjsbridge.handlerExample;

import android.support.annotation.Keep;
import android.util.Log;

import com.dove.easyjsbridge.BuildConfig;
import com.dove.easyjsbridge.core.JavaHandlerWithCallback;

/**
 * Created by flyer on 2017/6/20.
 */

public class AppInfoHandler extends JavaHandlerWithCallback{

    @Keep
    public String getAppInfo(String callbackId,String args){
        Log.e(AppInfoHandler.class.getSimpleName(),"args from js:" + args);
        return "window.jsBridge.callback.invoke('" + callbackId + "',' "+ BuildConfig.VERSION_NAME + "');";
    }

    public String getJsDefine(){
        return defineJsFunc(getDefaultNativeFunc());
    }

    @Override
    public String getJsObjectName() {
        return AppInfoHandler.class.getSimpleName();
    }

    @Override
    public String getProjectObjectName() {
        return "nativeUtils";
    }

    @Override
    public String getJavaHandlerName() {
        return AppInfoHandler.class.getName();
    }

    @Override
    public void setCallType() {
        mCallType = IMMEDIATE_CALL_JS;
    }

    @Override
    public String getDefaultNativeFunc() {
        return "getAppInfo";
    }
}
