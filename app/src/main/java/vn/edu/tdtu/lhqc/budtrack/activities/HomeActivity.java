package vn.edu.tdtu.lhqc.budtrack.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import vn.edu.tdtu.lhqc.budtrack.R;

public class HomeActivity extends AppCompatActivity {

    private View navHome;
    private View navWallet;
    private View navDashboard;
    private View navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Initialize navigation views
        navHome = findViewById(R.id.nav_home);
        navWallet = findViewById(R.id.nav_wallet);
        navDashboard = findViewById(R.id.nav_dashboard);
        navProfile = findViewById(R.id.nav_profile);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);

        // Set Home as active by default
        setNavSelected(R.id.nav_home);

        // Set up click listeners
        navHome.setOnClickListener(v -> setNavSelected(R.id.nav_home));
        navWallet.setOnClickListener(v -> setNavSelected(R.id.nav_wallet));
        navDashboard.setOnClickListener(v -> setNavSelected(R.id.nav_dashboard));
        navProfile.setOnClickListener(v -> setNavSelected(R.id.nav_profile));
        fabAdd.setOnClickListener(v -> {
            // Handle FAB click - add new item
            // TODO: Implement add functionality
        });
    }

    private void setNavSelected(int navId) {
        // Deselect all
        navHome.setSelected(false);
        navWallet.setSelected(false);
        navDashboard.setSelected(false);
        navProfile.setSelected(false);

        // Select the clicked one
        View selectedView = findViewById(navId);
        if (selectedView != null) {
            selectedView.setSelected(true);
        }

        // TODO: Handle navigation to different fragments/activities based on navId
    }
}