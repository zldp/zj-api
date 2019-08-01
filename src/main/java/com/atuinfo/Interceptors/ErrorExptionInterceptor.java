package com.atuinfo.Interceptors;

import com.atuinfo.core.Result;
import com.atuinfo.exception.ErrorMassageException;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import org.eclipse.jetty.http.HttpStatus;

/**
 * @author dp
 * @version 1.0.0
 * @date 2019-08-01 10:54
 */
public class ErrorExptionInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation invocation) {
        try {
            invocation.invoke();
        } catch (ErrorMassageException e) {
            e.printStackTrace();
            String strResponse = "" +
                    "<Response>\n" +
                    "    <ReturnCode>1</ReturnCode>\n" +
                    "    <ReturnInfo>"+e.getMessage()+"</ReturnInfo>\n" +
                    "</Response>";
            invocation.getController().renderText(strResponse);
        }

    }
}
