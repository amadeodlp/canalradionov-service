package com.amadeodlp.canalradionov.app.web.controller.user;

import com.amadeodlp.canalradionov.core.model.user.UserProfile;
import com.amadeodlp.canalradionov.core.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getUserProfile() {
        LOG.info("Getting user profile");
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfile> updateUserProfile(@RequestBody UserProfile userProfile) {
        LOG.info("Updating user profile");
        return ResponseEntity.ok(userService.updateUserProfile(userProfile));
    }
}
