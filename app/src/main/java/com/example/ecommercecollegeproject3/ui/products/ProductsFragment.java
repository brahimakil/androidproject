package com.example.ecommercecollegeproject3.ui.products;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.ecommercecollegeproject3.R;
import com.example.ecommercecollegeproject3.databinding.FragmentProductsBinding;

import java.util.ArrayList;

public class ProductsFragment extends Fragment {

    private FragmentProductsBinding binding;
    private SQLiteDatabase db;
    private Spinner categorySpinner;
    private static final int IMAGE_PICK_REQUEST = 1001;
    private String selectedImagePath = "";
    private EditText searchEditText;
    private ListView searchResultsListView;
    private ArrayAdapter<String> searchAdapter;
    private ArrayList<String> searchResults;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProductsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = getActivity().openOrCreateDatabase("ecommerce.db", getActivity().MODE_PRIVATE, null);
        createTables();

        categorySpinner = binding.spinnerCategory;
        searchEditText = binding.etSearchProduct;
        searchResultsListView = binding.listSearchResults;
        searchResults = new ArrayList<>();
        searchAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, searchResults);
        searchResultsListView.setAdapter(searchAdapter);

        binding.btnInsertProduct.setOnClickListener(v -> insertProduct());
        binding.btnDeleteProduct.setOnClickListener(v -> deleteProduct());
        binding.btnViewAllProducts.setOnClickListener(v -> viewAllProducts());
        binding.btnSelectImage.setOnClickListener(v -> selectImage());
        binding.btnUpdateProduct.setOnClickListener(v -> updateProduct());

        fillCategoriesSpinner();

        setupSearch();

        return root;
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().trim();
                if (!searchText.isEmpty()) {
                    searchProducts(searchText);
                    searchResultsListView.setVisibility(View.VISIBLE);
                } else {
                    searchResults.clear();
                    searchAdapter.notifyDataSetChanged();
                    searchResultsListView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchResultsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedProduct = searchResults.get(position);
            fillFormWithProductData(selectedProduct);
            searchResultsListView.setVisibility(View.GONE);
            searchEditText.setText("");
        });
    }

    private void fillCategoriesSpinner() {
        Cursor cursor = db.rawQuery("SELECT * FROM categories", null);
        if (cursor != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            while (cursor.moveToNext()) {
                @SuppressLint("Range") String categoryName = cursor.getString(cursor.getColumnIndex("category_name"));
                adapter.add(categoryName);
            }

            categorySpinner.setAdapter(adapter);
            cursor.close();
        }
    }

    private void insertProduct() {
        try {
            String productName = binding.etProductName.getText().toString().trim();
            String productPriceStr = binding.etProductPrice.getText().toString().trim();
            String productQuantityStr = binding.etProductQuantity.getText().toString().trim();

            if (productName.isEmpty() || productPriceStr.isEmpty() || productQuantityStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            float productPrice = Float.parseFloat(productPriceStr);
            int productQuantity = Integer.parseInt(productQuantityStr);

            if (categorySpinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
                Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check for duplicate product
            Cursor cursor = db.rawQuery("SELECT * FROM products WHERE product_name = ?", 
                new String[]{productName});
            if (cursor != null && cursor.moveToFirst()) {
                Toast.makeText(getContext(), "Product already exists", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }

            // Get category ID
            cursor = db.rawQuery("SELECT id FROM categories WHERE category_name = ?",
                    new String[]{categorySpinner.getSelectedItem().toString()});
            cursor.moveToFirst();
            @SuppressLint("Range") int categoryId = cursor.getInt(cursor.getColumnIndex("id"));
            cursor.close();

            // Insert product
            String insertQuery = "INSERT INTO products (product_name, product_price, product_quantity, category_id, image_path) VALUES (?, ?, ?, ?, ?)";
            db.execSQL(insertQuery, new Object[]{productName, productPrice, productQuantity, categoryId, selectedImagePath});

            Toast.makeText(getContext(), "Product inserted successfully", Toast.LENGTH_SHORT).show();
            clearForm();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error inserting product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProduct() {
        try {
            String productName = binding.etProductName.getText().toString().trim();
            String productPriceStr = binding.etProductPrice.getText().toString().trim();
            String productQuantityStr = binding.etProductQuantity.getText().toString().trim();

            if (productName.isEmpty() || productPriceStr.isEmpty() || productQuantityStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            float productPrice = Float.parseFloat(productPriceStr);
            int productQuantity = Integer.parseInt(productQuantityStr);

            // Get category ID
            Cursor cursor = db.rawQuery("SELECT id FROM categories WHERE category_name = ?",
                    new String[]{categorySpinner.getSelectedItem().toString()});
            cursor.moveToFirst();
            @SuppressLint("Range") int categoryId = cursor.getInt(cursor.getColumnIndex("id"));
            cursor.close();

            // Update product
            String updateQuery = "UPDATE products SET product_price = ?, product_quantity = ?, category_id = ?, image_path = ? WHERE product_name = ?";
            db.execSQL(updateQuery, new Object[]{productPrice, productQuantity, categoryId, selectedImagePath, productName});

            Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
            clearForm();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error updating product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProduct() {
        try {
            String productName = binding.etProductName.getText().toString().trim();
            if (productName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a product name", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = db.rawQuery("SELECT * FROM products WHERE product_name = ?", 
                new String[]{productName});
            if (cursor != null && cursor.moveToFirst()) {
                db.execSQL("DELETE FROM products WHERE product_name = ?", new String[]{productName});
                Toast.makeText(getContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
                clearForm();
            } else {
                Toast.makeText(getContext(), "Product not found", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error deleting product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void searchProducts(String query) {
        searchResults.clear();
        try {
            Cursor cursor = db.rawQuery(
                "SELECT product_name FROM products WHERE product_name LIKE ?",
                new String[]{"%" + query + "%"}
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String productName = 
                        cursor.getString(cursor.getColumnIndex("product_name"));
                    searchResults.add(productName);
                } while (cursor.moveToNext());
                cursor.close();
            }
            searchAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error searching products", Toast.LENGTH_SHORT).show();
        }
    }

    private void fillFormWithProductData(String productName) {
        try {
            Cursor cursor = db.rawQuery(
                "SELECT p.*, c.category_name FROM products p " +
                "JOIN categories c ON p.category_id = c.id " +
                "WHERE p.product_name = ?",
                new String[]{productName}
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                binding.etProductName.setText(cursor.getString(cursor.getColumnIndex("product_name")));
                binding.etProductPrice.setText(String.valueOf(cursor.getFloat(cursor.getColumnIndex("product_price"))));
                binding.etProductQuantity.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex("product_quantity"))));
                
                String categoryName = cursor.getString(cursor.getColumnIndex("category_name"));
                selectedImagePath = cursor.getString(cursor.getColumnIndex("image_path"));
                
                // Set category in spinner
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) categorySpinner.getAdapter();
                int position = adapter.getPosition(categoryName);
                categorySpinner.setSelection(position);
                
                // Set image
                if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                    binding.imageProduct.setVisibility(View.VISIBLE);
                    binding.imageProduct.setImageURI(Uri.parse(selectedImagePath));
                } else {
                    binding.imageProduct.setVisibility(View.GONE);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading product data", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewAllProducts() {
        try {
            Cursor cursor = db.rawQuery(
                "SELECT p.*, c.category_name FROM products p " +
                "JOIN categories c ON p.category_id = c.id", null);
                
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder productsList = new StringBuilder();
                do {
                    String productName = cursor.getString(cursor.getColumnIndex("product_name"));
                    float productPrice = cursor.getFloat(cursor.getColumnIndex("product_price"));
                    int productQuantity = cursor.getInt(cursor.getColumnIndex("product_quantity"));
                    String categoryName = cursor.getString(cursor.getColumnIndex("category_name"));

                    productsList.append("Name: ").append(productName)
                            .append("\nPrice: $").append(String.format("%.2f", productPrice))
                            .append("\nQuantity: ").append(productQuantity)
                            .append("\nCategory: ").append(categoryName)
                            .append("\n\n");
                } while (cursor.moveToNext());

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("All Products")
                        .setMessage(productsList.toString())
                        .setPositiveButton("Close", null)
                        .show();
                cursor.close();
            } else {
                Toast.makeText(getContext(), "No products found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error viewing products", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, IMAGE_PICK_REQUEST);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            selectedImagePath = getPathFromURI(selectedImageUri);
            binding.imageProduct.setImageURI(selectedImageUri);
            binding.imageProduct.setVisibility(View.VISIBLE);
        }
    }

    private String getPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }

    private void clearForm() {
        binding.etProductName.setText("");
        binding.etProductPrice.setText("");
        binding.etProductQuantity.setText("");
        binding.imageProduct.setVisibility(View.GONE);
        selectedImagePath = "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (db != null) {
            db.close();
        }
    }

    private void createTables() {
        String createCategoriesTable = "CREATE TABLE IF NOT EXISTS categories ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "category_name TEXT NOT NULL)";
        db.execSQL(createCategoriesTable);

        String createProductsTable = "CREATE TABLE IF NOT EXISTS products ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "product_name TEXT NOT NULL, "
                + "product_price REAL NOT NULL, "
                + "product_quantity INTEGER NOT NULL, "
                + "category_id INTEGER NOT NULL, "
                + "image_path TEXT, "
                + "FOREIGN KEY (category_id) REFERENCES categories(id))";
        db.execSQL(createProductsTable);
    }
}
