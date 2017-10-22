package com.example.suga.dog;

import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton iButton1 = (ImageButton) findViewById(R.id.iButton1);
        iButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), size_select.class);
                startActivity(intent);
            }
        });
    }
}
