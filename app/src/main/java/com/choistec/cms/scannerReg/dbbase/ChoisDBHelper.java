package com.choistec.cms.scannerReg.dbbase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChoisDBHelper extends SQLiteOpenHelper {
    public static SQLiteDatabase sDB;
    private static final String Group_id = "group_id";
    private static final String Sms_data = "sms_data";
    private static final String Sms_date = "sms_date";
    public static final String SMS_TABLE = "sms_table";
    private static final String Sms_Check = "sms_check";
    public ChoisDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public Cursor rawQuery(SQLiteDatabase db, String string, Object obj){
        Log.i("BJY", "rawQuery : " + string);
        Cursor cursor = null;

        try{
            if(db != null){
                cursor = db.rawQuery(string, null);
            }else{
                Log.d("BJY"," DB is Null !!!");
            }
        }catch (Exception e){

        }
        return cursor;
    }
    public static long insertValue(String sms_tabe, String group_id, String sms_data, String _date, boolean sms_check){
        ContentValues values = new ContentValues();
        values.put(Group_id, group_id);
        values.put(Sms_data, sms_data);
        values.put(Sms_date, _date);
        values.put(Sms_Check, sms_check);
        Log.d("BJY","insert into value :" +values.toString());
        return sDB.insert(sms_tabe, null, values);
    }
    public String createTable(){
        String create = "CREATE TABLE IF NOT EXISTS sms_table("
                        + " "
                        + " group_id varchar(45) not null,"
                        + " sms_data varchar(200) ,"
                        + " sms_date varchar(45), "
                        + " sms_check boolean);";


        return create;
    }
    public static Cursor selectAllData(String table_name, String _id){
        Cursor cursor = null;

        try{
            StringBuilder sb = new StringBuilder("Select * ");
            sb.append(" from "+table_name);
            sb.append(" where ");
            sb.append(ChoisDBProvider.GROUP_ID);
            sb.append(" = ");
            sb.append("'"+_id+"'");
            sb.append(" ORDER BY sms_date");
            sb.append(";");

            cursor = ChoisDBProvider.rawQuery(sDB, sb.toString(), null);

        }catch (Exception e ){

        }
        return cursor;
    }
}
