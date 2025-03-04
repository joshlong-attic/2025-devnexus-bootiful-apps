# Script

* spring petclinic?
* meet peanut and prancer
* let's build a service. spring is amazing at this sort of thing. i was just at confoo conference in magnificent Montreal, Canada. Fun fact, the conference _used_ to be all about PHP. I freebased PHP once! It sucked! It was super stressful, having the ability to express types that guaranteed me absolutely nothing in performance optimizations or type safety. 
* my hairline receded TEN INCHES! never doing that again! 
* start.spring.io (`service`): `config client`, `data jdbc`, `graalvm`, `web`, `modulith`, `grpc`, `postgres`, `graphql`, `resource server`, `devtools`
  * here's the full [share-able link](https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.4.4-SNAPSHOT&packaging=jar&jvmVersion=23&groupId=com.example&artifactId=service&name=service&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.service&dependencies=web,postgresql,modulith,oauth2-resource-server,data-jdbc,native,spring-grpc,graphql,cloud-config-client,devtools)
* manually add:

```xml
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-servlet-jakarta</artifactId>
</dependency>
```

_Or_, if `0.4.0` is out, just add:

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
* k, well, what about graphql? it's schema-first, as well. so let's define the schema in `src/main/resources/graphql/adoptions.graphqls`:

```graphql
type Dog {
    id: Int
    name: String
    owner: String
    description: String
}

type Mutation {
    adopt(dogId:Int, name:String): Boolean
}

type Query {
    all: [Dog]
}
```

* now let's define the controller for the graphql endpoint:

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

* make sure to define `spring.graphql.graphiql.enabled` == `true`.
* goto `localhost:8080/graphiql`
* k, what bout grpc? it's schema-first, so let's define the schema in `src/main/proto` (_not_ `src/main/resources/proto`!)

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


* well, we've handled the api aspect. we've adopted Prancer. but how did we find him? for that, join Dr. Mark Pollack and me in an hour or two as we look at the amazing Spring AI project