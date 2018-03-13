package no.cantara.util.debug;

import ch.qos.logback.core.rolling.RollingFileAppender;

public class MinuteAppender<E> extends RollingFileAppender<E> {
//    private int interval = 0; // minutes
//    @Override
//    public void rollover() {
//        if (this.interval % 2 == 0) {
//            super.rollover();
//            this.interval = 0;
//        }
//        this.interval++;
//    }

    private static long start = System.currentTimeMillis(); // minutes
    private int rollOverTimeInMinutes = 1;

    @Override
    public void rollover()
    {
        long currentTime = System.currentTimeMillis();
        int maxIntervalSinceLastLoggingInMillis = rollOverTimeInMinutes * 60 * 1000;

        if ((currentTime - start) >= maxIntervalSinceLastLoggingInMillis)
        {
            super.rollover();
            start = System.currentTimeMillis();
        }
    }
}
