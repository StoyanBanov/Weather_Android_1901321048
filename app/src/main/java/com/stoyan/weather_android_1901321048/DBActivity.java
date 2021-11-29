package com.stoyan.weather_android_1901321048;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public abstract class DBActivity extends RESTActiviry {

    protected interface OnQuerySuccess{
        public void OnSuccess();
    }
    protected interface OnSelectSuccess{
        public void OnElementSelected(
                String ID, String Place, String Type, String Date
        );
    }

    protected void SelectSQL(String SelectQ, String[] args, OnSelectSuccess success)
            throws Exception
    {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(getFilesDir().getPath()+"/QueryHistory.db", null);
        Cursor cursor = db.rawQuery(SelectQ, args);
        while (cursor.moveToNext()){
            String ID=cursor.getString(cursor.getColumnIndex("ID"));
            String Place=cursor.getString(cursor.getColumnIndex("Place"));
            String Type=cursor.getString(cursor.getColumnIndex("Type"));
            String Date=cursor.getString(cursor.getColumnIndex("Date"));
            success.OnElementSelected(ID, Place, Type, Date);
        }
        db.close();
    }

    protected void ExecSQL(String SQL, Object[] args, OnQuerySuccess success)
            throws Exception
    {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(getFilesDir().getPath()+"/QueryHistory.db", null);
        if(args!=null)
            db.execSQL(SQL, args);
        else
            db.execSQL(SQL);

        db.close();
        success.OnSuccess();
    }

    protected void initDB() throws Exception{
        ExecSQL(
                "CREATE TABLE if not exists QueryHistory(" +
                        "ID integer PRIMARY KEY AUTOINCREMENT, " +
                        "Place text not null, " +
                        "Type text not null, " +
                        "Date text not null " +
                        ")",
                null,
                ()-> Toast.makeText(getApplicationContext(),"DB init Successfull", Toast.LENGTH_SHORT).show()
        );
    }
}
