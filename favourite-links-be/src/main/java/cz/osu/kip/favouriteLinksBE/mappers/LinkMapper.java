package cz.osu.kip.favouriteLinksBE.mappers;

import cz.osu.kip.favouriteLinksBE.model.db.LinkEntity;
import cz.osu.kip.favouriteLinksBE.model.dto.LinkCreateDto;
import cz.osu.kip.favouriteLinksBE.model.dto.LinkDto;
import cz.osu.kip.favouriteLinksBE.model.dto.LinkUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LinkMapper {
    LinkEntity fromCreate(LinkCreateDto dto);
    LinkDto to(LinkEntity dto);
    @Mapping(target = "id", source = "id")
    LinkEntity fromUpdate(int id, LinkUpdateDto dto);
}
