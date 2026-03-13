package dev.resource_server.domain.user.repository;

import dev.resource_server.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByAuthId(Long authId);

    boolean existsByAuthId(Long authId);

    Optional<Object> findByid(Long id);
}
