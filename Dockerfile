FROM java:8-jdk

RUN mkdir -p /opt/app/
WORKDIR /opt/app/

ADD gradle* /opt/app/
ADD gradle/ /opt/app/gradle/
ADD *.gradle /opt/app/

RUN /bin/bash gradlew compileJava

ADD . /opt/app/

RUN /bin/bash gradlew install

WORKDIR /opt/app/src/test/docker/docker-dropwizard-example/

RUN /bin/bash /opt/app/gradlew oneJar

RUN mkdir -p /opt/dropwizard/
RUN mv build/libs/docker-dropwizard-example-standalone.jar /opt/dropwizard/
RUN mv config.yml /opt/dropwizard/

WORKDIR /opt/dropwizard/

EXPOSE 8081

CMD ["java", "-jar", "-Done-jar.silent=true", "docker-dropwizard-example-standalone.jar", "server", "config.yml"]