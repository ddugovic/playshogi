package com.playshogi.website.gwt.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.playshogi.library.database.DbConnection;
import com.playshogi.library.database.KifuRepository;
import com.playshogi.library.database.ProblemRepository;
import com.playshogi.library.database.UserRepository;
import com.playshogi.library.database.models.PersistentKifu;
import com.playshogi.library.database.models.PersistentProblem;
import com.playshogi.library.database.models.PersistentUserProblemStats;
import com.playshogi.library.models.record.GameRecord;
import com.playshogi.library.shogi.files.GameRecordFileReader;
import com.playshogi.library.shogi.models.formats.kif.KifFormat;
import com.playshogi.library.shogi.models.formats.usf.UsfFormat;
import com.playshogi.website.gwt.shared.models.LoginResult;
import com.playshogi.website.gwt.shared.models.ProblemDetails;
import com.playshogi.website.gwt.shared.services.ProblemsService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProblemsServiceImpl extends RemoteServiceServlet implements ProblemsService {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ProblemsServiceImpl.class.getName());


    private static final String PATH = "/playshogi/tsume/7/";
    private final ProblemRepository problemRepository;
    private final KifuRepository kifuRepository;
    private final UserRepository userRepository;
    private final Authenticator authenticator = Authenticator.INSTANCE;

    public ProblemsServiceImpl() {
        DbConnection dbConnection = new DbConnection();
        problemRepository = new ProblemRepository(dbConnection);
        kifuRepository = new KifuRepository(dbConnection);
        userRepository = new UserRepository(dbConnection);
    }

    @Override
    public String getProblemUsf(final String problemId) {
        try {
            GameRecord gameRecord = GameRecordFileReader.read(KifFormat.INSTANCE,
                    PATH + "tsume_07_" + problemId + ".kif");
            String tsume = UsfFormat.INSTANCE.write(gameRecord.getGameTree());
            System.out.println(tsume);
            return tsume;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading the problem " + problemId, e);
        }
        return null;
    }

    @Override
    public ProblemDetails getProblem(final String problemId) {
        LOGGER.log(Level.INFO, "getting problem: " + problemId);

        PersistentProblem persistentProblem = problemRepository.getProblemById(Integer.parseInt(problemId));

        if (persistentProblem == null) {
            LOGGER.log(Level.INFO, "Could not load problem");
            return null;
        }

        return queryProblemDetails(persistentProblem);
    }


    @Override
    public ProblemDetails getRandomProblem() {
        LOGGER.log(Level.INFO, "getting random problem");

        PersistentProblem persistentProblem = problemRepository.getRandomProblem();

        if (persistentProblem == null) {
            LOGGER.log(Level.INFO, "Could not load a random problem");
            return null;
        }

        return queryProblemDetails(persistentProblem);
    }

    @Override
    public ProblemDetails getRandomProblem(int numMoves) {
        LOGGER.log(Level.INFO, "getting random problem of " + numMoves + " moves");

        PersistentProblem persistentProblem = problemRepository.getRandomProblem(numMoves);

        if (persistentProblem == null) {
            LOGGER.log(Level.INFO, "Could not load a random problem of " + numMoves + " moves");
            return null;
        }

        return queryProblemDetails(persistentProblem);
    }

    private ProblemDetails queryProblemDetails(PersistentProblem persistentProblem) {
        PersistentKifu persistentKifu = kifuRepository.getKifuById(persistentProblem.getKifuId());

        if (persistentKifu == null) {
            LOGGER.log(Level.INFO, "Could not load the problem kifu for id " + persistentProblem.getKifuId());
            return null;
        }

        String usf = UsfFormat.INSTANCE.write(persistentKifu.getKifu());
        LOGGER.log(Level.INFO, "Sending problem:\n" + usf);

        return getProblemDetails(persistentProblem, usf);
    }

    private ProblemDetails getProblemDetails(PersistentProblem persistentProblem, String usf) {
        ProblemDetails problemDetails = new ProblemDetails();
        problemDetails.setId("" + persistentProblem.getId());
        problemDetails.setKifuId(persistentProblem.getKifuId());
        problemDetails.setNumMoves(persistentProblem.getNumMoves());
        problemDetails.setElo(persistentProblem.getElo());
        problemDetails.setPbType(persistentProblem.getPbType().getDescription());
        problemDetails.setUsf(usf);
        return problemDetails;
    }

    @Override
    public void saveUserProblemAttempt(String sessionId, String problemId, boolean success, int timeMs) {
        LOGGER.log(Level.INFO, "Saving pb stats for the user");
        LoginResult loginResult = authenticator.checkSession(sessionId);
        if (loginResult != null && loginResult.isLoggedIn()) {
            PersistentUserProblemStats userProblemStats = new PersistentUserProblemStats(loginResult.getUserId(),
                    Integer.parseInt(problemId), timeMs, success);
            userRepository.insertUserPbStats(userProblemStats);
            LOGGER.log(Level.INFO, "Saved pb stats for the user: " + userProblemStats);
        } else {
            LOGGER.log(Level.INFO, "Not saving stats for guest user");
        }
    }

    public static void main(final String[] args) {
        for (int i = 100; i <= 900; i++) {
            // System.out.println("Reading problem " + i);
            new ProblemsServiceImpl().getProblemUsf("" + i);
        }
    }

}
