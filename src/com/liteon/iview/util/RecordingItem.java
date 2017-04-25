package com.liteon.iview.util;

public class RecordingItem {
    private final String url;
    private final String name;
    private final String time;
    private final String size;
    private boolean isSelected;
    private String localPath;
    private String otgPath;

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public String getSize() {
        return size;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getOtgPath() {
        return otgPath;
    }

    public RecordingItem(String url, String name, String time, String size) {
        this.url = url;
        this.name = name;
        this.time = time;
        this.size = size;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void setOtgPath(String otgPath) {
        this.otgPath = otgPath;
    }

    @Override
    public String toString() {
        return url;
    }
}
