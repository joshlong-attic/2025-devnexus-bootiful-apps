package com.example.service.adoptions;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;


@Controller
class DogGraphQlController {

    private final DogAdoptionService dogAdoptionService;

    DogGraphQlController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @QueryMapping
    Collection<Dog> all() {
        return this.dogAdoptionService.all();
    }

    @MutationMapping
    void adopt(@Argument int dogId, @Argument String name) {
        this.dogAdoptionService.adopt(dogId, name);
    }
}

@Controller
@ResponseBody
class DogAdoptionApiController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionApiController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @GetMapping("/dogs")
    Collection<Dog> allDogs() {
        return this.dogAdoptionService.all();
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

    Collection<Dog> all() {
        return this.dogRepository.findAll();
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


