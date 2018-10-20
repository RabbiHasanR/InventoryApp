package com.example.diu.inventoryapp.Adapter;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.diu.inventoryapp.ProductData.ProductContracts.ProductEntry;
import com.example.diu.inventoryapp.R;

public class ProductCursorAdapter extends CursorAdapter {
    private int stockStatus;
    private byte[] byteImage=null;

    public ProductCursorAdapter(Context context,Cursor cursor){
        super(context,cursor,0);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(final View view, Context context, final Cursor cursor) {
        final TextView modelNameTextView=(TextView)view.findViewById(R.id.item_model_name);
        final TextView brandNameTextView=(TextView)view.findViewById(R.id.item_brand_name);
        TextView colorTextView=(TextView)view.findViewById(R.id.item_colour);
        TextView sizeTextView=(TextView)view.findViewById(R.id.item_size);
        final TextView priceTextView=(TextView)view.findViewById(R.id.item_price);
        final TextView stockTextView = (TextView) view.findViewById(R.id.item_stock);
        final TextView currencyTextView = (TextView) view.findViewById(R.id.item_currency);
        ImageView productImageView=(ImageView)view.findViewById(R.id.item_image_view);
        Button saleButton=(Button)view.findViewById(R.id.sale_button);

        final int columnID = cursor.getColumnIndex(ProductEntry._ID);
        int modelColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_MODEL_NAME);
        int brandColumnIndex=cursor.getColumnIndex(ProductEntry.COLUMN_BRAND_NAME);
        int sizeColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SIZE);
        int colourColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_COLOUR);
        int stockStatusColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_STOCK_STATUS);
        final int stockColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_STOCK_LEVEL);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_UNIT_PRICE);
        int imageColumnIndex=cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PHOTO);

        byteImage=cursor.getBlob(imageColumnIndex);
        String modelName = cursor.getString(modelColumnIndex);
        String brandName = cursor.getString(brandColumnIndex);
        int size = cursor.getInt(sizeColumnIndex);
        // Convert the integer values into text for display
        String sizeDisplayed;
        if (size == 2) {
            sizeDisplayed = context.getString(R.string.size_large);
        } else if (size == 1) {
            sizeDisplayed = context.getString(R.string.size_medium);
        } else {
            sizeDisplayed = context.getString(R.string.size_small);
        }

        String colour = cursor.getString(colourColumnIndex);

        Float price = cursor.getFloat(priceColumnIndex);
        // Display value as a price i.e. 2 decimal places
        String priceDisplay = String.format("%.02f", price);

        String stockLevel = cursor.getString(stockColumnIndex);
        int stockQuantity = cursor.getInt(stockColumnIndex);
         stockStatus = cursor.getInt(stockStatusColumnIndex);
        String stockDisplay;
        // Change colour of the view to greyed out if stock level is zero
        if (stockStatus == 1) {
            stockDisplay = context.getString(R.string.not_in_stock);
            modelNameTextView.setTextColor(context.getResources().getColor(R.color.greyedOutColor));
            brandNameTextView.setTextColor(context.getResources().getColor(R.color.greyedOutColor));
            priceTextView.setTextColor(context.getResources().getColor(R.color.greyedOutColor));
            currencyTextView.setTextColor(context.getResources().getColor(R.color.greyedOutColor));
        } else {
            stockDisplay = stockLevel + " " + context.getString(R.string.in_stock_display);
            modelNameTextView.setTextColor(context.getResources().getColor(R.color.normalTextColor));
            brandNameTextView.setTextColor(context.getResources().getColor(R.color.normalTextColor));
            priceTextView.setTextColor(context.getResources().getColor(R.color.normalTextColor));
            currencyTextView.setTextColor(context.getResources().getColor(R.color.normalTextColor));
        }

        modelNameTextView.setText(modelName);
        brandNameTextView.setText(brandName);
        sizeTextView.setText(sizeDisplayed);
        colorTextView.setText(colour);
        priceTextView.setText(priceDisplay);
        stockTextView.setText(stockDisplay);
        if(byteImage!=null){
            productImageView.setImageBitmap(getBitmapFromByte(byteImage));
        }
        else{
            productImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.demo_mobile));
        }

        final int position=cursor.getPosition();
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.moveToPosition(position);
                int oldQuantity=(cursor.getInt(stockColumnIndex));
                if(oldQuantity>0){
                    oldQuantity--;
                    if (oldQuantity > 0) {
                        int newStockLevel = oldQuantity;
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(ProductEntry.COLUMN_STOCK_LEVEL, newStockLevel);
                        String whereArg = ProductEntry._ID + " =?";
                        //Get the item id which should be updated
                        int item_id = cursor.getInt(columnID);
                        String itemIDArgs = Integer.toString(item_id);
                        String[] selectionArgs = {itemIDArgs};
                        int rowsAffected = view.getContext().getContentResolver().update(
                                ContentUris.withAppendedId(ProductEntry.CONTENT_URI, item_id),
                                contentValues,
                                whereArg, selectionArgs);
                        String newQu = cursor.getString(stockColumnIndex);
                        stockTextView.setText(newQu + " " +
                                view.getContext().getString(R.string.in_stock_display));
                    } else {
                        int newStockLevel = oldQuantity;
                        stockStatus = 1;
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(ProductEntry.COLUMN_STOCK_LEVEL, newStockLevel);
                        contentValues.put(ProductEntry.COLUMN_STOCK_STATUS, stockStatus);
                        String whereArg = ProductEntry._ID + " =?";
                        //Get the item id which should be updated
                        int item_id = cursor.getInt(columnID);
                        String itemIDArgs = Integer.toString(item_id);
                        String[] selectionArgs = {itemIDArgs};
                        int rowsAffected = view.getContext().getContentResolver().update(
                                ContentUris.withAppendedId(ProductEntry.CONTENT_URI, item_id),
                                contentValues,
                                whereArg, selectionArgs);
                        stockTextView.setText(view.getContext().getString(R.string.not_in_stock));
                        modelNameTextView.setTextColor(view.getContext().getResources().getColor(R.color.greyedOutColor));
                        brandNameTextView.setTextColor(view.getContext().getResources().getColor(R.color.greyedOutColor));
                        priceTextView.setTextColor(view.getContext().getResources().getColor(R.color.greyedOutColor));
                        currencyTextView.setTextColor(view.getContext().getResources().getColor(R.color.greyedOutColor));
                    }
                }
            }
        });

    }

    //convet byte array image to bitmap
    public static Bitmap getBitmapFromByte(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
