package com.playshogi.library.shogi.engine;

import com.playshogi.library.models.record.GameRecord;
import com.playshogi.library.shogi.models.formats.usf.UsfFormat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueuedKifuAnalyzer {

    private static final Logger LOGGER = Logger.getLogger(QueuedKifuAnalyzer.class.getName());

    public enum Status {
        QUEUED,
        IN_PROGRESS,
        COMPLETED,
        NOT_STARTED
    }

    private static final int CAPACITY = 10;
    private final static int TIME_MS = 2000;


    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(CAPACITY);

    private final USIConnector usiConnector;
    private volatile boolean shutdown = false;

    private final Map<String, List<PositionEvaluation>> kifuEvaluations = new ConcurrentHashMap<>();
    private final Map<String, Status> kifuStatus = new ConcurrentHashMap<>();

    public QueuedKifuAnalyzer(final EngineConfiguration engineConfiguration) {
        usiConnector = new USIConnector(engineConfiguration);
        new Thread("Analysis-Queue") {
            @Override
            public void run() {
                while (!shutdown) {
                    String kifuUsf = null;
                    try {
                        kifuUsf = queue.take();
                        doAnalyse(kifuUsf);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Error processing the analysis of " + kifuUsf, ex);
                    }
                }
            }
        }.start();
    }

    public int getQueueSize() {
        return queue.size();
    }

    public synchronized void analyzeKifu(final String kifuUsf) {
        if (shutdown) {
            throw new IllegalStateException("QueuedTsumeSolver is shutdown");
        }

        if (!usiConnector.isConnected()) {
            usiConnector.connect();
        }

        queue.add(kifuUsf);
    }

    public Status getStatus(final String kifuUsf) {
        return kifuStatus.getOrDefault(kifuUsf, Status.NOT_STARTED);
    }

    public List<PositionEvaluation> getEvaluation(final String kifuUsf) {
        return kifuEvaluations.get(kifuUsf);
    }

    public int getQueuedPosition(final String kifuUsf) {
        return Arrays.asList(queue.toArray(new String[0])).indexOf(kifuUsf);
    }

    private void doAnalyse(final String kifuUsf) {
        GameRecord gameRecord = UsfFormat.INSTANCE.readSingle(kifuUsf);
        kifuEvaluations.put(kifuUsf, new CopyOnWriteArrayList<>());
        kifuStatus.put(kifuUsf, Status.IN_PROGRESS);

        usiConnector.analyzeKifu(gameRecord.getGameTree(), TIME_MS,
                evaluation -> kifuEvaluations.get(kifuUsf).add(evaluation));

        kifuStatus.put(kifuUsf, Status.COMPLETED);
    }

    public synchronized void shutDown() {
        shutdown = true;
        usiConnector.disconnect();
    }
}
