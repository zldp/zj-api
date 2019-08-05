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
            final List<Record> List= Db.find("Select * From 医疗卡类别 Where ID=?",0);
            if (List.size()==0&&List!=null){
                throw new ErrorMassageException("未找到卡类别ID信息！请与软件供应商联系！");
            }
            List<Map<String, Object>> Records = new ArrayList<>();
            for (Record record : List) {
                Map<String, Object> map = record.getColumns();
                Records.add(map);
            }
            Map<String, Object> result = Records.get(0);
            Map<String, Object> map = new HashMap<>();
            map.put("id", result.get("号序").toString());
            map.put("coding", result.get("编码").toString());
            map.put("name", result.get("名称").toString());
            map.put("shortName", result.get("短名").toString());
            map.put("assemblyUnit", result.get("部件").toString());
            map.put("base", result.get("备注").toString());
            map.put("partProject", result.get("特定项目").toString());
            map.put("clearingForm", result.get("结算方式").toString());
            map.put("cnCipher", result.get("卡号密文").toString());

//          CardInfo.卡号长度 = Convert.ToInt16(btFunc.GetNum(dr.Get("卡号长度").ToString()));
            map.put("cnLength", Func.GetNum(result.get("卡号长度").toString(),0));           //卡号长度 NUMBER(5)
            map.put("qsLogo", Func.GetNum(result.get("缺省标志").toString(),0));            //缺省标志 NUMBER(1)
            map.put("isFixed", Func.GetNum(result.get("是否固定").toString(),0));           //是否固定 NUMBER(1)
            map.put("isExeControl", Func.GetNum(result.get("是否严格控制").toString(),0));    //是否严格控制 NUMBER(1)
            map.put("isMadeByMyself", Func.GetNum(result.get("是否自制").toString(),0));        //是否自制 NUMBER(1)
            map.put("isExist", Func.GetNum(result.get("是否存在帐户").toString(),0));         //是否存在帐户 NUMBER(1)
            map.put("isRefund", Func.GetNum(result.get("是否退现").toString(),0));          //是否退现 NUMBER(1)
            map.put("isFullyWithdraw", Func.GetNum(result.get("是否全退").toString(),0));   //是否全退 NUMBER(1)
            map.put("isReuse", Func.GetNum(result.get("是否重复使用").toString(),0));     //是否重复使用 NUMBER(1)
            map.put("isStartUsing", Func.GetNum(result.get("是否启用").toString(),0));  //是否启用 NUMBER(1)
            map.put("pwdLength", Func.GetNum(result.get("密码长度").toString(),0));     //密码长度 NUMBER(2)
            map.put("pwdLengthLimit", Func.GetNum(result.get("密码长度限制").toString(),0));//密码长度限制 NUMBER(2)
            map.put("pwdRules", Func.GetNum(result.get("密码规则").toString(),0));//密码规则 NUMBER(2)
            map.put("fuzzyOrNot", Func.GetNum(result.get("是否模糊查找").toString(),0));  //是否模糊查找 NUMBER(1)
            map.put("pwdEntryRestriction", Func.GetNum(result.get("密码输入限制").toString(),0));//密码输入限制 NUMBER(1)
            map.put("defaultPassword", Func.GetNum(result.get("是否缺省密码").toString(),0));//是否缺省密码 NUMBER(1)
            map.put("riskClass", Func.GetNum(result.get("险类").toString(),0));//险类   NUMBER(3)
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
