package com.jdmdsoftware.wallexpress;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class WallService extends Service {

    ArrayList<WallData> wallsArray;
    WallpaperManager wallpaperManager;
    final String notificationChannelId = "WallExpress Service";
    final int startForegroundId = 2002;
    ScheduledExecutorService scheduledExecutorService;


    public WallService() {
        
    }

    public int onStartCommand(Intent intent, int flag, int startId){
        //Toast.makeText(this, "WallExpress goes background now.", Toast.LENGTH_SHORT).show();
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(this::listWallsFile,600,600,TimeUnit.SECONDS);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(
                    notificationChannelId,
                    notificationChannelId,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            getSystemService(NotificationManager.class).createNotificationChannel(notificationChannel);
            Notification.Builder builder = new Notification.Builder(this,notificationChannelId)
                    .setContentText("WallExpressing Back-to-Back")
                    .setContentTitle("WallExpress")
                    .setSmallIcon(R.drawable.smallicon2);

            startForeground(startForegroundId,builder.build());


        }

        return super.onStartCommand(intent,flag,startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void listWallsFile(){
        wallsArray = new ArrayList<>();
        File path = new File(Environment.getExternalStorageDirectory() + "/WallExpressPhotos");

        File[] files = path.listFiles();

        if(files.length != 0){
            for (int i=0;i<files.length;i++){
                WallData data = new WallData(files[i].getAbsolutePath());
                wallsArray.add(data);
            }

            placeTheWalls();

        }else {
            Toast.makeText(this, "There are no photos in the folder", Toast.LENGTH_SHORT).show();
        }
    }

    public void placeTheWalls(){
        wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        Log.i("WallService","here we set again");
        int homeWall = new Random().nextInt(wallsArray.size()-1);
        //int lockWall = new Random().nextInt(wallsArray.size()-1);

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(new File(wallsArray.get(homeWall).getPath()))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(getApplicationContext(), "failed to set wallpaper", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        try {
                            wallpaperManager.setBitmap(resource, null, true, WallpaperManager.FLAG_SYSTEM);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return false;
                    }
                }).submit();

//        Glide.with(getApplicationContext())
//                .asBitmap()
//                .load(new File(wallsArray.get(lockWall).getPath()))
//                .listener(new RequestListener<Bitmap>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
//                        Toast.makeText(getApplicationContext(), "failed to set wallpaper", Toast.LENGTH_SHORT).show();
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//                        try {
//                            wallpaperManager.setBitmap(resource, null, false, WallpaperManager.FLAG_LOCK);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                        return false;
//                    }
//                }).submit();

    }
}
