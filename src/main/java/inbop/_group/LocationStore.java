package inbop._group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class LocationStore {
    private final Map<String, LocationDto> last = new ConcurrentHashMap<>();
    
    public void upsert(String userId, LocationDto dto) { 
        last.put(userId, dto); 
    }
    
    public List<LocationDto> findAll() { 
        return new ArrayList<>(last.values()); 
    }
}





