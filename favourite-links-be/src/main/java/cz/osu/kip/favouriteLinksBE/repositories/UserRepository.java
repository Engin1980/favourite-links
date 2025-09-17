package cz.osu.kip.favouriteLinksBE.repositories;

import cz.osu.kip.favouriteLinksBE.model.db.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
}
