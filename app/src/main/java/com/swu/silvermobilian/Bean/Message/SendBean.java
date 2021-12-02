package com.swu.silvermobilian.Bean.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SendBean implements Serializable {

    // list에서 현재 내 Row의 Index를 저장
    private int index;

    // 문자 번호
    private String phone;
    // 문자 내용
    private String content;
    // 답장 내용
    private String comment;
    // 등록 날짜
    private String regDate;

    // (구) SaveBean
    private List<SendBean> sendBeanList = new ArrayList<>();

    public List<SendBean> getSBList() {
        return sendBeanList;
    }

    // (구) SendBean
    private List<SendBean> chatBeanList = new ArrayList<>();

    public List<SendBean> getCBList() {
        return chatBeanList;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getSelIdx() {
        return index;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // (구) CommonBean
    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

}
