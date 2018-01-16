package no.cantara.service.loadtest.util;

import no.cantara.service.loadtest.LoadTestExecutorService;

import java.util.concurrent.*;

public class TimedProcessingUtil {


    public static void runWithTimeout(final Runnable runnable, long timeout, TimeUnit timeUnit) throws Exception {
        runWithTimeout(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                runnable.run();
                return null;
            }
        }, timeout, timeUnit);
    }

    public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit) throws Exception {
        if (LoadTestExecutorService.isRunning()) {
            final ExecutorService runWithTimeoutExecutor = Executors.newSingleThreadExecutor();
            final Future<T> future = runWithTimeoutExecutor.submit(callable);
            runWithTimeoutExecutor.shutdown(); // This does not cancel the already-scheduled task.
            try {
                return future.get(timeout, timeUnit);
            } catch (TimeoutException e) {
                //remove this if you do not want to cancel the job in progress
                //or set the argument to 'false' if you do not want to interrupt the thread
                future.cancel(true);
                throw e;
            } catch (ExecutionException e) {
                //unwrap the root cause
                future.cancel(true);
                Throwable t = e.getCause();
                if (t instanceof Error) {
                    throw (Error) t;
                } else if (t instanceof Exception) {
                    throw (Exception) t;
                } else {
                    throw new IllegalStateException(t);
                }
            }

        }
        return null;
    }
}