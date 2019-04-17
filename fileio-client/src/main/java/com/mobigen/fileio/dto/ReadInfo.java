package com.mobigen.fileio.dto;

import java.util.List;

public class ReadInfo {
    private List<String> readLines;
    private PosInfo posInfo;

    @Override
    public String toString() {
        return "ReadInfo{" +
                "readLines=" + readLines +
                ", posInfo=" + posInfo +
                '}';
    }

    public List<String> getReadLines() {
        return readLines;
    }

    public void setReadLines(List<String> readLines) {
        this.readLines = readLines;
    }

    public PosInfo getPosInfo() {
        return posInfo;
    }

    public void setPosInfo(PosInfo posInfo) {
        this.posInfo = posInfo;
    }

}
