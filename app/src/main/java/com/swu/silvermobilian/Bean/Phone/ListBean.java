package com.swu.silvermobilian.Bean.Phone;

public class ListBean {

    private String name;
    private String num;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public ListBean(String name, String num) {
        this.num = num;
        this.name = name;
    }
}
