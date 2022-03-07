package com.example.remidication;


import java.io.Serializable;

// class to save information about aévery medication
public class NewReminderList implements Serializable {
    public String getMedication_name() {
        return medication_name;
    }

    public void setMedication_name(String medication_name) {
        this.medication_name = medication_name;
    }

    String medication_name;
    public NewReminderList() {
    }

}
