package inbop._group;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationStore store;
    
    public LocationController(LocationStore store) { 
        this.store = store; 
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Void> upsert(@PathVariable String userId, @RequestBody LocationDto body) {
        body.setUserId(userId);
        if (body.getTimestamp() <= 0) {
            body.setTimestamp(System.currentTimeMillis());
        }
        store.upsert(userId, body);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<LocationDto> all() { 
        return store.findAll(); 
    }
}





