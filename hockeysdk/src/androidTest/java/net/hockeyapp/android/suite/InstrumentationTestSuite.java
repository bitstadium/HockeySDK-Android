package net.hockeyapp.android.suite;

import net.hockeyapp.android.ExceptionHandlerTest;
import net.hockeyapp.android.TrackingTest;
import net.hockeyapp.android.UtilTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ExceptionHandlerTest.class, TrackingTest.class, UtilTest.class})
public class InstrumentationTestSuite {
}
