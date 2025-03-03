package com.example.service.adoptions;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
class DogAdoptionController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    // http --form POST http://localhost:8080/dogs/45/adoptions name=jlong
    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String name) {
        this.dogAdoptionService.adopt(dogId, name);
    }

}

@Service
@Transactional
class DogAdoptionService {

    private final DogRepository dogRepository;

    private final ApplicationEventPublisher publisher;

    DogAdoptionService(DogRepository dogRepository, ApplicationEventPublisher publisher) {
        this.dogRepository = dogRepository;
        this.publisher = publisher;
    }

    void adopt(int id, String owner) {
        this.dogRepository.findById(id).ifPresent(dog -> {
            var updated = this.dogRepository.save(new Dog(dog.id(), dog.name(), owner, dog.description()));
            this.publisher.publishEvent(new DogAdoptionEvent(updated.id()));
            System.out.println("adopted [" + updated + "]");
        });
    }

}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String owner, String description) {
}


