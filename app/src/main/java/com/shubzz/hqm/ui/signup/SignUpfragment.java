package com.shubzz.hqm.ui.signup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    public static final MediaType JSON = MediaType.parse("application/json");
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_BLOCK = "block";
    private static final String KEY_EMPTY = "";
    ProgressDialog progressDialog;
    AlertDialog.Builder builder;
    private SessionHandler sessionHandler;
    private EditText f_name, l_name, smno, spass, cpass;
    private Button btnregister;
    private Spinner spinner;
    private Handler mhandler;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.signup_fragment, container, false);
        sessionHandler = new SessionHandler(getActivity());
        mhandler = new Handler(Looper.getMainLooper());
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
                return position != 0;
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
        progressDialog.setMessage("Signing Up...");

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

                if (validateInput(fname, lname, pno, password, cpassword, block[0])) {
                    registerUser(fullnm, pno, password, block[0]);
                }
            }
        });
    }


    private boolean validateInput(String fname, String lname, String pno, String password, String cpassword, String block) {
        if (fname.isEmpty()) {
            this.f_name.setError("First name is empty");
            this.f_name.requestFocus();
            return false;
        }
        if (lname.isEmpty()) {
            this.l_name.setError("Last name is empty");
            this.l_name.requestFocus();
            return false;
        }
        if (pno.isEmpty()) {
            this.smno.setError("Mobile no field is empty.");
            this.smno.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            spass.setError("Password is empty.");
            this.spass.requestFocus();
            return false;
        }
        if (cpassword.isEmpty()) {
            cpass.setError("Password is empty.");
            this.cpass.requestFocus();
            return false;
        }
        if (password.length() < 8) {
            this.cpass.setError("Password must be of 8 character");
            this.cpass.requestFocus();
            return true;
        }
        if (!password.equals(cpassword)) {
            cpass.setError("Password does not match.");
            this.cpass.requestFocus();
            return false;
        }
        if (block.isEmpty()) {
            ((TextView) spinner.getSelectedView()).setError("Please select a valid block");
            spinner.requestFocus();
            return false;
        }
        return true;
    }

    //-----------------------------------NETWORKING

    private void registerUser(String fullname, String pno, String password, String block) {
        progressDialog.show();

        OkHttpClient client = new OkHttpClient();
        JSONObject body = new JSONObject();
        try {
            body.put(KEY_USERNAME, pno);
            body.put(KEY_PASSWORD, password);
            body.put(KEY_FULL_NAME, fullname);
            body.put(KEY_BLOCK, block);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final RequestBody reqbody = RequestBody.Companion.create(body.toString(), JSON);
        Request request = new Request.Builder()
                .url(sessionHandler.getUrl())
                .post(reqbody)
                .addHeader("x-reaq", "a34c9f37")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                progressDialog.dismiss();
                call.cancel();
                builder.setMessage("Please Check you internet connection.").setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
                Log.d("Login Error: ", e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                progressDialog.dismiss();
                switch (response.code()) {
                    case 200:
                        success(response.body().string());
                        break;
                    case 500:
                        builder.setMessage("Some error Occured.").setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                        mhandler.post(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        });
                        break;
                    case 400:
                        builder.setMessage("Pleasae Contact Admin").setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                        mhandler.post(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        });
                        break;
                }
            }
        });
    }

    private void success(String response) {
        try {
            JSONObject resp = new JSONObject(response);
            if (resp.getInt(KEY_STATUS) == 0) {
                builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Successfully Registered Please Login").setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
            } else if (resp.getInt(KEY_STATUS) == 1) {
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        smno.setError("mobile no already in use!");
                        smno.requestFocus();
                    }
                });
            } else if (resp.getInt(KEY_STATUS) == 4) {
                builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Please Input Detail as Specified").setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
            } else {
                builder.setMessage("Some Error Occcured Please tyr Again Later or Contact Admin ").setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
            }
        } catch (JSONException e) {
            builder.setMessage("Some Error Occcured Please tyr Again Later or Contact Admin").setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            mhandler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }
    }
}
