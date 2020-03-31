package com.example.newsreader;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class RetriveNewsData extends AsyncTask<String, Integer, String> {

   private WeakReference<MainActivity> activityWeakReference;
   MainActivity activity;

    public RetriveNewsData(MainActivity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.activity = activityWeakReference.get();
    }

    @Override
    protected String doInBackground(String... urls) {
        String contentData = "";
        try {

            URL url = new URL(urls[0]);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            int data = reader.read();

            while (data != -1) {
                char currentCharacter = (char) data;
                contentData += currentCharacter;
                data = reader.read();
            }

            Log.i("NEWS CONTENT", contentData);

            return contentData;

        } catch (Exception e) {
            e.printStackTrace();

            Log.i("Connection ERROR", "ERROR HAPPENED");
            SQLiteDatabase sqLiteDatabase = MainActivity.sqLiteDatabase;
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS news (id Integer PRIMARY KEY, title VARCHAR, url VARCHAR)");

            ArrayList<String> headLines = new ArrayList<>();
            ArrayList<String> newsUrl = new ArrayList<>();
            try {
                Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM news", null);

                int titleIndex = cursor.getColumnIndex("title");
                int urlIndex = cursor.getColumnIndex("url");
                cursor.moveToFirst();

                while (cursor != null) {
                    Log.i("FROM BACK TITLE  ::: ", cursor.getString(titleIndex));
                    Log.i("URL  ::: ", cursor.getString(urlIndex));
                    headLines.add(cursor.getString(titleIndex));
                    newsUrl.add(cursor.getString(urlIndex));
                    cursor.moveToNext();
                }


            } catch (Exception error) {
                error.printStackTrace(); Log.i("HEADLINES ::", headLines.toString());

                    MainActivity.newsHeadLines = headLines;
                    MainActivity.newsUrls = newsUrl;

            }
            return null;
        }

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (activity == null || activity.isFinishing()) {
            return;
        }
        activity.progressBar.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (s != null) {
            SQLiteDatabase sqLiteDatabase = MainActivity.sqLiteDatabase;
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS news (id Integer PRIMARY KEY, title VARCHAR, url VARCHAR)");

            sqLiteDatabase.execSQL("DELETE FROM news");

            try {
                JSONObject jsonObject = new JSONObject(s);
                Log.i("SOURCE", jsonObject.getString("articles").toString());
                JSONArray jsonArray = new JSONArray(jsonObject.getString("articles"));
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    Log.i("Title: ", jsonObject1.getString("title").toString());
                    Log.i("URL: ", jsonObject1.getString("url").toString());

                    String sql = "INSERT INTO news (title, url) values (?,?)";
                    SQLiteStatement statement = sqLiteDatabase.compileStatement(sql);
                    statement.bindString(1, jsonObject1.getString("title"));
                    statement.bindString(2, jsonObject1.getString("url"));
                    statement.execute();

                    MainActivity.newsHeadLines.add(jsonObject1.getString("title"));
                    MainActivity.newsUrls.add(jsonObject1.getString("url"));
                    MainActivity.listViewAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        activity.progressBar.setVisibility(View.GONE);
    }

}

