package com.Alura.Foro_HUB.topic;

import com.Alura.Foro_HUB.topic.dto.TopicCreateDTO;
import com.Alura.Foro_HUB.topic.dto.TopicResponseDTO;
import com.Alura.Foro_HUB.topic.dto.TopicUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topics")
public class TopicController {

    private final TopicService service;
    public TopicController(TopicService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<TopicResponseDTO> create(@RequestBody @Valid TopicCreateDTO dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    @GetMapping
    public Page<TopicResponseDTO> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public TopicResponseDTO detail(@PathVariable Long id) {
        return service.detail(id);
    }

    @PutMapping("/{id}")
    public TopicResponseDTO update(@PathVariable Long id, @RequestBody TopicUpdateDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
