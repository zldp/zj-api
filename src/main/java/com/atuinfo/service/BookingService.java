package com.atuinfo.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atuinfo.exception.ErrorMassageException;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.Map;

/**
 * @author dp
 * @version 1.0.0
 * @date 2019-08-01 10:25
 */
@Before(Tx.class)
public class BookingService {
    public String cancelBook(String strRequest){
        Map<String, Object> result = JSONObject.parseObject(strRequest, new TypeReference<Map<String, Object>>() {
        });

        // 用来判断属性是不是为空
        if (null==result.get("account")) {
            throw new ErrorMassageException("account不能为空");
        }
        if (null == result.get("password")) {
            throw new ErrorMassageException("password不能为空");
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
