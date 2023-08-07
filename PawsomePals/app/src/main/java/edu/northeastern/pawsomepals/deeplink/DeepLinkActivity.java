package edu.northeastern.pawsomepals.deeplink;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class DeepLinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri deepLinkUri = getIntent().getData();

        if (deepLinkUri != null) {
            PackageManager packageManager = getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(getApplication().getPackageName());
            startActivity(intent);
            finish();

            String deepLinkPath = deepLinkUri.getPath();
        }
    }
}