import appender.impl.ConsoleAppender;
import appender.impl.DBAppender;
import appender.impl.FileAppender;
import config.LogConfig;
import module.enums.LogLevel;
import singleton.Logger;

public class LoggerExample {
    public static void main(String[] args) throws InterruptedException {

        // ── Config 1: Single appender (Console) ──
        LogConfig config = new LogConfig(LogLevel.INFO, new ConsoleAppender());
        Logger logger = Logger.getInstance(config);
        System.out.println("===== Config 1: INFO level + ConsoleAppender =====\n");
        logger.debug("in debug...."); // filtered - below INFO
        logger.info("in info....");
        logger.warn("in warn....");
        logger.error("in error....");

        // ── Config 2: Single appender (File) ──
        System.out.println("\n===== Config 2: INFO level + FileAppender =====\n");
        logger.setLogConfig(new LogConfig(LogLevel.INFO, new FileAppender()));
        logger.debug("in debug....");
        logger.info("in info....");
        logger.warn("in warn....");
        logger.error("in error....");

        // ── Config 3: Single appender (DB) ──
        System.out.println("\n===== Config 3: INFO level + DBAppender =====\n");
        DBAppender dbAppender = new DBAppender();
        logger.setLogConfig(new LogConfig(LogLevel.INFO, dbAppender));
        logger.debug("in debug....");
        logger.info("in info....");
        logger.warn("in warn....");
        logger.error("in error....");
        dbAppender.getDBLogs();

        // ── Config 4: MULTIPLE appenders at once (Console + File + DB) ──
        System.out.println("\n===== Config 4: INFO level + Console + File + DB (fan-out) =====\n");
        DBAppender multiDb = new DBAppender();
        LogConfig multiConfig = new LogConfig(LogLevel.INFO,
                new ConsoleAppender(),    // appender 1
                new FileAppender(),       // appender 2
                multiDb                   // appender 3
        );
        logger.setLogConfig(multiConfig);
        logger.info("this goes to Console, File AND DB simultaneously");
        logger.warn("warning - fan-out to all appenders");
        logger.error("error   - fan-out to all appenders");
        System.out.println("\n--- DB log store ---");
        multiDb.getDBLogs();

        // ── Config 5: Add appender dynamically at runtime ──
        System.out.println("\n===== Config 5: Add appender dynamically =====\n");
        LogConfig dynamicConfig = new LogConfig(LogLevel.DEBUG, new ConsoleAppender());
        logger.setLogConfig(dynamicConfig);
        logger.info("only console right now");
        dynamicConfig.addAppender(new FileAppender());   // add file at runtime
        logger.warn("now goes to Console + File");

        System.out.println("\n===== Config 4: Thread-Safety Demo (WARNING + Console) =====\n");
        logger.setLogConfig(new LogConfig(LogLevel.WARNING,new ConsoleAppender()));

//        Runnable task =()-> {
//            for(int i = 1; i <=3; i++){
//                logger.log(LogLevel.INFO, Thread.currentThread().getName() + " log entry" + i);
//            }
//        };
//        Thread thread3 = new Thread(task, "thread-3");
//        Thread thread1 =new Thread(task,"thread-1");
//        Thread thread2 =new Thread(task, "thread-2");
//        thread1.start();thread2.start();thread3.start();
//        thread1.join();thread2.join();thread3.join();
    }
}
