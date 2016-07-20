/*
 * Copyright (c) 2016. Self Training Systems, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by TienNguyen <tien.workinfo@gmail.com - tien.workinfo@icloud.com>, October 2015
 */

package com.training.tiennguyen.booklistingproject.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.training.tiennguyen.booklistingproject.R;
import com.training.tiennguyen.booklistingproject.adapters.BookAdapter;
import com.training.tiennguyen.booklistingproject.constants.VariableConstants;
import com.training.tiennguyen.booklistingproject.models.Book;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * MainActivity
 *
 * @author TienNguyen
 */
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.edtSearch)
    protected EditText edtSearch;
    @BindView(R.id.btnSearch)
    protected Button btnSearch;
    @BindView(R.id.lvBook)
    protected ListView lvBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        initView();
    }

    /**
     * initView
     */
    private void initView() {
        ButterKnife.bind(this);

        // Check connection
        if (verifyInternetConnection()) {
            // Show the content
            edtSearch.setText(VariableConstants.EMPTY_STRING_PROVIDED);
            edtSearch.setEnabled(true);
            lvBook.setVisibility(View.VISIBLE);
            btnSearch.setVisibility(View.VISIBLE);
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DownloadWebpageTask().execute(edtSearch.getText().toString());
                }
            });
        } else {
            // Load the message diagram
            edtSearch.setText(this.getString(R.string.error_no_connection));
            edtSearch.setEnabled(false);
            lvBook.setVisibility(View.GONE);
            btnSearch.setVisibility(View.GONE);
        }
    }

    /**
     * verifyInternetConnection
     *
     * @return boolean
     */
    public boolean verifyInternetConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Given a URL, establishes an HttpUrlConnection and retrieves
     * the web page content as a InputStream, which it returns as  a string.
     *
     * @param myurl String
     * @return String
     * @throws IOException
     */
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(VariableConstants.URL_API + myurl.toLowerCase() + VariableConstants.URL_API_MAX_RESULTS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            if (response == 200) {
                is = conn.getInputStream();
                return readIt(is);
            } else {
                return VariableConstants.EMPTY_STRING_PROVIDED;
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Reads an InputStream and converts it to a String.
     *
     * @param stream InputStream
     * @return String
     * @throws IOException
     */
    public String readIt(InputStream stream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }

        return total.toString();
    }

    /**
     * Uses AsyncTask to create a task away from the main UI thread. This task takes a
     * URL string and uses it to create an HttpUrlConnection. Once the connection
     * has been established, the AsyncTask downloads the contents of the webpage as
     * an InputStream. Finally, the InputStream is converted into a string, which is
     * displayed in the UI by the AsyncTask's onPostExecute method.
     */
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return getApplicationContext().getString(R.string.error_unable_retrieve);
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                if (result.isEmpty()) {
                    lvBook.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), getApplication().getString(R.string.message_no_value), Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject reader = new JSONObject(result);
                    if (reader.length() > 0) {
                        // Converting
                        final List<Book> books = new ArrayList<>();
                        JSONArray items = reader.optJSONArray("items");
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            String id = item.optString("id");
                            String link = item.optJSONObject("volumeInfo").optString("infoLink");
                            String image = item.optJSONObject("volumeInfo").optJSONObject("imageLinks").optString("smallThumbnail");
                            String title = item.optJSONObject("volumeInfo").optString("title");
                            String description = item.optJSONObject("volumeInfo").optString("description");

                            Book book = new Book(id, link, image, title, description);
                            books.add(book);
                        }

                        // Populating
                        if (!books.isEmpty()) {
                            BookAdapter adapter = new BookAdapter(getApplicationContext(), R.layout.book_list_item, books);
                            lvBook.setVisibility(View.VISIBLE);
                            lvBook.setAdapter(adapter);
                            lvBook.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    String url = books.get(i).getLink();
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(url));
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
