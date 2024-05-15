package pl.jakubirla.clouddrive.files;

import java.util.List;

public class FileToRead {
    private String name;
    private String type;
    private long date;
    private long size;
    private List<FileToRead> children;

    public FileToRead(String name, String type, long date, long size, List<FileToRead> children) {
        this.name = name;
        this.type = type;
        this.date = date;
        this.size = size;
        this.children = children;
    }

    @Override
    public String toString() {
        return "{" +
                "\"name\":\"" + name + "\"" +
                ",\"type\":\"" + type + "\"" +
                ",\"date\":" + date +
                ",\"size\":" + size +
                ",\"children\":" + (children == null ? "[]" : children) +
                "}";
    }
}

