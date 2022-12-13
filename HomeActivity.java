package com.buttondemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    String number;
    int n;
    LinearLayout dynamicview;
    LinearLayout.LayoutParams lprams;
    GridLayout gridLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dynamicview = (LinearLayout) findViewById(R.id.dynamicview);
        gridLayout = new GridLayout(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        gridLayout.setLayoutParams(layoutParams);
        gridLayout.setColumnCount(2);

        lprams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        Intent i = getIntent();
        Bundle extras = i.getExtras();

     //Extracting the stored data from the bundle
        //     String user_name = extras.getString("USER_NAME");

        if (extras != null) {
            number = extras.getString("number");
            Log.d("mytag", "n" + number);
            n = Integer.valueOf(number);
        }
        onclick(n);
    }

    public void onclick(final int n){
        final int b = n - 1;
        dynamicview.removeAllViews();
        for (int i = 0; i < n; i++){
            if (i == 0){
                Button btn = new Button(this);
                btn.setId(i + 1);
                btn.setText("Button" + (i + 1));
                btn.setLayoutParams(lprams);
                btn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                final int index = i;
                btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v){
                        onclick(b);
                        Log.i("TAG", "The index is" + index);
                    }
                });
                dynamicview.addView(btn);

            } else {

              Button btn = new Button(this);
                btn.setId(i + 1);
                btn.setText("Button" + (i + 1));
                btn.setLayoutParams(lprams);
                btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                final int index = i;
                btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.i("TAG", "The index is" + index);
                    }
                });
               dynamicview.addView(btn);

               // }
            }
        }
    }

    public static void onclickAdd(int n){

        for(int i=0;i<=n;i++){
            String online_text="";

        }
    }
}
