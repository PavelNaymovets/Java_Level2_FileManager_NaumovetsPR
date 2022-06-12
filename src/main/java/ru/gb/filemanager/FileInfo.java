package ru.gb.filemanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo {
    private static final String UP_TOKEN = "[..]";
    private String fileName;
    private long length;

    public FileInfo(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            if (Files.isDirectory(path)) {
                this.length = -1L;
            } else {
                this.length = Files.size(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Something wrong with file: " + path.toAbsolutePath());
        }
    }

    public FileInfo(String fileName, long length){
        this.fileName = fileName;
        this.length = length;
    }

    public boolean isDirectory(){
        return length == -1L;
    }

    public boolean isUpElement(){
        return length == -2L;
    }

    public static String getUpToken(){
        return UP_TOKEN;
    }
    public String getFileName() {
        return fileName;
    }

    public long getLength() {
        return length;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setLength() {
        this.length = length;
    }


}
