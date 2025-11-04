package inbop._group;

public class LocationDto {
    private String userId;
    private double latitude;
    private double longitude;
    private long timestamp;

    // getter/setter
    public String getUserId() { 
        return userId; 
    }
    
    public void setUserId(String userId) { 
        this.userId = userId; 
    }
    
    public double getLatitude() { 
        return latitude; 
    }
    
    public void setLatitude(double latitude) { 
        this.latitude = latitude; 
    }
    
    public double getLongitude() { 
        return longitude; 
    }
    
    public void setLongitude(double longitude) { 
        this.longitude = longitude; 
    }
    
    public long getTimestamp() { 
        return timestamp; 
    }
    
    public void setTimestamp(long timestamp) { 
        this.timestamp = timestamp; 
    }
}




