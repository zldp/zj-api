package com.atuinfo.service;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atuinfo.common.ExecPublic;
import com.atuinfo.common.Initiation;
import com.atuinfo.common.StrUtil;
import com.atuinfo.common.UserInfo;
import com.atuinfo.core.ResultCode;
import com.atuinfo.exception.ErrorMassageException;
import com.atuinfo.util.MapToXmlUtile;
import com.atuinfo.util.StaxonUtils;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.*;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yz
 * @version 1.0.0
 * @date 2019-08-01 10:25
 */
@Before(Tx.class)
public class BookingService {

    public Map<String,Object> FormatValidation(String strRequest){
        Map<String, Object> params = (Map<String, Object>) JSONObject.parseObject(StaxonUtils.xml2json(strRequest), Map.class).get("Request");
        if (null == params) {
            throw new ErrorMassageException("格式错误,请参照文档以正确格式传参");
        }
        return  params;
    }


    public Object execute(String sql){
        return Db.execute(new ICallback() {
            @Override
            public Object call(Connection conn) {
                try {
                    CallableStatement proc = conn.prepareCall("{"+ sql +"}");

                    proc.execute();
                } catch (SQLException e) {
                    throw new ErrorMassageException(e.getMessage());
                }

                return null;
            }

        });

    }
    /**
     * 取消预约
     * @param strRequest
     * @return
     */
    public String cancelBook(String strRequest){
        // 解析xml 得到参数
        Map<String, Object> params = FormatValidation(strRequest);
        final String thirdPartyNo = StrUtil.objToStr(params.get("thirdPartyNo"));
        final String bookingOrderId = StrUtil.objToStr(params.get("bookingOrderId"));
        if (StrKit.isBlank(bookingOrderId)) {
            throw new ErrorMassageException("bookingOrderId 不能为空");
        }
        //判断当前号源是否存在
        final List<Record> recordList=Db.find(" Select A.诊室,A.门诊号,A.姓名,A.性别,A.年龄,B.费别,C.编码 As 付款方式,发生时间,号序\n" + "  From 病人挂号记录 A,病人信息 B,医疗付款方式 C\n" +
                "\tWhere A.病人ID=B.病人ID And B.医疗付款方式=C.名称 and A.No=? and A.预约=1 And A.记录性质=2 and A.记录状态=1",bookingOrderId);
        //System.out.println(recordList.size());
        if(recordList.size()==0){
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
                try {
                    CallableStatement proc = conn.prepareCall("{Call Zl_三方机构挂号_Delete (?,?,?,?,?)}");
                    proc.setString(1, bookingOrderId);
                    proc.setString(2, thirdPartyNo);
                    proc.setString(3,"取消预约");
                    proc.setString(4,null);
                    proc.setString(5,null);

                    proc.execute();
                } catch (SQLException e) {
                    throw new ErrorMassageException("执行Call Zl_三方机构挂号_Delete存储过程错误");
                }

                    //params = RecordBuilder.me.build(DbKit.getConfig(), rs);
                    //代码来到这里就说明你的存储过程已经调用成功，如果有输出参数，接下来就是取输出参数的一个过程
                    /*Record record = new Record();
                    //国税有税源无
                    record.set("GSYSYW",proc.getObject(1));
                    //国税无税源有
                    record.set("GSWSYY",proc.getObject(2));
                    //识别号不同名称相同
                    record.set("SBHBTMCT",proc.getObject(3));
                    //识别号相同名称不同
                    record.set("SBHTMCBT",proc.getObject(4));
                    //识别号名称都相同
                    record.set("SBHMCXT",proc.getObject(5));
                    //setAttr("Count",record);
                    return proc;*/
                return null;
            }
        });
        /**
         * 逻辑代码
         */
        // 返回结果集

        return MapToXmlUtile.mapToXml(ResultCode.SUCCESS, "成功", null,false);
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
        // 解析xml 得到参数
        Map<String, Object> params = FormatValidation(strRequest);
        final String bookingOrderId = StrUtil.objToStr(params.get("bookingOrderId"));//预约单编号
        final String thirdPartyNo = StrUtil.objToStr(params.get("thirdPartyNo"));  //第三方支付流水号
        final String outTradeNo = StrUtil.objToStr(params.get("outTradeNo"));   //业务订单号
        final String payFee = StrUtil.objToStr(params.get("payFee"));//     挂号：支付费用
        final String tradeType = StrUtil.objToStr(params.get("tradeType"));//支付方式
        if (StrKit.isBlank(bookingOrderId)) {

        }

        // 判断三方返回的费用与总费用是否相等
        final Record recordList=Db.findFirst("  Select 病人ID,nvl(sum(实收金额),0) as 金额 From 门诊费用记录 Where NO=? and 记录性质=4 group by 病人id",bookingOrderId);
        if (recordList == null) {
            throw new ErrorMassageException("数据不存在");
        }
        //把查询的sql数据放进集合
        Map<String, Object> nursingRecords = recordList.getColumns();

        //得到单个数据的值
        String patientId = String.valueOf(nursingRecords.get("病人id"));//病人ID
        String sum = String.valueOf(nursingRecords.get("金额"));//获取数据库的金额
        //将金额转换成金额类型
        BigDecimal MoneySum =  new BigDecimal(sum);    //数据库金额
        BigDecimal payFeeSum = new BigDecimal(payFee);//前台传递的金额
        // 如果两次金额不一致
        if(!MoneySum.equals(payFeeSum)){
            throw new ErrorMassageException("挂号金额合计与该号的价格不一致"); //提示错误
        }
        //获取病人预约信息
        final Record PatientInfoList=Db.findFirst(" \n" +
                "Select A.号别,A.诊室,A.门诊号,A.姓名,A.性别,A.年龄,B.费别,nvl(C.编码,'09') As 付款方式,发生时间,号序,D.ID As" +
                " 医生ID,D.姓名 As 医生姓名,A.执行部门ID As 科室ID From 病人挂号记录 A,病人信息 B,医疗付款方式 C,人员表 D " +
                "Where A.病人ID=B.病人ID And B.医疗付款方式=C.名称 And A.No=? And A.预约=1 And A.记录性质=2 And 执行人=D.姓名",bookingOrderId);
        //接收数据
        if (PatientInfoList == null) {//判断是否有值
            throw new ErrorMassageException("预约挂号单信息丢失或已经取号，请重新获取！");
        }
       Map<String, Object> Records = PatientInfoList.getColumns();


        //取值  后续会调用到
        String hb=Records.get("号别").toString();          //获取号别
        long   doctorId= ((BigDecimal)Records.get("医生ID")).longValue();  //医生ID
        Timestamp timestamp = PatientInfoList.getTimestamp("发生时间");
        Date date = new Date(timestamp.getTime());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String startTime= simpleDateFormat.format(date) ;  //发生时间
        String doctorName = String.valueOf(Records.get("医生姓名"));  //医生姓名
        long   departmentsId= ((BigDecimal) Records.get("科室ID")).longValue();//科室ID
        String consultingRoom = String.valueOf(Records.get("诊室"));//诊室
        String patientNumber = String.valueOf(Records.get("门诊号"));//门诊号
        String name = String.valueOf(Records.get("姓名"));//姓名
        String sex = String.valueOf(Records.get("性别"));//性别
        String age = String.valueOf(Records.get("年龄"));//年龄
        String payMethod = String.valueOf(Records.get("付款方式"));//付款方式
        String fb = String.valueOf(Records.get("费别"));//费别
        String hx = String.valueOf(Records.get("号序"));//号序

        final Record List=Db.findFirst("Select 项目ID From 挂号安排 Where 号码=?",hb);
        //把查询的sql数据放进集合
        Map<String, Object> rs = List.getColumns();
        if (List == null) {
            throw new ErrorMassageException("预约挂号单收费信息已作废，请重新挂号！");
        }
        String ProjectId= String.valueOf(rs.get("项目ID"));//项目ID

        ExecPublic.InitCardInfo(ExecPublic.GetHisCardTypeID(tradeType));
        //事务开始
