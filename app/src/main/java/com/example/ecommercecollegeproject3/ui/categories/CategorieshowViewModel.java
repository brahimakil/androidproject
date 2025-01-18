package com.example.ecommercecollegeproject3.ui.categories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CategorieshowViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CategorieshowViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Categories fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}