package just.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import just.constants.AutoInfoConstants;

public class AutoInfoOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME="car_network_info.db";//数据库名称
    private static final int DB_VERSION=1;

    private static AutoInfoOpenHelper mHelper;

    private AutoInfoOpenHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static AutoInfoOpenHelper getInstance(Context context) {
        if(mHelper==null) {
            synchronized (AutoInfoOpenHelper.class) {
                if(mHelper==null) {
                    mHelper=new AutoInfoOpenHelper(context);
                }
            }
        }
        return mHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="create table "+ AutoInfoConstants.AUTO_INFO_TABLE_NAME+" ( "+
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
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
