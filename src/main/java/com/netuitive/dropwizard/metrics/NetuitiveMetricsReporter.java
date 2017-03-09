package com.netuitive.dropwizard.metrics;

import java.net.SocketException;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

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
import com.netuitive.ananke.statsd.client.NetuitiveStatsDClient;
import com.netuitive.ananke.statsd.client.request.GaugeRequest;
import com.netuitive.ananke.statsd.client.request.SetRequest;

import lombok.Builder;

/**
 * Created by bspindler on 6/12/16.
 */
public class NetuitiveMetricsReporter extends ScheduledReporter  {

    private static final Logger LOG = LoggerFactory.getLogger(NetuitiveMetricsReporter.class);

    private final String host;
    private final int port;
    private NetuitiveStatsDClient client;
    
    @Builder
    private NetuitiveMetricsReporter(MetricRegistry registry,
                            String host, // hostname of netuitive-statsd server
                            int port, // port of netuitive-statsd server
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter) {
        super(registry, "netuitive-reporter", filter, rateUnit, durationUnit);
        this.host = host;
        this.port = port;
         
        try {
            client = new NetuitiveStatsDClient(this.host, this.port);
        } catch (SocketException e) {
            LOG.error("Unable to create a Socket on host " + this.host + " and port " + this.port, e);
        }
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

        if (!gauges.isEmpty()) {
            LOG.debug("Gauges");
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                String metricName = entry.getKey();
                if (NumberUtils.isNumber(entry.getValue().getValue().toString()))
                {
                    double metricValue = Double.parseDouble(entry.getValue().getValue().toString());
                    pushGauge(metricName, metricValue);
                }

            }
        }

        if (!counters.isEmpty()) {
            LOG.debug("Counters");
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                String metricName = entry.getKey();
                long metricValue = entry.getValue().getCount();
                pushCounter(metricName, metricValue);
            }
        }

        if (!meters.isEmpty()) {
            LOG.debug("Meters");
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                String metricName = entry.getKey();
                pushMeter(metricName, entry.getValue());
            }
        }

        if (!timers.isEmpty()) {
            LOG.debug("Timers");
            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                String metricName = entry.getKey();
                pushMeter(metricName, entry.getValue());
                pushSnapshot(metricName, entry.getValue().getSnapshot());
            }
        }

        if (!histograms.isEmpty()) {
            LOG.debug("Histograms");
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                String metricName = entry.getKey();
                pushSnapshot(metricName, entry.getValue().getSnapshot());
            }
        }

    }
    
    /**
     * Pushes the Metered metrics as Gauges
     * 
     * @param metricName
     *            Base metric name for this Meter
     * @param meter
     *            Metered object to report
     */
    void pushMeter(String metricName, Metered meter)
    {
        pushCounter(metricName + ".count", meter.getCount());
        pushGauge(metricName + ".meanRate", meter.getMeanRate());
        pushGauge(metricName + ".1MinuteRate", meter.getOneMinuteRate());
        pushGauge(metricName + ".5MinuteRate", meter.getFiveMinuteRate());
        pushGauge(metricName + ".15MinuteRate", meter.getFifteenMinuteRate());     
    }
    
    /**
     * Pushes the Snapshot metrics as Gauges
     * 
     * @param metricName
     *            Base metric name for this Snapshot
     * @param snapshot
     *            Snapshot object to report
     */
    void pushSnapshot(String metricName, Snapshot snapshot)
    {
        pushGauge(metricName + ".min", new Double(snapshot.getMin()));
        pushGauge(metricName + ".max", new Double(snapshot.getMax()));
        pushGauge(metricName + ".mean", snapshot.getMean());
        pushGauge(metricName + ".stdDev", snapshot.getStdDev());
        pushGauge(metricName + ".median", snapshot.getMedian());
        pushGauge(metricName + ".75percentile", snapshot.get75thPercentile());
        pushGauge(metricName + ".95percentile", snapshot.get95thPercentile());
        pushGauge(metricName + ".98percentile", snapshot.get98thPercentile());
        pushGauge(metricName + ".99percentile", snapshot.get99thPercentile());
        pushGauge(metricName + ".999percentile", snapshot.get999thPercentile());
        
    }
    
    /***
     * Uses the Ananke client to push one Counter metric to StatsD
     * 
     * @param metricName
     *            Metric name
     * @param metricValue
     *            Metric value
     */
    void pushCounter(String metricName, Long metricValue)
    {
        LOG.debug("Pushing counter: {}={}", metricName, metricValue);
        SetRequest req = new SetRequest()
                .withMetric(metricName)
                .withValue(metricValue);
        client.set(req);
    }

    /***
     * Uses the Ananke client to push one Gauge metric to StatsD
     * 
     * @param metricName
     *            Metric name
     * @param metricValue
     *            Metric value
     */
    void pushGauge(String metricName, Double metricValue)
    {
        LOG.debug("Pushing gauge: {}={}", metricName, metricValue);
        GaugeRequest req = new GaugeRequest()
                .withMetric(metricName)
                .withValue(metricValue);
        client.gauge(req);
    }
}
