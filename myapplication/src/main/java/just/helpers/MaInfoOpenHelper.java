package just.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import just.constants.MaInfoConstants;

public class MaInfoOpenHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION=1;

    private static MaInfoOpenHelper mHelper;

    private MaInfoOpenHelper(Context context) {
        super(context.getApplicationContext(), AutoInfoOpenHelper.DB_NAME, null, DB_VERSION);
    }

    public static MaInfoOpenHelper getInstance(Context context) {
        if(mHelper==null) {
            synchronized (MaInfoOpenHelper.class) {
                if(mHelper==null) {
                    mHelper=new MaInfoOpenHelper(context);
                }
            }
        }
        return mHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="create table "+ MaInfoConstants.MA_INFO_TABLE_NAME+" ( "+
                "_id integer primary key autoincrement, "+
                MaInfoConstants.COLUMN_VIN+" text, " +
                MaInfoConstants.COLUMN_MILEAGE+" real, " +//里程数为浮点型
                MaInfoConstants.COLUMN_GASOLINE_VOLUME+" integer, " +
                MaInfoConstants.COLUMN_ENGINE_PERFOR+" text, " +
                MaInfoConstants.COLUMN_TRANSMISSION_PERFOR+" text, " +
                MaInfoConstants.COLUMN_LAMP+" text, " +
                MaInfoConstants.COLUMN_SCAN_TIME+" text, "+
                MaInfoConstants.COLUMN_IS_SYNC+" integer )";//用于判断是否同步到了云端，1表示是
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
