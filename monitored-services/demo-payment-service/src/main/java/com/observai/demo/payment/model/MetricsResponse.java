package com.observai.demo.payment.model;

public class MetricsResponse {
    private double cpu;
    private double memory;
    private int requestCount;
    private double errorRate;
    private double responseTime;

    public MetricsResponse() {
    }

    public MetricsResponse(double cpu, double memory, int requestCount, double errorRate, double responseTime) {
        this.cpu = cpu;
        this.memory = memory;
        this.requestCount = requestCount;
        this.errorRate = errorRate;
        this.responseTime = responseTime;
    }

    public double getCpu() {
        return cpu;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public double getMemory() {
        return memory;
    }

    public void setMemory(double memory) {
        this.memory = memory;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public double getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(double responseTime) {
        this.responseTime = responseTime;
    }
}
