package just.beans;

import java.io.Serializable;

/**
 * Created by 24540 on 2016/5/23.
 */
public class BodyInfo implements Serializable {
    private String body;//加油站的主要信息
    private String create_time;//产生时间
    private String name;//商品名称
    private String pay_type;//支付方式
    private String total_fee;//交易总额
    private String trade_state;//交易状态
    private String out_trade_no;//商品单号
    private String transaction_id;//交易单号
    private String orien_time;  //预约时间
    private String Car_info;//汽车信息
    private String usename;//用户名
    private boolean mCanUsed;//判断是否可以使用
    private double liter;

    public double getLiter() {
        return liter;
    }

    public void setLiter(double liter) {
        this.liter = liter;
    }

    public boolean ismCanUsed() {
        return mCanUsed;
    }

    public void setmCanUsed(boolean mCanUsed) {
        this.mCanUsed = mCanUsed;
    }

    public String getOrien_time() {
        return orien_time;
    }

    public void setOrien_time(String orien_time) {
        this.orien_time = orien_time;
    }

    public String getCar_info() {
        return Car_info;
    }

    public void setCar_info(String car_info) {
        Car_info = car_info;
    }

    public String getUsename() {
        return usename;
    }

    public void setUsename(String usename) {
        this.usename = usename;
    }


    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }


    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPay_type() {
        return pay_type;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }

    public String getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }

    public String getTrade_state() {
        return trade_state;
    }

    public void setTrade_state(String trade_state) {
        this.trade_state = trade_state;
    }
}
