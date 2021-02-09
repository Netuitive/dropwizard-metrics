# Netuitive Dropwizard Integration
Dropwizard is part Java framework and part Java library that assists in operating web services. The Netuitive Dropwizard integration uses a [DropWizard.io](http://www.dropwizard.io/) `netuitive` reporter to send metrics to [Virtana Linux agent](https://docs.virtana.com/en/linux-agent.html).

## Using the DropWizard Integration

1. Include the appropriate [Ananke library dependency](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.netuitive%22%20AND%20a%3A%22ananke%22). You'll also need a working StatsD ([Virtana StatsD](https://docs.virtana.com/en/netuitive-statsd.html) or [Etsy StatsD](https://docs.virtana.com/en/etsy-statsd.html)) integration.
1. Add `dropwizard-metrics` to your POM. There's also additional dependency formats for other build managers [here](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.netuitive%22%20AND%20a%3A%22dropwizard-metrics%22).

        <dependency>
          <groupId>com.netuitive</groupId>
          <artifactId>dropwizard-metrics</artifactId>
          <version>1.0.0</version>
          <type>pom</type>
        </dependency>

1. Add the following to your Dropwizard `config.yml` file:

        metrics:
          frequency: 1 minute
          reporters:
              - type: netuitive
                host: netuitive-agent
                port: 8125

## Testing with Docker Stack

| Directory                                   | Purpose                                                           |
|:--------------------------------------------|:------------------------------------------------------------------|
| `src/test/docker`                           | Root directory for this test suite.                               |
| `src/test/docker/docker-dropwizard-example` | Dropwizard example application for testing.                       |
| `src/test/docker/docker-compose.yml`        | `docker-compose` file used to define and launch test environment. |

### Runtime Environment Used for Testing

* Docker version 1.10.3, build 20f81dd
* `docker-compose` version 1.6.2, build 4d72027
* VirtualBox Version 5.0.16 r105871

### Docker Stack Defines Two Containers:
* `dropwizard`
    * Example Dropwizard application with `dropwizard-metrics-netuitive` reporter (requires local gradle install to work).
    * Updated `config.yml` file to enable `netuitive` reporter.
* `netuitive-agent`
    * Docker Linux agent to ship data to Netuitive. **Note:** some environment variables need to be updated to configure the agent.

![Alt](/diagram.png "containers")

## Starting the Example Docker Environment

1. Run the following to start the example application and Netuitive Docker agent:

        docker-compose up -d

Docker will build the `dropwizard-metrics` and example project within the container from source and run the Netuitive Docker agent alongside it.

### Running the tests
1. Change to the test docker directory, which contains a test stack definition for testing.

        cd src/test/docker;

1. In that directory run:

        docker-compose up -d --build

1. Now create and run your tests:

        ./run_tests.sh

1. Now shut down the stack:

        docker-compose stop
