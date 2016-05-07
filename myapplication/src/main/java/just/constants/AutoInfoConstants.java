package just.constants;

/**
 * 与汽车信息有关的常量类
 */
public class AutoInfoConstants {
    public static final String AUTO_INFO_TABLE_NAME="tb_auto_info";//汽车信息表名

    public static final String COLUMN_USERNAME="username";//车主注册账号
    public static final String COLUMN_BRAND="brand";//汽车品牌
    public static final String COLUMN_MODEL="model";//型号
    public static final String COLUMN_LICENSE_PLATE_NUM="licensePlateNum";//车牌号码
    public static final String COLUMN_ENGINE_NUM="engineNum";//发动机号
    public static final String COLUMN_BODY_LEVEL="bodyLevel";//车身级别
    public static final String COLUMN_VIN="vin";//车架号
    public static final String COLUMN_ADD_TIME="addTime";//汽车信息添加时间
    public static final String COLUMN_IS_SYNC="isSync";//用于判断是否已经存入了云端
    public static final String COLUMN_IS_DEL_WITH_CLOUD="isDelete";//用于判断是否已经在云端删除
}
