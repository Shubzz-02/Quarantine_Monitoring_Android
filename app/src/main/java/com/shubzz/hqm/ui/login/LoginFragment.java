package com.shubzz.hqm.ui.login;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.shubzz.hqm.MainActivity;
import com.shubzz.hqm.R;
import com.shubzz.hqm.utils.SessionHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginFragment extends Fragment {

    private EditText mno, pass;
    private TextView forgot_pass;
    private Button btnSignIn;
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    public static final MediaType JSON = MediaType.parse("application/json");

    private ProgressDialog progressDialog;
    private AlertDialog.Builder builder;
    private Handler mhandler;


    private SessionHandler sessionHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login_fragment, container, false);
        sessionHandler = new SessionHandler(getActivity());
        mhandler = new Handler(Looper.getMainLooper());
        initView(v);
        return v;
    }

    private void initView(View v) {
        mno = v.findViewById(R.id.mno);
        pass = v.findViewById(R.id.pass);
        forgot_pass = v.findViewById(R.id.forgot_pass);
        btnSignIn = v.findViewById(R.id.btnSignIn);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");

        builder = new AlertDialog.Builder(getActivity());

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inmo = mno.getText().toString().trim();
                String inPassword = pass.getText().toString().trim();
                if (inmo.startsWith("+")) {
                    inmo = inmo.substring(3);
                } else if (inmo.startsWith("0")) {
                    inmo = inmo.substring(1);
                }
                if (validateData(inmo, inPassword)) {
                    try {
                        signuser(inmo, inPassword);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private boolean validateData(String mno, String pass) {
        if (mno.isEmpty()) {
            this.mno.setError("Mobile no field is empty.");
            this.mno.requestFocus();
            return false;
        }
        if (pass.isEmpty()) {
            this.pass.setError("Password is empty.");
            this.pass.requestFocus();
            return false;
        }
        if (pass.length() < 8) {
            this.pass.setError("Password must be of 8 character");
            this.pass.requestFocus();
            return true;
        }
        if (mno.length() != 10) {
            this.mno.setError("Wrong mobile no.");
            this.mno.requestFocus();
            return false;
        } else {
            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher matcher = pattern.matcher(mno);
            if (!matcher.matches()) {
                this.mno.setError("Please enter valid Mobile Number");
                this.mno.requestFocus();
                return false;
            } else {
                return true;
            }
        }
    }

    private void loadMainactivity() {
        Intent i = new Intent(getActivity(), MainActivity.class);
        i.putExtra("login", 1);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    //------------------------------------NETWORKING

    private void signuser(final String inmo, String inPassword) throws IOException {
        progressDialog.show();
        //https://medium.com/@appmattus/android-security-ssl-pinning-1db8acb6621e
//        CertificatePinner certPinner = new CertificatePinner.Builder()
//                .add("appmattus.com",
//                        "sha256/4hw5tz+scE+TW+mlai5YipDfFWn1dqvfLG+nU7tq1V8=")
//                .build();
//        OkHttpClient client = new OkHttpClient()
//                .newBuilder()
//                .certificatePinner(certPinner)
//                .build();

        OkHttpClient client = new OkHttpClient();
        JSONObject body = new JSONObject();
        try {
            body.put(KEY_USERNAME, inmo);
            body.put(KEY_PASSWORD, inPassword);
            Log.d("Login Request: ", body.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final RequestBody reqbody = RequestBody.Companion.create(body.toString(), JSON);
        Request request = new Request.Builder()
                .url(sessionHandler.getUrl())
                .post(reqbody)
                .addHeader("x-reaq", "e469b5d0")
                .build();
        //Log.d("login req: ", request.toString());
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
                Log.d("Response: ", response.toString());
                switch (response.code()) {
                    case 200:
                        success(response.body().string(), inmo);
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

    private void success(String response, String username) {
        Log.d("response : ", response);
        try {
            JSONObject resp = new JSONObject(response);
            if (resp.getInt(KEY_STATUS) == 0) {
                sessionHandler.loginUser(username, resp.getString(KEY_FULL_NAME), resp.getString("uq_key"), resp.getString("block"));
                loadMainactivity();
            } else {
                builder.setMessage("Wrong Username/Password").setCancelable(false)
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
        }
    }


}
