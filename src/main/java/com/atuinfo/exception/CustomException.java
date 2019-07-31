package com.atuinfo.exception;

/**
 * 自定义异常(CustomException)
 * @author dp926454
 * @date 2018/8/30 13:59
 */
public class CustomException extends RuntimeException {

    public CustomException(String msg){
        super(msg);
    }

    public CustomException() {
        super();
    }
}
