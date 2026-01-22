package net.javaguides.springboot_jutjubic.dto;

public class LocationDTO {
    private Double latitude;
    private Double longitude;
    private String locationName;
    private Boolean isApproximated;

    public LocationDTO() {}

    public LocationDTO(Double latitude, Double longitude, Boolean isApproximated) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.isApproximated = isApproximated;
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Boolean getIsApproximated() { return isApproximated; }
    public void setIsApproximated(Boolean isApproximated) { this.isApproximated = isApproximated; }
}