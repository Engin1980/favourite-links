package cz.osu.kip.favouriteLinksBE.model.db;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "links")
public class LinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @lombok.NonNull
    @Column(nullable = false)
    private String url;

    @lombok.NonNull
    @Column(nullable = false)
    private String title;
}
