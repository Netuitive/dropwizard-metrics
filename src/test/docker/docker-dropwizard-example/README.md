# Docker Dropwizard image

Example Docker image for running a Dropwizard Application in a container.

Requires:
* [Docker](https://www.docker.com/)
* [Boot2Docker](http://boot2docker.io/)
* JDK (to compile java file locally)
* [Gradle](https://gradle.org/) (for build automation)

To run locally:

```
gradle run
# ./go
```

To build docker image:

```
gradle dockerBuildImage
# ./dockerBuildImage.sh (requires oneJar task to build dropwizard application)
```

To run docker image:

```
gradle dockerRunImage
./dockerRunImage.sh (requires built image)
```

When image is running use `boot2docker ip` to get the docker IP and `docker ps` to see the port assigned to the container port 8080, then curl `http://<dockerip>:<port>/hello` to call the dropwizard application running in the container.

If using LINUX you can use localhost and have to `sudo` docker commands.

## Details

This is a bare bones example for building an image for running a single Dropwizard application. It uses the standard docker java:jre-8 image as base, copies necessary files into image into folder `/opt/dropwizard` and runs command to start dropwizard application.

Dockerfile:

```
FROM java:8-jre
COPY config.yml /opt/dropwizard/
COPY build/libs/docker-dropwizard-application-standalone.jar /opt/dropwizard/
EXPOSE 8081
WORKDIR /opt/dropwizard
CMD ["java", "-jar", "-Done-jar.silent=true", "docker-dropwizard-application-standalone.jar", "server", "config.yml"]
```