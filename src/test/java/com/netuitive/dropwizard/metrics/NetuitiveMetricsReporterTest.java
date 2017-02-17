package com.netuitive.dropwizard.metrics;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.TreeMap;
import java.io.OutputStream;
import java.util.SortedMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NetuitiveMetricsReporterTest {
	
	@Mock
	private NetuitiveMetricsReporter reporter;

	@BeforeTest
	public void init(){
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	@SuppressWarnings("rawtypes")
	public void testReport() {
		SortedMap<String, Gauge> gauges = new TreeMap<String, Gauge>();
		Gauge<Double> g = new Gauge<Double>(){
		     public Double getValue() {
		         return new Double(7.0);
		     }
		};
		gauges.put("M1", g);
		SortedMap<String, Counter> counter = new TreeMap<String, Counter>();
		SortedMap<String, Histogram> histogram = new TreeMap<String, Histogram>();
		SortedMap<String, Meter> meter = new TreeMap<String, Meter>();
		SortedMap<String, Timer> timer = new TreeMap<String, Timer>();
		reporter.report(gauges, counter, histogram, meter, timer);
		verify(reporter, times(1)).report(gauges, counter, histogram, meter, timer);
	}
	
	@Test
	public void testPushGauge() {
		
		reporter.pushGauge("M1",  new Double(7.0));
		verify(reporter).pushGauge("M1", new Double(7.0));
        
	}
	
	@Test
	public void testPushCounter() {
		
		reporter.pushCounter("M1",  new Long(7));
		verify(reporter).pushCounter("M1", new Long(7));
        
	}
	
	@Test
	public void testPushSnapshot() {
		Snapshot s = new Snapshot(){

			@Override
			public double getValue(double quantile) {

				return 0;
			}

			@Override
			public long[] getValues() {
				
				return null;
			}

			@Override
			public int size() {
				
				return 0;
			}

			@Override
			public long getMax() {
				
				return 0;
			}

			@Override
			public double getMean() {
				
				return 0;
			}

			@Override
			public long getMin() {
				
				return 0;
			}

			@Override
			public double getStdDev() {
				
				return 0;
			}

			@Override
			public void dump(OutputStream output) {
				
				
			}
			
		};
		reporter.pushSnapshot("M1",  s);
		verify(reporter).pushSnapshot("M1", s);
        
	}
	
	@Test
	public void testPushMeter() {
		Metered m = new Metered()
				{

					@Override
					public long getCount() {
						
						return 1;
					}

					@Override
					public double getFifteenMinuteRate() {
						
						return 2.0;
					}

					@Override
					public double getFiveMinuteRate() {
						
						return 3.0;
					}

					@Override
					public double getMeanRate() {
						
						return 4.0;
					}

					@Override
					public double getOneMinuteRate() {
						
						return 5.0;
					}
			
				};
		reporter.pushMeter("M1",  m);
		verify(reporter).pushMeter("M1", m);
        
	}
}
