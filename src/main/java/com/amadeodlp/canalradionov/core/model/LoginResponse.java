package com.amadeodlp.canalradionov.core.model;

public record LoginResponse(
    String token,
    SessionResponse session
) {}
