package com.example.identityservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.Calendar;
import java.util.Date;

@Data
@Entity()
@NoArgsConstructor
@Table(name = "PasswordResetToken", indexes = {
        @Index(name = "password_reset_token_id", columnList = "id", unique = true),
        @Index(name = "user_id_idx", columnList = "user_id")
})
public class PasswordResetToken {
    private static final int EXPIRATION = 60 * 24; /* Last only 24h */

    @Id
    @UuidGenerator
    private String id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Date expiryDate;

    public PasswordResetToken(final String token, final User user) {
        super();
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate();
    }

    private Date calculateExpiryDate() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, PasswordResetToken.EXPIRATION);
        return new Date(cal.getTime().getTime());
    }

    public void updateToken(final String token) {
        this.token = token;
        this.expiryDate = calculateExpiryDate();
    }
}
