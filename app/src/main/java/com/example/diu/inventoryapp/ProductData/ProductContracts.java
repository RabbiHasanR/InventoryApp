package com.example.diu.inventoryapp.ProductData;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProductContracts {

    //initialize content authority for content uri
    public static final String CONTENT_AUTHORITY = "com.example.diu.inventoryapp";
    //initialize base content uri for content uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //initialize the content uri paths(table name) for content uri
    public static final String PATH_PRODUCTS = "Products";

    /** URI matcher code for the content URI for the pets table */
    public static final int PRODUCTS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    public static final int PRODUCTS_ID = 101;


    public ProductContracts(){}

    public static final class ProductEntry implements BaseColumns{
        //initialize the complete content uri
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String TABLE_NAME="Products";
        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_MODEL_NAME = "model";
        public final static String COLUMN_BRAND_NAME = "brand";
        public final static String COLUMN_COLOUR = "colour";
        public final static String COLUMN_SIZE = "size";
        public final static String COLUMN_STOCK_STATUS = "stockstatus";
        public final static String COLUMN_STOCK_LEVEL = "stock";
        public final static String COLUMN_RESTOCK_AMOUNT = "restock";
        public final static String COLUMN_UNIT_PRICE = "price";
        public final static String COLUMN_PRODUCT_PHOTO="photo";

        public static final int SIZE_SMALL = 0;
        public static final int SIZE_MEDIUM = 1;
        public static final int SIZE_LARGE = 2;

        public static final int STOCK_IN = 0;
        public static final int STOCK_OUT = 1;

        public static boolean isValidSize(int size) {
            if (size == SIZE_SMALL || size == SIZE_MEDIUM || size == SIZE_LARGE) {
                return true;
            }
            return false;
        }

        public static boolean isValidStock(int stock) {
            if (stock == STOCK_IN || stock == STOCK_OUT) {
                return true;
            }
            return false;
        }
    }
}
