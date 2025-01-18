package com.example.ecommercecollegeproject3.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ecommercecollegeproject3.databinding.FragmentHomeBinding;
import com.example.ecommercecollegeproject3.R;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SQLiteDatabase db;
    private Spinner categoryFilterSpinner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        db = getActivity().openOrCreateDatabase("ecommerce.db", getActivity().MODE_PRIVATE, null);
        
        categoryFilterSpinner = root.findViewById(R.id.spinner_category_filter);
        setupCategorySpinner();
        
        return root;
    }

    private void setupCategorySpinner() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("All Categories");
        
        Cursor cursor = db.rawQuery("SELECT category_name FROM categories", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String categoryName = cursor.getString(cursor.getColumnIndex("category_name"));
                categories.add(categoryName);
            } while (cursor.moveToNext());
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, categories) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(android.R.color.black));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(android.R.color.black));
                view.setBackgroundColor(getResources().getColor(android.R.color.white));
                return view;
            }
        };
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(adapter);
        
        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                LinearLayout productsContainer = binding.productsContainer;
                productsContainer.removeAllViews();
                
                if (selectedCategory.equals("All Categories")) {
                    displayProducts(productsContainer);
                } else {
                    displayFilteredProducts(productsContainer, selectedCategory);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void displayFilteredProducts(LinearLayout productsContainer, String category) {
        productsContainer.removeAllViews();
        Cursor cursor = db.rawQuery(
            "SELECT p.* FROM products p " +
            "JOIN categories c ON p.category_id = c.id " +
            "WHERE c.category_name = ?", 
            new String[]{category}
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Using existing product display logic
                @SuppressLint("Range") String productName = cursor.getString(cursor.getColumnIndex("product_name"));
                @SuppressLint("Range") float productPrice = cursor.getFloat(cursor.getColumnIndex("product_price"));
                @SuppressLint("Range") int productQuantity = cursor.getInt(cursor.getColumnIndex("product_quantity"));
                @SuppressLint("Range") String productImage = cursor.getString(cursor.getColumnIndex("image_path"));

                // Create a new LinearLayout to hold product details
                LinearLayout productLayout = new LinearLayout(getContext());
                productLayout.setOrientation(LinearLayout.VERTICAL);
                productLayout.setPadding(24, 24, 24, 24);
                productLayout.setBackgroundResource(R.drawable.custom_product_background);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(16, 16, 16, 16);
                productLayout.setLayoutParams(params);

                // Add the product image
                ImageView productImageView = new ImageView(getContext());
                productImageView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 500));
                Glide.with(getContext())
                        .load(productImage)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(productImageView);
                productLayout.addView(productImageView);

                // Add product name
                TextView productNameText = new TextView(getContext());
                productNameText.setText(productName);
                productNameText.setTextSize(20);
                productNameText.setTextColor(getResources().getColor(R.color.black));
                productNameText.setTypeface(null, android.graphics.Typeface.BOLD);
                productNameText.setPadding(0, 16, 0, 8);
                productLayout.addView(productNameText);

                // Add product price
                TextView productPriceText = new TextView(getContext());
                productPriceText.setText("Price: $" + productPrice);
                productPriceText.setTextSize(18);
                productPriceText.setTextColor(getResources().getColor(R.color.purple_700));
                productPriceText.setTypeface(null, android.graphics.Typeface.BOLD);
                productPriceText.setPadding(0, 4, 0, 8);
                productLayout.addView(productPriceText);

                TextView productQuantityText = new TextView(getContext());
                productQuantityText.setText("Quantity: " + productQuantity);
                productQuantityText.setTextSize(18);
                productQuantityText.setTypeface(null, android.graphics.Typeface.BOLD);
                productQuantityText.setPadding(0, 4, 0, 16);
                productLayout.addView(productQuantityText);

                productLayout.setOnClickListener(v -> openWhatsApp(productName, productPrice));

                productsContainer.addView(productLayout);

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(getContext(), "No products in this category", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayProducts(LinearLayout productsContainer) {
        // Query to fetch all products
        Cursor cursor = db.rawQuery("SELECT * FROM products", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String productName = cursor.getString(cursor.getColumnIndex("product_name"));
                @SuppressLint("Range") float productPrice = cursor.getFloat(cursor.getColumnIndex("product_price"));
                @SuppressLint("Range") int productQuantity = cursor.getInt(cursor.getColumnIndex("product_quantity"));
                @SuppressLint("Range") String productImage = cursor.getString(cursor.getColumnIndex("image_path"));

                LinearLayout productLayout = new LinearLayout(getContext());
                productLayout.setOrientation(LinearLayout.VERTICAL);
                productLayout.setPadding(24, 24, 24, 24);
                productLayout.setBackgroundResource(R.drawable.custom_product_background);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(16, 16, 16, 16);
                productLayout.setLayoutParams(params);

                ImageView productImageView = new ImageView(getContext());
                productImageView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 500));
                Glide.with(getContext())
                        .load(productImage)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(productImageView);
                productLayout.addView(productImageView);

                TextView productNameText = new TextView(getContext());
                productNameText.setText(productName);
                productNameText.setTextSize(20);
                productNameText.setTextColor(getResources().getColor(R.color.black));
                productNameText.setTypeface(null, android.graphics.Typeface.BOLD);
                productNameText.setPadding(0, 16, 0, 8);
                productLayout.addView(productNameText);

                TextView productPriceText = new TextView(getContext());
                productPriceText.setText("Price: $" + productPrice);
                productPriceText.setTextSize(18);
                productPriceText.setTextColor(getResources().getColor(R.color.purple_700));
                productPriceText.setTypeface(null, android.graphics.Typeface.BOLD);
                productPriceText.setPadding(0, 4, 0, 8);
                productLayout.addView(productPriceText);

                TextView productQuantityText = new TextView(getContext());
                productQuantityText.setText("Quantity: " + productQuantity);
                productQuantityText.setTextSize(18);
                productQuantityText.setTypeface(null, android.graphics.Typeface.BOLD);
                productQuantityText.setPadding(0, 4, 0, 16);
                productLayout.addView(productQuantityText);

                productLayout.setOnClickListener(v -> openWhatsApp(productName, productPrice));

                productsContainer.addView(productLayout);

            } while (cursor.moveToNext());
        } else {
            Toast.makeText(getContext(), "No products found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWhatsApp(String productName, float productPrice) {
        String phoneNumber = "70049615";
        String message = "Hey, please I would like to know more about:\n\n" +
                "Product Name: " + productName + "\n" +
                "Price: $" + productPrice;

        String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        intent.setPackage("com.whatsapp");

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "WhatsApp is not installed or available on your device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) {
            db.close();
        }
        binding = null;
    }
}
