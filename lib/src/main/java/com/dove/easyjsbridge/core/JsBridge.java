package com.dove.easyjsbridge.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by flyer on 2017/6/16.
 */

public class JsBridge {

    public final static String DEFAULT_PROJECT_NAME =  "jsProject";

    private HashMap<String,JavaHandler> mJsCallJavaMap = new HashMap<>();

    private WebView mWebView;
    private Handler mHandler;

    private JsBridge(){
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    @JavascriptInterface
    public void callJava(String json){
        // 解析 json 看起参数，如果参数有 callback，那么生成一个 callback 函数
        // 否则直接分发调用本地方法
        NativeObject javaObject = parseJson(json);
        dispatchMessage(javaObject);
    }

    /**
     * 解析 Js 端传入的 json 数据
     * @param json Js 端 invoke 方法中调用
     *             callJava 方法传入的 json 数据
     * @return {@linkplain NativeObject}
     */
    private NativeObject parseJson(String json){
        JsonNode jsonNode = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonNode = objectMapper.readTree(json);
        } catch (IOException e) {
            Log.e("JsBridge","error json 解析错误，js 调用传入的参数错误!!");
        }
        NativeObject javaObject = new NativeObject();
        javaObject.objectId = jsonNode.get("objectId").asText();
        javaObject.callbackId = jsonNode.get("callbackId").asText();
        javaObject.methodName = jsonNode.get("methodName").asText();
        javaObject.args = parseArgs(jsonNode.get("args"));
        return javaObject;
    }

