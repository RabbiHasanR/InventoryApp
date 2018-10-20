package com.example.diu.inventoryapp.ProductData;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.diu.inventoryapp.ProductData.ProductContracts.ProductEntry;

public class ProductsDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG=ProductsDbHelper.class.getSimpleName();
    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="inventory.db";

    /**
     * Drop Table Query for upgrade database
     */
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;


    public ProductsDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE =
                "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                        + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + ProductEntry.COLUMN_MODEL_NAME + " TEXT NOT NULL, "
                        + ProductEntry.COLUMN_BRAND_NAME + " TEXT NOT NULL, "
                        + ProductEntry.COLUMN_COLOUR + " TEXT NOT NULL, "
                        + ProductEntry.COLUMN_SIZE + " INTEGER NOT NULL, "
                        + ProductEntry.COLUMN_STOCK_STATUS + " INTEGER NOT NULL, "
                        + ProductEntry.COLUMN_STOCK_LEVEL + " INTEGER NOT NULL, "
                        + ProductEntry.COLUMN_RESTOCK_AMOUNT + " INTEGER NOT NULL, "
                        + ProductEntry.COLUMN_PRODUCT_PHOTO + " BLOB, "
                        + ProductEntry.COLUMN_UNIT_PRICE + " REAL NOT NULL DEFAULT 0); ";

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);

    }
}
