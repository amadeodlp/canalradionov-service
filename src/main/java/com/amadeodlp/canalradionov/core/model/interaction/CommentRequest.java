package com.amadeodlp.canalradionov.core.model.interaction;

public record CommentRequest(
    String targetId,
    String targetType,
    String content,
    String replyTo
) {}
