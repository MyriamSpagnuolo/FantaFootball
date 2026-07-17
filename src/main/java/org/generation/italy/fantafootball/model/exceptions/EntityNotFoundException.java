package org.generation.italy.fantafootball.model.exceptions;

public class EntityNotFoundException extends  Exception{
    public EntityNotFoundException(String className, long id) {
        super(String.format("La entity con id %d non e' stata trovata", id));
    }
}
