package com.example.service.adoptions;

import com.example.service.adoptions.grpc.AdoptionRequest;
import com.example.service.adoptions.grpc.AdoptionsGrpc;
import com.example.service.adoptions.grpc.DogsResponse;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collection;

// grpcurl -plaintext -d '{}' localhost:8080  Adoptions.All
// grpcurl -plaintext -d '{"id":"45","name":"cbono"}' localhost:8080  Adoptions.Adopt
@GrpcService
class DogAdoptionGrpcService extends AdoptionsGrpc.AdoptionsImplBase {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionGrpcService(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @Override
    public void adopt(AdoptionRequest request, StreamObserver<Empty> responseObserver) {
        this.dogAdoptionService.adopt(request.getId(), request.getName());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void all(Empty request, StreamObserver<DogsResponse> responseObserver) {
        var grpcDogs = this.dogAdoptionService
                .all()
                .stream()
                .map(d -> com.example.service.adoptions.grpc.Dog.newBuilder()
                        .setId(d.id())
                        .setName(d.name())
                        .build())
                .toList();

        var dr = DogsResponse.newBuilder();
        dr.addAllDogs(grpcDogs);
        responseObserver.onNext(dr.build());
        responseObserver.onCompleted();
    }
}

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
    boolean adopt(@Argument int dogId, @Argument String name) {
        this.dogAdoptionService.adopt(dogId, name);
        return true;
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


@Configuration
@ImportRuntimeHints(HintsConfiguration.Hints.class)
class HintsConfiguration {

    static class Hints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            var mcs = MemberCategory.values();
            for (var c : new Class<?>[]{Instant.class})
                hints.reflection().registerType(c, mcs);
        }
    }

}