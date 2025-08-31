package com.staffs.api.identity.infrastructure;

import com.staffs.api.common.security.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="app_user")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppUserJpa {
    @Id private String id;
    @Column(nullable=false, unique=true) private String email;
    @Column(nullable=false) private String passwordHash; // bcrypt
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role;
    private String department;
    private String managerId; // who manages this user (null for admins)
}
