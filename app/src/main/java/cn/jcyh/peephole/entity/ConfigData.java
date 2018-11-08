package cn.jcyh.peephole.entity;

/**
 * Created by jogger on 2018/8/16.
 */
public class ConfigData {
    private DoorbellConfig cateEyeSet;
    private VideoConfig catEyeConfig;

    public DoorbellConfig getCateEyeSet() {
        return cateEyeSet;
    }

    public void setCateEyeSet(DoorbellConfig cateEyeSet) {
        this.cateEyeSet = cateEyeSet;
    }

    public VideoConfig getCatEyeConfig() {
        return catEyeConfig;
    }

    public void setCatEyeConfig(VideoConfig catEyeConfig) {
        this.catEyeConfig = catEyeConfig;
    }

    public static class VideoConfig {
        private String id;
        private String appId;
        private int videoTimeLimit = 108;//通话时长限制
        private int videoFrequencyLimit = 2;//通话频率限制

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public int getVideoTimeLimit() {
            return videoTimeLimit;
        }

        public void setVideoTimeLimit(int videoTimeLimit) {
            this.videoTimeLimit = videoTimeLimit;
        }

        public int getVideoFrequencyLimit() {
            return videoFrequencyLimit;
        }

        public void setVideoFrequencyLimit(int videoFrequencyLimit) {
            this.videoFrequencyLimit = videoFrequencyLimit;
        }
    }

    @Override
    public String toString() {
        return "ConfigData{" +
                "cateEyeSet=" + cateEyeSet +
                ", catEyeConfig=" + catEyeConfig +
                '}';
    }
}
