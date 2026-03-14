package com.potatoes.Naengu.post.dto;

public record FeedQueryRequest(
        int size,
        String cursorCreatedAt,
        Long cursorId,
        String sort
) {}
