# [DropWizard.io](http://www.dropwizard.io/) `netuitive` reporter for sending metrics to Netuitive's Linux agent. 

## add `dropwizard-metrics` to your pom
```
<dependency>
  <groupId>io.dropwizard.metrics</groupId>
  <artifactId>metrics-netuitive</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

## add to your dropwizard.io config.yml
```
metrics:
  frequency: 1 minute
  reporters:
      - type: netuitive
        host: netuitive-agent
        port: 8125
```

## Testing w/Docker stack

| dir                                       | purpose                                                        |
|:------------------------------------------|:---------------------------------------------------------------|
| src/test/docker                           | root directory for this test suite                             |
| src/test/docker/docker-dropwizard-example | dropwizard example  application for testing                    |
| src/test/docker/docker-compose.yml        | docker-compose file used to define and launch test environment |

### runtime environment used for testing

* Docker version 1.10.3, build 20f81dd
* docker-compose version 1.6.2, build 4d72027
* VirtualBox Version 5.0.16 r105871

### Docker stack defines two containers: 
* dropwizard:
    * example dropwizard app w/dropwizard-metrics-netuitive reporter (requires local gradle install to work) 
    * updated config.yml file to enable netuitive reporter
* netuitive-agent
    * docker linux agent to ship data to netuitive
    * note: some environment variables need to be updated to configure the agent

![Alt](/diagram.png "containers")

### Running the tests
```
cd src/test/docker;
```
This directory contains a `docker-compose.yml` file which contains a test stack definition for testing.
In that directory run: 

```
docker-compose up -d --build
```
Now create and run your tests

`./run_tests.sh`

Now shut down stack 

```
docker-compose stop
```

