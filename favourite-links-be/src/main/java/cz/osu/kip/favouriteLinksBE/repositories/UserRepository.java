package cz.osu.kip.favouriteLinksBE.repositories;

import cz.osu.kip.favouriteLinksBE.model.db.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
  Optional<UserEntity> findById(int id);
  Optional<UserEntity> findByEmail(String email);
}
