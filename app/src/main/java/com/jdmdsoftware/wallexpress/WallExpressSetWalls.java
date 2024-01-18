package com.jdmdsoftware.wallexpress;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.IOException;

public class WallExpressSetWalls extends AppCompatActivity {
    private ImageView imageView;
    private Button buttonSet;
    WallpaperManager wallpaperManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall_express_set_walls);
        wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        imageView = findViewById(R.id.imaging);
        buttonSet = findViewById(R.id.btn_setwalls);

        Glide.with(this).load(new File(getIntent().getStringExtra("path"))).into(imageView);

        buttonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(WallExpressSetWalls.this)
                        .asBitmap()
                        .load(new File(getIntent().getStringExtra("path")))
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                Toast.makeText(WallExpressSetWalls.this, "failed to set wallpaper", Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                try {
                                    wallpaperManager.setBitmap(resource,null,true,WallpaperManager.FLAG_SYSTEM);
                                    Toast.makeText(WallExpressSetWalls.this, "Wallpaper set successful", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return false;
                            }
                        }).submit();
            }
        });
    }
}