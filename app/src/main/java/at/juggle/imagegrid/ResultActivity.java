package at.juggle.imagegrid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import at.juggle.artistgrid.R;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);





    }
    public void myClickHandler(View view) {
        int id = view.getId();
        if (id == R.id.instagramShare) {
            try {
                Uri uriImage = FileProvider.getUriForFile(FinalActivity.this,
                        getString(R.string.file_provider_authority), new File(imagePath));
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uriImage);
                intent.setPackage("com.instagram.android");
                startActivity(intent);
            } catch (Exception i) {
                Toast.makeText(this, getString(R.string.no_instagram_app), Toast.LENGTH_SHORT).show();
            }


        }

        if (id == R.id.whatsup_share) {
            try {
                Uri uriImage = FileProvider.getUriForFile(FinalActivity.this,
                        getString(R.string.file_provider_authority), new File(imagePath));

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uriImage);
                intent.setPackage("com.whatsapp");
                startActivity(intent);
            } catch (Exception i) {
                Toast.makeText(this, getString(R.string.no_whatsapp_app), Toast.LENGTH_SHORT).show();
            }
        }
        if (id == R.id.facebook_share) {
            initShareIntent("face");
        }
        if (id == R.id.more) {
            Uri uriImage = FileProvider.getUriForFile(FinalActivity.this,
                    getString(R.string.file_provider_authority), new File(imagePath));

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uriImage);
            startActivity(sharingIntent);
        }
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

                    Uri uriImage = FileProvider.getUriForFile(FinalActivity.this,
                            getString(R.string.file_provider_authority), new File(imagePath));

                    share.putExtra(Intent.EXTRA_SUBJECT, "Created With #Photo Collage Editor App");
                    share.putExtra(Intent.EXTRA_TEXT, "Created With #Photo Collage Editor App");
                    share.putExtra(Intent.EXTRA_STREAM, uriImage);
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Toast.makeText(this, getString(R.string.no_facebook_app), Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(Intent.createChooser(share, "Select"));
        }
    }
}