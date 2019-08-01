package com.atuinfo.service;

import com.atuinfo.exception.ErrorMassageException;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author dp
 * @version 1.0.0
 * @date 2019-08-01 10:25
 */
@Before(Tx.class)
public class BookingService {
    public String cancelBook(String strRequest){
        // 用来判断属性是不是为空
        if (StrKit.isBlank(strRequest)) {
            throw new ErrorMassageException("strRequest不能为空");
        }
        /**
         * 逻辑代码
         */
        // 返回结果集
        String strResponse = "" +
                "<Response>\n" +
                "    <ReturnCode>0</ReturnCode>\n" +
                "    <ReturnInfo>预约取消（OutPatBookingCancel）交易成功！</ReturnInfo>" +
                "\n</Response>";
        return strResponse;
    }
}
