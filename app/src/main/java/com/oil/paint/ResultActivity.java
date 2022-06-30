package com.oil.paint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import at.juggle.artistgrid.R;

public class ResultActivity extends AppCompatActivity {
    File outputFile;
    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] byteArray = getIntent().getByteArrayExtra("image");
                    Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OilConversion");
                    if (!directory.exists()) directory.mkdirs();
                    outputFile = new File(directory, "OilConversion_" + (android.text.format.DateFormat.format("yyyy_MM_dd-hh_mm_ss", new java.util.Date())) + ".jpg");
                    FileOutputStream outStream = new FileOutputStream(outputFile);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
                    outStream.flush();
                    outStream.close();
//                    uri = MediaStore.Images.Media.insertImage(getContentResolver(), outputFile.getAbsolutePath(), outputFile.getName(), outputFile.getName());

                } catch (IOException e) {
                    e.printStackTrace();

                }
                // a potentially time consuming task
            }
        }).start();

        myClickHandler();
    }

    public void myClickHandler() {
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.homebtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, SelectImageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.instagramShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uriImage = FileProvider.getUriForFile(ResultActivity.this,
                            getString(R.string.file_provider_authority), outputFile);
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, uriImage);
                    intent.setPackage("com.instagram.android");
                    startActivity(intent);
                } catch (Exception i) {
                    Toast.makeText(ResultActivity.this, getString(R.string.no_instagram_app), Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.whatsup_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uriImage = FileProvider.getUriForFile(ResultActivity.this,
                            getString(R.string.file_provider_authority), outputFile);

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, uriImage);
                    intent.setPackage("com.whatsapp");
                    startActivity(intent);
                } catch (Exception i) {
                    Toast.makeText(ResultActivity.this, getString(R.string.no_whatsapp_app), Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.facebook_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initShareIntent("face");
            }
        });
        findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uriImage = FileProvider.getUriForFile(ResultActivity.this,
                        getString(R.string.file_provider_authority), outputFile);

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("image/*");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uriImage);
                startActivity(sharingIntent);
            }
        });
        findViewById(R.id.saveimg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OilConversion");

                Toast.makeText(getApplicationContext(), "Saved to " + directory + "!", Toast.LENGTH_LONG).show();

            }
        });


    }

    private void initShareIntent(String type) {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("image/jpeg");

        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(
                share, 0);
        if (!resInfo.isEmpty()) {
            // FilePath = getImagePath();

            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(type)
                        || info.activityInfo.name.toLowerCase().contains(type)) {

                    Uri uriImage = FileProvider.getUriForFile(ResultActivity.this,
                            getString(R.string.file_provider_authority), outputFile);

                    share.putExtra(Intent.EXTRA_STREAM, uriImage);
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Toast.makeText(this, "No App Found", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(Intent.createChooser(share, "Select"));
        }
    }
}