package com.yellowflash.drugsbazaar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yellowflash.helpers.AppConstants;
import com.yellowflash.helpers.AppHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView rvDistributors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //fetch data from database and show list of distributors
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_distributors:
                //Show Dialog to enter dbzid

                showAddDistributorDialog();
                break;

        }
        return true;
    }

    private void showAddDistributorDialog() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setTitle(AppConstants.ADDDBZTITLE);
        dialog.setContentView(R.layout.dialog_add_distributors);
        final EditText etDbzid = (EditText) dialog.findViewById(R.id.et_distributor_dbzid);
        Button btnSearch = (Button) dialog.findViewById(R.id.btn_search_distributor);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dbzId = etDbzid.getText().toString();
                if (dbzId.equals("") && dbzId.length() < 10) {
                    etDbzid.setHint(AppConstants.ERRORDBZID);
                    etDbzid.setHintTextColor(Color.RED);
                } else {
                    dialog.dismiss();

                }
            }
        });
        dialog.show();
    }

    private class SearchDbzId extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle(AppConstants.DIALOGTITLE);
            dialog.setMessage(AppConstants.SEARCHDBZIDMSG);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            try {
            } catch (NullPointerException e) {
                Toast.makeText(MainActivity.this, AppConstants.TRYAGAIN, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            Connection conn = null;
            try {
                String driver = "net.sourceforge.jtds.jdbc.Driver";
                Class.forName(driver).newInstance();

                String connString = "jdbc:jtds:sqlserver://117.218.1.164/myfins";
                String username = "dbz";
                String password = "Password1";
                conn = DriverManager.getConnection(connString, username, password);
                Log.w("Connection", "open");
                Statement stmt = conn.createStatement();
                ResultSet reset = stmt.executeQuery("select * from company where drgbzrid=" + params[0]);
                StringBuilder sb = new StringBuilder();
                ResultSetMetaData rsmd = reset.getMetaData();
                Log.i("Columns", String.valueOf(rsmd.getColumnCount()));
// create arraylist
                if (reset.getRow() > 0) {
                    while (reset.next()) {
                        //create Company object
                        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                            Log.i("Vaue", reset.getString(i));
                            sb.append(reset.getString(i)).append(",");
                            //set company data
                            // connect csv writer
                            // fill data in local database
                        }
                        //add company to arralist
                        sb.append("\n");
                    }
                }
                // insert arraylist to database
                Log.i("Result", sb.toString());
                conn.close();
                return null;
            } catch (Exception e) {
                Log.w("Error connection", "" + e.getMessage());
            }
            return null;
        }
    }
}
