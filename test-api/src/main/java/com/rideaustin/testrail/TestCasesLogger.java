package com.rideaustin.testrail;

import org.apache.commons.io.FileUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Created on 15/02/2018
 *
 * @author sdelaysam
 */

public class TestCasesLogger extends RunListener {

    private static final Description FAILED = Description.createTestDescription(String.class, "failed");
    private static final String LOG_FILE_PATH = System.getProperty("testcases.logfile");

    public TestCasesLogger() {
        FileUtils.deleteQuietly(new File(LOG_FILE_PATH));
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);

        Collection<Annotation> annotations = description.getAnnotations();
        if (annotations == null) {
            return;
        }
        annotations.stream()
                .filter(annotation -> annotation instanceof TestCases)
                .findFirst()
                .map(annotation -> (TestCases) annotation)
                .map(TestCases::value)
                .map(values -> String.join(",", values))
                .ifPresent(s -> log(s, description));
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        failure.getDescription().addChild(FAILED);
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        super.testAssumptionFailure(failure);
        failure.getDescription().addChild(FAILED);
    }

    private void log(String testCases, Description description) {
        try {
            File file = new File(LOG_FILE_PATH);
            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("Unable to create file: " + LOG_FILE_PATH);
            }

            try (FileOutputStream stream = new FileOutputStream(file, true);
                 PrintWriter printer = new PrintWriter(stream)) {

                boolean passed = !description.getChildren().contains(FAILED);
                printer.print(passed ? "PASSED" : "FAILED");
                printer.print(':');
                printer.print(description.getDisplayName());
                printer.print(':');
                printer.println(testCases);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
