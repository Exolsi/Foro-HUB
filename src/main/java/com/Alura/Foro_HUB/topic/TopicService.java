package com.Alura.Foro_HUB.topic;

import com.Alura.Foro_HUB.topic.dto.TopicCreateDTO;
import com.Alura.Foro_HUB.topic.dto.TopicResponseDTO;
import com.Alura.Foro_HUB.topic.dto.TopicUpdateDTO;
import com.Alura.Foro_HUB.user.User;
import com.Alura.Foro_HUB.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TopicService {

    private final TopicRepository topics;
    private final UserRepository users;

    public TopicService(TopicRepository topics, UserRepository users) {
        this.topics = topics; this.users = users;
    }

    public TopicResponseDTO create(TopicCreateDTO dto) {
        User current = getCurrentUser();
        Topic t = topics.save(new Topic(dto.title(), dto.content(), current));
        return map(t);
    }

    public Page<TopicResponseDTO> list(Pageable pageable) {
        return topics.findAll(pageable).map(this::map);
    }

    public TopicResponseDTO detail(Long id) {
        Topic t = topics.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic no encontrado: " + id));
        return map(t);
    }

    public TopicResponseDTO update(Long id, TopicUpdateDTO dto) {
        Topic t = topics.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic no encontrado: " + id));
        ensureOwnerOrAdmin(t);
        t.update(dto.title(), dto.content());
        return map(topics.save(t));
    }

    public void delete(Long id) {
        Topic t = topics.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic no encontrado: " + id));
        ensureOwnerOrAdmin(t);
        topics.delete(t);
    }

    // --- helpers ---
    private TopicResponseDTO map(Topic t) {
        return new TopicResponseDTO(
                t.getId(), t.getTitle(), t.getContent(),
                t.getAuthor().getUsername(), t.getCreatedAt(), t.getUpdatedAt()
        );
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return users.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));
    }

    private void ensureOwnerOrAdmin(Topic t) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !t.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("No tienes permisos para modificar/eliminar este tópico");
        }
    }
}
