

### About

This is An `Android` library for `Java` and `JavaScript` communication .
iOS repo [OCTWebViewBridge](https://github.com/octree/OCTWebViewBridge)


### Important

**All is builded by `webview.loadUrl()` , which means this repo is not an `JavaScript` interpreter .**

**This bridge handle below problems between Js and java communication**

+ `@JavaScriptInterface` cannot inject `window.mine.func`  into `JavaScript`
+ `@JavaScriptInterface` cannot handle the callback from `JavaScript`



### Principle

see this article [WebView JS Java communication](https://github.com/flyer88/EasyJsBridge/blob/master/WebView%20%E7%9A%84%20JS%20%E5%92%8C%20Java%20%E9%80%9A%E4%BF%A1%E9%97%AE%E9%A2%98.md)



### Usage

#### First

In Java create a handler, like below `LogHandler` ,and override functions.

```java
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

```

#### Second

In JavaScript just call it.

```javascript
function callJava() {            			       	 	    
    window.nativeUtils.LogHandler.log("123")
}
```

