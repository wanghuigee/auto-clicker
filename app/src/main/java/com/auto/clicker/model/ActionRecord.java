package com.auto.clicker.model;

public class ActionRecord {
    public String type;       // click, long_click, swipe
    public long timestamp;    // 时间戳

    // 点击/长按坐标
    public float x;
    public float y;

    // 滑动坐标
    public float startX;
    public float startY;
    public float endX;
    public float endY;
    public int swipeDuration;

    public ActionRecord() {}

    public static ActionRecord createClick(float x, float y) {
        ActionRecord record = new ActionRecord();
        record.type = "click";
        record.x = x;
        record.y = y;
        record.timestamp = System.currentTimeMillis();
        return record;
    }

    public static ActionRecord createLongClick(float x, float y) {
        ActionRecord record = new ActionRecord();
        record.type = "long_click";
        record.x = x;
        record.y = y;
        record.timestamp = System.currentTimeMillis();
        return record;
    }

    public static ActionRecord createSwipe(float startX, float startY, float endX, float endY, int duration) {
        ActionRecord record = new ActionRecord();
        record.type = "swipe";
        record.startX = startX;
        record.startY = startY;
        record.endX = endX;
        record.endY = endY;
        record.swipeDuration = duration;
        record.timestamp = System.currentTimeMillis();
        return record;
    }
}
