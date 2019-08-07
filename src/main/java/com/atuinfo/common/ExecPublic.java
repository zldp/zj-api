package com.atuinfo.common;

import com.atuinfo.exception.ErrorMassageException;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yz
 * @company Atu
 * @create  2019-08-02 11:08
 *
 */
public class ExecPublic {
    /**
     * 获取身份识别卡类别
     * @param BankCardType
     * @return
     */
    public static long GetHisCardTypeID(String BankCardType)
    {
        long HisCardTypeID = 0;
        switch (BankCardType)
        {
            case "1"://1.就诊卡
                HisCardTypeID = 1;
                break;
            case "2"://2.建行卡
                HisCardTypeID = 182;
                break;
            case "4"://4.身份证
                HisCardTypeID = 2;
                break;
            default:
                HisCardTypeID = Long.parseLong(BankCardType);
                break;
            //throw new Exception("未找到卡类别ID【" + BankCardType + "】的信息！请与软件供应商联系！");
        }
        if (HisCardTypeID == -1)
            throw new ErrorMassageException("此类别的卡暂不被支持！请与软件供应商联系！");
        return HisCardTypeID;
    }


    /**
     *根据银行传入支付方式
     * @param intCardTypeID
     */
    public static void InitCardInfo(long intCardTypeID) {
//        btCommand cmd = new btCommand("");
//        btDataReader dr = new btDataReader();
        try {
            final Record List= Db.findFirst("Select * From 医疗卡类别 Where ID=?",intCardTypeID);
            if (List==null){
                throw new ErrorMassageException("未找到卡类别ID信息！请与软件供应商联系！");
            }

            Map<String, Object> result = List.getColumns();
            CardInfo.setP_intID((Integer) result.get("号序"));
            CardInfo.setY_str编码(StrUtil.objToStr(result.get("编码")));
            CardInfo.setY_str名称(StrUtil.objToStr(result.get("名称")));
            CardInfo.setY_str短名(StrUtil.objToStr(result.get("短名")));
            CardInfo.setY_str部件(StrUtil.objToStr(result.get("部件")));
            CardInfo.setY_str备注(StrUtil.objToStr(result.get("备注")));
            CardInfo.setY_str特定项目(StrUtil.objToStr(result.get("特定项目")));
            CardInfo.setY_str结算方式(StrUtil.objToStr(result.get("结算方式")));
            CardInfo.setY_str卡号密文(StrUtil.objToStr(result.get("卡号密文")));
            CardInfo.setY_int卡号长度((Integer) result.get("卡号长度"));
            CardInfo.setY_int缺省标志((Integer) result.get("缺省标志"));
            CardInfo.setY_int是否固定((Integer) result.get("是否固定"));
            CardInfo.setY_int是否严格控制((Integer) result.get("是否严格控制"));
            CardInfo.setY_int是否自制((Integer) result.get("是否自制"));
            CardInfo.setY_int是否存在帐户((Integer) result.get("是否存在帐户"));
            CardInfo.setY_int是否退现((Integer) result.get("是否退现"));
            CardInfo.setY_int是否全退((Integer) result.get("是否全退"));
            CardInfo.setY_int是否重复使用((Integer) result.get("是否重复使用"));
            CardInfo.setY_int是否启用((Integer) result.get("是否启用"));
            CardInfo.setY_int密码长度((Integer) result.get("密码长度"));
            CardInfo.setY_int密码长度限制((Integer) result.get("密码长度限制"));
            CardInfo.setY_int密码规则((Integer) result.get("密码规则"));
            CardInfo.setY_int是否模糊查找((Integer) result.get("是否模糊查找"));
            CardInfo.setY_int是否缺省密码((Integer) result.get("是否缺省密码"));
            CardInfo.setY_int险类((Integer) result.get("险类"));
            CardInfo.setY_int是否模糊查找((Integer) result.get("是否模糊查找"));
            CardInfo.setY_int是否模糊查找((Integer) result.get("是否模糊查找"));
            CardInfo.setY_int是否模糊查找((Integer) result.get("是否模糊查找"));
            CardInfo.setY_int是否模糊查找((Integer) result.get("是否模糊查找"));
            CardInfo.setY_int是否模糊查找((Integer) result.get("是否模糊查找"));


        }catch (Exception e){
            //dr.Close();
            //cmd.Close();
            //将错误抛向上一层
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public static CardTypeItem CardInfo = new CardTypeItem();




}
