package com.shubzz.hqm.adapter;

public class PersonDailyAtt {

    private String name;
    private String mno;
    private String age;
    private String wfro;
    private String aav;
    private String ccwp;
    private String isfc;

    public PersonDailyAtt(String name, String mno, String age, String wfro, String aav, String ccwp, String isfc) {
        this.name = name;
        this.mno = mno;
        this.age = age;
        this.wfro = wfro;
        this.aav = aav;
        this.ccwp = ccwp;
        this.isfc = isfc;
    }

    public String getName() {
        return name;
    }

    public String getMno() {
        return mno;
    }

    public String getAge() {
        return age;
    }

    public String getWfro() {
        return wfro;
    }

    public String getAav() {
        return aav;
    }

    public String getCcwp() {
        return ccwp;
    }

    public String getIsfc() {
        return isfc;
    }

}
