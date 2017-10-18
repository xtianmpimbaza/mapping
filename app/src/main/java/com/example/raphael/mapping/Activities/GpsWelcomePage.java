package com.example.raphael.mapping.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.raphael.mapping.R;

public class GpsWelcomePage extends AppCompatActivity {
    public Button button;
    public TextView Text1, Text2, Text3, Text4, Text5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_welcome_page);

        Text1 = (TextView) findViewById(R.id.text1);
        Text2 = (TextView) findViewById(R.id.text2);
        Text3 = (TextView) findViewById(R.id.text3);
        Text4 = (TextView) findViewById(R.id.text4);
        Text5 = (TextView) findViewById(R.id.text5);
        button = (Button) findViewById(R.id.gps);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlertDialog();
            }
        });

    }

    public void createAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GpsWelcomePage.this);

//        AlertDialog.Builder alertDialog = new AlertDialog.Builder((new ContextThemeWrapper(this, R.style.AlertDialogCustom)));

        // Setting Dialog Title
        alertDialog.setTitle("This service is paid for");

        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want continue?");
        // Setting Icon to Dialog
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent l = new Intent(GpsWelcomePage.this, MainActivity.class);
                startActivity(l);
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}
