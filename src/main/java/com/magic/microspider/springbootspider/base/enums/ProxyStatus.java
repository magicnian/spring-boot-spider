package com.magic.microspider.springbootspider.base.enums;

/**
 * Created by liunn on 2018/1/11.
 */
public enum ProxyStatus {

    IS_REMOVED(1, "已删除"),
    IS_ACTIVE(0, "正常");

    private int code;

    private String msg;

    private ProxyStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int code() {
        return code;
    }

    public String msg() {
        return msg;
    }
}
