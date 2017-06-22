package com.dove.easyjsbridge.core;

/**
 * Created by flyer on 2017/6/20.
 */

public interface JavaHandler {

    /**
     * 构建 js 对象的名字
     * window.{@linkplain JavaHandler#getProjectObjectName()}.{@linkplain JavaHandler#getJsObjectName()}
     * @return 返回 pluginName
     */
    String getJsObjectName();

    /**
     * 整个项目的名字，window.{@linkplain JavaHandler#getProjectObjectName()}
     * 所有的注入点的对象都是挂在 projectName 下面的
     * @return projectName
     */
    String getProjectObjectName();

    /**
     * native 的 {@linkplain JavaHandler} 的名字
     * 当前类的全名，用于直接反射调用
     * @return 返回 javaCall 名字
     */
    String getJavaHandlerName();


    /**
     * 默认的本地方法名，用来处理 Js 端的 callback 内容
     * @return 本地方法名，一定要一致，不然无法反射
     */
    public abstract String getDefaultNativeFunc();

}
