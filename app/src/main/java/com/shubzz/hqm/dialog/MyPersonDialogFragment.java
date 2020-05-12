package com.shubzz.hqm.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.shubzz.hqm.R;

public class MyPersonDialogFragment extends DialogFragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.aperson_fragment, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            setCancelable(false);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView name = view.findViewById(R.id.name);
        name.setText(getArguments().getString("name"));
        final RadioGroup isfccf = view.findViewById(R.id.isfccf_radiogroup);
        final RadioGroup aav = view.findViewById(R.id.aav_radiogroup);
        final RadioGroup ciclp = view.findViewById(R.id.ciclp_radiogroup);
        final RadioGroup sfcf = view.findViewById(R.id.sfcf_radiogroup);
        final ImageButton cancel = view.findViewById(R.id.imgbtn_cancle);
        final ImageButton apply = view.findViewById(R.id.imgbtn_apply);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogListener dialogListener = (DialogListener) getActivity();
                dialogListener.onFinishEditDialog(null,-1);
                dismiss();
            }
        });
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] selectId = new int[4];
                selectId[0] = isfccf.getCheckedRadioButtonId();
                selectId[1] = aav.getCheckedRadioButtonId();
                selectId[2] = ciclp.getCheckedRadioButtonId();
                selectId[3] = sfcf.getCheckedRadioButtonId();
                if (selectId[0] > 0 && selectId[1] > 0 && selectId[2] > 0 && selectId[3] > 0) {
                    String[] res = new String[7];
                    RadioButton radioButton = view.findViewById(selectId[0]);
                    res[0] = radioButton.getText().toString();
                    radioButton = view.findViewById(selectId[1]);
                    res[1] = radioButton.getText().toString();
                    radioButton = view.findViewById(selectId[2]);
                    res[2] = radioButton.getText().toString();
                    radioButton = view.findViewById(selectId[3]);
                    res[3] = radioButton.getText().toString();
                    res[4] = getArguments().getString("name");
                    res[5] = getArguments().getString("mno");
                    res[6] = getArguments().getString("age");
                    DialogListener dialogListener = (DialogListener) getActivity();
                    dialogListener.onFinishEditDialog(res,getArguments().getInt("position"));
                    dismiss();
                }else{
                    Toast.makeText(getActivity(), "Please Select all Options ", Toast.LENGTH_LONG).show();

                }
            }
        });
//        final EditText pname = view.findViewById(R.id.person_name_add);
//        final EditText fname = view.findViewById(R.id.f_name_add);
//        final EditText age = view.findViewById(R.id.age_add);
//        final EditText gender = view.findViewById(R.id.gender_add);
//        final EditText pno = view.findViewById(R.id.pmno_add);
//        final EditText cfrom = view.findViewById(R.id.cfrom_add);
//        final EditText cdate = view.findViewById(R.id.rdate_add);
//        Button btnDone = view.findViewById(R.id.btnDone);
//        btnDone.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                DialogListener dialogListener = (DialogListener) getActivity();
//                dialogListener.onFinishEditDialog(pname.getText().toString(),fname.getText().toString(),
//                        age.getText().toString(),gender.getText().toString(),pno.getText().toString(),
//                        cfrom.getText().toString(),cdate.getText().toString());
//                dismiss();
//            }
//        });


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public interface DialogListener {
        void onFinishEditDialog(String[] data,int position);
    }
}
