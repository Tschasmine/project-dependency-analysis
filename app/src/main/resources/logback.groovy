import ch.qos.logback.classic.Level
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.LevelFilter

import static ch.qos.logback.core.spi.FilterReply.ACCEPT
import static ch.qos.logback.core.spi.FilterReply.DENY

appender("FILE", FileAppender) {
    filter(LevelFilter) {
        level = DEBUG
        onMatch = ACCEPT
        onMismatch = DENY
    }
    file = "frontend.log"
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

appender("STDOUT", ConsoleAppender) {
    filter(LevelFilter) {
        level = INFO
        onMatch = ACCEPT
        onMismatch = DENY
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%msg%n"
    }
}

logger("com.tschasmine", INFO, ["STDOUT", "FILE"])

root(WARN)
