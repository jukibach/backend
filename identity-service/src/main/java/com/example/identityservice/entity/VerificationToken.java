package com.example.identityservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Data
@Entity()
@NoArgsConstructor
@Table(name = "VerificationToken", indexes = {
        @Index(name = "verification_token_id", columnList = "id", unique = true),
        @Index(name = "user_id_idx", columnList = "user_id")
})
public class VerificationToken {
    private static final int EXPIRATION = 60 * 24; /* Last only 24h */

    @Id
    @UuidGenerator
    private String id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Date expiryDate;

    public VerificationToken(final String token, final User user) {
        super();
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate();
    }

    private Date calculateExpiryDate() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, VerificationToken.EXPIRATION);
        return new Date(cal.getTime().getTime());
    }

    public void updateToken(final String token) {
        this.token = token;
        this.expiryDate = calculateExpiryDate();
    }

}
