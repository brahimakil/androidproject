package com.example.ecommercecollegeproject3.ui.products;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProductViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ProductViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is products fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}