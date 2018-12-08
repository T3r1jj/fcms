package io.github.t3r1jj.fcms.backend;

import org.testng.annotations.Test;

public class FcmsApplicationIT {
    @Test
    public void start() throws InterruptedException {
        Thread runner = new Thread(() -> {  // Workaround for SilentExitException caused by spring-boot-devtools restart
            FcmsApplication.main(new String[]{});
        });
        runner.start();
        runner.join();
    }
}
