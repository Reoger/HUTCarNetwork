package just.beans;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.bmob.v3.BmobObject;

/**
 * 汽车信息实体类
 */
public class AutoInfo extends BmobObject {
    private String username;//关联车主注册的账号
    private String brand;//汽车品牌
    private String model;//汽车型号
    private String licensePlateNum;//车牌号
    private String engineNum;//发动机号
    private String bodyLevel;//车身级别
    private String vin;//车架号
    private String addTime;//添加信息的时间，为了解决本地数据库与云数据库数据类型的冲突，使用统一格式的字符串类型

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

    public void setLicensePlateNum(String licensePlate) {
        this.licensePlateNum = licensePlate;
    }

    public String getEngineNum() {
        return engineNum;
    }

    public void setEngineNum(String engineNum) {
        this.engineNum = engineNum;
    }

    public String getBodyLevel() {
        return bodyLevel;
    }

    public void setBodyLevel(String bodyLevel) {
        this.bodyLevel = bodyLevel;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(addTime);
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }
}
