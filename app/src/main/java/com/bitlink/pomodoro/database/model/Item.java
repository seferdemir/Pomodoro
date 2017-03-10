package com.bitlink.pomodoro.database.model;

public class Item {

    private int id;
    private String taskName;
    private short workSessionDuration;
    private short breakDuration;
    private short longBreakDuration;
    private int workSessionColor;
    private int breakColor;
    private int longBreakColor;
    private short totalWorkSession;
    private String addedDate;

    public Item() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getBreakColor() {
        return breakColor;
    }

    public void setBreakColor(int breakColor) {
        this.breakColor = breakColor;
    }

    public short getBreakDuration() {
        return breakDuration;
    }

    public void setBreakDuration(short breakDuration) {
        this.breakDuration = breakDuration;
    }

    public int getLongBreakColor() {
        return longBreakColor;
    }

    public void setLongBreakColor(int longBreakColor) {
        this.longBreakColor = longBreakColor;
    }

    public short getLongBreakDuration() {
        return longBreakDuration;
    }

    public void setLongBreakDuration(short longBreakDuration) {
        this.longBreakDuration = longBreakDuration;
    }

    public short getWorkSessionDuration() {
        return workSessionDuration;
    }

    public void setWorkSessionDuration(short workSessionDuration) {
        this.workSessionDuration = workSessionDuration;
    }

    public int getWorkSessionColor() {
        return workSessionColor;
    }

    public void setWorkSessionColor(int workSessionColor) {
        this.workSessionColor = workSessionColor;
    }

    public short getTotalWorkSession() {
        return totalWorkSession;
    }

    public void setTotalWorkSession(short totalWorkSession) {
        this.totalWorkSession = totalWorkSession;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }
}
