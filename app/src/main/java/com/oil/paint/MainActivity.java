package com.oil.paint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.jabistudio.androidjhlabs.filter.EdgeFilter;
import com.jabistudio.androidjhlabs.filter.GrayscaleFilter;
import com.jabistudio.androidjhlabs.filter.InvertFilter;
import com.jabistudio.androidjhlabs.filter.OilFilter;
import com.jabistudio.androidjhlabs.filter.PosterizeFilter;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.ortiz.touchview.TouchImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import at.juggle.artistgrid.R;

public class MainActivity extends AppCompatActivity {
    private static int RESULT_SETTINGS_ACTIVITY = 2;
    public final static String PREFS_NAME = "PrefsFileArtistGrid";
    public final static String IMG_CACHED = "image.png";
    private int lineColor;
    boolean colorpicker, squareGrid, saveFileOnExit;
    Bitmap buffer = null, original = null;
    private float maxImageSide = 1200;








    File outputFile;
    private AdView mAdView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Iconify.with(new FontAwesomeModule());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setIcon(R.drawable.splashmain);


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setImageDrawable(new IconDrawable(this, FontAwesomeIcons.fa_folder_open).colorRes(R.color.colorPrimaryDark).actionBarSize());
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                        .setAction("Action", null).show();
//                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
////                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
////                        Manifest.permission.READ_EXTERNAL_STORAGE);
////
////                if (permissionCheck == PackageManager.PERMISSION_GRANTED)
//                startActivityForResult(i, RESULT_LOAD_IMAGE);
//            }
//        });

//        final FloatingActionButton fabfull = (FloatingActionButton) findViewById(R.id.leaveFullscreenButton);
//        fabfull.setImageDrawable(new IconDrawable(this, FontAwesomeIcons.fa_chevron_down).colorRes(R.color.colorPrimaryDark).actionBarSize());
//        fabfull.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                fabfull.setVisibility(View.INVISIBLE);
//                getSupportActionBar().show();
//            }
//        });

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        lineColor = settings.getInt("lineColor", 0);
        colorpicker = settings.getBoolean("colorpicker", false);
        saveFileOnExit = settings.getBoolean("savefileonexit", true);
        squareGrid = settings.getBoolean("squareGrid", false);
        TouchImageView view = (TouchImageView) findViewById(R.id.mainImageView);
        view.setMaxZoom(5f);
        view.setOnTouchListener(new ColorPickerOnTouchListener(view, this));

        // Get the intent that started this activity
        Intent intent = getIntent();
        // Figure out what to do based on the intent type
        if (intent != null ) {
            String uri = intent.getStringExtra("imageUri");
            Uri data = Uri.parse(uri);
//            Uri data = intent.getStringExtra("imageUri");

            if (data !=null ) openImage(data);
            else {
                Toast.makeText(getApplicationContext(), "Error: Could not open image!", Toast.LENGTH_LONG).show();
            }
        }


