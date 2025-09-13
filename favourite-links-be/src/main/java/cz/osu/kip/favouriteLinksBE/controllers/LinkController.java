package cz.osu.kip.favouriteLinksBE.controllers;

import cz.osu.kip.favouriteLinksBE.mappers.LinkMapper;
import cz.osu.kip.favouriteLinksBE.model.db.LinkEntity;
import cz.osu.kip.favouriteLinksBE.model.dto.LinkCreateDto;
import cz.osu.kip.favouriteLinksBE.model.dto.LinkDto;
import cz.osu.kip.favouriteLinksBE.services.LinkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/links")
public class LinkController {

    private final LinkService linkService;
    private final LinkMapper linkMapper;

    public LinkController(LinkService linkService, LinkMapper linkMapper) {
        this.linkService = linkService;
        this.linkMapper = linkMapper;
    }

    @GetMapping
    public List<LinkDto> getAll() {
        var links = linkService.getAll();
        var dtos = links.stream().map(linkMapper::to).toList();
        return dtos;
    }

    @PostMapping
    public LinkDto create(@RequestBody LinkCreateDto dto) {
        LinkEntity entity = linkMapper.fromCreate(dto);
        LinkEntity created = linkService.create(entity);
        System.out.println(created.getId());
        LinkDto result = linkMapper.to(created);
        return result;
    }
}
