package net.hockeyapp.android.metrics;

import net.hockeyapp.android.utils.Util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Util.class})
public class ChannelTests {

    @Test
    public void testBatchConstantsIfDebuggerIsAttached() {
        mockStatic(Util.class);
        when(Util.isDebuggerConnected()).thenReturn(true);

        assertEquals(Channel.getMaxBatchCount(), Channel.MAX_BATCH_COUNT_DEBUG);
        assertEquals(Channel.getMaxBatchInterval(), Channel.MAX_BATCH_INTERVAL_DEBUG);
    }

    @Test
    public void testBatchConstantsIfDebuggerIsNotAttached() {
        mockStatic(Util.class);
        when(Util.isDebuggerConnected()).thenReturn(false);

        assertEquals(Channel.getMaxBatchCount(), Channel.MAX_BATCH_COUNT);
        assertEquals(Channel.getMaxBatchInterval(), Channel.MAX_BATCH_INTERVAL);
    }
}
