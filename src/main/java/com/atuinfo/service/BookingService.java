package com.atuinfo.service;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atuinfo.common.ExecPublic;
import com.atuinfo.common.Initiation;
import com.atuinfo.common.UserInfo;
import com.atuinfo.exception.ErrorMassageException;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.*;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yz
 * @version 1.0.0
 * @date 2019-08-01 10:25
 */
@Before(Tx.class)
public class BookingService {
    /**
     * 取消预约
     * @param strRequest
     * @return
     */
    public String cancelBook(String strRequest){
        Map<String, Object> result = JSONObject.parseObject(strRequest, new TypeReference<Map<String, Object>>() {});
        final String bookingOrderId= String.valueOf(result.get("bookingOrderId"));
        //判断当前号源是否存在
        final List<Record> recordList=Db.find(" Select A.诊室,A.门诊号,A.姓名,A.性别,A.年龄,B.费别,C.编码 As 付款方式,发生时间,号序\n" + "  From 病人挂号记录 A,病人信息 B,医疗付款方式 C\n" +
                "\tWhere A.病人ID=B.病人ID And B.医疗付款方式=C.名称 and A.No=? and A.预约=1 And A.记录性质=2 and A.记录状态=1",bookingOrderId);
        //System.out.println(recordList.size());
        if(recordList.size()==0&&recordList!=null){
            throw new ErrorMassageException("号源不存在");
        }
        List<Map<String, Object>> nursingRecords = new ArrayList<>();
        for (Record record : recordList) {
            Map<String, Object> map = record.getColumns();
            nursingRecords.add(map);
        }
        //调用取消预约存储过程
        Db.execute(new ICallback() {
            @Override
            public Object call(Connection conn) throws SQLException {
                List<Record> result = null ;
                ResultSet rs = null;
                try {

                    CallableStatement proc = conn.prepareCall("{Call Zl_三方机构挂号_Delete (?,?)}");
                    proc.setString(1, bookingOrderId);
                    proc.setString(2,"取消预约");

                    rs = proc.executeQuery();
                    result = RecordBuilder.me.build(DbKit.getConfig(), rs);;
                } catch (SQLException e) {
                    e.getErrorCode();
                    throw new ErrorMassageException("执行Zl_三方机构挂号_Delete存储过程错误");
                }
                return result;
            }
        });
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

    /**
     * 预约取号
     * @param strRequest
     * @return
     */
    public String bookingGetNo(String strRequest){
        double dbl冲预交 = 0;
        String str医保结算方式 = "";
        String strRem = "";

        //获取前台传过来的参数
        Map<String, Object> result = JSONObject.parseObject(strRequest, new TypeReference<Map<String, Object>>() {});
        final String bookingOrderId= String.valueOf(result.get("bookingOrderId"));//预约单编号
        final String thirdPartyNo=String.valueOf(result.get("thirdPartyNo"));  //第三方支付流水号
        final String outTradeNo=String.valueOf(result.get("outTradeNo"));   //业务订单号
        final String payFee=String.valueOf(result.get("payFee"));//     挂号：支付费用
        final String tradeType=String.valueOf(result.get("tradeType"));//支付方式

        // 判断三方返回的费用与总费用是否相等
        final List<Record> recordList=Db.find("  Select 病人ID,nvl(sum(实收金额),0) as 金额 From 门诊费用记录 Where NO=? and 记录性质=4 group by 病人id",bookingOrderId);
        if(recordList.size()==0&&recordList!=null){
            throw new ErrorMassageException("数据不存在");
        }
        //把查询的sql数据放进集合
        List<Map<String, Object>> nursingRecords = new ArrayList<>();
        for (Record record : recordList) {
            Map<String, Object> map = record.getColumns();
            nursingRecords.add(map);
        }
        //得到单个数据的值
        String patientId=nursingRecords.get(0).get("病人id").toString();//病人ID
        String sum= nursingRecords.get(0).get("金额").toString();//获取数据库的金额
        //将金额转换成金额类型
        BigDecimal MoneySum =  new BigDecimal(sum);    //数据库金额
        BigDecimal payFeeSum = new BigDecimal(payFee);//前台传递的金额
        // 如果两次金额不一致
        if(MoneySum!=payFeeSum){
            throw new ErrorMassageException("挂号金额合计与该号的价格不一致"); //提示错误
        }
        //获取病人预约信息
        final List<Record> PatientInfoList=Db.find(" \n" +
                "Select A.号别,A.诊室,A.门诊号,A.姓名,A.性别,A.年龄,B.费别,nvl(C.编码,'09') As 付款方式,发生时间,号序,D.ID As" +
                " 医生ID,D.姓名 As 医生姓名,A.执行部门ID As 科室ID From 病人挂号记录 A,病人信息 B,医疗付款方式 C,人员表 D " +
                "Where A.病人ID=B.病人ID And B.医疗付款方式=C.名称 And A.No=? And A.预约=1 And A.记录性质=2 And 执行人=D.姓名",bookingOrderId);
        //接收数据
        List<Map<String, Object>> Records = new ArrayList<>();
        for (Record record : PatientInfoList) {
            Map<String, Object> map = record.getColumns();
            Records.add(map);
        }
        if(Records.size()==0){//判断是否有值
            throw new ErrorMassageException("预约挂号单信息丢失或已经取号，请重新获取！");
        }
        //取值  后续会调用到
        String hb=Records.get(0).get("号别").toString();          //获取号别
        long   doctorId= (long) Records.get(0).get("医生ID");  //医生ID
        String startTime=Records.get(0).get("发生时间").toString();  //发生时间
        String doctorName=Records.get(0).get("医生姓名").toString();  //医生姓名
        long   departmentsId= (long) Records.get(0).get("科室ID");//科室ID
        String consultingRoom=Records.get(0).get("诊室").toString();//诊室
        String patientNumber=Records.get(0).get("门诊号").toString();//门诊号
        String name=Records.get(0).get("姓名").toString();//姓名
        String sex=Records.get(0).get("性别").toString();//性别
        String age=Records.get(0).get("年龄").toString();//年龄
        String payMethod=Records.get(0).get("付款方式").toString();//付款方式
        String fb=Records.get(0).get("费别").toString();//费别
        String hx=Records.get(0).get("号序").toString();//号序

        final List<Record> List=Db.find("Select 项目ID From 挂号安排 Where 号码=?",hb);
        //把查询的sql数据放进集合
        List<Map<String, Object>> rs = new ArrayList<>();
        for (Record record : List) {
            Map<String, Object> map = record.getColumns();
            rs.add(map);
        }
        if(rs.size()==0){
            throw new ErrorMassageException("预约挂号单收费信息已作废，请重新挂号！");
        }
        String ProjectId=Records.get(0).get("项目ID").toString();//项目ID

        ExecPublic.InitCardInfo(ExecPublic.GetHisCardTypeID(tradeType));
        //事务开始
//        cmd.BeginTrans();
//        blnTran = true;

        // 调用Zl_预约挂号接收_Insert全部以现金将病人进行接收。
        final List<Record> List1=Db.find("Select 病人结帐记录_id.nextval From dual");
        List<Map<String, Object>> rs1 = new ArrayList<>();
        for (Record record : List1) {
            Map<String, Object> map = record.getColumns();
            rs1.add(map);
        }
        //结账ID
        String NEXTVAL=rs1.get(0).get("nextval").toString();//病人结帐记录_id
        String strHISVERSION = Initiation.HISVERSION.trim();


       String strSql = "Call Zl_预约挂号接收_Insert(";
        //No
        strSql += "'" + bookingOrderId + "'";
        //实际票号
        strSql += ",NULL";
        //领用ID
        strSql += ",NULL";
        //结帐id
        strSql += "," + NEXTVAL.toString();
        //诊室
        strSql += ",'" + consultingRoom + "'";
        //病人ID
        strSql += "," + patientId.toString();
        //标识号【门诊号】
        strSql += "," + patientNumber;
        //姓名
        strSql += ",'" + name + "'";
        //性别
        strSql += ",'" + sex + "'";
        //年龄
        strSql += ",'" + age + "'";
        //付款方式
        strSql += ",'" + payMethod + "'";
        //费别
        strSql += ",'" + fb + "'";
        //结算方式
//        strSql += ",'" + ExecPublic.CardInfo.结算方式 + "'";
        //现金支付
        strSql += "," + payFeeSum;
        //预交支付
        strSql += ",0";
        //个帐支付
        strSql += ",0";
        //发生时间
        strSql += ",to_date('" + startTime + "','YYYY-MM-DD HH24:MI:SS')";
        //序号
        if (strHISVERSION != "35.120")
            strSql += "," + hx;
        //操作员编号
        strSql += ",'" + UserInfo.编号 + "'";
        //操作员姓名
        strSql += ",'" + UserInfo.姓名 + "'";
        //生成队列_In
        if (strHISVERSION != "35.120")
            strSql += ",1";
        //登记时间_In
        strSql += ",sysdate";
        //卡类别id_In
//        strSql += "," + ExecPublic.CardInfo.ID;
//        //结算卡序号_In
        strSql += ",Null";
//        //卡号_In
        strSql += ",'" + thirdPartyNo + "'";
//        //交易流水号_In
        strSql += ",'" + outTradeNo + "'";
//        //交易说明_In
        strSql += ",'" + strRem + "'";
        strSql += ")";

        Db.find(strSql);


        //⑥ 调用zl_病人结算记录_update更新医保结算方式。若为三方支付传入三方支付信息。
        if (!str医保结算方式.equals(""))
        {
            //提交挂号交易[扣预交金]
            if (dbl冲预交 > 0)
            {
                //走冲预交模式
                strSql = "Call zl_病人结算记录_update(" + NEXTVAL + ",'" + str医保结算方式 + "',0,Null,1)";
            }
            else
            {
                //走三方模式
                strSql = "Call zl_病人结算记录_update(" + NEXTVAL + "," +
                        "'" + str医保结算方式 + "',0,'" + ExecPublic.CardInfo.getP_intID() + "',0,'" + ExecPublic.CardInfo.getP_intID() + "',Null," +
                        "'" + thirdPartyNo + "','" + outTradeNo + "','" + strRem + "')";
            }
            Db.find(strSql);
        }

        //强制保存挂号订单号到病人挂号记录的交易水号
        strSql = "Update 病人挂号记录" +
                "    Set 交易流水号='" + outTradeNo + "',交易说明='" + strRem + "'" +
                "    Where 记录性质 = 1 And 记录状态 = 1 And No='" + bookingOrderId + "'";
        Db.find(strSql);

        //强制保存挂号订单号到病人预交记录的交易水号
        strSql = "Update 病人预交记录" +
                "    Set 交易流水号='" + outTradeNo + "',交易说明='" + strRem + "'" +
                "    Where 结帐id = " + NEXTVAL+ " And 卡类别ID=" + ExecPublic.CardInfo.getP_intID();
        Db.find(strSql);


        //⑦ 调用Zl_病人挂号汇总_Update更新病人挂号汇总。
        strSql = "Call Zl_病人挂号汇总_Update('";
        //医生姓名
        strSql += doctorName;
        //医生ID
        strSql += "'," + (doctorId > 0 ? doctorId : "Null");
        //收费细目ID
        strSql += "," + ProjectId;
        //执行部门ID
        strSql += "," + departmentsId;
        //发生时间
        strSql += ",to_date('" + startTime + "','YYYY-MM-DD HH24:MI:SS')";
        //预约标志【是否为预约接收:0-非预约挂号; 1-预约挂号,2-预约接收】
        strSql += ",2";
        //号码
        strSql += ",'" + hb + "'";
        strSql += ")";
        Db.find(strSql);


//        cmd.CommitTrans();
//        blnTran = false;
//        dr.Close();
//        cmd.Close();
//        strResponse = "" +
//                "<Response>\n" +
//                "    <ReturnCode>0</ReturnCode>\n" +
//                "    <ReturnInfo>挂号取号（OutPatRegisteGetNo）交易成功！</ReturnInfo>" +
//                strDetail +
//                "\n</Response>";
//        return strResponse;
//    }
//            catch (Exception se)
//    {
//        dr.Close();
//        cmd.Close();
//        btLogService.WriteErrorLog(se.ToString() + "\n" + strSql);
//        strResponse = "<Response>\n" +
//                "    <ReturnCode>1</ReturnCode>\n" +
//                "    <ReturnInfo>" + btFunc.btSubstrCenter( se.Message.ToString()) + "</ReturnInfo>\n" +
//                "</Response>";

        return null;
    }





}
