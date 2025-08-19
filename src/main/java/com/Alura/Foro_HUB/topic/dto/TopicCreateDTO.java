package com.Alura.Foro_HUB.topic.dto;

import jakarta.validation.constraints.NotBlank;

public record TopicCreateDTO(@NotBlank String title, @NotBlank String content) {}
