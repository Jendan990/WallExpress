package com.jdmdsoftware.wallexpress;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements RecyclerObjectItem {
    private static final int PERMISSIONS_REQUEST = 2000;
    private RecyclerView recyclerView;
    private WallAdapter wallAdapter;
    private ArrayList<WallData> arrayList;
    ActivityResultLauncher<Intent> activityResultLauncher;
    private boolean isRead = false;
    private boolean isWrite = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.wall_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this,2));
        arrayList = new ArrayList<>();
        wallAdapter = new WallAdapter(MainActivity.this,arrayList,this);
        recyclerView.setAdapter(wallAdapter);

        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

         */

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),new ActivityResultCallback<ActivityResult>(){
            public void onActivityResult(ActivityResult result){
                if(result.getResultCode() == Activity.RESULT_OK){
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                        if(Environment.isExternalStorageManager()){
                            createSystemFolder();
                        }else{
                            Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                       createSystemFolder();
                    }
                }
            }
        });

        if (!isPermissionGranted()){
            requestPermission();
        }else {
            createSystemFolder();
        }

    }

    boolean isPermissionGranted(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager();
        }else{
            int CHECK_READ = ContextCompat.checkSelfPermission(getApplicationContext(),READ_EXTERNAL_STORAGE);
            return CHECK_READ == PackageManager.PERMISSION_GRANTED;
        }
    }

    void requestPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            try{
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",new Object[]{getApplicationContext().getPackageName()})));
                //startActivityForResult(intent,PERMISSIONS_REQUEST);
                activityResultLauncher.launch(intent);
            }catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                //startActivityForResult(intent,PERMISSIONS_REQUEST);
                activityResultLauncher.launch(intent);

            }
        }else{
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE},PERMISSIONS_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int request,String [] permission,int[] grant){
        super.onRequestPermissionsResult(request,permission,grant);
        switch(request){
            case PERMISSIONS_REQUEST:
                if(grant.length>0){
                    boolean RP = grant[0] == PackageManager.PERMISSION_GRANTED;
                    if(RP){
                        Toast.makeText(getApplicationContext(),"Permission Granted",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Permission Denied by User",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void readThroughPath() {
        File path = new File(Environment.getExternalStorageDirectory() + "/WallExpressPhotos");

        File[] files = path.listFiles();

        if(files.length != 0){
            for (int i=0;i<files.length;i++){
                WallData data = new WallData(files[i].getAbsolutePath());
                arrayList.add(data);
            }
            wallAdapter.notifyDataSetChanged();

            if (!checkServiceActivity()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(MainActivity.this,WallService.class));
                }
            }

        }else {
            Toast.makeText(this, "There are no photos in the folder", Toast.LENGTH_SHORT).show();
        }
    }

    private void createSystemFolder() {

        File file = new File(Environment.getExternalStorageDirectory() + "/WallExpressPhotos");

        boolean success = true;

        if (!file.exists()){
            Toast.makeText(this, "The Folder specified Doesn't Exist.One will be created automatically", Toast.LENGTH_SHORT).show();

            success = file.mkdirs();
        }
        if (success){
            Toast.makeText(this, "Everything is on set.", Toast.LENGTH_SHORT).show();
            readThroughPath();
        }else{
            Toast.makeText(this, "permission was denied", Toast.LENGTH_SHORT).show();
        }

    }
    
    public boolean checkServiceActivity(){
        ActivityManager activityManager =(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo services : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (WallService.class.getName().equals(services.service.getClassName())){
                return true;
            }
        }
        return false;
    }


    @Override
    public void onClicked(int pos) {
        Intent wallSet = new Intent(getApplicationContext(),WallExpressSetWalls.class);
        wallSet.putExtra("path",arrayList.get(pos).getPath());
        startActivity(wallSet);
    }
}