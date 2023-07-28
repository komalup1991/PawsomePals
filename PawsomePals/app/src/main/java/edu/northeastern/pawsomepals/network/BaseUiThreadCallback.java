package edu.northeastern.pawsomepals.network;

import java.util.List;

import edu.northeastern.pawsomepals.models.BreedDetails;

public class BaseUiThreadCallback implements PawsomePalWebService.UiThreadCallback {
    @Override
    public void onGetAllBreedsDetails(List<BreedDetails> breeds) {
    }

    @Override
    public void onGetBreedsName(List<String> breedNames){
    }

    @Override
    public void onEmptyResult() {
    }

    @Override
    public void onError() {
    }
}