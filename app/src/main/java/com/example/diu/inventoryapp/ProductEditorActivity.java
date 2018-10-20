package com.example.diu.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.diu.inventoryapp.ProductData.ProductContracts.ProductEntry;
import com.example.diu.inventoryapp.ProductData.ProductsDbHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ProductEditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>  {
    private static final int PRODUCT_LOADER=0;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentUri;
    private EditText productModelEditText;
    private EditText productBrandEditText;
    private EditText productColorEditText;
    private EditText productPriceEditText;
    private EditText productStockEditText;
    private RadioGroup stockIndicator;
    private RadioButton inStockIndicator;
    private RadioButton outStockIndicator;
    private EditText restockEditText;
    private Spinner sizeSpinner;
    private TextView minusButton;
    private TextView plusButton;
    private TextView restockButton;
    private ImageView captureImage;
    private byte[] byteArray=null ;

    private static Integer REQUEST_CAMERA=1,SELECT_FILE=0;

    private int mSize = ProductEntry.SIZE_SMALL;
    private int mStockIndicator = ProductEntry.STOCK_IN;
    private String modelString;
    private String brandString;
    private String colourString;
    private String restockString;
    private String stockString;

    private boolean hasProductChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            hasProductChanged = true;
            return false;
        }
    };

    private View.OnClickListener minusAction=new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            String stockString = productStockEditText.getText().toString().trim();
            int stockLevel;
            if (TextUtils.isEmpty(stockString)) {
                stockLevel = 0;
            } else {
                stockLevel = Integer.parseInt(stockString);
            }
            if (stockLevel > 0) {
                stockLevel = stockLevel - 1;
                productStockEditText.setText(String.valueOf(stockLevel));
            }
        }
    };

    private View.OnClickListener plusAction=new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            String stockString = productStockEditText.getText().toString().trim();
            int stockLevel;
            if (TextUtils.isEmpty(stockString)) {
                stockLevel = 0;
            } else {
                stockLevel = Integer.parseInt(stockString);
            }
            stockLevel = stockLevel + 1;
            productStockEditText.setText(String.valueOf(stockLevel));
        }
    };

    private View.OnClickListener restockAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            stockString = productStockEditText.getText().toString().trim();
            restockString = restockEditText.getText().toString().trim();
            addRestockedItems();
        }
    };
    private View.OnClickListener imageAction=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectImage();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_editor);
        Intent intent=getIntent();
        if(intent!=null){
            mCurrentUri=intent.getData();
            if(mCurrentUri==null){
                setTitle(getString(R.string.editor_activity_title_new_product));
                invalidateOptionsMenu();
            }
            else{
                setTitle(getString(R.string.editor_activity_title_edit_product));
                getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
                // Make restock section visible
                LinearLayout restockSection = (LinearLayout) findViewById(R.id.restock_section);
                restockSection.setVisibility(View.VISIBLE);
                TextView restockButton = (TextView) findViewById(R.id.restock_button);
                restockButton.setOnTouchListener(mTouchListener);
                restockButton.setOnClickListener(restockAction);
            }
        }

        findView();
        setUpSpinner();
        setUpRadioListener();
    }

    private void findView(){
        productModelEditText = (EditText) findViewById(R.id.edit_mobile_model);
        productBrandEditText = (EditText) findViewById(R.id.edit_mobile_brand);
        productColorEditText = (EditText) findViewById(R.id.edit_mobile_color);
        productPriceEditText = (EditText) findViewById(R.id.edit_price);
        sizeSpinner = (Spinner) findViewById(R.id.spinner_size);
        productStockEditText = (EditText) findViewById(R.id.edit_number_stock);
        stockIndicator = (RadioGroup) findViewById(R.id.stock_boolean);
        inStockIndicator = (RadioButton) findViewById(R.id.in_stock);
        outStockIndicator = (RadioButton) findViewById(R.id.out_of_stock);
        restockEditText = (EditText) findViewById(R.id.restock);
         minusButton = (TextView) findViewById(R.id.minus_button);
         plusButton = (TextView) findViewById(R.id.plus_button);
         //click event for stock plus and minus button in editor activity
        minusButton.setOnClickListener(minusAction);
        plusButton.setOnClickListener(plusAction);
        captureImage=(ImageView)findViewById(R.id.image_view);
        captureImage.setOnClickListener(imageAction);
        captureImage.setOnTouchListener(mTouchListener);
        productModelEditText.setOnTouchListener(mTouchListener);
        productBrandEditText.setOnTouchListener(mTouchListener);
        productColorEditText.setOnTouchListener(mTouchListener);
        productStockEditText.setOnTouchListener(mTouchListener);
        productPriceEditText.setOnTouchListener(mTouchListener);
        sizeSpinner.setOnTouchListener(mTouchListener);
        stockIndicator.setOnTouchListener(mTouchListener);
        inStockIndicator.setOnTouchListener(mTouchListener);
        outStockIndicator.setOnTouchListener(mTouchListener);
        restockEditText.setOnTouchListener(mTouchListener);
        minusButton.setOnTouchListener(mTouchListener);
        plusButton.setOnTouchListener(mTouchListener);

    }

    private void setUpSpinner() {
        ArrayAdapter sizeSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.size_array,
                android.R.layout.simple_spinner_item);

        sizeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(sizeSpinnerAdapter);

        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) adapterView.getItemAtPosition(i);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.size_medium))) {
                        mSize = ProductEntry.SIZE_MEDIUM;
                    } else if (selection.equals(getString(R.string.size_large))) {
                        mSize = ProductEntry.SIZE_LARGE;
                    } else {
                        mSize = ProductEntry.SIZE_SMALL;
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mSize = ProductEntry.SIZE_SMALL;
            }
        });
    }

    private void setUpRadioListener() {
        // As stock level is tied to the value of these radio buttons, set up listener to ensure
        // that changes made to the radio buttons will change the stock level value
        stockIndicator.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.in_stock:
                        mStockIndicator = ProductEntry.STOCK_IN;
                        productStockEditText.setEnabled(true);
                        // As the minimum for in stock is 1,
                        productStockEditText.setText("1");
                        break;
                    case R.id.out_of_stock:
                        mStockIndicator = ProductEntry.STOCK_OUT;
                        productStockEditText.setText("0");
                        // As there is no need to enter any value since it is naturally 0
                        productStockEditText.setEnabled(false);
                        break;
                }
            }
        });
    }


    private void saveProduct(){
         modelString = productModelEditText.getText().toString().trim();
         brandString=productBrandEditText.getText().toString().trim();
         colourString = productColorEditText.getText().toString().trim();
         String stockString = productStockEditText.getText().toString().trim();
         restockString = restockEditText.getText().toString().trim();

        String priceInputString = productPriceEditText.getText().toString().trim();

        // The following try block is to ensure no blank values are saved in price
        float priceFloat;
        String priceString = null;
        try {
            priceFloat = Float.valueOf(priceInputString);
            priceString = String.format("%.02f", priceFloat);
        } catch (NumberFormatException nfe) {
            priceFloat = 0;
        }

        // The following is to capture the null or zero value inputs in stock
        if (TextUtils.isEmpty(stockString)) {
            changeStockToZero(stockString);
        } else if (stockString.equals("0")) {
            changeStockToZero(stockString);
        } else mStockIndicator = ProductEntry.STOCK_IN;

        if (TextUtils.isEmpty(restockString) || restockString == null) {
            restockString = "0";
        }

        if (mCurrentUri == null &&
                TextUtils.isEmpty(modelString) &&TextUtils.isEmpty(brandString) &&TextUtils.isEmpty(colourString)
                && TextUtils.isEmpty(priceInputString) && TextUtils.isEmpty(stockString)
                && mSize == ProductEntry.SIZE_SMALL && mStockIndicator ==ProductEntry.STOCK_IN) {
            Toast.makeText(this, getString(R.string.prompt_no_change), Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else if (mCurrentUri == null &&
                (TextUtils.isEmpty(modelString) || TextUtils.isEmpty(brandString)||TextUtils.isEmpty(colourString)
                        || TextUtils.isEmpty(priceInputString) || TextUtils.isEmpty(stockString))) {
            Toast.makeText(this, getString(R.string.prompt_missing), Toast.LENGTH_SHORT).show();
        } else {

            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_MODEL_NAME, modelString);
            values.put(ProductEntry.COLUMN_BRAND_NAME, brandString);
            values.put(ProductEntry.COLUMN_COLOUR, colourString);
            values.put(ProductEntry.COLUMN_UNIT_PRICE, priceString);
            values.put(ProductEntry.COLUMN_STOCK_LEVEL, stockString);
            values.put(ProductEntry.COLUMN_STOCK_STATUS, mStockIndicator);
            values.put(ProductEntry.COLUMN_RESTOCK_AMOUNT, restockString);
            values.put(ProductEntry.COLUMN_SIZE, mSize);
            values.put(ProductEntry.COLUMN_PRODUCT_PHOTO,byteArray);

            if (mCurrentUri == null) {
                //for save new item
                Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
                if (newUri == null) {
                    Toast.makeText(this, getString(R.string.prompt_error_saved), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.prompt_save_successful), Toast.LENGTH_SHORT).show();
                }
            } else {
                //for update exiting item
                int rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);

                if (rowsAffected == 0) {
                    Toast.makeText(this, getString(R.string.prompt_no_change), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.prompt_save_successful), Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    // Helper to change stock to zero value and set the check boxes accordingly
    private void changeStockToZero(String stockAmount) {
        inStockIndicator.setChecked(false);
        outStockIndicator.setChecked(true);
        mStockIndicator = ProductEntry.STOCK_OUT;
        stockAmount = "0";
    }
    private void addRestockedItems() {
        restockString = restockEditText.getText().toString().trim();
        if (TextUtils.isEmpty(restockString)) {
            restockString = "0";
            return;
        } else if (restockString.equals("0")) {
            return;
        }
        int newStockLevel = Integer.valueOf(restockString) + Integer.valueOf(stockString);
        restockString = "0";
        restockEditText.setText(restockString);
        productStockEditText.setText(String.valueOf(newStockLevel));
        Toast.makeText(this, getResources().getString(R.string.stocked) + "!", Toast.LENGTH_SHORT).show();
    }


    private void deleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_prompt);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteProduct();
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

    //create email message
    private String createEmailMessage(String modelName,String brandName, String colour, String quantity, int size) {
        String sizeDisplay;
        switch (size) {
            default:
                sizeDisplay = getResources().getString(R.string.size_small);
                break;
            case 1:
                sizeDisplay = getResources().getString(R.string.size_medium);
                break;
            case 2:
                sizeDisplay = getResources().getString(R.string.size_large);
                break;
        }
        String emailMessage = getResources().getString(R.string.model) + ": " + modelName;
        emailMessage += "\n" + getResources().getString(R.string.brand) + ": " + brandName;
        emailMessage += "\n" + getResources().getString(R.string.colour) + ": " + colour;
        emailMessage += "\n" + getResources().getString(R.string.size) + ": " + sizeDisplay;
        emailMessage += "\n" + getResources().getString(R.string.restock_amount) + ": " + quantity;
        return emailMessage;
    }

    //send email using email app
    private void sendEmail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.prompt_save_send);
        builder.setPositiveButton(R.string.positive_save_send, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveProduct();

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, "jasrabbi50@gmail.com");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Restock of ");
                intent.putExtra(Intent.EXTRA_TEXT, createEmailMessage(modelString,brandString, colourString, restockString, mSize));
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ProductEditorActivity.this, "There are no email applications installed.", Toast.LENGTH_SHORT).show();
                }
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


    private void deleteProduct() {
        if (mCurrentUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.error_delete), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_product_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentUri == null) {
            MenuItem deleteMenu = menu.findItem(R.id.action_delete);
            MenuItem orderMenu = menu.findItem(R.id.action_order);
            deleteMenu.setVisible(false);
            orderMenu.setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert  data" menu option
            case R.id.action_save:
                // Save pet to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete  entries" menu option
            case R.id.action_delete:
                deleteConfirmation();
                return true;
            //Respond to a click on the "Order from supplier" menu option
            case R.id.action_order:
                sendEmail();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!hasProductChanged) {
                    NavUtils.navigateUpFromSameTask(ProductEditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(ProductEditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                unSavedChangedDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!hasProductChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        unSavedChangedDialog(discardClickListener);
    }

    private void unSavedChangedDialog(DialogInterface.OnClickListener discardClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved);
        builder.setNegativeButton(R.string.discard, discardClickListener);
        builder.setPositiveButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_MODEL_NAME,
                ProductEntry.COLUMN_BRAND_NAME,
                ProductEntry.COLUMN_COLOUR,
                ProductEntry.COLUMN_SIZE,
                ProductEntry.COLUMN_UNIT_PRICE,
                ProductEntry.COLUMN_STOCK_LEVEL,
                ProductEntry.COLUMN_RESTOCK_AMOUNT,
                ProductEntry.COLUMN_STOCK_STATUS,
                ProductEntry.COLUMN_PRODUCT_PHOTO

        };

        return new CursorLoader(
                this,
                mCurrentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int modelColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_MODEL_NAME);
            int brandColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_BRAND_NAME);
            int colourColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_COLOUR);
            int sizeColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SIZE);
            int stockStatusColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_STOCK_STATUS);
            int stockLevelColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_STOCK_LEVEL);
            int restockColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_RESTOCK_AMOUNT);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_UNIT_PRICE);
            int imageColumnIndex=cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PHOTO);

            String modelName = cursor.getString(modelColumnIndex);
            String brandName = cursor.getString(brandColumnIndex);
            String colour = cursor.getString(colourColumnIndex);
            String restockAmount = cursor.getString(restockColumnIndex);
            byte[] byteImage=cursor.getBlob(imageColumnIndex);

            int size = cursor.getInt(sizeColumnIndex);
            int stockStatus = cursor.getInt(stockStatusColumnIndex);
            int stockLevel = cursor.getInt(stockLevelColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);

            productModelEditText.setText(modelName);
            productBrandEditText.setText(brandName);
            productColorEditText.setText(colour);
            productPriceEditText.setText(String.format("%.02f", price));
            productStockEditText.setText(Integer.toString(stockLevel));
            restockEditText.setText(restockAmount);
            if(byteImage!=null){
                captureImage.setImageBitmap(getBitmapFromByte(byteImage));
            }
            else {
                captureImage.setImageDrawable(this.getResources().getDrawable(R.drawable.demo_mobile));
            }


            switch (size) {
                case ProductEntry.SIZE_MEDIUM:
                    sizeSpinner.setSelection(1);
                    break;
                case ProductEntry.SIZE_LARGE:
                    sizeSpinner.setSelection(2);
                    break;
                default:
                    sizeSpinner.setSelection(0);
                    break;
            }

            switch (stockStatus) {
                case ProductEntry.STOCK_OUT:
                    inStockIndicator.setChecked(false);
                    outStockIndicator.setChecked(true);
                    break;
                case ProductEntry.STOCK_IN:
                    inStockIndicator.setChecked(true);
                    outStockIndicator.setChecked(false);
                    break;
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productModelEditText.setText("");
        productBrandEditText.setText("");
        productPriceEditText.setText("");
        productColorEditText.setText("");
        productStockEditText.setText("");
        restockEditText.setText("");
        restockEditText.setText("");
        sizeSpinner.setSelection(0);
        inStockIndicator.setChecked(true);
        outStockIndicator.setChecked(false);
        captureImage.setImageDrawable(this.getResources().getDrawable(R.drawable.demo_mobile));

    }

    //selectImage method
    public void selectImage(){
        final CharSequence[] item={"Camera","Gallery","Cancel"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Add Image");
        builder.setItems(item,
                new DialogInterface.OnClickListener() {
            Intent intent=new Intent();
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(item[which].equals("Camera")){
                             intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent,REQUEST_CAMERA);
                            //startActivity(intent);

                        }else if (item[which].equals("Gallery")){
                            intent=new Intent(intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent,SELECT_FILE);
                            //startActivity(intent);

                        }else if (item[which].equals("Cancel")){
                            dialog.dismiss();
                        }
                    }
                });
        //show alertDialog box
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== Activity.RESULT_OK){
            if(requestCode== REQUEST_CAMERA){
                Bundle bundle=data.getExtras();
                Bitmap bmp=(Bitmap)bundle.get("data");
                captureImage.setImageBitmap(bmp);
                //create object ByteArrayOutputStream
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //convert bitmap to byteArray
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArray = stream.toByteArray();

            }else if (requestCode==SELECT_FILE){
                Uri selectImageData=data.getData();
                captureImage.setImageURI(selectImageData);
                byteArray=convertImageToByte(selectImageData);

            }
        }
    }
//convert image uri to byte array
    public byte[] convertImageToByte(Uri uri){
        byte[] data = null;
        try {
            ContentResolver cr = getBaseContext().getContentResolver();
            InputStream inputStream = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            data = baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    //convet byte array image to bitmap
    public static Bitmap getBitmapFromByte(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
