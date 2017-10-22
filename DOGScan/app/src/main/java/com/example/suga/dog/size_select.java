package com.example.suga.dog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Button;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class size_select extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.size_select);

        ImageButton iButton2 = (ImageButton) findViewById(R.id.imageButton2);
        ImageButton iButton3 = (ImageButton) findViewById(R.id.imageButton3);

        iButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), recoding.class);
                startActivity(intent);
            }
        });
        iButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), recoding.class);
                startActivity(intent);
            }
        });
    }

}
