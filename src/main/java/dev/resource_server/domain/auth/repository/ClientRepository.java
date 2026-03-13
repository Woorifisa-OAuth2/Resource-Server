package dev.resource_server.domain.auth.repository;

import dev.resource_server.domain.auth.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findTopByOrderByIdDesc();

    boolean existsByClientName(String clientName);
}