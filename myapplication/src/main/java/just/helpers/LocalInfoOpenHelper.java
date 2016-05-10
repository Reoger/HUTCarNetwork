package just.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import just.constants.AutoInfoConstants;
import just.constants.MaInfoConstants;

public class LocalInfoOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME="car_network_info.db";//数据库名称
    private static final int DB_VERSION=1;

    private static LocalInfoOpenHelper mHelper;

    private LocalInfoOpenHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static LocalInfoOpenHelper getInstance(Context context) {
        if(mHelper==null) {
            synchronized (LocalInfoOpenHelper.class) {
                if(mHelper==null) {
                    mHelper=new LocalInfoOpenHelper(context);
                }
            }
        }
        return mHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql1="create table "+ AutoInfoConstants.AUTO_INFO_TABLE_NAME+" ( "+
                AutoInfoConstants.COLUMN_USERNAME+" text, " +
                AutoInfoConstants.COLUMN_BRAND+" text, " +
                AutoInfoConstants.COLUMN_MODEL+" text, " +
                AutoInfoConstants.COLUMN_LICENSE_PLATE_NUM+" text, " +
                AutoInfoConstants.COLUMN_ENGINE_NUM+" text, " +
                AutoInfoConstants.COLUMN_BODY_LEVEL+" text, " +
                AutoInfoConstants.COLUMN_ADD_TIME+" text, " +
                AutoInfoConstants.COLUMN_VIN+" text,"+
                AutoInfoConstants.COLUMN_IS_SYNC+" integer," + //用于判断是否同步到了云端，1表示是
                AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" integer )"; //用于判断是否需要在云端删除

        String sql2="create table "+ MaInfoConstants.MA_INFO_TABLE_NAME+" ( "+
                MaInfoConstants.COLUMN_VIN+" text, " +
                MaInfoConstants.COLUMN_USERNAME+" text, "+
                MaInfoConstants.COLUMN_MILEAGE+" real, " +//里程数为浮点型
                MaInfoConstants.COLUMN_GASOLINE_VOLUME+" integer, " +
                MaInfoConstants.COLUMN_ENGINE_PERFOR+" text, " +
                MaInfoConstants.COLUMN_TRANSMISSION_PERFOR+" text, " +
                MaInfoConstants.COLUMN_LAMP+" text, " +
                MaInfoConstants.COLUMN_SCAN_TIME+" text, "+
                MaInfoConstants.COLUMN_IS_SYNC+" integer, "+//用于判断是否同步到了云端，1表示是
                MaInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" integer )"; //用于判断是否需要在云端删除

        db.execSQL(sql1);
        db.execSQL(sql2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
