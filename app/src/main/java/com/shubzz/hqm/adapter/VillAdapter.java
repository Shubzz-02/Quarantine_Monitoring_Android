package com.shubzz.hqm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.shubzz.hqm.R;

import java.util.ArrayList;
import java.util.List;

public class VillAdapter extends RecyclerView.Adapter<VillAdapter.MyViewHolder> implements Filterable {

    private Context context;
    private List<Vill> villList;
    private List<Vill> villListFiltered;
    private VillAdapterListener listener;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView vill_name, vill_total;

        public MyViewHolder(View view) {
            super(view);
            vill_name = view.findViewById(R.id.vill_name);
            vill_total = view.findViewById(R.id.vill_total);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onVillSelected(villListFiltered.get(getAdapterPosition()));
                }
            });
        }
    }

    public VillAdapter(Context context, List<Vill> villList, VillAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.villList = villList;
        this.villListFiltered = villList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.vill_row, parent, false);
        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Vill vill = villListFiltered.get(position);
        holder.vill_name.setText(vill.getName());
        holder.vill_total.setText(vill.getTotal());
    }

    @Override
    public int getItemCount() {
        return villListFiltered.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    villListFiltered = villList;
                } else {
                    List<Vill> filteredList = new ArrayList<>();
                    for (Vill row : villList) {
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    villListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = villListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                villListFiltered = (ArrayList<Vill>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface VillAdapterListener {
        void onVillSelected(Vill vill);
    }


}
