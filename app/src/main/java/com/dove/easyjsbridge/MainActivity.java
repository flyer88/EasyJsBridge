package com.dove.easyjsbridge;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.dove.easyjsbridge.core.JsBridge;
import com.dove.easyjsbridge.handlerExample.AppInfoHandler;
import com.dove.easyjsbridge.handlerExample.LogHandler;
import com.dove.easyjsbridge.handlerExample.ObserveHandler;

public class MainActivity extends AppCompatActivity {

    RelativeLayout mRelativeLayout;
    boolean mIsCreate = false;
    String BUSINESS_URL = "file:///android_asset/index.html";
    JsBridge mJsBridge;
    LogHandler mLogHandler;
    AppInfoHandler mAppInfoHandler;
    ObserveHandler mObserveHandler;

    boolean mLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.root_rl);
        Button button = (Button) findViewById(R.id.show_data);
        Button invoke = (Button) findViewById(R.id.invoke_callback);
        final WebView webView = (WebView) findViewById(R.id.web_view);
        final WebSettings webSettings = webView.getSettings();
        initWebSettings(webSettings);
        mLogHandler = new LogHandler();
        mAppInfoHandler = new AppInfoHandler();
        mJsBridge = new JsBridge.Builder()
                .webView(webView)
                .build();
        mObserveHandler = new ObserveHandler(mJsBridge,"dove");
        webView.addJavascriptInterface(mJsBridge,"jsBridge");
        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("AddJavascriptInterface")
            @Override
            public void onClick(View view) {
                if (!mLoaded) {
                    webView.loadUrl(BUSINESS_URL);
                    mLoaded = true;
                } else {
                    mIsCreate = false;
                    webView.reload();
                }
            }
        });
        invoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mObserveHandler.call(mJsBridge.getWebView(),"{hello:'123'}");
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 25 && !mIsCreate){
                    mJsBridge.registHandler(mLogHandler.getJavaHandlerName(),mLogHandler);
                    mJsBridge.registHandler(mAppInfoHandler.getJavaHandlerName(),mAppInfoHandler);
                    mJsBridge.registHandler(mObserveHandler.getJavaHandlerName(),mObserveHandler);

                    // 去监听 html 文档载入，如果载入成功，载入 jsBridge 对象
                    StringBuilder logDefine = new StringBuilder(mLogHandler.getJsDefine());
                    StringBuilder appInfoDefine = new StringBuilder(mAppInfoHandler.getJsDefine());
                    StringBuilder observeDefine = new StringBuilder(mObserveHandler.getJsDefine());
                    StringBuilder jsBridge = new StringBuilder(mJsBridge.injectJsBridge());
                    StringBuilder jsCode =  jsBridge
                            .append(logDefine)
                            .append(appInfoDefine)
                            .append(observeDefine);
                    view.loadUrl("javascript:" + jsCode);
                    mIsCreate = true;
                }
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initWebSettings(WebSettings webSettings){
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUserAgentString("ios");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }
}
