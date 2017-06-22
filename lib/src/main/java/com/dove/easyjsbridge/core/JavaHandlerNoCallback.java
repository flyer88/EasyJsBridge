package com.dove.easyjsbridge.core;

/**
 * Created by flyer on 2017/6/20.
 * 直接调用 native 方法，没有返回值，没有回调
 * 入参来自 js
 */
public abstract class JavaHandlerNoCallback
        implements JavaHandler {

    /**
     * 同上，无入参
     * @param funcName native 函数名
     * @return js 函数的定义
     */
    protected String defineJsObj(String funcName){
        return "if(window." + getProjectObjectName() + " == undefined){" +
                    "window." + getProjectObjectName() + "= {};"+
                "};\n" +
                "if(" + "window." + getProjectObjectName() + "." + getJsObjectName() + "== undefined){\n" +
                    "window." + getProjectObjectName() + "." + getJsObjectName() + " = {\n" +
                        funcName + ":function(args){" +
                            "window.jsBridge.invoke('" + getJavaHandlerName() + "'," +
                                "'" + funcName + "'," +
                                "null,args" +
                            ")\n;" +
                        "}\n"+
                    "}\n"+
                "};\n";
    }

}
