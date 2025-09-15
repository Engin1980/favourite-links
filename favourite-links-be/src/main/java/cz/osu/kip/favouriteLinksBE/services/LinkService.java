package cz.osu.kip.favouriteLinksBE.services;

import cz.osu.kip.favouriteLinksBE.exceptions.EntityAlreadyExistsException;
import cz.osu.kip.favouriteLinksBE.exceptions.EntityNotFoundException;
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

    public List<LinkEntity> getAll() {
        return linkRepository.findAll();
    }

    public LinkEntity create(LinkEntity entity) {
        ensureNew(entity);
        entity = this.linkRepository.save(entity);
        return entity;
    }

    public LinkEntity update(LinkEntity entity) {
        ensureExists(entity);
        entity = this.linkRepository.save(entity);
        return entity;
    }

    private void ensureExists(LinkEntity entity) {
        if (!this.linkRepository.existsById(entity.getId())) {
            throw new EntityNotFoundException(LinkEntity.class, entity.getId());
        }
    }

    private void ensureNew(LinkEntity entity) {
        if (entity.getId() != null) {
            throw new EntityAlreadyExistsException(LinkEntity.class, entity.getId());
        }
    }
}
