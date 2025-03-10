package com.example.googlemaptest.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.googlemaptest.R;

public class AdActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        Button closeButton = findViewById(R.id.btn_close);
        closeButton.setOnClickListener(view -> {
            decreaseMarkerCreateCount();
            finish();
        });
    }

    private void decreaseMarkerCreateCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int markerCreateCount = sharedPreferences.getInt("markerCreateCount", 0);

        if (markerCreateCount > 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("markerCreateCount", markerCreateCount - 1);
            editor.apply();
            Log.d("AdActivity", "Decreased marker create count: " + markerCreateCount);
        }
    }
}