package RERSlearner;

import com.google.common.collect.ImmutableSet;
import de.learnlib.api.SUL;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static RERSlearner.BasicLearner.*;
import static RERSlearner.BasicLearner.LearningMethod.*;
import static RERSlearner.BasicLearner.TestingMethod.*;

/**
 * Created by rick on 17/03/2017.
 */
public class RERSExperiment {

    private static class ExperimentSetup {
        public final String path;
        public final String resultFile;
        public final Collection<String> inputs;
        public final InputStream in;
        public final PrintStream out;

        public ExperimentSetup(String path, Collection<String> inputs) {
            this(path, inputs, System.in, System.out);
        }

        public ExperimentSetup(String path, Collection<String> inputs, String inFile, String outFile) throws IOException {
            this(path, inputs, new FileInputStream(new File(inFile)), new PrintStream(new File(outFile)));
        }

        public ExperimentSetup(String path, Collection<String> inputs, InputStream in, PrintStream out){
            this.in = in;
            this.out = out;
            this.path = path.endsWith("/") ? path : path + '/' ;
            this.resultFile = this.path + "results/";
            new File(this.resultFile).mkdirs();
            this.inputs = inputs;
        }

        public SUL<String, String> getSUL() {
            File file = new File(this.path + "/a.out");
            if (!file.exists()) throw new IllegalStateException(file.getAbsolutePath() + " does not exist");
            return new ProcessSUL(file.getAbsolutePath());
        }
    }

//    private static HashMap<Integer, ExperimentSetup> experiments = new HashMap<>();
//    static {
//        try {
//            experiments.put(10, new ExperimentSetup("Problems/Problem10", ImmutableSet.of("1", "2", "3", "4", "5"),
//                    new FileInputStream(new File("")), new PrintStream(new File(path))));
//
//
//        } catch (FileNotFoundException ignored) {}
//    }

    /**
     * Example of how to learn a Mealy machine model from one of the compiled RERS programs.
     * @throws IOException
     */
    public static void run(ExperimentSetup exp, LearningMethod lm, TestingMethod tm) {
        SUL<String, String> sul = exp.getSUL();
        try {
            runControlledExperiment(sul, lm, tm, exp.inputs, exp.resultFile, exp.in, exp.out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sul instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) sul).close();
                } catch (Exception exception) {
                    // should not happen
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        LearningMethod[] learningMethods = {TTT, RivestSchapire, KearnsVazirani, LStar};
        TestingMethod[] testingMethods = {UserQueries, WMethod, WpMethod, RandomWalk};
        int count = 0;
        int[] problems = {10, 1, 11, 2};

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        for (int p : problems) {
            for (TestingMethod testingMethod : testingMethods) {
                for (LearningMethod learningMethod : learningMethods) {

                    executorService.execute(() -> {
                        // Set up inputs
                        ImmutableSet < String > inputs = (p == 1 || p == 10) ? ImmutableSet.of("1", "2", "3", "4", "5") :
                                ImmutableSet.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

                        // Get start time
                        long begin = System.currentTimeMillis();
                        String outfile = "results/p" + p + "/" + learningMethod + "/" + testingMethod + "/log.txt";
                        new File(outfile).getParentFile().mkdirs();
                        ExperimentSetup experiment;
                        try {
                            experiment = new ExperimentSetup("Problems/Problem" + p, inputs, "Problems/Problem" + p + "/out.klee", outfile);
                        } catch (IOException ex) {
                            System.out.println(ex);
                            return;
                        }

                        System.out.println("Started learning for problem " + p + " with " + learningMethod + " using " + testingMethod);
                        run(experiment, learningMethod, testingMethod);
                        long end = (System.currentTimeMillis() - begin) / 1000; // Seconds

                        System.out.println("Finishe learning for problem " + p + " with " + learningMethod + " using " + testingMethod + ", took " + end + "seconds");

                        PrintStream stream = experiment.out;
                        stream.println("Finished in " + end + " seconds");
                        stream.close();
                    });
                }
            }
        }
        executorService.shutdown();
    }
}