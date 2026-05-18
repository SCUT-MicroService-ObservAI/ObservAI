package com.observai.demo.order.model;

public class SlowResponse {
    private String result;
    private long delayMs;

    public SlowResponse() {
    }

    public SlowResponse(String result, long delayMs) {
        this.result = result;
        this.delayMs = delayMs;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }
}
