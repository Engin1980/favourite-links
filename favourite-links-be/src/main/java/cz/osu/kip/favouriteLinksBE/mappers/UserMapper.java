package cz.osu.kip.favouriteLinksBE.mappers;

import cz.osu.kip.favouriteLinksBE.model.db.UserEntity;
import cz.osu.kip.favouriteLinksBE.model.dto.UserCreateDto;
import cz.osu.kip.favouriteLinksBE.model.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    public UserEntity fromCreate(UserCreateDto dto);
    UserDto to(UserEntity user);
}
