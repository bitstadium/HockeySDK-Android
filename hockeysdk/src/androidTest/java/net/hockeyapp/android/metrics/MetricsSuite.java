package net.hockeyapp.android.metrics;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ChannelTests.class, MetricsManagerTests.class, PersistenceTests.class, SenderTests.class, TelemetryContextTests.class})
public class MetricsSuite {

}
