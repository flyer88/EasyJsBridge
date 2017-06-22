// js bridge
if(window.jsBridge == undefined){
    window.jsBridge = {
    }
}

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
        /**
         * 调用 native 方法，如果有回调或者传入函数
         * 那么需要对 callback 对象进行缓存
         * 
         */
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

// plugin
window.tdfire.observe = function(callback){
    window.bridge.invoke(callback.caller,callback.callerFunc,callback,"")
}