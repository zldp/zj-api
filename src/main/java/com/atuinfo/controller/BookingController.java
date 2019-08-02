package com.atuinfo.controller;

import com.atuinfo.Interceptors.ErrorExptionInterceptor;
import com.atuinfo.service.BookingService;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;

/**
 * @author dp
 * @version 1.0.0
 * @date 2019-08-01 11:02
 */
@Before(ErrorExptionInterceptor.class)
public class BookingController extends Controller {
    @Inject
    private BookingService bookingService;

    /**
     * 取消预约
     */
    public void index(){
        String strRequest = HttpKit.readData(getRequest());
        // 调用方法直接返回
        renderText(bookingService.cancelBook(strRequest));
    }


    /**
     * 预约取号
     */
    public void BookingGetNo(){
        String strRequest = HttpKit.readData(getRequest());
        renderText(bookingService.bookingGetNo(strRequest));
    }
}
