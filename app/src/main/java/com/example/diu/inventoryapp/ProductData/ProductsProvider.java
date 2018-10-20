package com.example.diu.inventoryapp.ProductData;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.diu.inventoryapp.ProductData.ProductContracts.ProductEntry;
import com.example.diu.inventoryapp.R;

public class ProductsProvider extends ContentProvider {

    public static final String LOG_TAG=ProductsProvider.class.getSimpleName();
    private ProductsDbHelper productsDbHelper;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(ProductContracts.CONTENT_AUTHORITY,ProductContracts.PATH_PRODUCTS,ProductContracts.PRODUCTS);
        sUriMatcher.addURI(ProductContracts.CONTENT_AUTHORITY,ProductContracts.PATH_PRODUCTS+"/#",ProductContracts.PRODUCTS_ID);
    }
    @Override
    public boolean onCreate() {
        productsDbHelper=new ProductsDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = productsDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ProductContracts.PRODUCTS:
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ProductContracts.PRODUCTS_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ProductContracts.PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case ProductContracts.PRODUCTS_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ProductContracts.PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri,ContentValues contentValues){
        String modelName = contentValues.getAsString(ProductEntry.COLUMN_MODEL_NAME);
        if (modelName == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_model_required));
        }
        String brandName = contentValues.getAsString(ProductEntry.COLUMN_BRAND_NAME);
        if (brandName == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_brand_required));
        }

        String colour = contentValues.getAsString(ProductEntry.COLUMN_COLOUR);
        if (colour == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_colour_required));
        }

        String stockLevel = contentValues.getAsString(ProductEntry.COLUMN_STOCK_LEVEL);
        if (stockLevel == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_stock_level));
        }

        Integer size = contentValues.getAsInteger(ProductEntry.COLUMN_SIZE);
        if (size == null || !ProductEntry.isValidSize(size)) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_size_required));
        }

        Integer stockStatus = contentValues.getAsInteger(ProductEntry.COLUMN_STOCK_STATUS);
        if (stockStatus == null || !ProductEntry.isValidStock(stockStatus)) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_stock_required));
        }

        Integer restockLevel = contentValues.getAsInteger(ProductEntry.COLUMN_RESTOCK_AMOUNT);
        if (restockLevel == null) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_restock));
        }

        Float price = contentValues.getAsFloat(ProductEntry.COLUMN_UNIT_PRICE);
        if (price == null && price < 0) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_price_required));
        }

        // Gets the database in write mode
        SQLiteDatabase db=productsDbHelper.getWritableDatabase();
        long newRowId=db.insert(ProductEntry.TABLE_NAME,null,contentValues);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowsDeleted;
        final int match=sUriMatcher.match(uri);
        SQLiteDatabase db=productsDbHelper.getWritableDatabase();
        switch (match){
            case ProductContracts.PRODUCTS:
                rowsDeleted = db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ProductContracts.PRODUCTS_ID:
                selection=ProductEntry._ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);

        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted!= 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match=sUriMatcher.match(uri);
        switch (match){
            case ProductContracts.PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);

            case ProductContracts.PRODUCTS_ID:
                selection=ProductEntry._ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri,values,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);


        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ProductEntry.COLUMN_MODEL_NAME)) {
            String modelName = values.getAsString(ProductEntry.COLUMN_MODEL_NAME);
            if (modelName == null) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_model_required));
            }
        }
        if (values.containsKey(ProductEntry.COLUMN_BRAND_NAME)) {
            String brandName = values.getAsString(ProductEntry.COLUMN_BRAND_NAME);
            if (brandName == null) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_brand_required));
            }
        }
        if (values.containsKey(ProductEntry.COLUMN_COLOUR)) {
            String colour = values.getAsString(ProductEntry.COLUMN_COLOUR);
            if (colour == null) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_colour_required));
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_SIZE)) {
            Integer size = values.getAsInteger(ProductEntry.COLUMN_SIZE);
            if (size == null || !ProductEntry.isValidSize(size)) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_size_required));
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_STOCK_STATUS)) {
            Integer stockStatus = values.getAsInteger(ProductEntry.COLUMN_STOCK_STATUS);
            if (stockStatus == null || !ProductEntry.isValidStock(stockStatus)) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_stock_required));
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_STOCK_LEVEL)) {
            Integer stockLevel = values.getAsInteger(ProductEntry.COLUMN_STOCK_LEVEL);
            if (stockLevel == null && stockLevel < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_stock_level));
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_RESTOCK_AMOUNT)) {
            Integer stockLevel = values.getAsInteger(ProductEntry.COLUMN_RESTOCK_AMOUNT);
            if (stockLevel == null && stockLevel < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_restock));
            }
        }


        if (values.containsKey(ProductEntry.COLUMN_UNIT_PRICE)) {
            Float price = values.getAsFloat(ProductEntry.COLUMN_UNIT_PRICE);
            if (price == null && price < 0) {
                throw new IllegalArgumentException(getContext().getString(R.string.error_price_required));
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = productsDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
