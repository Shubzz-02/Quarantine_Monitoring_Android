package com.shubzz.hqm.adapter;

public class Person {
    String nm,mno,age;

    public Person() {
    }

    public Person(String nm, String mno,String age) {
        this.nm = nm;
        this.mno = mno;
        this.age = age;
    }

    public String getNm() {
        return nm;
    }

    public String getMno() {
        return mno;
    }

    public String getAge() {
        return age;
    }
}
