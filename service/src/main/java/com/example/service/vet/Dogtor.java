package com.example.service.vet;

import com.example.service.adoptions.DogAdoptionEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class Dogtor {

    @ApplicationModuleListener
    void checkup(DogAdoptionEvent dogAdoptionEvent) {
        System.out.println("adoption dog [" + dogAdoptionEvent + ']');
    }
}
