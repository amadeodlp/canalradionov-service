package com.amadeodlp.canalradionov.core.services.user;

import com.amadeodlp.canalradionov.core.model.user.UserProfile;

public interface UserService {
    UserProfile getCurrentUserProfile();
    
    UserProfile updateUserProfile(UserProfile userProfile);
}
