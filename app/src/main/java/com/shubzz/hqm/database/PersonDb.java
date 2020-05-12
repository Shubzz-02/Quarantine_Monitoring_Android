package com.shubzz.hqm.database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PersonDb {
    public static String TABLE_NAME;
    public static String COLUMN_ID = "id";
    public static String COLUMN_WFRO = "wfro";
    public static String COLUMN_AAV = "aav";
    public static String COLUMN_PERSON = "person";
    public static String COLUMN_AGE = "age";
    public static String COLUMN_MNO = "mno";
    public static String COLUMN_CCWP = "ccwp";
    public static String COLUMN_ISFC = "isfc";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private int id;
    private String wfro;
    private String aav;
    private String ccwp;
    private String isfc;
    private String timestamp;

    public static String CREATE_TABLE;

    public PersonDb() {
    }

    public PersonDb(int id, String wfro, String aav, String ccwp, String isfc, String timestamp) {
        this.id = id;
        this.wfro = wfro;
        this.aav = aav;
        this.ccwp = ccwp;
        this.isfc = isfc;
        this.timestamp = timestamp;
    }


    public static String createTable(String table) {
        TABLE_NAME = ((new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())).split("-")[2] + "_" + table).trim().replaceAll(" ","_");
        CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_PERSON + " TEXT,"
                        + COLUMN_AGE + " TEXT,"
                        + COLUMN_MNO + " TEXT,"
                        + COLUMN_WFRO + " TEXT,"
                        + COLUMN_AAV + " TEXT,"
                        + COLUMN_CCWP + " TEXT,"
                        + COLUMN_ISFC + " TEXT,"
                        + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                        + ")";
        return CREATE_TABLE;
    }

    public int getId() {
        return id;
    }


    public String getWfro() {
        return wfro;
    }

    public void setWfro(String wfro) {
        this.wfro = wfro;
    }

    public String getAav() {
        return aav;
    }

    public void setAav(String aav) {
        this.aav = aav;
    }

    public String getCcwp() {
        return ccwp;
    }

    public void setCcwp(String ccwp) {
        this.ccwp = ccwp;
    }

    public String getIsfc() {
        return isfc;
    }

    public void setIsfc(String isfc) {
        this.isfc = isfc;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
