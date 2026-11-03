package com.samarthshukla.skycast.ui.forecast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.samarthshukla.skycast.R;
import com.samarthshukla.skycast.model.GeocodingResponse;
import java.util.ArrayList;
import java.util.List;

public class CitySuggestionAdapter extends RecyclerView.Adapter<CitySuggestionAdapter.Holder> {
    public interface Listener { void onCityClick(GeocodingResponse city); }
    private final List<GeocodingResponse> items=new ArrayList<>();
    private final Listener listener;
    public CitySuggestionAdapter(Listener listener){ this.listener=listener; }
    public void setCities(List<GeocodingResponse> cities){ items.clear(); if(cities!=null)items.addAll(cities); notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p,int t){ return new Holder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_city_suggestion,p,false)); }
    @Override public void onBindViewHolder(@NonNull Holder h,int position){
        GeocodingResponse c=items.get(position); h.name.setText(c.getName());
        String sub=c.getSubtitle(); h.subtitle.setText(sub.isEmpty()?"Location":sub);
        h.itemView.setOnClickListener(v->{ if(listener!=null)listener.onCityClick(c); });
    }
    @Override public int getItemCount(){ return items.size(); }
    static class Holder extends RecyclerView.ViewHolder {
        TextView name,subtitle; Holder(@NonNull View v){super(v); name=v.findViewById(R.id.tvSuggestionCityName); subtitle=v.findViewById(R.id.tvSuggestionCitySubtitle);}
    }
}