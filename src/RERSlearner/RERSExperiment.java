package RERSlearner;

import de.learnlib.api.SUL;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static RERSlearner.BasicLearner.*;

/**
 * Created by rick on 17/03/2017.
 */
public class RERSExperiment {
    final static String path = "/home/gregory/git/Learning/Problems/";

    public static void executeProgram(final int id) throws IOException {
        Map<Integer, String[]> map = new HashMap<>();
        map.put(10, new String[]{"1","2","3","4","5"});
        map.put(11, new String[]{"1","2","3","4","5","6","7","8","9","10"});


        for (LearningMethod lm : LearningMethod.values()) {
            for (TestingMethod tm : TestingMethod.values()) {
                if (tm == TestingMethod.UserQueries) {
                    continue;
                }
                new Thread(() -> {
                    final String logfile = path + "Problem" + id + "/" +  lm + "/" + tm + "/" + "log.txt";
                    new File(logfile).getParentFile().mkdirs();

                    try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logfile)))) {
                        run(id, lm, tm, map.get(id), out);
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }).start();
            }
        }
    }

    public static void run(final int id, final LearningMethod lm, final TestingMethod tm, final String[] nums, PrintWriter out) {
        final String folder = path + "Problem" + id + "/" +  lm + "/" + tm + "/";
        final SUL<String,String> sul = new ProcessSUL(path + "Problem" + id + "/a.out");

        final Collection<String> inputAlphabet = Arrays.asList(nums);


        try {
            runControlledExperiment(sul, lm, tm, inputAlphabet, out, folder);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sul instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) sul).close();
                } catch (Exception ignored) {}
            }
        }

    }

    /**
     * Example of how to learn a Mealy machine model from one of the compliled RERS programs.
     * @param args
     */
    public static void main(String [] args) throws IOException {
        RERSExperiment.executeProgram(10);
        RERSExperiment.executeProgram(11);

    }
}