package com.choistec.cms.scannerReg.dbbase;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ChoisDBProvider extends ContentProvider {
    public static SQLiteDatabase sDB;
    public static final String TAG = ChoisDBProvider.class.getSimpleName();
    public static final String GROUP_ID = "group_id";
    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count =0;
        StringBuilder sb = new StringBuilder("");
        String tbName ="";
        for(String key : values.keySet()){
            String value = values.getAsString(key);

            if(key.contains("str") || key.contains("dt")){
                if(value != null){
                    value = value.replaceAll("'","''");
                }
                sb.append(key+" = '" +value+ "'," );
            }else{
                sb.append(key + " = " +values.get(key) + ", ");
            }
        }
        String sbValue = sb.toString();

        sbValue = sbValue.substring(0, sbValue.length() - 2);
        String queryStr = "UPDATE " + tbName + " SET " + sbValue;

//        if(where != null){
//            queryStr =
//        }
        return 0;
    }
    public static String getUriToTable(Uri uri){
        //TODO if table is a lot make it method.
        String tb ="";
//        switch (mUriMatcher.match(uri)){
//            case TABLE_NAME:
//            tb = TABLE_NAME;
//            break;
//        }
        return tb;
    }
    public static Cursor rawQuery(SQLiteDatabase db, String string, Object obj){
        Cursor cursor = null;

        try{
            if(db != null){
                cursor = db.rawQuery(string, null);
            }else{
                Log.i("BJY"," rawQuery db is Null !!!");
            }
        }catch (Exception e){
            Log.e("BJY"," Exception !!!!");
        }
        return cursor;
    }
}
