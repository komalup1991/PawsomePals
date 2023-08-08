package edu.northeastern.pawsomepals.utils;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;

public class ActivityHelper {

    public static final int SUCCESS_CODE = 1;
    public static final String CREATION_STATUS = "CreationStatus";

    public static void setResult(Activity activity, boolean isSuccess){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(CREATION_STATUS, isSuccess ? SUCCESS_CODE : 0);
        activity.setResult(RESULT_OK, resultIntent);
    }
}
