package net.javaguides.springboot_jutjubic.messages;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UploadEventProto {

    public static class UploadEvent {
        private String videoId;
        private String title;
        private long fileSize;
        private String authorId;
        private String authorName;
        private String thumbnailUrl;
        private String videoUrl;
        private long timestamp;
        private List<String> tags;

        private UploadEvent(Builder builder) {
            this.videoId = builder.videoId;
            this.title = builder.title;
            this.fileSize = builder.fileSize;
            this.authorId = builder.authorId;
            this.authorName = builder.authorName;
            this.thumbnailUrl = builder.thumbnailUrl;
            this.videoUrl = builder.videoUrl;
            this.timestamp = builder.timestamp;
            this.tags = builder.tags;
        }

        public String getVideoId() { return videoId; }
        public String getTitle() { return title; }
        public long getFileSize() { return fileSize; }
        public String getAuthorId() { return authorId; }
        public String getAuthorName() { return authorName; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public String getVideoUrl() { return videoUrl; }
        public long getTimestamp() { return timestamp; }
        public List<String> getTags() { return tags; }

        public byte[] toByteArray() {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);

                // Simple serialization (not real protobuf, but works for demo)
                writeString(dos, videoId);
                writeString(dos, title);
                dos.writeLong(fileSize);
                writeString(dos, authorId);
                writeString(dos, authorName);
                writeString(dos, thumbnailUrl);
                writeString(dos, videoUrl);
                dos.writeLong(timestamp);
                dos.writeInt(tags.size());
                for (String tag : tags) {
                    writeString(dos, tag);
                }

                return baos.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("Serialization failed", e);
            }
        }

        private void writeString(DataOutputStream dos, String str) throws Exception {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            dos.writeInt(bytes.length);
            dos.write(bytes);
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public static class Builder {
            private String videoId;
            private String title;
            private long fileSize;
            private String authorId;
            private String authorName;
            private String thumbnailUrl;
            private String videoUrl;
            private long timestamp;
            private List<String> tags;

            public Builder setVideoId(String videoId) {
                this.videoId = videoId;
                return this;
            }

            public Builder setTitle(String title) {
                this.title = title;
                return this;
            }

            public Builder setFileSize(long fileSize) {
                this.fileSize = fileSize;
                return this;
            }

            public Builder setAuthorId(String authorId) {
                this.authorId = authorId;
                return this;
            }

            public Builder setAuthorName(String authorName) {
                this.authorName = authorName;
                return this;
            }

            public Builder setThumbnailUrl(String thumbnailUrl) {
                this.thumbnailUrl = thumbnailUrl;
                return this;
            }

            public Builder setVideoUrl(String videoUrl) {
                this.videoUrl = videoUrl;
                return this;
            }

            public Builder setTimestamp(long timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public Builder addAllTags(List<String> tags) {
                this.tags = tags;
                return this;
            }

            public UploadEvent build() {
                return new UploadEvent(this);
            }
        }
    }
}