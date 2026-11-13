package de.fhdortmund.bottledrop.usermanagementservice.repository;

import de.fhdortmund.bottledrop.usermanagementservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA automatically writes the SQL for this behind the scenes!
    Optional<User> findByEmail(String email);
}