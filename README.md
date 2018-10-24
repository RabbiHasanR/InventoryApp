# Project Title
Inventory App
# Description
Here this project insert porduct ,update product and delete product.
Also show all product in list view.
User can upload a product picture in database and show in list.
# Usages
### Add Runtime Permission
```
  <uses-feature android:name="android.hardware.camera.any" android:required="true" />
    <!--<uses-permission android:name="android.permission.CAMERA"></uses-permission>-->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"></uses-permission>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
    <uses-permission android:name="android.permission.MEDIA_CONTENT_CONTROL"
        tools:ignore="ProtectedPermissions"></uses-permission>

```
### Add Dependency
```
 dependencies {
        classpath 'com.android.tools.build:gradle:3.3.0-alpha13'
    }
```

### Add below code in your manifest.xml file.
````

<application
  
..............
<provider
            android:authorities="com.example.diu.inventoryapp"
            android:name=".ProductData.ProductsProvider"
            android:exported="false"/>
            </application>
````
