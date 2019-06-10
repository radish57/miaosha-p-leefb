package com.miaosha.constant;

public enum OrderInfoStatus {
    NOT_PAY(0);


    private int status;

    OrderInfoStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
