package com.mobigen.fileio.dto;

public class PosInfo {
    private long startPos = 0;
    private long endPos = 0;

    @Override
    public String toString() {
        return "PosInfo{" +
                "startPos=" + startPos +
                ", endPos=" + endPos +
                '}';
    }

    public long getStartPos() {
        return startPos;
    }

    public void setStartPos(long startPos) {
        this.startPos = startPos;
    }

    public long getEndPos() {
        return endPos;
    }

    public void setEndPos(long endPos) {
        this.endPos = endPos;
    }

}
