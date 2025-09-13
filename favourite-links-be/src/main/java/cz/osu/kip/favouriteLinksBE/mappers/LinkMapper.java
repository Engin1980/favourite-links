package cz.osu.kip.favouriteLinksBE.mappers;

import cz.osu.kip.favouriteLinksBE.model.db.LinkEntity;
import cz.osu.kip.favouriteLinksBE.model.dto.LinkCreateDto;
import cz.osu.kip.favouriteLinksBE.model.dto.LinkDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LinkMapper {
    LinkEntity fromCreate(LinkCreateDto dto);
    LinkDto to(LinkEntity dto);
}
