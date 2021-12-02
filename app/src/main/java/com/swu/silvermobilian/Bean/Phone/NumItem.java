package com.swu.silvermobilian.Bean.Phone;

public class NumItem {

    private String pname;
    private String pnum;

    public String getPname() {
        return pname;
    }

    public String getPnum() {
        return pnum;
    }


    public NumItem(String pname, String pnum) {
        this.pnum = pnum;
        this.pname = pname;
    }
}
