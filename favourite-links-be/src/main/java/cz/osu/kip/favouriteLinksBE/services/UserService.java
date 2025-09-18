package cz.osu.kip.favouriteLinksBE.services;

import cz.osu.kip.favouriteLinksBE.exceptions.EntityAlreadyExistsException;
import cz.osu.kip.favouriteLinksBE.exceptions.EntityNotFoundException;
import cz.osu.kip.favouriteLinksBE.exceptions.ServiceException;
import cz.osu.kip.favouriteLinksBE.model.db.UserEntity;
import cz.osu.kip.favouriteLinksBE.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public void createUser(UserEntity user) {
    ensureNew(user);
    userRepository.save(user);
  }

  private void ensureNew(UserEntity user) {
    if (user.getId() != null)
      throw new EntityAlreadyExistsException(UserEntity.class, user.getId());
  }

  public Optional<UserEntity> getUserById(int id) {
    Optional<UserEntity> ret = userRepository.findById(id);
    return ret;
  }

  public void deleteUser(int id) {
    try {
      userRepository.deleteById(id);
    } catch (Exception e) {
      throw new ServiceException(this, "Failed to delete user with id " + id, e);
    }
  }

  public Optional<UserEntity> getUserByEmail(String email) {
    Optional<UserEntity> ret = userRepository.findByEmail(email);
    return ret;
  }

  public List<UserEntity> getAllUsers() {
    return userRepository.findAll();
  }
}
