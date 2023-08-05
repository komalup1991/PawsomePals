package edu.northeastern.pawsomepals.utils;

import edu.northeastern.pawsomepals.models.Recipe;
import edu.northeastern.pawsomepals.models.Users;

public class BaseDataCallback implements FirebaseUtil.DataCallback {
    @Override
    public void onUserReceived(Users user) {

    }

    @Override
    public void onImageUriReceived(String imageUrl) {

    }

    @Override
    public void onError(Exception exception) {

    }

    @Override
    public void onDismiss() {

    }

    @Override
    public void onRecipeReceived(Recipe recipe) {

    }
}
