package javafxapplication1;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;

public final class LogWriter implements AutoCloseable {
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executor;
    private volatile boolean running = true;
    private Path file;
    private BufferedWriter writer;

    public LogWriter(Path file) throws IOException {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "LogWriter");
            t.setDaemon(true);
            return t;
        });
        setFile(file);
        start();
    }

    private void start() {
        executor.submit(() -> {
            try {
                while (running) {
                    String line = queue.take();
                    synchronized (this) {
                        if (writer != null) {
                            writer.write(line);
                            writer.newLine();
                            writer.flush();
                        }
                    }
                }
            } catch (InterruptedException ignored) {
            } catch (IOException io) {
                io.printStackTrace();
            }
        });
    }

    public synchronized void setFile(Path newFile) throws IOException {
        Path parent = newFile.getParent();
        if (parent != null) Files.createDirectories(parent);
        if (writer != null) {
            try { writer.flush(); writer.close(); } catch (IOException ignored) {}
        }
        writer = Files.newBufferedWriter(newFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        file = newFile;
    }

    public void submit(String line) { if (running) queue.offer(line); }
    public Path getFile() { return file; }

    @Override
    public void close() {
        running = false;
        executor.shutdownNow();
        synchronized (this) {
            if (writer != null) {
                try { writer.flush(); writer.close(); } catch (IOException ignored) {}
            }
        }
    }
}
