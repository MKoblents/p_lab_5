package shared.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingConfigurator {
    private static final Logger logger = LoggerFactory.getLogger(LoggingConfigurator.class);
    public static void configure(String level) {
        try {
            ch.qos.logback.classic.Logger root =
                    (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            root.setLevel(ch.qos.logback.classic.Level.toLevel(level));
            logger.debug("Logback root level set to: {}", level);
        } catch (Exception e) {
            System.err.println("Warning: Could not set log level to " + level + ", using default");
            logger.warn("Could not set log level to {}, using default", level);
        }
    }
}
