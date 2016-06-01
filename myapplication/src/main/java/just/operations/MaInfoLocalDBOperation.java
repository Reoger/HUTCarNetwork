package just.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import just.beans.MaInfo;
import just.constants.AutoInfoConstants;
import just.constants.MaInfoConstants;
import just.helpers.LocalInfoOpenHelper;

/**
 * 操作本地数据库的类
 */
public class MaInfoLocalDBOperation {

    public static void insert(Context context, MaInfo maInfo, int isSyncToCloud) {
        SQLiteDatabase db= LocalInfoOpenHelper.getInstance(context).getWritableDatabase();

        //有时候会从云端同步数据，所以需要判断本地是否已有重复的
        Cursor cursor = db.query(MaInfoConstants.MA_INFO_TABLE_NAME, new String[]{MaInfoConstants.COLUMN_SCAN_TIME},
                MaInfoConstants.COLUMN_VIN+" = ? and "+MaInfoConstants.COLUMN_SCAN_TIME+" = ? and "+MaInfoConstants.COLUMN_USERNAME+" = ?",
                new String[]{maInfo.getVin(),maInfo.getScanTime(),maInfo.getUsername()}, null, null, null);
        if (cursor.moveToFirst()) return;

        ContentValues values= getValuesForInsert(maInfo,isSyncToCloud);

        db.insert(MaInfoConstants.MA_INFO_TABLE_NAME,null,values);
        db.close();
    }

    public static List<MaInfo> queryAll(Context context) {
        return queryBy(context,null,null);
    }

    /**
     * 根据指定条件查询数据
     * @param context
     * @param selection  指定where的约束条件
     * @param selectionArgs  为where 中的占位符提供具体的值
     * @return  返回具有指定查询结果的List
     */
    public static List<MaInfo> queryBy(Context context, @Nullable String selection, @Nullable String[] selectionArgs) {
        List<MaInfo> list=new ArrayList<>();
        SQLiteDatabase db = LocalInfoOpenHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(MaInfoConstants.MA_INFO_TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                MaInfo maInfo =getMaInfoObject(cursor);
                list.add(maInfo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * 根据指定要求删除相关数据,删除的数据只能根据扫描的时间确定
     */
    public static boolean deleteBy(Context context, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = LocalInfoOpenHelper.getInstance(context).getWritableDatabase();
        int row=db.delete(MaInfoConstants.MA_INFO_TABLE_NAME,selection,selectionArgs);
        if(row>0) {
            return true;
        }
        return false;
    }

    /**
     * 同步至云端后，需更改本地数据库的MaInfoConstants.COLUMN_IS_SYNC属性列
     */
    public static void updateForIsSyncToCloud(Context context, String scanTime,String vin, int isSyncToCloud) {
        SQLiteDatabase db = LocalInfoOpenHelper.getInstance(context).getWritableDatabase();
        String str1="update "+MaInfoConstants.MA_INFO_TABLE_NAME+" set "+MaInfoConstants.COLUMN_IS_SYNC+" = ? " +
                "where "+MaInfoConstants.COLUMN_SCAN_TIME+" = ? and "+MaInfoConstants.COLUMN_VIN+" = ?";
        String[] str2={""+isSyncToCloud,scanTime,vin};
        db.execSQL(str1,str2);
        db.close();
    }

    public static void updateForIsDelWithCloud(Context context, String scanTime, String vin,int isDelWithCloud) {
        SQLiteDatabase db = LocalInfoOpenHelper.getInstance(context).getWritableDatabase();
        String str1="update "+MaInfoConstants.MA_INFO_TABLE_NAME+" set "+MaInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ? " +
                "where "+MaInfoConstants.COLUMN_SCAN_TIME+" = ? and "+MaInfoConstants.COLUMN_VIN+" = ?";
        String[] str2={""+isDelWithCloud,scanTime,vin};
        db.execSQL(str1,str2);
        db.close();
    }

    /**
     * 已知需查询的数据数目唯一，返回该数据的特定列的数据(只能得到一列的)
     * @param context
     * @param columns
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static String queryGetSpecifiedAttr(Context context, @Nullable String[] columns, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = LocalInfoOpenHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(MaInfoConstants.MA_INFO_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        String result="";
        if (cursor.moveToFirst()) {
            result=result+cursor.getInt(0);//考虑到有可能得到的数据为Integer型的
        }
        cursor.close();
        db.close();
        return result;
    }

    private static ContentValues getValuesForInsert(MaInfo maInfo, int isSyncToCloud) {
        ContentValues values=new ContentValues();

        values.put(MaInfoConstants.COLUMN_USERNAME, maInfo.getUsername());

        values.put(MaInfoConstants.COLUMN_MILEAGE, maInfo.getMileage());
        values.put(MaInfoConstants.COLUMN_GASOLINE_VOLUME, maInfo.getGasolineVolume());
        values.put(MaInfoConstants.COLUMN_ENGINE_PERFOR, maInfo.getEnginePerfor());
        values.put(MaInfoConstants.COLUMN_TRANSMISSION_PERFOR, maInfo.getTransmissionPerfor());
        values.put(MaInfoConstants.COLUMN_LAMP, maInfo.getLamp());
        values.put(MaInfoConstants.COLUMN_SCAN_TIME,maInfo.getScanTime());

        values.put(AutoInfoConstants.COLUMN_VIN, maInfo.getVin());
        values.put(AutoInfoConstants.COLUMN_IS_SYNC,isSyncToCloud);
        values.put(AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD,0);

        return values;
    }

    private static MaInfo getMaInfoObject(Cursor cursor) {
        MaInfo maInfo =new MaInfo();

        maInfo.setVin(cursor.getString(cursor.getColumnIndex(MaInfoConstants.COLUMN_VIN)));
        maInfo.setMileage(Float.parseFloat(cursor.getString(cursor.getColumnIndex(MaInfoConstants.COLUMN_MILEAGE))));
        maInfo.setGasolineVolume(Integer.parseInt(cursor.getString(cursor.getColumnIndex(MaInfoConstants.COLUMN_GASOLINE_VOLUME))));
        maInfo.setEnginePerfor(cursor.getString(cursor.getColumnIndex(MaInfoConstants.COLUMN_ENGINE_PERFOR)));
        maInfo.setTransmissionPerfor(cursor.getString(cursor.getColumnIndex(MaInfoConstants.COLUMN_TRANSMISSION_PERFOR)));
        maInfo.setLamp(cursor.getString(cursor.getColumnIndex(MaInfoConstants.COLUMN_LAMP)));
        maInfo.setUsername(cursor.getString(cursor.getColumnIndex(MaInfoConstants.COLUMN_USERNAME)));
        maInfo.setScanTime(cursor.getString(cursor.getColumnIndex(MaInfoConstants.COLUMN_SCAN_TIME)));

        return maInfo;
    }
}
