package no.cantara.service.testsupport;

import no.cantara.service.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestServerPlain {
    private static final Logger log = LoggerFactory.getLogger(TestServerPlain.class);

    private Main main;


    public TestServerPlain() {
    }


    public void start() throws InterruptedException {

        new Thread(() -> {
            main = new Main();
            main.start();
        }).start();
        do {
            Thread.sleep(100);
        } while (main == null || !main.isStarted());
    }

    public void stop() {
        main.stop();
    }

    public String getUrl() {
        return "http://localhost:" + main.getPort() + Main.CONTEXT_PATH;
    }
}

