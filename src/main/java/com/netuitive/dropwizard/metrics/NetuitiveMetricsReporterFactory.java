package com.netuitive.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.dropwizard.metrics.BaseReporterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 *
 * Created by bspindler on 6/12/16.
 */
@JsonTypeName("statsd")
public class NetuitiveMetricsReporterFactory extends BaseReporterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NetuitiveMetricsReporterFactory.class);

    @NotNull
    private String host;

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    @NotNull
    private int port;

    @JsonProperty
    public int getPort() { return port; }

    @JsonProperty
    public void setPort(int port) { this.port = port; }

    /**
     * Configures and builds a {@link ScheduledReporter} instance for the given registry.
     *
     * @param registry the metrics registry to report metrics from.
     * @return a reporter configured for the given metrics registry.
     */
    @Override
    public ScheduledReporter build(MetricRegistry registry) {

        LOG.info("Building NetuitiveMetricsReporter for host: {} and port: {}.", getHost(), getPort());

        NetuitiveMetricsReporter.Builder builder = NetuitiveMetricsReporter.forRegistry(registry)
                                                   .convertDurationsTo(getDurationUnit())
                                                   .convertRatesTo(getRateUnit())
                                                   .filter(getFilter());

        return builder.build(getHost(), getPort());
    }
}
