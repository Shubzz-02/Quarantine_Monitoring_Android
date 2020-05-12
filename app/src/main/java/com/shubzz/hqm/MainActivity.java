package com.shubzz.hqm;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shubzz.hqm.adapter.Vill;
import com.shubzz.hqm.adapter.VillAdapter;
import com.shubzz.hqm.dialog.MyDialogFragment;
import com.shubzz.hqm.utils.SessionHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements VillAdapter.VillAdapterListener,
        MyDialogFragment.DialogListener {

    private static final MediaType JSON = MediaType.parse("application/json");
    private TextView h1, h2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private SessionHandler sessionHandler;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private List<Vill> villList;
    private RecyclerView recyclerView;
    private VillAdapter mAdapter;
    private SearchView searchView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static final String KEY_uq = "uq_key";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_STATUS = "status";
    private static final String KEY_VNAME = "vill_name";

    MyDialogFragment dialog;
    ProgressDialog progressDialog;
    AlertDialog.Builder builder;
    private Handler mhandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionHandler = new SessionHandler(this);
        mhandler = new Handler(Looper.getMainLooper());
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        builder = new AlertDialog.Builder(this);
        setContentView(R.layout.activity_main);

        initUI();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new MyDialogFragment();
                FragmentTransaction ft;
                ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                dialog.show(ft, "dialog");
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            verifyUser();
        }


        if (!file(sessionHandler.getBlock() + ".json")) {
            fetchDataSync();
        } else {
            updatData();
        }
    }

    private void initUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        h1 = findViewById(R.id.h1);
        final View titleLayout = findViewById(R.id.layout_title);
        // h1 = titleLayout.findViewById(R.id.h1);
        h2 = titleLayout.findViewById(R.id.h2);
        titleLayout.post(new Runnable() {
            @Override
            public void run() {
                CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
                layoutParams.height = titleLayout.getHeight();
                toolbar.setLayoutParams(layoutParams);
            }
        });


        fab = findViewById(R.id.fab);

        String name = sessionHandler.getName().trim();
        h1.setText("Hello ");
        h1.append(name.substring(0, name.indexOf(' ')));
        h1.append("!");
        h2.setText("Block :");
        h2.append(sessionHandler.getBlock());
        setTitle(null);
        villList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new VillAdapter(this, villList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = findViewById(R.id.swipeContainer);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchDataSync();
                //mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView = findViewById(R.id.search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private boolean file(String s) {
        try {
            String filePath = getApplicationContext().getFilesDir() + "/" + s;
            File myFile = new File(filePath);
            if (myFile.exists()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void updatData() {
        villList.clear();
        String b = sessionHandler.getBlock();
        StringBuilder temp = new StringBuilder();
        try {
            FileInputStream fin = openFileInput(b + ".json");
            int c;
            while ((c = fin.read()) != -1) {
                temp = temp.append(Character.toString((char) c));
            }
            Log.d("File cont", temp.toString());
            fin.close();
            JSONArray jsonArray = new JSONArray(temp.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.getString("vill_name");
                String tot = obj.getString("tot_person");
                Vill vill = new Vill(name, tot);
                villList.add(vill);
            }
            mAdapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }

        if (sessionHandler.getVill_name().equals("")) {
            selectVill();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_search) {
//            return true;
//        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void onVillSelected(Vill vill) {
//        dialogFrag = MyFabFragment.newInstance();
//        dialogFrag.setParentFab(fab);
        if (!vill.getName().isEmpty() && vill.getName().equalsIgnoreCase(sessionHandler.getVill_name())) {
            Intent i = new Intent(getApplicationContext(), PersonActivity.class);
             startActivity(i);
        } else {
            builder.setMessage("You don't have permisssion to View this Village data").setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        //Toast.makeText(getApplicationContext(), vill.getName() + "is clicked", Toast.LENGTH_LONG).show();

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void logout() {
        try {
            String file = sessionHandler.getBlock() + ".json";
            getApplicationContext().deleteFile(file);
            file = sessionHandler.getVill_name() + ".json";
            getApplicationContext().deleteFile(file);
        } catch (Exception e) {
        }
        sessionHandler.logoutUser();
        Intent i = new Intent(getApplicationContext(), LSActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }


    @Override
    public void onFinishEditDialog(String inputText) {
        if (TextUtils.isEmpty(inputText)) {
            Log.d("Return ", "txt not entered");
        } else {
            Log.d("Return ", "txt entered " + inputText);
            addVill(inputText);
        }
    }


    //-----------------------NETWORKING

    private void fetchDataSync() {
        OkHttpClient client = new OkHttpClient();
        JSONObject body = new JSONObject();
        String username = sessionHandler.getKeyUsername();
        String key = sessionHandler.getSecKey();
        try {
            body.put(KEY_USERNAME, username);
            body.put(KEY_uq, key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final RequestBody reqbody = RequestBody.Companion.create(body.toString(), JSON);
        Request request = new Request.Builder()
                .url(sessionHandler.getUrl())
                .post(reqbody)
                .addHeader("x-reaq", "799182d8")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                mSwipeRefreshLayout.setRefreshing(false);
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
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                mSwipeRefreshLayout.setRefreshing(false);
                switch (response.code()) {
                    case 200:
                        getData(response.body().string());
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

    private void getData(@NotNull String res) {
        if (res.length() > 0) {
            String b = sessionHandler.getBlock();
            FileOutputStream fout = null;
            try {
                JSONArray arr = new JSONArray(res);
                fout = openFileOutput(b + ".json", Context.MODE_PRIVATE);
                fout.write(res.getBytes());
                fout.close();
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updatData();
                    }
                });
                //
            } catch (Exception e) {
                builder.setMessage("Something went wrong in refresh").setCancelable(false)
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

    //-------------verify
    private void verifyUser() {
        String name;
        String k;
        progressDialog.setMessage("Verifying Please Wait...");
        progressDialog.show();
        OkHttpClient client = new OkHttpClient();
        JSONObject body = new JSONObject();
        try {
            name = sessionHandler.getKeyUsername();
            k = sessionHandler.getSecKey();
            body.put(KEY_USERNAME, name);
            body.put(KEY_uq, k);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final RequestBody reqbody = RequestBody.Companion.create(body.toString(), JSON);
        Request request = new Request.Builder()
                .url(sessionHandler.getUrl())
                .post(reqbody)
                .addHeader("x-reaq", "2acb85cf")
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
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                progressDialog.dismiss();
                switch (response.code()) {
                    case 200:
                        doVerification(response.body().string());
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

    private void doVerification(String response) {
        try {
            JSONObject resp = new JSONObject(response);
            if (resp.getInt(KEY_STATUS) == 0) {

            } else {
                builder.setMessage("Verification Failed Please Login again!").setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logout();
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

    //----------Add Vill
    private void addVill(String inputVill) {
        progressDialog.setMessage("Adding Village...");
        progressDialog.show();

        JSONObject body = new JSONObject();
        String username = sessionHandler.getKeyUsername();
        String key = sessionHandler.getSecKey();

        try {
            username = sessionHandler.getKeyUsername();
            key = sessionHandler.getSecKey();
            body.put(KEY_USERNAME, username);
            body.put(KEY_VNAME, inputVill);
            body.put(KEY_uq, key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient();
        final RequestBody reqbody = RequestBody.Companion.create(body.toString(), JSON);
        Request request = new Request.Builder()
                .url(sessionHandler.getUrl())
                .post(reqbody)
                .addHeader("x-reaq", "a8737537")
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
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                progressDialog.dismiss();
                switch (response.code()) {
                    case 200:
                        checkIfAdded(response.body().string());

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

    private void checkIfAdded(String resp) {
        Log.d("resp :",resp);
        try {
            JSONObject response = new JSONObject(resp);
            if (response.getInt(KEY_STATUS) == 0) {
                builder.setMessage("Village Added").setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fetchDataSync();
                            }
                        });
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
            } else if (response.getInt(KEY_STATUS) == 9) {
                builder.setMessage("You have no permission to add new Village. If needed Contact Admin.").setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fetchDataSync();
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
        } catch (Exception e) {
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

    //----Select vill
    private void selectVill() {
        OkHttpClient client = new OkHttpClient();
        JSONObject body = new JSONObject();
        try {
            String username = sessionHandler.getKeyUsername();
            String key = sessionHandler.getSecKey();
            body.put(KEY_USERNAME, username);
            body.put(KEY_uq, key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final RequestBody reqbody = RequestBody.Companion.create(body.toString(), JSON);
        Request request = new Request.Builder()
                .url(sessionHandler.getUrl())
                .post(reqbody)
                .addHeader("x-reaq", "800598b7")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
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
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                switch (response.code()) {
                    case 200:
                        villSelect(response.body().string());
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

    private void villSelect(String resp) {
        try {
            JSONObject response = new JSONObject(resp);
            Log.d("Resp :",resp);
            if (response.getInt(KEY_STATUS) == 0) {
                sessionHandler.setVill_name(response.getString("vill_name"));
            } else {
                Log.d("vill response 2 ", response.toString());
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "No Village. Add New Village", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (Exception e) {
            //Toast.makeText(getApplicationContext(), "Error occured Try again", Toast.LENGTH_SHORT).show();
        }
    }


}
