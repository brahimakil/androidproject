package com.example.ecommercecollegeproject3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecommercecollegeproject3.databinding.ActivityMainBinding;
import com.example.ecommercecollegeproject3.utils.AdminAuth;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWhatsApp();
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_products, R.id.nav_categories)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_products || id == R.id.nav_categories) {
                if (AdminAuth.isAdminAuthenticated()) {
                    navController.navigate(id);
                    drawer.closeDrawers();
                } else {
                    AdminAuth.checkAdminPassword(this, () -> {
                        navController.navigate(id);
                        drawer.closeDrawers();
                    });
                }
            } else {
                navController.navigate(id);
                drawer.closeDrawers();
            }
            return true;
        });
    
    }

    private void openWhatsApp() {
        String phoneNumber = "70049615";
        String message = "Hey, please I would like to contact the manager! ";

        String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        intent.setPackage("com.whatsapp");

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "WhatsApp is not installed or available on your device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdminAuth.logout(); // Reset authentication state when app is closed
    }
}
