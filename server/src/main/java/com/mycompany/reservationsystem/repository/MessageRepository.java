package com.mycompany.reservationsystem.repository;

import com.mycompany.reservationsystem.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Optional: custom query methods

    // Find messages by label containing keyword
    List<Message> findByMessageLabelContainingIgnoreCase(String keyword);
    Optional<Message> findByMessageLabelIgnoreCase(String messageLabel);


    // Find messages by details containing keyword
    List<Message> findByMessageDetailsContainingIgnoreCase(String keyword);
}