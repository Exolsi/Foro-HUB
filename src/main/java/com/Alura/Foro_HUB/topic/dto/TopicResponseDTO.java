package com.Alura.Foro_HUB.topic.dto;

import java.time.LocalDateTime;

public record TopicResponseDTO(
        Long id, String title, String content, String author,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {}
