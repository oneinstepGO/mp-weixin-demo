package com.oneinstep.demo.bootdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class LogProducer {

    private String[] randomKeyWords = new String[]{"ERROR", "WARN", "INFO", "DEBUG", "TRACE"};

    @Scheduled(fixedRate = 1000)
    public void produceLog() {
        int randomIndex = (int) (Math.random() * randomKeyWords.length);
        String logLevel = randomKeyWords[randomIndex];
        LocalDateTime now = LocalDateTime.now();
        switch (logLevel) {
            case "ERROR":
                log.error("This is an error log, current time is {}", now);
                break;
            case "WARN":
                log.warn("This is a warn log, current time is {}", now);
                break;
            case "INFO":
                log.info("This is an info log, current time is {}", now);
                break;
            case "DEBUG":
                log.debug("This is a debug log, current time is {}", now);
                break;
            case "TRACE":
                log.trace("This is a trace log, current time is {}", now);
        }
    }

}
