package com.samarthshukla.skycast.ui.forecast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.samarthshukla.skycast.R;
import com.samarthshukla.skycast.model.ForecastDay;

import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private final List<ForecastDay> forecastList;

    public ForecastAdapter(List<ForecastDay> forecastList) {
        this.forecastList = forecastList;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);

        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ForecastViewHolder holder, int position) {

        ForecastDay forecast = forecastList.get(position);

        holder.tvDay.setText(forecast.getDay());
        holder.tvIcon.setText(forecast.getWeatherIcon());
        holder.tvMax.setText(forecast.getMaxTemp() + "°");
        holder.tvMin.setText(forecast.getMinTemp() + "°");
        holder.tvRain.setText(
                "Rain " + forecast.getPrecipitationProbability() + "%"
        );
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {

        TextView tvDay, tvIcon, tvMax, tvMin, tvRain;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDay = itemView.findViewById(R.id.tvForecastDay);
            tvIcon = itemView.findViewById(R.id.tvForecastIcon);
            tvMax = itemView.findViewById(R.id.tvForecastMax);
            tvMin = itemView.findViewById(R.id.tvForecastMin);
            tvRain = itemView.findViewById(R.id.tvForecastRain);
        }
    }
}