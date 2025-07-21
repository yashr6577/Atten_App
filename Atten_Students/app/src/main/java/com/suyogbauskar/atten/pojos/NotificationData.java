package com.suyogbauskar.atten.pojos;

public class NotificationData {
    private long timestamp;
    private String title, body, time;

    public NotificationData(long timestamp, String title, String body, String time) {
        this.timestamp = timestamp;
        this.title = title;
        this.body = body;
        this.time = time;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getTime() {
        return time;
    }
}
