package com.example.ecommercecollegeproject3.ui.categories;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ecommercecollegeproject3.databinding.FragmentCategoriesBinding;

public class CategoriesFragment extends Fragment {

    private FragmentCategoriesBinding binding;
    private SQLiteDatabase db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCategoriesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = getActivity().openOrCreateDatabase("ecommerce.db", getActivity().MODE_PRIVATE, null);

        createCategoriesTable();

        binding.btnInsertCategory.setOnClickListener(v -> insertCategory());
        binding.btnDeleteCategory.setOnClickListener(v -> deleteCategory());
        binding.btnViewAllCategories.setOnClickListener(v -> viewAllCategories());

        return root;
    }

    private void createCategoriesTable() {
        String createCategoriesTable = "CREATE TABLE IF NOT EXISTS categories ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "category_name TEXT NOT NULL)";
        db.execSQL(createCategoriesTable);
    }

    private void insertCategory() {
        String categoryName = binding.etCategoryName.getText().toString();

        if (categoryName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a category name", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = db.rawQuery("SELECT * FROM categories WHERE category_name = ?", new String[]{categoryName});
        if (cursor != null && cursor.moveToFirst()) {
            Toast.makeText(getContext(), "Category already exists", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        String insertCategoryQuery = "INSERT INTO categories (category_name) VALUES (?)";
        db.execSQL(insertCategoryQuery, new Object[]{categoryName});

        Toast.makeText(getContext(), "Category inserted successfully", Toast.LENGTH_SHORT).show();
    }

    private void deleteCategory() {
        String categoryName = binding.etCategoryName.getText().toString();

        if (categoryName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a category name", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Cursor cursor = db.rawQuery("SELECT id FROM categories WHERE category_name = ?",
                new String[]{categoryName});
            
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") int categoryId = cursor.getInt(cursor.getColumnIndex("id"));
                cursor.close();
                
                db.beginTransaction();
                try {
                    db.execSQL("DELETE FROM products WHERE category_id = ?",
                        new String[]{String.valueOf(categoryId)});
                    
                    db.execSQL("DELETE FROM categories WHERE category_name = ?",
                        new String[]{categoryName});
                    
                    db.setTransactionSuccessful();
                    Toast.makeText(getContext(), "Category and related products deleted successfully", 
                        Toast.LENGTH_SHORT).show();
                } finally {
                    db.endTransaction();
                }
            } else {
                Toast.makeText(getContext(), "Category does not exist", Toast.LENGTH_SHORT).show();
                if (cursor != null) cursor.close();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error deleting category: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }

    private void viewAllCategories() {
        Cursor cursor = db.rawQuery("SELECT * FROM categories", null);
        if (cursor != null && cursor.moveToFirst()) {
            StringBuilder categoriesList = new StringBuilder();
            do {
                @SuppressLint("Range") String categoryName = cursor.getString(cursor.getColumnIndex("category_name"));
                categoriesList.append("Category: ").append(categoryName).append("\n\n");
            } while (cursor.moveToNext());

            TextView categoriesTextView = new TextView(getContext());
            categoriesTextView.setText(categoriesList.toString());
            categoriesTextView.setPadding(16, 16, 16, 16);

            ScrollView scrollView = new ScrollView(getContext());
            scrollView.addView(categoriesTextView);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("All Categories")
                    .setView(scrollView)
                    .setPositiveButton("Close", null)
                    .show();
        } else {
            Toast.makeText(getContext(), "No categories available", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (db != null) {
            db.close();
        }
    }
}
