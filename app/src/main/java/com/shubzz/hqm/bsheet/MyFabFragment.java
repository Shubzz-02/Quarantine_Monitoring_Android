package com.shubzz.hqm.bsheet;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;
import com.shubzz.hqm.R;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFabFragment extends AAH_FabulousFragment {
    ImageButton imgbtn_apply,imgbtn_cancel;
    EditText pname, fname, age, gender, pno, cfrom, cdate;
    String spname, sfname, sage, sgender, spno, scfrom, scdate;
    private DisplayMetrics metrics;
    private HashMap<String, String> details;

    public static MyFabFragment newInstance() {
        MyFabFragment mff = new MyFabFragment();
        return mff;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        details = new HashMap<>();
        //details = ((PersonActivity) getActivity()).getDetails();
        metrics = this.getResources().getDisplayMetrics();

    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet, null);
        RelativeLayout rl_content = (RelativeLayout) contentView.findViewById(R.id.rl_content);
        final LinearLayout ll_buttons = (LinearLayout) contentView.findViewById(R.id.ll_buttons);
        imgbtn_cancel = (ImageButton) contentView.findViewById(R.id.imgbtn_cancle);
        imgbtn_apply = (ImageButton) contentView.findViewById(R.id.imgbtn_apply);
        // ViewPager vp_types = (ViewPager) contentView.findViewById(R.id.vp_types);
        imgbtn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
                if (validateInput(spname, sfname, sage, sgender, spno, scfrom, scdate)) {
                    closeFilter(details);
                } else {
                    //closeFilter(null);
                }
            }
        });
        imgbtn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFilter(null);
            }
        });

        pname = contentView.findViewById(R.id.person_name_add);
        fname = contentView.findViewById(R.id.f_name_add);
        age = contentView.findViewById(R.id.age_add);
        gender = contentView.findViewById(R.id.gender_add);
        pno = contentView.findViewById(R.id.pmno_add);
        cfrom = contentView.findViewById(R.id.cfrom_add);
        cdate = contentView.findViewById(R.id.rdate_add);
//        ed_vill_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View arg0, boolean hasfocus) {
//                if (hasfocus) {
//                    setViewgroupStatic(null);
//                }
//            }
//        });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if (ed_vill_name.isSelected()){
//                        setViewgroupStatic(null);
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }).start();

        setAnimationDuration(350); //optional; default 500ms
        setPeekHeight(300); // optional; default 400dp
        setCallbacks((Callbacks) getActivity()); //optional; to get back result
        setAnimationListener((AnimationListener) getActivity()); //optional; to get animation callbacks
        setViewgroupStatic(ll_buttons);// optional; layout to stick at bottom on slide
        setCancelable(false);
        //setViewPager(vp_types); //optional; if you use viewpager that has scrollview
        setViewMain(rl_content); //necessary; main bottomsheet view
        setMainContentView(contentView); // necessary; call at end before super
        super.setupDialog(dialog, style); //call super at last
    }

    private void getData() {
        spname = pname.getText().toString();
        sfname = fname.getText().toString();
        sage = age.getText().toString();
        sgender = gender.getText().toString();
        spno = pno.getText().toString();
        scfrom = cfrom.getText().toString();
        scdate = cdate.getText().toString();
    }


    HashMap<String, String> getDetails() {
        HashMap<String, String> d = new HashMap<>();

        return d;
    }


    private boolean validateInput(String pname, String fname, String age, String gender, String pno, String cfrom, String ddate) {
        if (pname.isEmpty()) {
            this.pname.setError("Name is empty");
            this.pname.requestFocus();
            return false;
        }
        if (fname.isEmpty()) {
            this.fname.setError("Father name is empty");
            this.fname.requestFocus();
            return false;
        }
        if (age.isEmpty()) {
            this.age.setError("Age empty.");
            this.age.requestFocus();
            return false;
        }
        if (gender.isEmpty()) {
            this.gender.setError("Gender is Empty");
            this.gender.requestFocus();
            return false;
        } else if (!gender.equalsIgnoreCase("Male") && !gender.equalsIgnoreCase("Female")
                && !gender.equalsIgnoreCase("Other")) {
            this.gender.setError("Valid (Male/Female/Other)");
            this.gender.requestFocus();
            return false;
        }
        if (pno.length() != 10) {
            this.pno.setError("Enter valid number");
            this.pno.requestFocus();
            return false;
        } else{
            if(pno.startsWith("+91") || pno.startsWith("0")){
                this.pno.setError("Don't include +91 or 0");
                this.pno.requestFocus();
                return false;
            }
        }
        if (cfrom.isEmpty()) {
            this.pno.setError("Place empty");
            this.pno.requestFocus();
            return false;
        }
        if (ddate.isEmpty()) {
            this.cdate.setError("Date Empty");
            this.cdate.requestFocus();
            return false;
        } else {
            String regex = "^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(ddate);
            if (!matcher.matches()) {
                this.cdate.setError("Valid (YYYY-MM-DD");
                this.cdate.requestFocus();
                return false;
            }
        }
        details.put("pname", pname);
        details.put("fname", fname);
        details.put("age", age);
        details.put("gender", gender);
        details.put("pno", pno);
        details.put("cfrom", cfrom);
        details.put("cdate", ddate);
        return true;
    }



}
