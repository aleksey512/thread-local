package info.biosfood.threadlocal.test;

import info.biosfood.threadlocal.SharedResource;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class SharedResourceWithFieldValueVisibleOnlyForThread {

    static final Logger LOG = Logger.getLogger(SharedResourceWithFieldValueVisibleOnlyForThread.class);

    SharedResource subject;

    @Before
    public void setup() {
        subject = new SharedResource();
    }

    @Test
    public void test() throws InterruptedException {
        ManyThreadsSimultaneously executor = ManyThreadsSimultaneouslyBuilder.create()
                .repeat(1, createJob(1))
                .repeat(1, createJob(2))
                .repeat(1, createJob(3))
                .build();

        executor.execute();

        Thread.sleep(5000);
    }

    Runnable createJob(final int assignValue) {
        return () -> {
            LOG.debug("gonna assign amount: " + assignValue);
            LOG.debug("amount before: " + subject.getAmount());

            subject.setAmount(assignValue);

            LOG.debug("amount after: " + subject.getAmount());

            try {
                Thread.sleep(100);
            } catch (Exception e) {}

            LOG.debug("amount after sleeping: " + subject.getAmount());
        };
    }

}
