package cn.jcyh.peephole.entity;

/**
 * 作者：jogger
 * 时间：2018/12/29 9:12
 * 描述：
 */
public class PlayAudio {
    private int count = 1;
    private String path;
    private float volume = 0.5f;

    public PlayAudio( String path) {
        this.path = path;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
}
