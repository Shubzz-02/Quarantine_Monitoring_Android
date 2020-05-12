package com.shubzz.hqm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shubzz.hqm.R;

import java.util.ArrayList;
import java.util.List;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.MyViewHolder> implements Filterable {

    private Context context;
    private List<Person> personList;
    private List<Person> personListFiltered;
    private PersonAdapterListener listener;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView personName, personMno;

        public MyViewHolder(View view) {
            super(view);

            // -------------- here add
            personName = view.findViewById(R.id.person_name);
            personMno = view.findViewById(R.id.m_no);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onPersonSelected(personListFiltered.get(getAdapterPosition()),getAdapterPosition());
                }
            });
        }

    }

    public PersonAdapter(Context context, List<Person> personList, PersonAdapterListener listener) {
        this.context = context;
        this.personList = personList;
        this.personListFiltered = personList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itenView = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_row, parent, false);
        return new MyViewHolder(itenView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        final Person person = personListFiltered.get(position);

        // -------------- here add
        holder.personName.setText(person.getNm());
        holder.personMno.setText(person.getMno());
    }

    @Override
    public int getItemCount() {
        return personListFiltered.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    personListFiltered = personList;
                } else {
                    List<Person> filteredList = new ArrayList<>();
                    for (Person row : personList) {
                        if (row.getNm().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    personListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = personListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                personListFiltered = (ArrayList<Person>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface PersonAdapterListener {
        void onPersonSelected(Person person,int position);
    }
}
