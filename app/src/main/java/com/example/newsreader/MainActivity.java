package com.example.newsreader;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.NetworkStats;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> newsHeadLines = new ArrayList<>();
    static ArrayList<String> newsUrls = new ArrayList<>();
    static ArrayAdapter<String> listViewAdapter;
    ListView listView;
    static SQLiteDatabase sqLiteDatabase;
    static boolean isNetworkConnected = false;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);

        try {
            sqLiteDatabase = this.openOrCreateDatabase("news", MODE_PRIVATE, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

            if (newsHeadLines.isEmpty()) {
                RetriveNewsData retriveNewsData = new RetriveNewsData(this);
                try {
                    
                    /*Get your API key from here https://newsapi.org/ 
                    because all of this codes has been written acording to the thier API format
                    */
                   // String l = retriveNewsData.execute("Your api key here").get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    updateNews();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    updateNews();
                }
                Log.i("FROM NETWORK CONNECTED", "TRUE");
            }


        listView = (ListView) findViewById(R.id.listView);
        listViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, newsHeadLines);
        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), NewsWebView.class);
                intent.putExtra("url", newsUrls.get(position));
                startActivity(intent);
            }
        });

        //updateNews();

    }


    public void updateNews() {

        if (newsHeadLines.isEmpty()) {
            try {
                Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM news", null);

                int titleIndex = cursor.getColumnIndex("title");
                int urlIndex = cursor.getColumnIndex("url");
                cursor.moveToFirst();

                while (cursor != null) {
                    Log.i("TITLE  ::: ", cursor.getString(titleIndex));
                    Log.i("URL  ::: ", cursor.getString(urlIndex));

                    newsHeadLines.add(cursor.getString(titleIndex));
                    newsUrls.add(cursor.getString(urlIndex));
                    cursor.moveToNext();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }




    }

    @Override
    protected void onStart() {
        super.onStart();


    }
}
