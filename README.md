# Devnexus 2025 Bootiful Apps

Hi, Spring fans! in this installment we look at how to build production worthy Spring Boot apps in 2025!

I want to show:

* modulith 
* gRPC
* GraphQL
* security
* HTMX
* production worthiness

## requirements

you'll need Java 23 or later

you'll need `protoc` as well as `grpcurl` or equivalent to build and try out the spring grpc examples.

## direction..

- got the server, which will act as an oauth resource server
- time to secure it. Oauth to the rescue, or to the hindrance?
- except this the oauth client and spring auth server r going to require lots of config, which I don’t want to remember by heart
- so new spring cloud config server pointing to a git config repo full of config
- new spring auth server (pointing to config server, which conveniently has all the config)
- new oauth client (pointing to config server, which conveniently has all the config)   
- And, thank buddha, `grpcurl` supports sending headers. So to make the demo faster, I could write a quick endpoint in the client that leaks the token then run `grpcurl` with a header, and point it to the gateway (which is also the client).

when it comes to security, i specifically want to enable the passkeys and one time token support on the auth server. 

so the beats would be:

* spring data jdbc
* modulith
* grpc
* graphql
* rest(-ish)
* config server
* auth server and passkeys
* resource server
* gateway && oauth client
* graalvm/virtual threads

curtsy.

