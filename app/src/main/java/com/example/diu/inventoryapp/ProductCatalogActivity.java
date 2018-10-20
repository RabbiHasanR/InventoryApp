package com.example.diu.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.diu.inventoryapp.Adapter.ProductCursorAdapter;
import com.example.diu.inventoryapp.ProductData.ProductContracts.ProductEntry;

import java.io.ByteArrayOutputStream;

public class ProductCatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private ProductCursorAdapter productCursorAdapter;
    private static final int PRODUCT_LOADER=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_catalog);
        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductCatalogActivity.this, ProductEditorActivity.class);
                startActivity(intent);
            }
        });
        /*if (productCursorAdapter.isEmpty()){
            invalidateOptionsMenu();

        }*/

        // Find the ListView which will be populated with the pet data
        ListView listView=(ListView)findViewById(R.id.list_view);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);
        productCursorAdapter = new ProductCursorAdapter(this, null);
        listView.setAdapter(productCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ProductCatalogActivity.this, ProductEditorActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });

        //kick of the loader
        getLoaderManager().initLoader(PRODUCT_LOADER,null,this);
    }

    private void insertDummyData() {

        // Prefilled data for user to enter to see how it can work
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_MODEL_NAME, "P8 PRO");
        values.put(ProductEntry.COLUMN_BRAND_NAME,"Symphony");
        values.put(ProductEntry.COLUMN_COLOUR, "Yellow");
        values.put(ProductEntry.COLUMN_STOCK_LEVEL, 10);
        values.put(ProductEntry.COLUMN_UNIT_PRICE, 7.00);
        values.put(ProductEntry.COLUMN_SIZE, ProductEntry.SIZE_SMALL);
        values.put(ProductEntry.COLUMN_STOCK_STATUS, ProductEntry.STOCK_IN);
        values.put(ProductEntry.COLUMN_RESTOCK_AMOUNT, 0);
        values.put(ProductEntry.COLUMN_PRODUCT_PHOTO,drawableToByteArray());

        Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
        if(uri==null){
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_product_failed), Toast.LENGTH_SHORT).show();
        }
        else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, getString(R.string.editor_insert_product_success), Toast.LENGTH_SHORT).show();
        }
    }

    //helper method for delete all product
    private void deleteAllProduct(){
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from alpacasso database");
    }

    private void deleteAllConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_prompt);
        builder.setPositiveButton(R.string.delete_all, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAllProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_product_catalog, menu);
        return true;
    }

   /* @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (productCursorAdapter.isEmpty()) {
            MenuItem deleteMenu = menu.findItem(R.id.action_delete_all);
            deleteMenu.setVisible(false);

        }
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert  demo data" menu option
            case R.id.action_demo_product:
                insertDummyData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all:
                deleteAllConfirmation();
                return true;
             //Respond to a click on the "Exit app" menu option
            case R.id.action_exit:
                confirmForExit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_MODEL_NAME,
                ProductEntry.COLUMN_BRAND_NAME,
                ProductEntry.COLUMN_COLOUR,
                ProductEntry.COLUMN_SIZE,
                ProductEntry.COLUMN_UNIT_PRICE,
                ProductEntry.COLUMN_STOCK_LEVEL,
                ProductEntry.COLUMN_STOCK_STATUS,
                ProductEntry.COLUMN_PRODUCT_PHOTO
        };

        return new CursorLoader(
                this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        productCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productCursorAdapter.swapCursor(null);

    }

    //convert drawable image to byte array
    private byte[] drawableToByteArray(){
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.mobile_demo_pic);
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitMapData = stream.toByteArray();
        return bitMapData;
    }

    @Override
    public void onBackPressed() {
        confirmForExit();
    }

    //create alertDialog
    public void confirmForExit(){
        AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.exit_message);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(R.string.exit_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProductCatalogActivity.this.finish();

            }
        });
        alertDialogBuilder.setNegativeButton(R.string.exit_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog=alertDialogBuilder.create();
        alertDialog.show();

    }
}