        findViewById(R.id.normal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (original!=null) applyFilters(3);
            }
        });
        findViewById(R.id.dark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (original!=null){
                    applyFilters(3);
                    applyFilters(0);
                }
            }
        });
        findViewById(R.id.light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (original!=null){
                            applyFilters(3);
                            applyFilters(2);

                }
            }
        });











        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        myClickHandler();


    }
    public void myClickHandler() {
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.result_mian).setVisibility(View.GONE);
                findViewById(R.id.editor_main).setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.homebtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectImageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.instagramShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uriImage = FileProvider.getUriForFile(MainActivity.this,
                            getString(R.string.file_provider_authority), outputFile);
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, uriImage);
                    intent.setPackage("com.instagram.android");
                    startActivity(intent);
                } catch (Exception i) {
                    Toast.makeText(MainActivity.this, getString(R.string.no_instagram_app), Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.whatsup_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uriImage = FileProvider.getUriForFile(MainActivity.this,
                            getString(R.string.file_provider_authority), outputFile);

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, uriImage);
                    intent.setPackage("com.whatsapp");
                    startActivity(intent);
                } catch (Exception i) {
                    Toast.makeText(MainActivity.this, getString(R.string.no_whatsapp_app), Toast.LENGTH_SHORT).show();
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
                Uri uriImage = FileProvider.getUriForFile(MainActivity.this,
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

                    Uri uriImage = FileProvider.getUriForFile(MainActivity.this,
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










    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.id_next).setIcon(new IconDrawable(this, FontAwesomeIcons.fa_share).colorRes(R.color.colorWhite).actionBarSize());
//        menu.findItem(R.id.action_save).setIcon(new IconDrawable(this, FontAwesomeIcons.fa_save).colorRes(R.color.colorWhite).actionBarSize());
//        menu.findItem(R.id.action_fullscreen).setIcon(new IconDrawable(this, FontAwesomeIcons.fa_arrows_alt).colorRes(R.color.colorWhite).actionBarSize());
//        menu.findItem(R.id.action_grid).setIcon(new IconDrawable(this, FontAwesomeIcons.fa_th).colorRes(R.color.colorWhite).actionBarSize());
//        menu.findItem(R.id.action_menu_filter).setIcon(new IconDrawable(this, FontAwesomeIcons.fa_filter).colorRes(R.color.colorWhite).actionBarSize());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       if (id == R.id.id_next) {
           if (buffer == null) return true;

findViewById(R.id.result_mian).setVisibility(View.VISIBLE);
findViewById(R.id.editor_main).setVisibility(View.GONE);


           new Thread(new Runnable() {
               public void run() {
                   try {

//                       Bitmap bmp=getIntent().getParcelableExtra("image");
//                    imageView.setImageBitmap(bitImage);
//                    Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                       File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OilConversion");
                       if (!directory.exists()) directory.mkdirs();
                       outputFile = new File(directory, "OilConversion_" + (android.text.format.DateFormat.format("yyyy_MM_dd-hh_mm_ss", new java.util.Date())) + ".jpg");
                       FileOutputStream outStream = new FileOutputStream(outputFile);
                       buffer.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
                       outStream.flush();
                       outStream.close();
//                    uri = MediaStore.Images.Media.insertImage(getContentResolver(), outputFile.getAbsolutePath(), outputFile.getName(), outputFile.getName());

                   } catch (IOException e) {
                       e.printStackTrace();

                   }
                   // a potentially time consuming task
               }
           }).start();



//           ByteArrayOutputStream stream = new ByteArrayOutputStream();
//           buffer.compress(Bitmap.CompressFormat.PNG, 80, stream);
//           byte[] byteArray = stream.toByteArray();
//
//           Intent in1 = new Intent(this, ResultActivity.class);
//           in1.putExtra("image",buffer);
//           startActivity(in1);
        }
//       else if (id == R.id.action_share) {
//            try {
////                File outputDir = getApplicationContext().getCacheDir(); // context being the Activity pointer
////                File outputFile = File.createTempFile("image", "jpeg", outputDir);
//                if (buffer!=null) {
//                    File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "artistgrid");
//                    if (!directory.exists()) directory.mkdirs();
//                    File outputFile = new File(directory, "grid_" + (android.text.format.DateFormat.format("yyyy_MM_dd-hh_mm_ss", new java.util.Date())) + ".jpg");
//                    FileOutputStream outStream = new FileOutputStream(outputFile);
//                    buffer.compress(Bitmap.CompressFormat.JPEG, 75, outStream);
//                    outStream.flush();
//                    outStream.close();
//                    String uri = MediaStore.Images.Media.insertImage(getContentResolver(), outputFile.getAbsolutePath(), outputFile.getName(), outputFile.getName());
//                    Intent sendIntent = new Intent();
//                    sendIntent.setAction(Intent.ACTION_SEND);
//                    sendIntent.setType("image/jpeg");
//                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(outputFile));
//                    startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//       else if (id == R.id.action_save) {
//            if (buffer == null) return true;
//            // save edges file
//            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "artistgrid");
//            if (!directory.exists()) directory.mkdirs();
//            File toSave = new File(directory, "grid_" + (android.text.format.DateFormat.format("yyyy_MM_dd-hh_mm_ss", new java.util.Date())) + ".png");
//            try {
//                FileOutputStream outStream = new FileOutputStream(toSave);
//                // todo ...
//                buffer.compress(Bitmap.CompressFormat.PNG, 100, outStream);
//                outStream.flush();
//                outStream.close();
//                Toast.makeText(getApplicationContext(), "Saved to " + directory + "!", Toast.LENGTH_LONG).show();
//                MediaStore.Images.Media.insertImage(getContentResolver(), toSave.getAbsolutePath(), toSave.getName(), toSave.getName());
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(getApplicationContext(), "Save failed!", Toast.LENGTH_LONG).show();
//            }
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }



    private void openImage(Uri selectedImage) {
        try {
            original = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            File file = new File(getApplicationContext().getFilesDir(), IMG_CACHED);
            if (file.exists()) file.delete();
            OutputStream out = new FileOutputStream(file);
            int w = original.getWidth();
            int h = original.getHeight();
            if (Math.max(w, h) > maxImageSide) {
                float scalefactor = 1f;
                if (h > w) {
                    scalefactor = maxImageSide / h;
                } else scalefactor = maxImageSide / w;
                original = Bitmap.createScaledBitmap(original, (int) (scalefactor * w), (int) (scalefactor * h), true);
            }
            original.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            paintLines(original);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Cannot open " + selectedImage.getPath() + "!", Toast.LENGTH_LONG).show();
        }
    }

    private void applyFilters(int which) {
        if (which==0) { // comic
            PosterizeFilter quantizeFilter = new PosterizeFilter();
            EdgeFilter edgeFilter = new EdgeFilter();
            InvertFilter invertFilter = new InvertFilter();
            GrayscaleFilter grayscaleFilter = new GrayscaleFilter();
            int[] pixels = AndroidUtils.bitmapToIntArray(original);
            int[] pixels2;
            pixels2 = quantizeFilter.filter(AndroidUtils.bitmapToIntArray(original), original.getWidth(), original.getHeight());
            pixels = grayscaleFilter.filter(pixels, original.getWidth(), original.getHeight());
            pixels = edgeFilter.filter(pixels, original.getWidth(), original.getHeight());
            pixels = invertFilter.filter(pixels, original.getWidth(), original.getHeight());
            Bitmap tmp1 = Bitmap.createBitmap(pixels, 0, original.getWidth(), original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
            Bitmap tmp2 = Bitmap.createBitmap(pixels2, 0, original.getWidth(), original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
            original = combineWithOverlay(tmp1, tmp2);
        }  else if (which==2) { // black & white
            OilFilter grayscaleFilter = new OilFilter();
            int[] pixels = AndroidUtils.bitmapToIntArray(original);
            pixels = grayscaleFilter.filter(pixels, original.getWidth(), original.getHeight());
            original = Bitmap.createBitmap(pixels, 0, original.getWidth(), original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        } else if (which==3) { // reset ..
            File file = new File(getApplicationContext().getFilesDir(), IMG_CACHED);
            if (file.exists()) {
                try {
                    original = BitmapFactory.decodeStream(new FileInputStream(file));
                    paintLines(original);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        paintLines(original);
    }

    protected Bitmap combineWithOverlay(Bitmap edges, Bitmap image) {
        Bitmap result = image.copy(Bitmap.Config.ARGB_8888, true);


        Paint p = new Paint();
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
        BitmapShader gradientShader = new BitmapShader(edges, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        p.setShader(gradientShader);


        Canvas c = new Canvas();
        c.setBitmap(result);
        c.drawBitmap(image, 0, 0, null);
        c.drawRect(0, 0, image.getWidth(), image.getHeight(), p);

        return result;
    }

    private void paintLines(Bitmap bitmap) {
        if (bitmap==null) return;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        buffer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(buffer);
//        Paint colorFilter = new Paint(); // todo: check filtering ...
//        colorFilter.setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.LIGHTEN));
//        colorFilter.setFilterBitmap(true);
        c.drawBitmap(bitmap, 0, 0, null);
//        int rowOffset = h / (rows + 1);
//        if (!squareGrid) {
//            for (int i = 0; i < cols; i++) {
//                for (int k = 0; k < lineWidth; k++) {
//                    c.drawLine((i + 1) * w / (cols + 1) + k, 0, (i + 1) * w / (cols + 1) + k, h, linePaint);
//                }
//            }
//        } else if (rows>0) {
//            int numColLines = w / rowOffset;
//            int offsetCols = (w%rowOffset)/2; // todo: make configurable ("where image starts")
//            for (int i=0; i<= numColLines; i++) {
//                for (int k = 0; k < lineWidth; k++) {
//                    c.drawLine((i) * rowOffset + k + offsetCols, 0, (i) * rowOffset + k + offsetCols, h, linePaint);
//                }
//            }
//        }
//
//        for (int i = 0; i < rows; i++) {
//            for (int k = 0; k < lineWidth; k++) {
//                c.drawLine(0, (i + 1) * rowOffset + k, w, (i + 1) * rowOffset + k, linePaint );
//            }
//        }


        TouchImageView view = (TouchImageView) findViewById(R.id.mainImageView);
        view.setImageBitmap(buffer);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("colorpicker", colorpicker);
        editor.putBoolean("savefileonexit", saveFileOnExit);
        // Commit the edits!
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        if (!saveFileOnExit) {
            File file = new File(getApplicationContext().getFilesDir(), IMG_CACHED);
            if (file.exists()) file.delete();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        rows = settings.getInt("rows", 4);
//        cols = settings.getInt("cols", 3);
//        lineWidth = settings.getInt("lineWidth", 3);
//        colorpicker = settings.getBoolean("colorpicker", false);
//        String tmp = settings.getString("currentImage", null);
//        try {
//            File file = new File(getApplicationContext().getFilesDir(), IMG_CACHED);
//            if (file.exists()) {
//                original = BitmapFactory.decodeStream(new FileInputStream(file));
//                paintLines(original);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static class ColorPickerOnTouchListener implements View.OnTouchListener {
        private final TouchImageView view;
        private MainActivity mainActivity;

        public ColorPickerOnTouchListener(TouchImageView view, MainActivity original) {
            this.view = view;
            this.mainActivity = original;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.i(getClass().getName(), "touch registered.");
            if (mainActivity.colorpicker) {
                float x = event.getX();
                float y = event.getY();
                PointF pointF = view.transformCoordTouchToBitmap(x, y, true);
                if (mainActivity.original != null && pointF.x < mainActivity.original.getWidth() && pointF.y < mainActivity.original.getHeight()) {
                    int color = mainActivity.original.getPixel(((int) pointF.x), ((int) pointF.y));
                    String hexColor = "";
                    hexColor += Color.red(color) < 16 ? "0" + Integer.toHexString(Color.red(color)) : Integer.toHexString(Color.red(color));
                    hexColor += Color.green(color) < 16 ? "0" + Integer.toHexString(Color.green(color)) : Integer.toHexString(Color.green(color));
                    hexColor += Color.blue(color) < 16 ? "0" + Integer.toHexString(Color.blue(color)) : Integer.toHexString(Color.blue(color));

                    String rgb = "(";
                    rgb += Color.red(color) + ", ";
                    rgb += Color.green(color) + ", ";
                    rgb += Color.blue(color) + ")";
                    Snackbar.make(view, mainActivity.getString(R.string.color) + " " + rgb + " / #" + hexColor, Snackbar.LENGTH_INDEFINITE).show();
                }
            }
            return true;
        }
    }
}
