package com.example.AndroidPaymentApp.models;

public class DateOfBirth {
    byte day;
    byte month;
    char year;

    public byte getDay() {
        return day;
    }

    public void setDay(byte day) {
        this.day = day;
    }

    public byte getMonth() {
        return month;
    }

    public void setMonth(byte month) {
        this.month = month;
    }

    public char getYear() {
        return year;
    }

    public void setYear(char year) {
        this.year = year;
    }

    public DateOfBirth(byte day, byte month, char year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }
}
