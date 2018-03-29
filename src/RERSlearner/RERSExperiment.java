package RERSlearner;

import com.google.common.collect.ImmutableSet;
import de.learnlib.api.SUL;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;

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

    private static HashMap<Integer, ExperimentSetup> experiments = new HashMap<>();
    static {
        try {
            experiments.put(10, new ExperimentSetup("Problems/Problem10", ImmutableSet.of("1", "2", "3", "4", "5"), new FileInputStream(new File("")), new PrintStream(new File(""))));
        } catch (FileNotFoundException e) {

        }
    }

    /**
     * Example of how to learn a Mealy machine model from one of the compiled RERS programs.
     * @throws IOException
     */
    public static void run(ExperimentSetup exp, BasicLearner.LearningMethod lm, BasicLearner.TestingMethod tm) {
        new Thread(() -> {
            SUL<String, String> sul = exp.getSUL();
            try {
                BasicLearner.runControlledExperiment(sul, lm, tm, exp.inputs, exp.resultFile, exp.in, exp.out);
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
        }).start();
    }

    public static void main(String[] args) {
        BasicLearner.LearningMethod[] learningMethods = {BasicLearner.LearningMethod.RivestSchapire};
        BasicLearner.TestingMethod[] testingMethods = {BasicLearner.TestingMethod.UserQueries};
        int[] experiment_numbers = {10};

        for (int experiment : experiment_numbers) {
            if (!experiments.containsKey(experiment)) throw new IllegalArgumentException("Experiment " + experiment + " not configured");
            for (BasicLearner.LearningMethod learningMethod : learningMethods) {
                for (BasicLearner.TestingMethod testingMethod : testingMethods) {
                    System.out.println("Started learning for experiment " + experiment + " with " + learningMethod + " using " + testingMethod);
                    run(experiments.get(experiment), learningMethod, testingMethod);
                }
            }
        }
    }
}