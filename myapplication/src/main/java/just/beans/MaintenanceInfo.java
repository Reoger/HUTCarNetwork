package just.beans;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.bmob.v3.BmobObject;

/**
 * 维护信息实体类
 */
public class MaintenanceInfo extends BmobObject {
    private String vin;//对应汽车的车架号
    private float mileage;//里程数
    private int gasolineVolume;//汽油量(整数百分比)
    private String enginePerfor;//发动机性能
    private String transmissionPerfor;//变速器性能
    private String lamp;//车灯
    private String scanDate;//扫描的时间

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public float getMileage() {
        return mileage;
    }

    public void setMileage(float mileage) {
        this.mileage = mileage;
    }

    public int getGasolineVolume() {
        return gasolineVolume;
    }

    public void setGasolineVolume(int gasolineVolume) {
        this.gasolineVolume = gasolineVolume;
    }

    public String getEnginePerfor() {
        return enginePerfor;
    }

    public void setEnginePerfor(String enginePerfor) {
        this.enginePerfor = enginePerfor;
    }

    public String getTransmissionPerfor() {
        return transmissionPerfor;
    }

    public void setTransmissionPerfor(String transmissionPerfor) {
        this.transmissionPerfor = transmissionPerfor;
    }

    public String getLamp() {
        return lamp;
    }

    public void setLamp(String lamp) {
        this.lamp = lamp;
    }

    public String getScanDate() {
        return scanDate;
    }

    public void setScanDate(Date scanDate) {
        this.scanDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(scanDate);
    }
}
