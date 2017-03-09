package com.netuitive.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.dropwizard.metrics.BaseReporterFactory;
import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

@JsonTypeName("netuitive-rest")
public class NetuitiveMetricsCloudReporterFactory extends BaseReporterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NetuitiveMetricsCloudReporterFactory.class);

    @NotNull
    @Getter @Setter
    @JsonProperty
   private String apiKey;

    @NotNull
    @Getter @Setter
    @JsonProperty
    private String elementName;


    @Getter @Setter
    @JsonProperty
    private String elementType;

    @NotNull
    @Getter @Setter
    @JsonProperty
    private String apiHost;

    /**
     * Configures and builds a {@link ScheduledReporter} instance for the given registry.
     *
     * @param registry the metrics registry to report metrics from.
     * @return a reporter configured for the given metrics registry.
     */
    @Override
    public ScheduledReporter build(MetricRegistry registry) {

            String apiKey = getApiKey();
            String apiHost = getApiHost();
            String elementName = getElementName();
            String elementType = getElementType();
            LOG.info("Building NetuitiveMetricsCloudReporter for apiKey: {}, apiHost: {}, elementName: {}, elementType: {}.", apiKey, apiHost, elementName, elementType);

            NetuitiveMetricsCloudReporter.NetuitiveMetricsCloudReporterBuilder builder = new NetuitiveMetricsCloudReporter.NetuitiveMetricsCloudReporterBuilder();
            return builder
                    .registry(registry)
                    .durationUnit(getDurationUnit())
                    .rateUnit(getRateUnit())
                    .filter(getFilter())
                    .apiKey(apiKey)
                    .apiHost(apiHost)
                    .elementName(elementName)
                    .elementType(elementType)
                    .build();
    }
}
