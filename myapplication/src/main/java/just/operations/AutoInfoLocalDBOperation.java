package just.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import just.beans.AutomobileInfo;
import just.constants.AutoInfoConstants;
import just.helpers.AutoInfoOpenHelper;

/**
 * 操作本地数据库的类
 */
public class AutoInfoLocalDBOperation {

    public static void insert(Context context, AutomobileInfo automobileInfo, int isSyncToCloud) {
        SQLiteDatabase db= AutoInfoOpenHelper.getInstance(context).getWritableDatabase();

        //暂时先用这种方式防止插入重复的数据
        Cursor cursor = db.query(AutoInfoConstants.AUTO_INFO_TABLE_NAME, new String[]{AutoInfoConstants.COLUMN_VIN},AutoInfoConstants.COLUMN_VIN+" = ?", new String[]{automobileInfo.getVin()}, null, null, null);
        if (cursor.moveToFirst()) return;

        ContentValues values= getValuesForInsert(automobileInfo,isSyncToCloud);

        db.insert(AutoInfoConstants.AUTO_INFO_TABLE_NAME,null,values);
        db.close();
    }

    public static List<AutomobileInfo> queryAll(Context context) {
        return queryBy(context,null,null,null);
    }

    /**
     * 根据指定条件查询数据
     * @param context
     * @param columns  指定查询的列名
     * @param selection  指定where的约束条件
     * @param selectionArgs  为where 中的占位符提供具体的值
     * @return  返回具有指定查询结果的List
     */
    public static List<AutomobileInfo> queryBy(Context context, @Nullable String[] columns, @Nullable String selection, @Nullable String[] selectionArgs) {
        List<AutomobileInfo> list=new ArrayList<>();
        SQLiteDatabase db = AutoInfoOpenHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(AutoInfoConstants.AUTO_INFO_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                AutomobileInfo automobileInfo=getAutomobileInfoObject(cursor);
                list.add(automobileInfo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
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
        SQLiteDatabase db = AutoInfoOpenHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(AutoInfoConstants.AUTO_INFO_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        String result="";
        if (cursor.moveToFirst()) {
            result=result+cursor.getInt(0);//考虑到有可能得到的数据为Integer型的
        }
        cursor.close();
        db.close();
        return result;
    }

    /**
     * 根据指定要求删除相关数据
     */
    public static boolean deleteBy(Context context, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = AutoInfoOpenHelper.getInstance(context).getWritableDatabase();
        int row=db.delete(AutoInfoConstants.AUTO_INFO_TABLE_NAME,selection,selectionArgs);
        if(row>0) {
            return true;
        }
        return false;
    }

    /**
     * 同步至云端后，需更改本地数据库的AutoInfoConstants.COLUMN_IS_SYNC属性列
     */
    public static void updateForIsSyncToCloud(Context context, String vin, int isSyncToCloud) {
        SQLiteDatabase db = AutoInfoOpenHelper.getInstance(context).getWritableDatabase();
        String str1="update "+AutoInfoConstants.AUTO_INFO_TABLE_NAME+" set "+AutoInfoConstants.COLUMN_IS_SYNC+" = ? " +
                "where "+AutoInfoConstants.COLUMN_VIN+" = ?";
        String[] str2={""+isSyncToCloud,vin};
        db.execSQL(str1,str2);
        db.close();
    }

    /**
     * 更改本地数据的AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD值
     */
    public static void updateForIsDelWithCloud(Context context, String vin, int isDelWithCloud) {
        SQLiteDatabase db = AutoInfoOpenHelper.getInstance(context).getWritableDatabase();
        String str1="update "+AutoInfoConstants.AUTO_INFO_TABLE_NAME+" set "+AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD+" = ? " +
                "where "+AutoInfoConstants.COLUMN_VIN+" = ?";
        String[] str2={""+isDelWithCloud,vin};
        db.execSQL(str1,str2);
        db.close();
    }

    private static ContentValues getValuesForInsert(AutomobileInfo automobileInfo, int isSyncToCloud) {
        ContentValues values=new ContentValues();

        values.put(AutoInfoConstants.COLUMN_USERNAME,automobileInfo.getUsername());
        values.put(AutoInfoConstants.COLUMN_BRAND,automobileInfo.getBrand());
        values.put(AutoInfoConstants.COLUMN_MODEL,automobileInfo.getModel());
        values.put(AutoInfoConstants.COLUMN_LICENSE_PLATE_NUM,automobileInfo.getLicensePlateNum());
        values.put(AutoInfoConstants.COLUMN_ENGINE_NUM,automobileInfo.getEngineNum());
        values.put(AutoInfoConstants.COLUMN_BODY_LEVEL,automobileInfo.getBodyLevel());
        values.put(AutoInfoConstants.COLUMN_VIN,automobileInfo.getVin());
        values.put(AutoInfoConstants.COLUMN_ADD_TIME,automobileInfo.getAddTime());
        values.put(AutoInfoConstants.COLUMN_IS_SYNC,isSyncToCloud);
        values.put(AutoInfoConstants.COLUMN_IS_DEL_WITH_CLOUD,0);

        return values;
    }

    private static AutomobileInfo getAutomobileInfoObject(Cursor cursor) {
        AutomobileInfo automobileInfo=new AutomobileInfo();
        automobileInfo.setBrand(cursor.getString(cursor.getColumnIndex(AutoInfoConstants.COLUMN_BRAND)));
        automobileInfo.setModel(cursor.getString(cursor.getColumnIndex(AutoInfoConstants.COLUMN_MODEL)));
        automobileInfo.setLicensePlateNum(cursor.getString(cursor.getColumnIndex(AutoInfoConstants.COLUMN_LICENSE_PLATE_NUM)));
        automobileInfo.setEngineNum(cursor.getString(cursor.getColumnIndex(AutoInfoConstants.COLUMN_ENGINE_NUM)));
        automobileInfo.setBodyLevel(cursor.getString(cursor.getColumnIndex(AutoInfoConstants.COLUMN_BODY_LEVEL)));
        automobileInfo.setUsername(cursor.getString(cursor.getColumnIndex(AutoInfoConstants.COLUMN_USERNAME)));
        automobileInfo.setVin(cursor.getString(cursor.getColumnIndex(AutoInfoConstants.COLUMN_VIN)));
        automobileInfo.setAddTime(cursor.getString(cursor.getColumnIndex(AutoInfoConstants.COLUMN_ADD_TIME)));
        return automobileInfo;
    }
}
