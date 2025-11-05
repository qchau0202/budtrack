package vn.edu.tdtu.lhqc.budtrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import vn.edu.tdtu.lhqc.budtrack.R;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up Get Started button click listener
        Button btnGetStarted = findViewById(R.id.btn_get_started);
        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> {
                 Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
                 startActivity(intent);
            });
        }
    }
}

