package client.inputWorkers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConsoleInputReader implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleInputReader.class);
    private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        logger.debug("Console input reader started");
        while (running) {
            try {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line != null && !line.trim().isEmpty()) {
                        inputQueue.offer(line);
                    }
                } else {
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                logger.error("Error reading console input: {}", e.getMessage());
            }
        }
    }

    public String pollCommand() throws InterruptedException {
        return inputQueue.poll(100, TimeUnit.MILLISECONDS);
    }

     public String takeCommand() throws InterruptedException {
        return inputQueue.take();
    }
    public void stop() {
        running = false;
        scanner.close();
    }
}