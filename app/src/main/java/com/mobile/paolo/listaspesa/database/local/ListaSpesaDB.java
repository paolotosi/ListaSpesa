package com.mobile.paolo.listaspesa.database.local;

import android.provider.BaseColumns;

/**
 * Created by paolo on 17/08/17.
 */

/**
 * Local database contains only products belonging to the taken list
 * Structure is similar to product table in remote DB
 */

public class ListaSpesaDB
{
    public static final String DATABASE_NAME = "ListaSpesa.db";
    public static final int DATABASE_VERSION = 2;
    
    // Names of the columns of the database
    public static class LocalProduct implements BaseColumns
    {
        public static final String TABLE_LOCAL_PRODUCT_LIST = "local_products";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BRAND = "brand";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_QUANTITY = "quantity";
    }
    
}
