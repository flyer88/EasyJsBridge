package com.dove.easyjsbridge.core;

/**
 * Created by flyer on 2017/6/18.
 * js 调用 Java
 * Java 运行结果，execJs 返回结果到 js 端
 * Java 记录 callback，native 触发，然后 execJs 返回结果到 js 端
 */
public abstract class JavaHandlerWithCallback implements JavaHandler {

    public static int IMMEDIATE_CALL_JS = 1;
    public static int NATIVE_CALL_JS = 2;

    protected int mCallType;

    /**
     * 设置调用类型
     *
     * {@linkplain JavaHandlerWithCallback#IMMEDIATE_CALL_JS}
     *  立即调用 js，返回结果
     *
     * {@linkplain JavaHandlerWithCallback#NATIVE_CALL_JS}
     * native 触发才调用 js，返回结果
     */
    public abstract void setCallType();

    public int getCallType() {
        return mCallType;
    }



    /**
     * 定义整个 callback 的 js 代码，调用时的参数来自 js
     * @param nativeFuncName 调用的 native 函数名
     * @return 返还定义的 js 代码
     */
    protected String defineJsFunc(String nativeFuncName){
        setCallType();
        return  "if(window." + getProjectObjectName() + " == undefined){" +
                    "window." + getProjectObjectName()  + "={}" +
                "};\n"+
                "window." + getProjectObjectName() + "." + getJsObjectName() + " = function(callback,args){" +
                    "window.jsBridge.invoke('" + getJavaHandlerName() + "'," +
                        "'" + nativeFuncName + "'," +
                        "callback," +
                        "args" +
                    ");" +
                "};\n";
    }

    /**
     * 例子中暂时没有用到
     *
     * 定义整个 callback 的 js 代码，调用时的参数来自本地，塞入 js 端
     * @param nativeFuncName 调用的 native 函数名
     * @param nativeArgs 定义 js 时调用需要的参数
     * @return 整个定义的 js 代码
     */
    protected String defineJsFunc(String nativeFuncName, String nativeArgs){
        setCallType();
        return "if(window." + getProjectObjectName() + " == undefined){" +
                        "window." + getProjectObjectName()  + "={}" +
                "};\n"+
                "window." + getProjectObjectName() + "." + getJsObjectName() + " = function(callback){" +
                    "window.jsBridge.invoke('" + getJavaHandlerName() + "'," +
                        "'" + nativeFuncName + "'," +
                        "callback," +
                        "'"+ nativeArgs + "'" +
                    ");" +
                "};\n";
    }
}
