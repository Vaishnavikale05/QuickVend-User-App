package com.example.quickvenduser.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.quickvenduser.R;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    private Context context;
    private List<String> filterList;
    private OnFilterClickListener onFilterClickListener;

    public FilterAdapter(Context context, List<String> filterList, OnFilterClickListener onFilterClickListener) {
        this.context = context;
        this.filterList = filterList;
        this.onFilterClickListener = onFilterClickListener;
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_filter, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FilterViewHolder holder, int position) {
        String filter = filterList.get(position);
        holder.filterName.setText(filter);
        holder.itemView.setOnClickListener(v -> onFilterClickListener.onFilterClick(filter));
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    public interface OnFilterClickListener {
        void onFilterClick(String filter);
    }

    public static class FilterViewHolder extends RecyclerView.ViewHolder {
        TextView filterName;

        public FilterViewHolder(View itemView) {
            super(itemView);
            filterName = itemView.findViewById(R.id.filter_name);
        }
    }
}
