package com.magic.microspider.springbootspider.base.enums;

/**
 * 爬虫站点状态
 * Created by liunn on 2018/1/11.
 */
public enum SiteStatus {
    NEW(0, "新增"),
    RUNNING(1, "正在运行"),
    COMPLETED(2, "完成"),
    REMOVED(-2, "已删除");

    private int code;

    private String msg;

    private SiteStatus(int code, String msg) {
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
