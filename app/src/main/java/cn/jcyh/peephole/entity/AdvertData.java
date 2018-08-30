package cn.jcyh.peephole.entity;

import java.util.List;

/**
 * Created by jogger on 2018/8/10.
 */
public class AdvertData {
    private AdvertConfig advertConfig;
    private List<Advert> adverts;

    public AdvertConfig getAdvertConfig() {
        return advertConfig;
    }

    public void setAdvertConfig(AdvertConfig advertConfig) {
        this.advertConfig = advertConfig;
    }

    public List<Advert> getAdverts() {
        return adverts;
    }

    public void setAdverts(List<Advert> adverts) {
        this.adverts = adverts;
    }

    @Override
    public String toString() {
        return "AdvertData{" +
                "advertConfig=" + advertConfig +
                ", adverts=" + adverts +
                '}';
    }

    public class AdvertConfig {
        private int displayCount;
        private int displayTime;
        private boolean isAutoPlay;
        private boolean isWebSite;

        public int getDisplayCount() {
            return displayCount;
        }

        public void setDisplayCount(int displayCount) {
            this.displayCount = displayCount;
        }

        public int getDisplayTime() {
            return displayTime;
        }

        public void setDisplayTime(int displayTime) {
            this.displayTime = displayTime;
        }

        public boolean isAutoPlay() {
            return isAutoPlay;
        }

        public void setAutoPlay(boolean autoPlay) {
            isAutoPlay = autoPlay;
        }

        public boolean isWebSite() {
            return isWebSite;
        }

        public void setWebSite(boolean webSite) {
            isWebSite = webSite;
        }

        @Override
        public String toString() {
            return "AdvertConfig{" +
                    "displayCount=" + displayCount +
                    ", displayTime=" + displayTime +
                    ", isAutoPlay=" + isAutoPlay +
                    ", isWebSite=" + isWebSite +
                    '}';
        }
    }

    public class Advert {
        private String imageUrl;
        private String url;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return "Advert{" +
                    "imageUrl='" + imageUrl + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}
