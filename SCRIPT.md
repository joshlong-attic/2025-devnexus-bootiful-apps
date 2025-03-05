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

## http
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

## event based decomposition  
* `vet` package
* build `Dogtor`
* "what do you mean by event driven?"
* decompose with events

## graphql 
* nice, but that's old hat.
* k, well, what about GraphQL? it's schema-first, as well. so let's define the schema in `src/main/resources/graphql/adoptions.graphqls`:

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

* make sure to define `spring.graphql.graphiql.enabled` == `true` to use the graphiql console to try some stuff out.
* go to `localhost:8080/graphiql`

## grpc
* timing: 7 mins
* k, what bout gRPC? it's _also_ schema-first, so let's define the schema in `src/main/proto` (_not_ `src/main/resources/proto`!)

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
* run the Maven build to get it to generate the code. Make sure to add the two `protobuf/{grpc-java,java}` folders as `Source Roots`.

* now let's define the gRPC service: 

```java

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


```

* well, we've handled the api aspect. we've got HTTP, GraphQL, and even gRPC! we've adopted Prancer. but how did we find him? for that, join Dr. Mark Pollack and me in an hour or two as we look at the amazing Spring AI project
* we've got a working service. but what good would a service be without a client?
* we can build a simple client that could conceivably be exposed to the outside world, offering up our static html pages, and forwarding requests to our backend services. 
* but what about security?
* we're going to use Oauth. but i don't want to redundantly muddy each service i build with the concerns of Security. instead we'll centralize all of that in the Spring Authorization Server.
* let's stand one up. 
* start.spring.io (`auth`): `Config Client`, `Web`, `Authorization Server`, `Devtools`

```java
package com.example.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder pw) {
        var users = List.of(
                User.withUsername("jlong").password(pw.encode("pw")).roles("USER").build(),
                User.withUsername("rwinch").password(pw.encode("pw")).roles("USER", "ADMIN").build()
        );
        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .with(authorizationServer(), as -> as.oidc(Customizer.withDefaults()))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oneTimeTokenLogin(configurer -> configurer.tokenGenerationSuccessHandler((request, response, oneTimeToken) -> {
                    var msg = "go to http://localhost:9090/login/ott?token=" + oneTimeToken.getTokenValue();
                    System.out.println(msg);
                    response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                    response.getWriter().print("you've got console mail!");
                }))
                .webAuthn(c -> c
                        .rpId("localhost")
                        .rpName("bootiful passkeys")
                        .allowedOrigins("http://localhost:9090")
                )
                .formLogin(Customizer.withDefaults())
                .build();
    }
}


```

* it'll require some configuration. and indeed a lot of our services require configuration. so let's centralize that, as well, using the spring cloud config server.
* start.spring.io (`config`): `Config Server`, `Devtools`, `Web`
* specify the git.uri at `$HOME/Desktop/config/`; `server.port = 8888`; add `@EnableConfigServer` 
* 