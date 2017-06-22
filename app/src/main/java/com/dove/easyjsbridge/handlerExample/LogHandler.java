package com.dove.easyjsbridge.handlerExample;

import android.support.annotation.Keep;
import android.util.Log;

import com.dove.easyjsbridge.core.JavaHandlerNoCallback;

/**
 * Created by flyer on 2017/6/20.
 */

public class LogHandler extends JavaHandlerNoCallback{


    public LogHandler(){

    }

    @Keep
    public void log(String args){
        Log.e("js obj: " + getJsObjectName(),args);
    }

    public String getJsDefine(){
        return defineJsObj("log");
    }


    @Override
    public String getJsObjectName() {
        return LogHandler.class.getSimpleName();
    }

    @Override
    public String getProjectObjectName() {
        return "nativeUtils";
    }

    @Override
    public String getJavaHandlerName() {
        return LogHandler.class.getName();
    }

    @Override
    public String getDefaultNativeFunc() {
        return "log";
    }

}
