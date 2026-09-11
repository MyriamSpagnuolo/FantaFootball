package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.springframework.data.domain.PageRequest;

final class PlayerPagination {
    private PlayerPagination() {
    }

    static PageRequest of(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("INVALID_PAGE", "La pagina deve essere maggiore o uguale a zero");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("INVALID_PAGE_SIZE", "La dimensione della pagina deve essere tra 1 e 100");
        }
        if ((long) page * size > Integer.MAX_VALUE) {
            throw new BadRequestException("INVALID_PAGE", "La pagina richiesta supera il limite supportato");
        }
        // Le query definiscono l'ordine per ruolo, cognome, nome e ID prima della paginazione.
        return PageRequest.of(page, size);
    }
}
