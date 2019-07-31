package com.atuinfo.Interceptors;

import com.atuinfo.core.Result;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import org.eclipse.jetty.http.HttpStatus;

public class ExceptionInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation invocation) {
        try {
            invocation.invoke();
        }catch (Exception e){

            Result result = new Result(HttpStatus.INTERNAL_SERVER_ERROR_500,"服务器内部出错",null);

            invocation.getController().renderJson(result);
        }
    }
}
