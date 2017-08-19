package com.mobile.paolo.listaspesa.database.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobile.paolo.listaspesa.model.objects.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paolo on 17/08/17.
 */

public class ProductsLocalDatabaseHelper implements ProductDAO
{
    private static ProductsLocalDatabaseHelper instance;
    private SQLiteDatabase database;
    private LocalDatabaseHelper dbHelper;
    private Context context;

    private String[] allColumns = { ListaSpesaDB.LocalProduct.COLUMN_NAME,
                                    ListaSpesaDB.LocalProduct.COLUMN_BRAND,
                                    ListaSpesaDB.LocalProduct.COLUMN_DESCRIPTION,
                                    ListaSpesaDB.LocalProduct.COLUMN_QUANTITY};

    private ProductsLocalDatabaseHelper(Context context)
    {
        this.context = context;
    }

    public static synchronized ProductsLocalDatabaseHelper getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new ProductsLocalDatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void open()
    {
        if(dbHelper == null) {
            dbHelper = new LocalDatabaseHelper(context);
        }
        database = dbHelper.getWritableDatabase();
    }

    @Override
    public void close()
    {
        dbHelper.close();
    }

    @Override
    public List<Product> getAllProducts()
    {
        List<Product> productList = new ArrayList<>();

        // Query = SELECT * FROM local_products
        Cursor cursor = database.query( ListaSpesaDB.LocalProduct.TABLE_LOCAL_PRODUCT_LIST,
                                        allColumns,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null);

        // Select first row of result
        cursor.moveToFirst();

        // Iterate over result
        while(!cursor.isAfterLast())
        {
            Product product = cursorToProduct(cursor);
            productList.add(product);
            cursor.moveToNext();
        }

        cursor.close(); // remember to always close the cursor!
        return productList;
    }

    @Override
    public Product insertProduct(Product product)
    {
        // Insert in the db
        database.insert(ListaSpesaDB.LocalProduct.TABLE_LOCAL_PRODUCT_LIST, null, productToValues(product));

        // Read from DB the inserted person and return it
        // Query = SELECT * FROM local_products WHERE name = product.getName()
        Cursor cursor = database.query( ListaSpesaDB.LocalProduct.TABLE_LOCAL_PRODUCT_LIST,
                                        allColumns,
                                        ListaSpesaDB.LocalProduct.COLUMN_NAME + " = ?",
                                        new String[] {"" + product.getName()},
                                        null,
                                        null,
                                        null);
        // Select first row of result
        cursor.moveToFirst();

        // Convert to product
        Product queryResult = cursorToProduct(cursor);

        // Close cursor
        cursor.close();

        return queryResult;
    }

    @Override
    public void resetAllProducts()
    {
        // Query = DELETE * FROM local_products
        database.delete(ListaSpesaDB.LocalProduct.TABLE_LOCAL_PRODUCT_LIST, null, null);
    }

    @Override
    public void deleteSingleProduct(Product product)
    {
        // Query = DELETE * FROM local_products WHERE name = product.getName()
        database.delete(ListaSpesaDB.LocalProduct.TABLE_LOCAL_PRODUCT_LIST,
                        ListaSpesaDB.LocalProduct.COLUMN_NAME + " = ?",
                        new String[] {"" + product.getName()});
    }

    @Override
    public void deleteProducts(List<Product> productList)
    {
        for(Product product : productList)
        {
            deleteSingleProduct(product);
        }
    }

    @Override
    public void updateSingleProduct(String key, String newName, String newBrand, String newDescription, int newQuantity)
    {
        Product product = new Product(newName, newBrand, newDescription, newQuantity);
        ContentValues dataToInsert = productToValues(product);
        // Query = UPDATE local_products SET name = newName, brand = newBrand, description = newDescription, quantity = newQuantity
        //         WHERE name = key
        database.update(ListaSpesaDB.LocalProduct.TABLE_LOCAL_PRODUCT_LIST,
                        dataToInsert,
                        ListaSpesaDB.LocalProduct.COLUMN_NAME + " = ?",
                        new String[] {"" + key});
    }

    // Convert from Product object to database item
    private ContentValues productToValues(Product product)
    {
        ContentValues values = new ContentValues();
        values.put(ListaSpesaDB.LocalProduct.COLUMN_NAME, product.getName());
        values.put(ListaSpesaDB.LocalProduct.COLUMN_BRAND,  product.getBrand());
        values.put(ListaSpesaDB.LocalProduct.COLUMN_DESCRIPTION, product.getDescription());
        values.put(ListaSpesaDB.LocalProduct.COLUMN_QUANTITY, product.getQuantity());
        return values;
    }

    // Convert from database item to Product object
    private Product cursorToProduct(Cursor cursor)
    {
        String name = cursor.getString(0);
        String brand = cursor.getString(1);
        String description = cursor.getString(2);
        int quantity = cursor.getInt(3);
        return new Product(name, brand, description, quantity);
    }
}
