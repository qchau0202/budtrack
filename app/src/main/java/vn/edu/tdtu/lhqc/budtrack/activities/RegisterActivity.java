package vn.edu.tdtu.lhqc.budtrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import vn.edu.tdtu.lhqc.budtrack.R;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vn.edu.tdtu.lhqc.budtrack.utils.ThemeManager.applySavedTheme(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Navigate to Login screen
        TextView signUpTextView = findViewById(R.id.tv_login);
        if (signUpTextView != null) {
            signUpTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}