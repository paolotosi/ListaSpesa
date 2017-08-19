package com.mobile.paolo.listaspesa.database.local;

import com.mobile.paolo.listaspesa.model.objects.Product;

import java.util.List;

/**
 * Created by paolo on 17/08/17.
 */

public interface ProductDAO
{
    // Database access
    void open();
    void close();

    // Product methods
    List<Product> getAllProducts();
    Product insertProduct(Product product);
    void resetAllProducts();
    void deleteSingleProduct(Product product);
    void deleteProducts(List<Product> productList);
    void updateSingleProduct(String key, String newName, String newBrand, String newDescription, int newQuantity);
}
