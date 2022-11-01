package com.example.oauthjwt.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Member {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Getter
    @RequiredArgsConstructor
    public enum Role {

        USER("ROLE_USER"), ADMIN("ROLE_ADMIN");

        private final String key;

    }
}


