package edu.northeastern.pawsomepals.deeplink;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import edu.northeastern.pawsomepals.ui.login.HomeActivity;

public class DeepLinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri deepLinkUri = getIntent().getData();

        if (deepLinkUri != null) {
            String feedId = deepLinkUri.getQueryParameter("feedId");
            if (feedId == null) {
            PackageManager packageManager = getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(getApplication().getPackageName());
                if (intent != null) {
                    startActivity(intent);
                }
            } else {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("feedId", feedId);
                startActivity(intent);
            }
        }
        finish();

           // String deepLinkPath = deepLinkUri.getPath();
        }
    }