//        cmd.BeginTrans();
//        blnTran = true;

        // 调用Zl_预约挂号接收_Insert全部以现金将病人进行接收。
        final Record List1=Db.findFirst("Select 病人结帐记录_id.nextval From dual");
        Map<String, Object> rs1 = List1.getColumns();

        //结账ID
        String NEXTVAL = String.valueOf(rs1.get("nextval"));//病人结帐记录_id
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
        strSql += ",'" + ExecPublic.CardInfo.getY_str结算方式() + "'";
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
        strSql += "," + 12;
//        //结算卡序号_In
        strSql += ",Null";
//        //卡号_In
        strSql += ",'" + thirdPartyNo + "'";
//        //交易流水号_In
        strSql += ",'" + outTradeNo + "'";
//        //交易说明_In
        strSql += ",'" + strRem + "'";
        strSql += ")";
        execute(strSql);



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
           execute(strSql);
        }

        //强制保存挂号订单号到病人挂号记录的交易水号
        strSql = "Update 病人挂号记录" +
                "    Set 交易流水号='" + outTradeNo + "',交易说明='" + strRem + "'" +
                "    Where 记录性质 = 1 And 记录状态 = 1 And No='" + bookingOrderId + "'";
        Db.update(strSql);

        //强制保存挂号订单号到病人预交记录的交易水号
        strSql = "Update 病人预交记录" +
                "    Set 交易流水号='" + outTradeNo + "',交易说明='" + strRem + "'" +
                "    Where 结帐id = " + NEXTVAL+ " And 卡类别ID=" + ExecPublic.CardInfo.getP_intID();
        Db.update(strSql);


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
        execute(strSql);




        Map result = new HashMap();
        result.put("bookingOrderId", bookingOrderId);

        return MapToXmlUtile.mapToXml(ResultCode.SUCCESS, "挂号取号（OutPatRegisteGetNo）交易成功！", result, false);
    }





}
