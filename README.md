# Devnexus 2025: Bootiful Apps

Hi, Spring fans! in this installment we look at how to build production worthy Spring Boot apps in 2025!

You'll need `protoc` as well as `grpcurl` or equivalent to build and try out the spring grpc examples. you'll need maven. and java 23 or later, ideally graalvm. 

## direction..

- got the server, which will act as an oauth resource server
- time to secure it. Oauth to the rescue, or to the hindrance?
- except this the oauth client and spring auth server r going to require lots of config, which I don’t want to remember by heart
- so new spring cloud config server pointing to a git config repo full of config
- new spring auth server (pointing to config server, which conveniently has all the config)
- new oauth client (pointing to config server, which conveniently has all the config)   
- And, thank buddha, `grpcurl` supports sending headers. So to make the demo faster, I could write a quick endpoint in the client that leaks the token, and then run `grpcurl` with a header, and point it to the gateway (which is also the client).

```shell
grpcurl -H 'Authorization: Bearer token123' \
        -H 'x-custom-header: custom-value' \
        -plaintext \
        localhost:port \
        Adoptions/YourMethodName
```



* spring data jdbc
* modulith
* grpc
* graphql
* rest(-ish)
* config server
* auth server, one-time tokens, and passkeys
* resource server
* gateway && oauth client
* graalvm/virtual threads

curtsy.

