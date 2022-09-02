# Lean Rest 

This project provides a REST api for the [Lean engine](https://github.com/projectlean/lean-engine). 

## Steps to get going locally 

* build the lean engine
* run `mvn clean install jetty:run -DCONFIG_PATH=<PATH_TO_THIS_REPOSITORY>/src/main/resources/` to use the leanrest.properties file 

## Build and run the container 

* build the container image: `docker build .-t lean-rest` 
* run the container: `docker run -p 8080:808 -v <PATH_TO_LEAN_ENGINE_REPOSITORY>/src/test/resources/presentations:/reports lean-rest`

## Use the api 

After you've built the api and started locally or in the container, visit

* http://localhost:8080/lean/presentations to get a list of available presentations in JSON 
* http://localhost:8080/lean/presentations/<PRESENTATION_NAME> to get the SVG for a single presentation
