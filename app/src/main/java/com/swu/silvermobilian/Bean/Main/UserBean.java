package com.swu.silvermobilian.Bean.Main;

import java.io.Serializable;

public class UserBean implements Serializable {

    private String id;
    private String pw;
    private String pw2;
    private String myname;
    private String mytel;
    private String myadr;
    private String memo;
    private String proname;
    private String protel;

    private String name;
    private String num;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getPw2() {
        return pw2;
    }

    public void setPw2(String pw2) {
        this.pw2 = pw2;
    }

    public String getMyname() {
        return myname;
    }

    public void setMyname(String myname) {
        this.myname = myname;
    }

    public String getMytel() {
        return mytel;
    }

    public void setMytel(String mytel) {
        this.mytel = mytel;
    }

    public String getMyadr() {
        return myadr;
    }

    public void setMyadr(String myadr) {
        this.myadr = myadr;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getProname() {
        return proname;
    }

    public void setProname(String proname) {
        this.proname = proname;
    }

    public String getProtel() {
        return protel;
    }

    public void setProtel(String protel) {
        this.protel = protel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }


    public void logout() {
        id = null;
        pw = null;
        pw2 = null;
    }
}