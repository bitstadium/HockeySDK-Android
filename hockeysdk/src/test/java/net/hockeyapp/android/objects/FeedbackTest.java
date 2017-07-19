package net.hockeyapp.android.objects;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

public class FeedbackTest {

    private Feedback sut;

    @Before
    public void setUp() throws Exception {
        this.sut = new Feedback();
    }

    @Test
    public void setNameWorks() {
        this.sut.setName("John Smith");
        assertTrue(this.sut.getName().equals("John Smith"));
    }
}
