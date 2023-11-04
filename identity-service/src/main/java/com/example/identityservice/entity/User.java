package com.example.identityservice.entity;

import com.example.identityservice.validation.annotations.ValidEmail;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "userName"),
                @UniqueConstraint(columnNames = "email")},
        indexes = {
                @Index(name = "user_id_idx", columnList = "id", unique = true)
        })
public class User extends BaseEntity {
    @Column(nullable = false)
    private String badgeId;

    @Column(nullable = false)
    @NotBlank
    @Size(max = 20)
    private String userName;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    @NotBlank
    @Size(max = 20)
    private String firstName;

    @Column(nullable = false)
    @NotBlank
    @Size(max = 20)
    private String lastName;

    @Column(nullable = false)
    @NotBlank
    @Size(max = 50)
    @ValidEmail
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    private boolean enabled;
}
