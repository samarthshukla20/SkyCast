package com.samarthshukla.skycast.ui.forecast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
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

        holder.tvMax.setText(forecast.getMaxTemp() + "°");
        holder.tvMin.setText(forecast.getMinTemp() + "°");

        holder.tvRain.setText(
                "Rain " + forecast.getPrecipitationProbability() + "%"
        );

        // Load the Lottie animation.
        String animationFile = forecast.getWeatherIcon();

        holder.tvIcon.cancelAnimation();
        holder.tvIcon.setAnimation(animationFile);
        holder.tvIcon.setRepeatCount(-1);
        holder.tvIcon.playAnimation();
    }

    @Override
    public void onViewRecycled(@NonNull ForecastViewHolder holder) {
        super.onViewRecycled(holder);
        holder.tvIcon.cancelAnimation();
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {

        TextView tvDay, tvMax, tvMin, tvRain;
        LottieAnimationView tvIcon;

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