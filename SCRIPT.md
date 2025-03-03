# Script

* spring petclinic
* peanut, prancer
* start.spring.io (`service`): `config client`, `data jdbc`, `graalvm`, `web`, `modulith`, `grpc`, `postgres`, `graphql`, `resource server`, `devtools`
* manually add:

```xml
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-servlet-jakarta</artifactId>
</dependency>
```

_or_, if `0.4.0` is out, just add:

```xml
<dependency>
    <groupId>org.springframework.grpc</groupId>
    <artifactId>spring-grpc-server-web-spring-boot-starter</artifactId>
</dependency>
```

* comment out `resource server` and `config client`
* data oriented programming
* data jdbc
* `adoptions` package 
* build `DogAdoptionService`
* `vet` package
* build `Dogtor`
* "what do you mean by event driven?"
* decompose with events
* http controller

```java
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
```

* test: ` http --form POST http://localhost:8080/dogs/45/adoptions name=jlong `
* nice, but that's old hat. 
* k, what bout graphql? it's schema-first, so let's define the schema in `src/main/proto` (_not_ `src/main/resources/proto`!)

```protobuf
syntax = "proto3";

import "google/protobuf/empty.proto";
option java_multiple_files = true;
option java_package = "com.example.service.adoptions.grpc";
option java_outer_classname = "AdoptionsProto";

service Adoptions {
  rpc Adopt (AdoptionRequest) returns (google.protobuf.Empty) {}
  rpc All (google.protobuf.Empty) returns (DogsResponse) {}
}

message AdoptionRequest {
  int32 id = 1;
  string name = 2;
}

message Dog {
  int32  id = 1;
  string  name = 2;
  string  owner = 3;
  string  description = 4;
}

message DogsResponse {
  repeated Dog dogs = 1;
}

```


```java

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


```