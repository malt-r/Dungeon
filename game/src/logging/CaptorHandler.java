package logging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class CaptorHandler extends Handler {
    private final List<LogRecord> logList = new ArrayList<>();
    private final Map<CustomLogLevel, List<LogRecord>> logMap = new HashMap<>();
    private final List<Logger> monitoredLoggers = new ArrayList<>();

    public CaptorHandler() {}

    /**
     * Add a new Logger to be monitored
     *
     * @param logger Logger to add to monitoring
     */
    public void addLogger(Logger logger) {
        if (!monitoredLoggers.contains(logger)) {
            monitoredLoggers.add(logger);
        }
    }

    /**
     * remove a single registered Logger
     *
     * @param logger logger to remove from monitoring
     */
    public void removeLogger(Logger logger) {
        monitoredLoggers.remove(logger);
    }

    /**
     * Accepts new Logs to internally record
     *
     * @param record new Log to record
     */
    @Override
    public void publish(LogRecord record) {
        for (Logger logger : monitoredLoggers) {
            if (logger.getName().equals(record.getLoggerName())) {
                CustomLogLevel level = (CustomLogLevel) record.getLevel();
                logList.add(record);

                List<LogRecord> logLevelList =
                        logMap.computeIfAbsent(level, k -> new ArrayList<>());
                logLevelList.add(record);
            }
        }
    }

    /** get the list containing all Logs */
    public List<LogRecord> getLogList() {
        return logList;
    }

    /**
     * get all monitored logs filtered by a specific log level
     *
     * @param level Level to filter logs by
     * @return a List containing all logs of a specific log level
     */
    public List<LogRecord> getLogsByLevel(Level level) {
        if (logMap.get((CustomLogLevel) level) != null) {
            return logMap.get((CustomLogLevel) level);
        } else {
            return new ArrayList<>();
        }
    }

    /** clears all Logs and mapped logs */
    public void clearLogs() {
        logList.clear();
        logMap.clear();
    }

    /** clears all monitored loggers */
    public void clearLoggers() {
        monitoredLoggers.clear();
    }

    /** clears all logs and monitored loggers */
    public void clear() {
        clearLogs();
        clearLoggers();
    }

    // only here to appease override need
    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}
}
