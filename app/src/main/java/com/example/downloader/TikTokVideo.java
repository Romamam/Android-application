package com.example.downloader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class TikTokVideo {
    private List<String> video;

    public List<String> getVideo() {
        return video;
    }

    public void setVideo(List<String> video) {
        this.video = video;
    }
}
