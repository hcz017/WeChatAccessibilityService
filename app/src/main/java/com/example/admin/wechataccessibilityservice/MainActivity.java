package com.example.admin.wechataccessibilityservice;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mSetAccessibilityButton;
    private static final String TAG = "michael";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSetAccessibilityButton = (Button)findViewById(R.id.set_accessibility_button);
        mSetAccessibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"You click the set button,start activity to settings....");
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);//进入setting的accessibility界面手动打开此功能
                startActivity(intent);
            }
        });
    }
}
