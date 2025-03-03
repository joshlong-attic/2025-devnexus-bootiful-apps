package com.example.service.adoptions;

import org.jmolecules.event.annotation.Externalized;


@Externalized
public record DogAdoptionEvent (int dogId) {
}
