package com.mobigen.fileio.dto;

import java.io.Serializable;

public class FileLog implements Serializable {
    private String type;
    private String dateOri;
    private String date;
    private String hostName;
    private String sysName;
    private String content;

    @Override
    public String toString() {
        return "(\'" + dateOri + "\',\'" + date + "\',\'" + hostName
                + "\',\'" + sysName + "\',\'" + content +"\')";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDateOri() {
        return dateOri;
    }

    public void setDateOri(String dateOri) {
        this.dateOri = dateOri;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
