package cz.osu.kip.favouriteLinksBE.services;

import cz.osu.kip.favouriteLinksBE.model.db.LinkEntity;
import cz.osu.kip.favouriteLinksBE.repositories.LinkRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LinkService {
    private final LinkRepository linkRepository;


    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public List<LinkEntity> getAll(){
        return linkRepository.findAll();
    }
}
