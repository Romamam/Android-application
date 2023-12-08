package com.example.downloader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ironsource.mediationsdk.IronSource;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 7;
    String videoURL;
    Button createVideo;
    EditText videourl;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_main);
        videourl = (EditText) findViewById(R.id.downloadVideo);
        //registerForContextMenu(downloadVideo);
        createVideo = findViewById(R.id.createVideo);
        IronSource.init(this, "1502deb19", IronSource.AD_UNIT.INTERSTITIAL, IronSource.AD_UNIT.BANNER);
        IronSource.loadInterstitial();
        createVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                requestPerms();
                IronSource.showInterstitial("DefaultInterstitial");
                videoURL = videourl.getText().toString().trim();
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    createDirectory("TikTokVideos");
                    TikTokVideo receivedVideo = urlVideo();
                    //System.out.println(receivedVideo);
                    List<String> list = receivedVideo.getVideo();
                    String mainVideo = list.get(0);
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    int filename = nameFile() ;


                    DownloadFromUrl(mainVideo,Environment.getExternalStorageDirectory() +"/TikTokVideos/" +filename+".mp4" );

                    MediaScannerConnection.scanFile(MainActivity.this,
                            new String[] { Environment.getExternalStorageDirectory().toString() },
                            null,
                            new MediaScannerConnection.OnScanCompletedListener() {

                                public void onScanCompleted(String path, Uri uri) {

                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> uri=" + uri);
                                }
                            });

                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    public void DownloadFromUrl(String imageURL, String fileName) {  //this is the downloader method
        try {

            URL url = new URL(imageURL); //you can write here any link
            File file = new File(fileName);

            long startTime = System.currentTimeMillis();
            Log.d("ImageManager", "download begining");
            Log.d("ImageManager", "download url:" + url);
            Log.d("ImageManager", "downloaded file name:" + fileName);
            /* Open a connection to that URL. */
            URLConnection ucon = url.openConnection();


            InputStream is = ucon.getInputStream();

            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            //We create an array of bytes
            byte[] data = new byte[50];
            int current = 0;

            while ((current = bis.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, current);
            }


            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer.toByteArray());
            fos.close();
            Log.d("ImageManager", "download ready in "
                    + ((System.currentTimeMillis() - startTime) / 1000)
                    + " sec");


        } catch (IOException e) {
            Log.d("ImageManager", "Error: " + e);
        }

    }


    public int nameFile(){
        Random r = new Random();
        int max = 1000000;
        int min = 100000;
        int intRandom = r.nextInt(max - min) + min;
        return intRandom;
    }

    public TikTokVideo urlVideo() {
        OkHttpClient client = new OkHttpClient();
        FutureTask<Response> responseFutureTask = new FutureTask<>(() -> {
            Request request = new Request.Builder()
                    .url("https://tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com/vid/index?url=" + videoURL )
                    .get()
                    .addHeader("X-RapidAPI-Host", "tiktok-downloader-download-tiktok-videos-without-watermark.p.rapidapi.com")
                    .addHeader("X-RapidAPI-Key", "a9da07e5dfmshd6499c37b3eb66fp169540jsne60b2774768c")
                    .build();
            return client.newCall(request).execute();
        });
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.execute(responseFutureTask);

        Response response = null;
        try {
            response = responseFutureTask.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        assert response != null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.body().string(), TikTokVideo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void createDirectory(String folderName) {
        File folder = new File(Environment.getExternalStorageDirectory(),folderName);
        if (!folder.exists()) {
            folder.mkdirs();
            Boolean ff = folder.mkdir();
            if (ff) {
                Toast.makeText(MainActivity.this, "Folder created successfully!", Toast.LENGTH_SHORT).show();
                Log.d("ImageManager", "new Sanya");
            } else {
                Toast.makeText(MainActivity.this, "Failed to create folder", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(MainActivity.this, "Folder already exist", Toast.LENGTH_SHORT).show();
            Log.d("ImageManager", "not new Sanya");
        }
    }
    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(permissions,PERMISSION_REQUEST_CODE);

        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri));
            String[] str = new String[1];
            str[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, PERMISSION_REQUEST_CODE);
        }
    }
}