package com.netuitive.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import io.dropwizard.metrics.BaseReporterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

@JsonTypeName("netuitive-rest")
public class NetuitiveMetricsCloudReporterFactory extends BaseReporterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NetuitiveMetricsCloudReporterFactory.class);

    @NotNull
    private String apiKey;

    @JsonProperty
    public String getApiKey() {
        return apiKey;
    }

    @JsonProperty
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @NotNull
    private String elementName;

    @JsonProperty
    public String getElementName() {
        return elementName;
    }

    @JsonProperty
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    private String elementType;

    @JsonProperty
    public String getElementType() {
        return elementType;
    }

    @JsonProperty
    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    private String apiHost;

    @JsonProperty
    public String getApiHost() {
        return apiHost;
    }

    @JsonProperty
    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }


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
            
            NetuitiveMetricsCloudReporter.Builder builder = NetuitiveMetricsCloudReporter.forRegistry(registry)
                                                       .convertDurationsTo(getDurationUnit())
                                                       .convertRatesTo(getRateUnit())
                                                       .filter(getFilter());
    
            return builder.build(apiKey, apiHost, elementName, elementType);
    }
}
