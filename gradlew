package com.shubzz.hqm.ui.signup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.shubzz.hqm.R;
import com.shubzz.hqm.utils.SessionHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpfragment extends Fragment {
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_BLOCK = "block";
    private static final String KEY_EMPTY = "";
    public static final MediaType JSON = MediaType.parse("application/json");

    private SessionHandler sessionHandler;
    private EditText f_name, l_name, smno, spass, cpass;
    private Button btnregister;
    private Spinner spinner;

    ProgressDialog progressDialog;
    AlertDialog.Builder builder;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.signup_fragment, container, false);
        sessionHandler = new SessionHandler(getActivity());
        initView(v);
        return v;
    }

    private void initView(View v) {
        f_name = v.findViewById(R.id.f_name);
        l_name = v.findViewById(R.id.l_name);
        smno = v.findViewById(R.id.smno);
        spass = v.findViewById(R.id.spass);
        cpass = v.findViewById(R.id.cpass);
        btnregister = v.findViewById(R.id.register);
        String[] values =
                {"Select your Block", "Agustyamuni", "Jakholi", "Ukhimath"};
        spinner = v.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), R.layout.simple_spinner_item, values) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
        final String[] block = new String[1];
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (position == 0) {
                    block[0] = "";
                } else {
                    block[0] = parent.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");

        builder = new AlertDialog.Builder(getActivity());


        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fname = f_name.getText().toString().trim();
                String lname = l_name.getText().toString().trim();
                String fullnm = fname + " " + lname;
                String pno = smno.getText().toString().trim();
                if (pno.startsWith("+")) {
                    pno = pno.substring(3);
                } else if (pno.startsWith("0")) {
                    pno = pno.substring(1);
                }
                String password = spass.getText().toString().trim();
                String cpassword = cpass.getText().toString().trim();

                if (validateInput(fname