### Java 和 JS 通信基本介绍

`Android` 上基于 `WebView` 的 `JS` 和 `Java` 通信机制非常简单

1. `Java` 插入 `JS` 借助 `webView.loadUrl("javascript:xxx")` 即可

2. `Js` 调用 `native` 方法

   `Java` 本地写类，同时对该类的方法`xxx` 标志上 `@JavaScriptInterface `

   然后调用` webView.addJavascriptInterface(new NativeObjce,"native")`

   Js 端调用 `window.native.xxx()` 即可



### `JS` 调用 `Java` 限制太多

1. 无法创建  `window.native.logUtils.log()` 此类方法

   即，所有的 `JS` 对象创建都只能创建在 `window` 对象下面，并且无法再往下一层

2. 无法处理回调

   即， 没办法创建 `window.native.showDialog(function (json) {xxxx})` 此类方法

   ​

### 解决方案

首先感谢 [octree](https://github.com/octree/OCTWebViewBridge) 提供的思路，通过一个注入点去搞定

`Java` 无法创建 `JS` 对象中的对象，但是 `JS` 自己可以，因此，可以借助 `webView.loadUrl("javascript:"xxx)` 实现

然后问题来了，`JS` 自己创建的对象是无法调用到 `Java` 层的，那该如何是好，参考如下



#### 第一个限制解决

Java 端调用 `webView.addJavascriptInterface(new JsBridge(),"jsBridge");`  注入 `jsBridge` 对象

```java
public class JsBridge {
  @JavascriptInterface
    public void callJava(String json){
      // 反射直接唤起，具体可参看库的源码
    }
}
...
webView.addJavascriptInterface(new JsBridge(),"jsBridge");
```

然后调用 `webView.loadUrl()`, 在 `JS` 端写入如下代码，当然也可以在 `JS` 端直接写好

```javascript
if(window.jsBridge.invoke == undefined){
    window.jsBridge.invoke = function(objectId,methodName, ...args){
        window.jsBridge.callJava(JSON.stringify({
                'objectId':objectId,
                'methodName':methodName,
                'args': args
            })
        );
    }
}
```

最后，创建 `LogHandler` 对象的 `JS` 代码如下

```javascript
if(window.natvieUtils == undefined){window.nativeUtils = {}}
if(window.nativeUtils.LogHandler == undefined){window.nativeUtils.LogHandler = {}}
window.nativeUtils.LogHandler.log = function (args){
  window.jsBridge.invoke('com.dove.easyjsbridge.LogHandler','log',args);
}
```

`Java` 端则只需要调用 `webView.loadUrl("javascript:" + jsCode)` 就可以( `jsCode` 就是上面的 `LogHandler` 创建的 `JS` 代码)



**注入整个链路就是**

> `Java` 端借助`webView.addJavascriptInterface` 将 `JsBridge` 注入到 `JS `
>
> — > 然后调用 `webView.loadUrl("javascript:" + jsCode)` 
>
> ​	将 `window.jsBridge.invoke` 方法和 `LogHandler` 的 `jsCode `一起注入
>
> —> `JS` 调用 `window.nativeUtils.LogHandler.log `



**JS 端调用的链路**

>`window.nativeUtils.LogHandler.log` // `JS` 调用层
>
>—> `window.jsBridge.invoke('com.dove.easyjsbridge.LogHandler','log',args);` // `JS` 调用层
>
>—> `window.jsBridge.callJava(JSON.stringify({'objectId':objectId,'methodName':methodName,'args': args}));`// `JS` 调用层
>
>—> `JsBridge.callJava()` // `Java` 调用层
>
> —> 反射直接唤起对应的 `Java` 方法  // `Java` 调用层





#### 第二个限制解决

`JS` 端试图调用 `window.native.showDialog(function (json) {xxxx})`，此时需要缓存这个 `callback`

整体调用流程和上面类似，在插入`window.jsBridge.invoke` 方法时  ,加入一个 `callbackId` 的缓存就可以解决，代码如下

```javascript
if(window.jsBridge.callback == undefined){
    window.jsBridge.callback = {
        index:0,
        caller:"",
        callerFunc:"",
        cache:{},
        invoke: function(id,args){
            let key = '' + id;
            let callbackFunc = window.bridge.callback.cache[key];
            callbackFunc(args);
        }
    }
}
if(window.jsBridge.invoke == undefined){
    window.jsBridge.invoke = function(objectId,methodName,callback, ...args){
        let index = -1;
        if(callback != null && callback != undefined){
            window.bridge.callback.index += 1;
            index = window.bridge.callback.index;
            window.jsBridge.callback.cache['' + index] = callback;  
        }  
        window.jsBridge.callJava(JSON.stringify({
                'objectId':objectId,
                'methodName':methodName,
                'callback': callback,
                'args': args
            })
        );
    }
}
```



Java 端对于 `callback` 的解决方案决定 `handler` 自己的目的，可以缓存一个 `callbackId`，也可以缓存多个 `callbackId`，然后在用户操作或者合适的时机调用对应的 `callback` 即可

```java
String jsCode = "window.jsBridge.callback.invoke(" + callbackId +"," + args +")"
webView.loadUrl("javascript:" + jsCode);
```





#### 本方案碰到的问题

1. `window.jsBridge.callback` 和 `window.jsBridge.invoke` 方法注入时机不好把握
2. 调用 `JsBridge.callJava` 时线程需要切换
3. 反射方法的直接调用有一定性能问题

对于第一个问题，[safe-java-js-webview-bridge](https://github.com/pedant/safe-java-js-webview-bridge) 参考了该开源库的做法，在 `progress > 25` 的时候进行注入

第二个问题，用 handler 进行线程切换

第三个问题暂无解决





