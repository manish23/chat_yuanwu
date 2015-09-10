package com.ihs.demo.message;


import com.ihs.message.types.HSBaseMessage;

/**
 * Created by wuchen on 15-9-5.
 */
public class ChatEntity {
    private String text;
    private String status;
    private String date;
    private boolean issend = true;
    public String getText(){
        return text;
    }
    public String getDate(){
        return date;
    }
    private HSBaseMessage hsBaseMessage;
    public String getStatus(){
        return  status;
    }
    public void setText(String date, String status){
        this.date = date;   this.status = status;
    }
    public HSBaseMessage getHsBaseMessage(){
        return hsBaseMessage;
    }
    public boolean get_Issend(){
        return issend;
    }
    public ChatEntity(String text, String date, String status, HSBaseMessage hsBaseMessage, boolean issend){
        this.date = date;   this.text = text;   this.issend = issend;
        this.status = status;   this.hsBaseMessage = hsBaseMessage;
    }
}
