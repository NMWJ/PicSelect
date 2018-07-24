package com.rx.entity;

public class Image {

    private boolean check;
    private String path;

    public Image(boolean check, String path) {
        this.check = check;
        this.path = path;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
