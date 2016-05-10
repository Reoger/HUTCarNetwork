package just.constants;

/**
 * 与汽车信息有关的常量类
 */
public class MaInfoConstants {
    public static final String MA_INFO_TABLE_NAME="tb_ma_info";//维护信息表名

    public static final String COLUMN_VIN="vin";//对应的汽车的车架号
    public static final String COLUMN_MILEAGE="mileage";//里程数
    public static final String COLUMN_GASOLINE_VOLUME="gasolineVolume";//汽油量
    public static final String COLUMN_ENGINE_PERFOR="enginePerfor";//发动机性能
    public static final String COLUMN_TRANSMISSION_PERFOR="transmissionPerfor";//变速器性能
    public static final String COLUMN_LAMP="lamp";//车灯
    public static final String COLUMN_SCAN_TIME="scanTime";
    public static final String COLUMN_IS_SYNC="isSync";//用于判断是否已经存入了云端
    public static final String COLUMN_IS_DEL_WITH_CLOUD="isDelete";//用于判断是否已经在云端删除
    public static final String COLUMN_USERNAME="username";//维护信息对应的用户名
}
