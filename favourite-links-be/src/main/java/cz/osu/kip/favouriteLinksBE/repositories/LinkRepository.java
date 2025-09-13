package cz.osu.kip.favouriteLinksBE.repositories;

import cz.osu.kip.favouriteLinksBE.model.db.LinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends JpaRepository<LinkEntity, Integer> {
}
