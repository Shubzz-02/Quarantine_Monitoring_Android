package com.shubzz.hqm;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shubzz.hqm.adapter.Person;
import com.shubzz.hqm.adapter.PersonAdapter;
import com.shubzz.hqm.adapter.PersonDailyAtt;
import com.shubzz.hqm.bsheet.MyFabFragment;
import com.shubzz.hqm.database.DatabaseHelper;
import com.shubzz.hqm.dialog.MyPersonDialogFragment;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PersonActivity extends AppCompatActivity implements PersonAdapter.PersonAdapterListener,
        AAH_FabulousFragment.Callbacks, AAH_FabulousFragment.AnimationListener,
        MyPersonDialogFragment.DialogListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchView searchView;
    private List<Person> personList;
    private PersonAdapter mAdapter;
    private FloatingActionButton fab;
    private SessionHandler sessionHandler;
    private HashMap<String, String> details;

    private static final MediaType JSON = MediaType.parse("application/json");
    private static final String KEY_uq = "uq_key";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_STATUS = "status";
    private static final String KEY_VNAME = "vill_name";
    private List<PersonDailyAtt> list;
    private Map<Integer, PersonDailyAtt> map;

    MyPersonDialogFragment dialog;
    private MyFabFragment dialogFrag;
    ProgressDialog progressDialog;
    AlertDialog.Builder builder;
    private Handler mhandler;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionHandler = new SessionHandler(this);
        mhandler = new Handler(Looper.getMainLooper());
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        builder = new AlertDialog.Builder(this);
        db = new DatabaseHelper(this);
        list = new ArrayList<>();
        map = new HashMap<>();
        map.clear();
        setContentView(R.layout.activity_person);

        initUI();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogFrag.show(getSupportFragmentManager(), dialogFrag.getTag());
            }
        });


        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            verifyUser();
        }

        if (!file(sessionHandler.getVill_name() + ".json")) {
            fetchDataSync();
        } else {
            updateData();
        }
    }

    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(sessionHandler.getVill_name());
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);

        dialogFrag = MyFabFragment.newInstance();
        dialogFrag.setParentFab(fab);

        personList = new ArrayList<>();
        recyclerView = findViewById(R.id.person_recycler_view);
        mAdapter = new PersonAdapter(this, personList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = findViewById(R.id.person_swipeContainer);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //updateData();
                fetchDataSync();
                //mSwipeRefreshLayout.setRefreshing(false);
            }
        });


        //----------------search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = findViewById(R.id.person_search);
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
    public void onBackPressed() {
        int s = map.size();
        String [][] res = new String[s][7];
        for(int i=0;i<s;i++){
            PersonDailyAtt a = map.get(i);
            res[i][0] = a.getName();
            res[i][1] = a.getMno();
            res[i][2] = a.getAge();
            res[i][3] = a.getWfro();
            res[i][4] = a.getAav();
            res[i][5] = a.getCcwp();
            res[i][6] = a.getIsfc();
        }
        try {
            JSONArray ja = new JSONArray(res);
            Log.d("res ",ja.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    ///---------------------FAB section
    @Override
    public void onOpenAnimationStart() {

    }

    @Override
    public void onOpenAnimationEnd() {

    }

    @Override
    public void onCloseAnimationStart() {

    }

    @Override
    public void onCloseAnimationEnd() {

    }

    @Override
    public void onResult(Object result) {
        if (result == null) {
            Toast.makeText(this, "Cancled", Toast.LENGTH_LONG).show();
        } else {
            details = (HashMap<String, String>) result;
            Log.d("data", details.toString());
            addPerson();
            //Toast.makeText(this, "HAHAHAHA", Toast.LENGTH_LONG).show();
        }

    }


    public void updateData() {
        personList.clear();
        String v = sessionHandler.getVill_name();
        StringBuilder temp = new StringBuilder();
        try {
            FileInputStream fin = openFileInput(v + ".json");
            int c;
            while ((c = fin.read()) != -1) {
                temp = temp.append(Character.toString((char) c));
            }
            fin.close();
            JSONArray jsonArray = new JSONArray(temp.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.getString("person_name");
                String p_no = obj.getString("p_no");
                String age = obj.getString("age");
                Person person = new Person(name, p_no, age);
                personList.add(person);
            }
            mAdapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(false);
        } catch (Exception e) {
            Log.e("error: ", "Error no file");
        }
    }

    @Override
    public void onPersonSelected(Person person, int position) {
        //Toast.makeText(getApplicationContext(), person.getNm() + "is clicked." + " at position "+position, Toast.LENGTH_LONG).show();
        dialog = new MyPersonDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putString("name", person.getNm());
        bundle.putString("mno", person.getMno());
        bundle.putString("age", person.getAge());
        dialog.setArguments(bundle);
        FragmentTransaction ft;
        ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialog.show(ft, "dialog");
    }

    @Override
    public void onFinishEditDialog(String[] data, int position) {
        if (data == null) {
            Toast.makeText(getApplicationContext(), "Canceled", Toast.LENGTH_LONG).show();
        } else {
            Log.d("return :", Arrays.deepToString(data));
            if(map.containsKey(position)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    map.replace(position,new PersonDailyAtt(data[4], data[5], data[6], data[0], data[1], data[2], data[3]));
                }else{
                    map.remove(position);
                    map.put(position,new PersonDailyAtt(data[4], data[5], data[6], data[0], data[1], data[2], data[3]));
                }
            }else{
                map.put(position,new PersonDailyAtt(data[4], data[5], data[6], data[0], data[1], data[2], data[3]));
            }
            //db.insertPerson(data[4], data[5], data[6], data[0], data[1], data[2], data[3]);
            //atabase
        }

    }

    //------------------NETWOTKING


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
//                PersonActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        AlertDialog alertDialog = builder.create();
//                        alertDialog.show();
//                    }
//                });
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

    private void fetchDataSync() {
        OkHttpClient client = new OkHttpClient();
        JSONObject body = new JSONObject();
        String username = sessionHandler.getKeyUsername();
        String key = sessionHandler.getSecKey();
        try {
            body.put(KEY_USERNAME, username);
            body.put(KEY_uq, key);
            body.put(KEY_VNAME, sessionHandler.getVill_name());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final RequestBody reqbody = RequestBody.Companion.create(body.toString(), JSON);
        Request request = new Request.Builder()
                .url(sessionHandler.getUrl())
                .post(reqbody)
                .addHeader("x-reaq", "03539ace")
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

    private void getData(String res) {
        if (res.length() > 0) {
            String b = sessionHandler.getVill_name();
            FileOutputStream fout = null;
            try {
                JSONArray arr = new JSONArray(res);
                fout = openFileOutput(b + ".json", Context.MODE_PRIVATE);
                fout.write(res.getBytes());
                fout.close();
                mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateData();
                    }
                });
            } catch (Exception e) {
            }
        }
    }

    private void addPerson() {
        progressDialog.setMessage("Adding Person Details...");
        progressDialog.show();
        JSONObject body = new JSONObject();
        String username = sessionHandler.getKeyUsername();
        String key = sessionHandler.getSecKey();
        String inputVill = sessionHandler.getVill_name();
        try {
            username = sessionHandler.getKeyUsername();
            key = sessionHandler.getSecKey();
            body.put(KEY_USERNAME, username);
            body.put(KEY_VNAME, inputVill);
            body.put(KEY_uq, key);
            body.put("pname", details.get("pname"));
            body.put("fname", details.get("fname"));
            body.put("cdate", details.get("cdate"));
            body.put("gender", details.get("gender"));
            body.put("pno", details.get("pno"));
            body.put("age", details.get("age"));
            body.put("cfrom", details.get("cfrom"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        final RequestBody reqbody = RequestBody.Companion.create(body.toString(), JSON);
        Request request = new Request.Builder()
                .url(sessionHandler.getUrl())
                .post(reqbody)
                .addHeader("x-reaq", "9b201a40")
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

    private void checkIfAdded(String res) {
        Log.d("Resp :", res);
        try {
            JSONObject response = new JSONObject(res);
            if (response.getInt(KEY_STATUS) == 0) {
                builder.setMessage("Person Detail Added").setCancelable(false)
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
            } else if (response.getInt(KEY_STATUS) == 4) {
                builder.setMessage(response.getString("message")).setCancelable(false)
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


}
