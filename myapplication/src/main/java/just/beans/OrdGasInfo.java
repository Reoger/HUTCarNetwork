package just.beans;

import cn.bmob.v3.BmobObject;

/**
 * Created by 24540 on 2016/5/21.
 */
public class OrdGasInfo extends BmobObject {
    private String payId;
    private String time;
    private String username;//关联车主注册的账号
    private String brand;//汽车品牌
    private String model;//汽车型号
    private String licensePlateNum;//车牌号
    private String engineNum;//发动机号
    private String reservationTime;//预约时间
    private String name;//姓名
    private double liter;//加油的升数
    private String goodsName;//商品的名字

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public boolean ismIsUsed() {
        return mIsUsed;
    }

    public void setmIsUsed(boolean mIsUsed) {
        this.mIsUsed = mIsUsed;
    }

    private boolean mIsUsed;//判断是否可以用于加油

    public double getLiter() {
        return liter;
    }

    public void setLiter(double liter) {
        this.liter = liter;
    }

    public String getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(String reservationTime) {
        this.reservationTime = reservationTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }




    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLicensePlateNum() {
        return licensePlateNum;
    }

    public void setLicensePlateNum(String licensePlateNum) {
        this.licensePlateNum = licensePlateNum;
    }

    public String getEngineNum() {
        return engineNum;
    }

    public void setEngineNum(String engineNum) {
        this.engineNum = engineNum;
    }



    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId;
    }
}
