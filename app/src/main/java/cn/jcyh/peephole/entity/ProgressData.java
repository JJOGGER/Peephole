package cn.jcyh.peephole.entity;

/**
 * Created by jogger on 2018/8/22.
 */
public class ProgressData {
    private long currentBytes;
    private long contentLength;
    private boolean isDone;

    public ProgressData() {
    }

    public ProgressData(long totalBytesRead, long contentLength, boolean isDone) {
        this.currentBytes = totalBytesRead;
        this.contentLength = contentLength;
        this.isDone = isDone;
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public void setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    @Override
    public String toString() {
        return "ProgressData{" +
                "currentBytes=" + currentBytes +
                ", contentLength=" + contentLength +
                ", isDone=" + isDone +
                '}';
    }
}
