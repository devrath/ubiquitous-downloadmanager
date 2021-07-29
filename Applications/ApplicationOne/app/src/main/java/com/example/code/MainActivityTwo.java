package com.example.code;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class MainActivityTwo extends AppCompatActivity {

    private static final int REQUEST_CODE = 100;
    //public static final String imageURL = "http://www.tutorialspoint.com/java/java_tutorial.pdf";
    public static final String imageURL = "http://speedtest.ftp.otenet.gr/files/test10Mb.db";
   // String imageName = "java_tutorial.pdf";
    String imageName = "test.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // storage runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }

        Button btnDownloadImage = findViewById(R.id.initiateDownloadId);
        btnDownloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImage(imageURL, imageName);
            }
        });
    }

    public void downloadImage(String url, String outputFileName) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(imageName);
        request.setDescription("Downloading " + imageName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.allowScanningByMediaScanner();
        //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, outputFileName);
        //request.setDestinationInExternalPublicDir(Environment.getExternalStorageDirectory().toString() + File.separator, outputFileName);
        request.setDestinationInExternalFilesDir(this, Environment.getExternalStorageDirectory().toString() + File.separator, outputFileName);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
}