package com.mobile.paolo.listaspesa.database.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mobile.paolo.listaspesa.model.objects.Product;

/**
 * Created by paolo on 17/08/17.
 */

/**
 * This class handles some local database operations.
 */

public class LocalDatabaseHelper extends SQLiteOpenHelper
{
    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + ListaSpesaDB.LocalProduct.TABLE_LOCAL_PRODUCT_LIST + "("
            + ListaSpesaDB.LocalProduct.COLUMN_ID + " varchar primary key,"
            + ListaSpesaDB.LocalProduct.COLUMN_NAME + " varchar not null,"
            + ListaSpesaDB.LocalProduct.COLUMN_BRAND + " varchar not null,"
            + ListaSpesaDB.LocalProduct.COLUMN_DESCRIPTION + " varchar,"
            + ListaSpesaDB.LocalProduct.COLUMN_QUANTITY + " integer not null);";
    
    
    public LocalDatabaseHelper(Context context)
    {
        super(context, ListaSpesaDB.DATABASE_NAME, null, ListaSpesaDB.DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase database)
    {
        try
        {
            database.beginTransaction();
            database.execSQL(DATABASE_CREATE);
            database.setTransactionSuccessful();
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            database.endTransaction();
        }
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.w(LocalDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        try
        {
            db.beginTransaction();
            db.execSQL("DROP TABLE IF EXISTS " + ListaSpesaDB.LocalProduct.TABLE_LOCAL_PRODUCT_LIST);
            db.setTransactionSuccessful();
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            db.endTransaction();
        }
        onCreate(db);
    }
    
}
