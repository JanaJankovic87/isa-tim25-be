package net.javaguides.springboot_jutjubic.dto;

import java.io.Serializable;

public class TranscodingMessage implements Serializable {

    private Long videoId;
    private String videoPath;
    private String outputDir;
    private String[] presets = new String[]{ "720p", "480p"};

    public TranscodingMessage() {}

    public TranscodingMessage(Long videoId, String videoPath, String outputDir, String[] presets) {
        this.videoId = videoId;
        this.videoPath = videoPath;
        this.outputDir = outputDir;
        this.presets = (presets != null && presets.length > 0)
                ? presets
                : new String[]{ "720p", "480p"};
    }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }

    public String getOutputDir() { return outputDir; }
    public void setOutputDir(String outputDir) { this.outputDir = outputDir; }

    public String[] getPresets() {
        return presets != null ? presets : new String[]{ "720p", "480p"};
    }
    public void setPresets(String[] presets) { this.presets = presets; }

    @Override
    public String toString() {
        return "TranscodingMessage{videoId=" + videoId + ", videoPath='" + videoPath + "'}";
    }
}