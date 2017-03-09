package com.netuitive.dropwizard.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.netuitive.iris.client.AbstractRestClient;
import com.netuitive.iris.client.metric.NetuitiveIngestMetricRestClient;
import com.netuitive.iris.client.request.metric.IngestRequest;
import com.netuitive.iris.entity.Element;
import com.netuitive.iris.entity.Metric;
import com.netuitive.iris.entity.Sample;

import lombok.Builder;

public class NetuitiveMetricsCloudReporter extends ScheduledReporter  {

    private static final Logger LOG = LoggerFactory.getLogger(NetuitiveMetricsCloudReporter.class);

    private final String apiHost;
    private final String apiKey;
    private final String elementName;
    private final String elementType;
    private NetuitiveIngestMetricRestClient client;
    
    @Builder
    private NetuitiveMetricsCloudReporter(MetricRegistry registry,
                            String apiKey, 
                            String apiHost, 
                            String elementName, 
                            String elementType, 
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter) {
        super(registry, "netuitive-cloud-reporter", filter, rateUnit, durationUnit);
        this.apiKey = apiKey;
        this.apiHost = (StringUtils.isBlank(apiHost) ? AbstractRestClient.API_HOST : apiHost);
        this.elementName = elementName;
        this.elementType = (StringUtils.isBlank(elementType) ? "Dropwizard" : elementType);

        client = new NetuitiveIngestMetricRestClient(AbstractRestClient.SCHEME, this.apiHost, this.apiKey);
    }


    /**
     * Called periodically by the polling thread. Subclasses should report all the given metrics.
     *
     * @param gauges     all of the gauges in the registry
     * @param counters   all of the counters in the registry
     * @param histograms all of the histograms in the registry
     * @param meters     all of the meters in the registry
     * @param timers     all of the timers in the registry
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        
        
        LOG.info("Pushing {} gauges, {} counters, {} histograms, {} meters and {} timers to Netuitive",
                (gauges == null ? 0 : gauges.size()), 
                (counters == null ? 0 : counters.size()), 
                (histograms == null ? 0 : histograms.size()), 
                (meters == null ? 0 : meters.size()), 
                (timers == null ? 0 : timers.size()) 
                );

        long startTime = System.currentTimeMillis();

        // Prepare a single Element in which to place the metrics
        Element element = new Element();
        element.setName(elementName);
        element.setId(elementName);
        element.setType(elementType);      
        List<Element> elements = new ArrayList<Element>();
        elements.add(element);
        
        if (!gauges.isEmpty()) {
            LOG.debug("Gauges");
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                String metricName = entry.getKey();
                if (NumberUtils.isNumber(entry.getValue().getValue().toString()))
                {
                    double metricValue = Double.parseDouble(entry.getValue().getValue().toString());
                    addMetricSample(element, metricName, metricValue, "GAUGE");
                }

            }
        }

        if (!counters.isEmpty()) {
            LOG.debug("Counters");
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                String metricName = entry.getKey();
                long metricValue = entry.getValue().getCount();
                addMetricSample(element, metricName, metricValue, "COUNTER");
            }
        }

        if (!meters.isEmpty()) {
            LOG.debug("Meters");
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                String metricName = entry.getKey();
                pushMeter(element, metricName, entry.getValue());
            }
        }

        if (!timers.isEmpty()) {
            LOG.debug("Timers");
            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                String metricName = entry.getKey();
                pushMeter(element, metricName, entry.getValue());
                pushSnapshot(element, metricName, entry.getValue().getSnapshot());
            }
        }

        if (!histograms.isEmpty()) {
            LOG.debug("Histograms");
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                String metricName = entry.getKey();
                pushSnapshot(element, metricName, entry.getValue().getSnapshot());
            }
        }
        
        LOG.debug("Element to send: "+element);
        
        client.ingest(new IngestRequest(elements));
        
        LOG.debug("Push duration:" + (System.currentTimeMillis() - startTime) + " millis");

    }
    
    /**
     * Pushes the Metered metrics as Gauges
     * 
     * @param metricName
     *            Base metric name for this Meter
     * @param meter
     *            Metered object to report
     * @param element
     *            The Element on which to place these metrics and samples
     */
    void pushMeter(Element element, String metricName, Metered meter)
    {
        String type = "GAUGE";
        addMetricSample(element, metricName + ".count", new Double(meter.getCount()), "COUNTER");
        addMetricSample(element, metricName + ".meanRate", meter.getMeanRate(), type);
        addMetricSample(element, metricName + ".1MinuteRate", meter.getOneMinuteRate(), type);
        addMetricSample(element, metricName + ".5MinuteRate", meter.getFiveMinuteRate(), type);
        addMetricSample(element, metricName + ".15MinuteRate", meter.getFifteenMinuteRate(), type);     
    }
    
    /**
     * Pushes the Snapshot metrics as Gauges
     * 
     * @param metricName
     *            Base metric name for this Snapshot
     * @param snapshot
     *            Snapshot object to report
     * @param element
     *            The Element on which to place these metrics and samples
     */
    void pushSnapshot(Element element, String metricName, Snapshot snapshot)
    {
        String type = "GAUGE";
        addMetricSample(element, metricName + ".min", new Double(snapshot.getMin()), type);
        addMetricSample(element, metricName + ".max", new Double(snapshot.getMax()), type);
        addMetricSample(element, metricName + ".mean", snapshot.getMean(), type);
        addMetricSample(element, metricName + ".stdDev", snapshot.getStdDev(), type);
        addMetricSample(element, metricName + ".median", snapshot.getMedian(), type);
        addMetricSample(element, metricName + ".75percentile", snapshot.get75thPercentile(), type);
        addMetricSample(element, metricName + ".95percentile", snapshot.get95thPercentile(), type);
        addMetricSample(element, metricName + ".98percentile", snapshot.get98thPercentile(), type);
        addMetricSample(element, metricName + ".99percentile", snapshot.get99thPercentile(), type);
        addMetricSample(element, metricName + ".999percentile", snapshot.get999thPercentile(), type);
        
    }
    
    /***
     * Adds the Metric and Sample data to the single Element
     * 
     * @param element
     *            The Element on which to place the metric and sample
     * @param metricName
     *            the name of the metric (and id)
     * @param metricValue
     *            the numeric sample value
     * @param type
     *            the metric type (COUNTER or GAUGE)
     * @return
     */
    Element addMetricSample(Element element, String metricName, Double metricValue, String type)
    {
        Metric m = new Metric();
        m.setId(metricName);
        m.setType(type);
        element.getMetrics().add(m);
        
        Sample s = new Sample(metricName, new java.util.Date(), metricValue);
        element.getSamples().add(s);
        
        return element;
    }

    /***
     * Adds the Metric and Sample data to the single Element
     * 
     * @param element
     *            The Element on which to place the metric and sample
     * @param metricName
     *            the name of the metric (and id)
     * @param metricValue
     *            the numeric sample value
     * @param type
     *            the metric type (COUNTER or GAUGE)
     * @return
     */
    Element addMetricSample(Element element, String metricName, Long metricValue, String type)
    {
        return addMetricSample(element, metricName, metricValue.doubleValue(), type);
    }
}
