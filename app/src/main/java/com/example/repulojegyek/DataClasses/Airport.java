package com.example.repulojegyek.DataClasses;

import androidx.annotation.NonNull;

public class Airport {
    private String name;
    private String code;
    private String city;
    private String countryCode;


    public Airport() {}

    public Airport(String name, String city, String code, String countryCode/*, String terminal, String gate*/) {
        this.name = name;
        this.code = code;
        this.city = city;
        this.countryCode = countryCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @NonNull
    @Override
    public String toString() {
        return countryCode + " - " + city + " (" + code + ")";
    }
}
