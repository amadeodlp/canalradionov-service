package com.amadeodlp.canalradionov.core.auth;

/**
 * Defines user permission scopes for the application.
 * Each scope represents a permission to perform a specific operation on a resource.
 */
public enum UserScope {
    // User management scopes
    USER_READ(ScopeOperation.READ),
    USER_WRITE(ScopeOperation.WRITE),
    USER_ADMIN(ScopeOperation.ADMIN),
    
    // Radio station scopes
    RADIO_READ(ScopeOperation.READ),
    RADIO_WRITE(ScopeOperation.WRITE),
    RADIO_ADMIN(ScopeOperation.ADMIN),
    
    // Playlist scopes
    PLAYLIST_READ(ScopeOperation.READ),
    PLAYLIST_WRITE(ScopeOperation.WRITE),
    PLAYLIST_ADMIN(ScopeOperation.ADMIN),
    
    // Music track scopes
    TRACK_READ(ScopeOperation.READ),
    TRACK_WRITE(ScopeOperation.WRITE),
    TRACK_ADMIN(ScopeOperation.ADMIN),
    
    // Streaming scopes
    STREAM_ACCESS(ScopeOperation.READ),
    STREAM_CONTROL(ScopeOperation.WRITE),
    STREAM_ADMIN(ScopeOperation.ADMIN);
    
    private final ScopeOperation operation;
    
    UserScope(ScopeOperation operation) {
        this.operation = operation;
    }
    
    public ScopeOperation getOperation() {
        return operation;
    }
}