    /**
     * 考虑三种情况
     * 类型一：调用 Java 方法，立即执行 native 方法，无返回值
     * 类型二：调用 Java 方法，立即执行 native 方法，有返回值，
     *        然后将返回值插入并运行 js
     * 类型三：调用 Java 方法，callback 形式，记录 callback id，
     *        不是立刻触发，等待 native 触发，有无返回值都可，
     *        native 触发时触发对应 callbackId，然后插入并运行 js代码
     * @param nativeObject
     */
    private void dispatchMessage(NativeObject nativeObject) {
        String objectId = nativeObject.objectId;
        String callbackId = nativeObject.callbackId;
        String methodName = nativeObject.methodName;
        String args = nativeObject.args;

        if (!mJsCallJavaMap.containsKey(objectId)) {
            Log.e("JsBridge","JS 调用的对象未注册");
            return;
        }
        JavaHandler javaHandler= mJsCallJavaMap.get(objectId);
        if ("-1".equals(callbackId)
                && javaHandler instanceof JavaHandlerNoCallback) {
            // callback id 为空说不需要回调
            // 对象 类型是 JavaHandlerNoCallback
            // 直接调用对应的方法
            dynamicInvokeNativeFunc0(javaHandler,methodName,args);
            return;
        }
        if (!"-1".equals(callbackId)
                &&javaHandler instanceof JavaHandlerWithCallback){
            if (((JavaHandlerWithCallback) javaHandler).getCallType()
                    == JavaHandlerWithCallback.IMMEDIATE_CALL_JS){
                // 调用后立即返还值到 js 端，需要执行 webview.loadUrl
                // 此处应该还需要 callbackId
                final String jsCode = dynamicInvokeNativeFunc1(javaHandler,methodName,callbackId,args);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:" + jsCode);
                    }
                });
                return;
            } else {
                String jsCode = dynamicInvokeNativeFunc1(javaHandler,methodName,callbackId,args);
                // 不立即执行，等待 native 触发的 callback
                // 其实就是挂的那个方法记录了 callbackId，然后还有 args，然后 native 再调用 js
                return;
            }
        }
        Log.e("JsBridge","JS 调用入参错误");
    }

    /**
     * 动态调用本地方法
     * @param javaHandler 最初 jsBridge 注入对象的 objectId
     *                    {@linkplain JavaHandlerWithCallback#getJavaHandlerName()}
     * @param methodName 调用的方法名，默认值是 {@linkplain JavaHandlerWithCallback#getDefaultNativeFunc()}
     * @param callbackId 回调 id ，用于拼凑出 js 代码，然后直接调用 webview 运行 js
     * @param args 调用本地方法的入参
     * @return 返回 js 代码，运行 js，用于把结果返还个 js 端
     */
    private String dynamicInvokeNativeFunc1(JavaHandler javaHandler,String methodName,String callbackId,String args){
        try {
            Class clazz = javaHandler.getClass();
            Method method = clazz.getDeclaredMethod(methodName,String.class,String.class);
            return (String) method.invoke(javaHandler,callbackId,args);
        } catch (NoSuchMethodException e) {
            Log.e("JsBridge","native 调用错误");
        } catch (InvocationTargetException e) {
            Log.e("JsBridge","native 调用错误");
        } catch (IllegalAccessException e) {
            Log.e("JsBridge","native 调用错误");
        }
        return null;
    }

    /**
     * 动态调用本地方法
     * @param javaHandler 最初 jsBridge 注入对象的 objectId
     *                    {@linkplain JavaHandlerNoCallback#getJavaHandlerName()}
     * @param methodName 调用的方法名，默认值是 {@linkplain JavaHandlerNoCallback#getDefaultNativeFunc()}
     * @param args 调用本地方法的入参
     */
    private void dynamicInvokeNativeFunc0(JavaHandler javaHandler,String methodName,String args){
        try {
            Class clazz = javaHandler.getClass();
            Method method = clazz.getDeclaredMethod(methodName,String.class);
            method.invoke(javaHandler, args);
        } catch (NoSuchMethodException e) {
            Log.e("JsBridge","native 调用错误");
        } catch (InvocationTargetException e) {
            Log.e("JsBridge","native 调用错误");
        } catch (IllegalAccessException e) {
            Log.e("JsBridge","native 调用错误");
        }
    }

    /**
     * 解析 jsonNode 获取参数字符串
     * @param argNodes
     * @return
     */
    private String parseArgs(JsonNode argNodes){
        StringBuilder args = new StringBuilder();
        if ("ARRAY".equals(argNodes.getNodeType().name())){
            Iterator<JsonNode> iterator = argNodes.iterator();
            while (iterator.hasNext()){
                args = args.append(iterator.next().asText());
            }
        }
        return args.toString();
    }

    /**
     * 定义 callback 对象
     * @return 返还定义的 js 代码
     */
    private String defineCallback(){
         return "if(window.jsBridge.callback == undefined){\n" +
                "    window.jsBridge.callback = {\n" +
                "        index:0,\n" +
                "        caller:\"\",\n" +
                "        callerFunc:\"\",\n" +
                "        cache:{},\n" +
                "        invoke: function(id,args){\n" +
                "            let key = '' + id;\n" +
                "            let callbackFunc = window.jsBridge.callback.cache[key];\n" +
                "            callbackFunc(args);\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    /**
     * 定义 bridge invoke 方法
     * 所有的 js 方法调用本地，都会经过这个 invoke 方法
     * 例子 {@linkplain JavaHandlerNoCallback#defineJsObj(String)}
     * 或者 {@linkplain JavaHandlerWithCallback#defineJsFunc(String)}
     * invoke 方法被调用时，会调用 bridge 的 callJava 方法，该方法会调用
     * {@linkplain JsBridge#callJava(String)} , 通过 callJava 方法进行分发到具体的 native 方法
     * @return 返还定义的 js 代码
     */
    private String defineInvoke(){
        return "if(window.jsBridge.invoke == undefined){\n" +
                // 其实 callback 和 ...args 都是一样的是入参，但是区别在于，callback 是一个函数
                // 也就是说，这个 callback 是由 native 来唤起的，
                // 因此此处需要特别的标注出 callback，方便处理
                "    window.jsBridge.invoke = function(objectId,methodName,callback, ...args){\n" +
                    /**
                     * 调用 native 方法，如果有回调或者传入函数
                     * 那么需要对 callback 对象进行缓存
                     */
                "        let index = -1;\n" +
                "        if(callback != null && callback != undefined){\n" +
                "            window.jsBridge.callback.index += 1;\n" +
                "            index = window.jsBridge.callback.index;\n" +
                "            window.jsBridge.callback.cache['' + index] = callback;  \n" +
                "        }\n" +
                "        \n" +
                "        window.jsBridge.callJava(JSON.stringify({\n" +
                "                'objectId':objectId,\n" +
                "                'methodName':methodName,\n" +
                "                'callbackId': index,\n" +
                "                'args': args\n" +
                "            })\n" +
                "        );\n" +
                "    }\n" +
                "}";
    }

    /**
     * 定义 invoke 和 callback
     * @return 返还整个定义的 js 代码
     */
    public String injectJsBridge(){
        String callback = defineCallback();
        String invoke = defineInvoke();
        return callback + invoke;
    }

    /**
     * 调用 js 端，之前设置的 callback
     * @param callbackId
     * @param args
     * @return
     */
    public String invokeJsCallback(String callbackId, String args){
        return "window.jsBridge.callback.invoke(" + callbackId +"," + args +")";
    }


    /**
     * 注册 handler
     * @param objectId
     * @param javaHandler
     */
    public void registHandler(String objectId,JavaHandler javaHandler){
        mJsCallJavaMap.put(objectId,javaHandler);
    }

    public WebView getWebView() {
        return mWebView;
    }


    // build 模式
    // 方便后期添加功能加入参数
    public static class Builder{

        private JsBridge mJsBridge;

        public Builder(){
            this.mJsBridge = new JsBridge();
        }

        public Builder webView(WebView webView){
            mJsBridge.mWebView = webView;
            return this;
        }

        public JsBridge build(){
            return mJsBridge;
        }

    }

    public static class NativeObject {
        String objectId;
        String methodName;
        String callbackId;
        String args;
    }

}
