package com.staffs.api.identity.infrastructure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AppUserRepository extends JpaRepository<AppUserJpa, String> {
    Optional<AppUserJpa> findByEmail(String email);
}

