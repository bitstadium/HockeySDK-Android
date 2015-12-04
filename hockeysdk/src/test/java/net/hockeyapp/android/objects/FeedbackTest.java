package net.hockeyapp.android.objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FeedbackTest {

    private Feedback sut;

    @Before
    public void setUp() throws Exception {
        this.sut = new Feedback();
    }

    @Test
    public void setNameWorks() {
        this.sut.setName("John Smith");
        Assert.assertTrue(this.sut.getName().equals("John Smith"));
    }
}
