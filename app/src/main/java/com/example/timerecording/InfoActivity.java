package com.example.timerecording;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;


public class InfoActivity extends AppCompatActivity {

private WebView _webContent;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Initialisierung der UI
        _webContent = findViewById(R.id.WebContent);
        }

@Override
protected void onStart() {
        super.onStart();

        // Google anzeigen
        _webContent.loadUrl("https://www.google.de");
        }
        }
