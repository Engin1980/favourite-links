package cz.osu.kip.favouriteLinksBE.model.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @lombok.NonNull
    @Column(unique = true, nullable = false)
    private String email;

    @lombok.NonNull
    @Column(unique = true, nullable = false)
    private String keycloakId;
}
