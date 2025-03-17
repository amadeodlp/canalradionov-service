package com.amadeodlp.canalradionov.core.services.user;

import com.amadeodlp.canalradionov.core.model.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    
    // Mock data storage - would be replaced with real database in production
    private final Map<String, UserProfile> users = new HashMap<>();
    
    public UserServiceImpl() {
        // Initialize with sample data
        initializeSampleData();
    }
    
    // Typically this would be injected from authentication context
    private String getCurrentUserId() {
        // In a real implementation, this would get the user ID from the security context
        return "user123";
    }
    
    @Override
    public UserProfile getCurrentUserProfile() {
        LOG.info("Getting profile for current user");
        String userId = getCurrentUserId();
        
        return users.getOrDefault(userId, createDefaultProfile(userId));
    }
    
    @Override
    public UserProfile updateUserProfile(UserProfile updatedProfile) {
        LOG.info("Updating profile for user {}", updatedProfile.id());
        
        // In a real implementation, we'd validate that the current user can only update their own profile
        // and validate the input data
        
        // Store the updated profile
        users.put(updatedProfile.id(), updatedProfile);
        
        return updatedProfile;
    }
    
    private UserProfile createDefaultProfile(String userId) {
        return new UserProfile(
                userId,
                "user_" + userId,
                "user_" + userId + "@example.com",
                "New User",
                "/images/default-avatar.png",
                "I'm new here!",
                LocalDateTime.now(),
                List.of()
        );
    }
    
    private void initializeSampleData() {
        UserProfile sampleUser = new UserProfile(
                "user123",
                "johndoe",
                "john.doe@example.com",
                "John Doe",
                "/images/john-avatar.png",
                "I love jazz and classic rock!",
                LocalDateTime.now().minusMonths(6),
                List.of("show1", "show3")
        );
        
        users.put("user123", sampleUser);
    }
}
