package com.samarthshukla.skycast.model;

public class GeocodingResponse {
    private String name, state, country;
    private double lat, lon;
    public String getName(){ return name; }
    public String getState(){ return state; }
    public String getCountry(){ return country; }
    public double getLat(){ return lat; }
    public double getLon(){ return lon; }
    public String getSubtitle(){
        StringBuilder s=new StringBuilder();
        if(state!=null && !state.trim().isEmpty()) s.append(state.trim());
        if(country!=null && !country.trim().isEmpty()){ if(s.length()>0)s.append(", "); s.append(country.trim()); }
        return s.toString();
    }
}