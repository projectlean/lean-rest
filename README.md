# Lean Rest 

This project provides a REST api for the [Lean engine](https://github.com/projectlean/lean-engine). 

## Steps to get going locally 

* build the lean engine
* run `mvn clean install jetty:run -DCONFIG_PATH=<PATH_TO_THIS_REPOSITORY>/src/test/resources/` to use the leanrest.properties file
* For example: `mvn clean install jetty:run -DCONFIG_PATH=src/test/resources/`
* To debug set variable `MAVEN_OPTS` to `-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005` and then attach your debugger to this port.

## Build and run the container (experimental)

* build the container image: `docker build . -t lean-rest` 
* run the container: `docker run -p 8080:8080 -v <PATH_TO_THIS_REPOSITORY>/src/test/resources/:/lean lean-rest`
* For example:

```
docker run -p 8080:8080 -v `(pwd)`/src/test/resources/:/lean/ lean
```


## Use the REST API 

After you've built the API and started locally or in the container, visit

* http://localhost:8080/lean/render/main to get to a test presentation. 

### Metadata

| Service                         | Type | Description                                                 |
|:--------------------------------|:----:|:------------------------------------------------------------|
| `/lean/metadata/types`          | GET  | List the types of metadata available.                       |
| `/lean/metadata/list/<key>/`    | GET  | List the element names of the specified type key.           |
| `/lean/metadata/<key>/<name>/`  | GET  | Get the element with the given type key and name.           |
| `/lean/metadata/<key>/`         | POST | Save the element with the given type key and name.          |
| `/lean/metadata/presentations/` | GET  | List the high level details of the available presentations. |

### Rendering

| Service                                      | Type | Description                                                   |
|:---------------------------------------------|:----:|:--------------------------------------------------------------|
| `/lean/render/main`                          | GET  | Renders and displays the main presentation in HTML.           |
| `/lean/render/presentation/<name>/`          | GET  | Renders a presentation. Returns the ID of the rendering.      |
| `/lean/render/info/pages/<renderId>/`        | GET  | Get the number of pages for the given presentation rendering. |
| `/lean/render/page/<renderId>/<type>/<page>` | GET  | Get the page of a rendering for types `SVG` or `HTML`.        |
| `/lean/render/lookupActions/`                | POST | Returns possible actions for an ActionsRequest (1).           |

(1) An ActionsRequest looks like this:

```
{
  "renderId"   : "811bedf3-8836-44dd-894e-7290850c52a7",
  "pageNumber" : 0,
  "x"          : 123,
  "y"          : 456
}
```
