package com.yellowflash.drugsbazaar;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.opencsv.CSVWriter;
import com.yellowflash.helpers.AppConstants;
import com.yellowflash.helpers.AppHelper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;

public class RegistrationActivity extends AppCompatActivity {
    EditText etName, etAddr, etTown, etDlNum, etTinNum, etPhoneNum;
    Spinner spnType;
    Button btnRegister;
    String type[] = {"Retailer", "Whole Seller", "Customer"};
    String typeValues[] = {"2", "1", "4"};
    String name, address, town, dlNum, tinNum, phnNum, selectedType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        instantiateWidgets();
        selectedType = typeValues[0];
        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = typeValues[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = etName.getText().toString();
                address = etAddr.getText().toString();
                town = etTown.getText().toString();
                dlNum = etDlNum.getText().toString();
                tinNum = etTinNum.getText().toString();
                phnNum = etPhoneNum.getText().toString();
                if (name.equals("")) {
                    etName.setError(getString(R.string.hint_name));
                } else if (address.equals("")) {
                    etAddr.setError(getString(R.string.hint_address));
                } else if (town.equals("")) {
                    etTown.setError(getString(R.string.hint_town));
                } else if (dlNum.equals("")) {
                    etDlNum.setError(getString(R.string.hint_dl_num));
                } else if (tinNum.equals("")) {
                    etTinNum.setError(getString(R.string.hint_tin_num));
                } else if (phnNum.equals("")) {
                    etPhoneNum.setError(getString(R.string.hint_phne));
                } else {
                    if (AppHelper.isConnectingToInternet(RegistrationActivity.this)) {
                        new Register().execute();

                    }
                }
            }
        });
    }

    private void instantiateWidgets() {
        etAddr = (EditText) findViewById(R.id.et_reg_address);
        etName = (EditText) findViewById(R.id.et_reg_name);
        etPhoneNum = (EditText) findViewById(R.id.et_reg_phone);
        etDlNum = (EditText) findViewById(R.id.et_reg_dl_number);
        etTinNum = (EditText) findViewById(R.id.et_reg_tin_number);
        etTown = (EditText) findViewById(R.id.et_reg_town);
        btnRegister = (Button) findViewById(R.id.btn_reg_complete);
        spnType = (Spinner) findViewById(R.id.spn_reg_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RegistrationActivity.this,
                android.R.layout.simple_spinner_item, type);
        spnType.setAdapter(adapter);
    }

    private String createCsvFile(List<String[]> list) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File csvFileDir = new File(path + "/DolphinApp/", "Files");
        csvFileDir.mkdir();
        String csvFilePath = csvFileDir.getAbsolutePath() + "/collection.csv";
        File file = new File(csvFilePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath));
            writer.writeAll(list);
            writer.close();
            return csvFilePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class Register extends AsyncTask<Void, Void, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(RegistrationActivity.this);
            dialog.setTitle(AppConstants.DIALOGTITLE);
            dialog.setMessage(AppConstants.REGISTERUSERMSG);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            dialog.dismiss();
            Log.i("response", s);
            new CheckUserSatus().execute(name, town);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                StringBuilder requestBuilder = new StringBuilder();
                requestBuilder.append("compid,name,add1,add2,town,area,type,dlno1,dlno2,dlno3,tin,srin,mobile,mfg");
                requestBuilder.append("\n");
                requestBuilder.append("0").append(",'")
                        .append(name).append("','")
                        .append(address).append("','")
                        .append("").append("','")
                        .append(town).append("','")
                        .append("").append("','")
                        .append(selectedType).append("','")
                        .append(dlNum).append("','")
                        .append("").append("','")
                        .append("").append("','")
                        .append(tinNum).append("','")
                        .append("").append("','")
                        .append(phnNum).append("','")
                        .append("'");
                Log.i("Data", requestBuilder.toString());
                byte[] bytes = requestBuilder.toString().getBytes();
                ByteArrayBody bab = new ByteArrayBody(bytes, "company.csv");
                String postReceiverUrl = "http://117.218.1.164/dbz/compreg.aspx";
                HttpClient httpclient = new DefaultHttpClient();

                HttpPost httppost = new HttpPost(postReceiverUrl);
                MultipartEntity reqEntity = new MultipartEntity(
                        HttpMultipartMode.BROWSER_COMPATIBLE);
                reqEntity.addPart("uploaded", bab);
                httppost.setEntity(reqEntity);
                HttpResponse response = null;
                response = httpclient.execute(httppost);
                return EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class CheckUserSatus extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(RegistrationActivity.this);
            dialog.setTitle(AppConstants.DIALOGTITLE);
            dialog.setMessage(AppConstants.CHECKUSERDETAILS);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            dialog.dismiss();
            Log.i("response", s);
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
                ResultSet reset = stmt.executeQuery("select drgbzrid,compid from company where compname='" + params[0] + "' and comptown='" + params[1] + "'");
                StringBuilder sb = new StringBuilder();
                ResultSetMetaData rsmd = reset.getMetaData();
                Log.i("Columns", String.valueOf(reset.getRow()));
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
                return sb.toString();
            } catch (Exception e) {
                Log.w("Error connection", "" + e.getMessage());
            }
            return null;
        }
    }
}
