package cz.osu.kip.favouriteLinksBE.exceptions;

import lombok.Getter;

@Getter
public class EntityAlreadyExistsException extends RuntimeException {
    private final Class<?> entityClass;
    private final String identifier;


    public EntityAlreadyExistsException(Class<?> entityClass, String identifier) {
        super("Entity " + entityClass.getSimpleName() + " with identifier " + identifier + " not found.");
        this.entityClass = entityClass;
        this.identifier = identifier;
    }

    public EntityAlreadyExistsException(Class<?> entityClass, int id) {
        this(entityClass, "id == " + id);
    }
}
