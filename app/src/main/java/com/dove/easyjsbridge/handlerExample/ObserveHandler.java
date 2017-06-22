package com.dove.easyjsbridge.handlerExample;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Keep;
import android.webkit.WebView;

import com.dove.easyjsbridge.core.JavaHandlerWithCallback;
import com.dove.easyjsbridge.core.JsBridge;

import static com.dove.easyjsbridge.core.JsBridge.DEFAULT_PROJECT_NAME;

/**
 * Created by flyer on 2017/6/17.
 */

public class ObserveHandler extends JavaHandlerWithCallback {


    private String projectName = DEFAULT_PROJECT_NAME;
    private JsBridge jsBridge;
    private Handler mHandler;
    private String mCallbackId;

    public ObserveHandler(JsBridge jsBridge, String projectName){
        this.jsBridge = jsBridge;
        this.projectName = projectName;
    }

    public String getJsDefine(){
        return defineJsFunc(getDefaultNativeFunc());
    }

    @Keep
    public void execObserve(String callbackId,String args){
        mCallbackId = callbackId;
    }

    public void call(final WebView webView, String args){
        final String jsCode = jsBridge.invokeJsCallback(mCallbackId,args);
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            webView.loadUrl("javascript:" + jsCode);
        } else {
            if (mHandler == null) {
                mHandler = new Handler(Looper.getMainLooper());
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:" + jsCode);
                }
            });
        }
    }

    @Override
    public String getJsObjectName() {
        return ObserveHandler.class.getSimpleName();
    }

    @Override
    public void setCallType() {
        mCallType = NATIVE_CALL_JS;
    }

    @Override
    public String getProjectObjectName() {
        return projectName;
    }

    @Override
    public String getJavaHandlerName() {
        return ObserveHandler.class.getName();
    }

    @Override
    public String getDefaultNativeFunc() {
        return "execObserve";
    }


}
