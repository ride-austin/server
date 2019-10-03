package com.rideaustin.testrail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codepine.api.testrail.TestRail;
import com.codepine.api.testrail.model.Case;
import com.codepine.api.testrail.model.CaseField;
import com.codepine.api.testrail.model.Result;
import com.codepine.api.testrail.model.ResultField;
import com.codepine.api.testrail.model.Run;
import com.codepine.api.testrail.model.Status;

import lombok.extern.slf4j.Slf4j;

/**
 * Created on 19/02/2018
 *
 * @author sdelaysam
 */
@Slf4j
public class TestCasesUploader {

    private static final int TESTRAIL_PROJECT_ID = 322;
    private static final String TESTRAIL_PASSED_STATUS = "passed_auto";
    private static final String TESTRAIL_FAILED_STATUS = "failed_auto";
    private static final String TESTRAIL_ENDPOINT = "";
    private static final Pattern PATTERN = Pattern.compile("^(PASSED|FAILED):(.+):(.*)$");

    public static void main(String[] arguments) throws IOException {
        Integer projectId = readInteger("projectId", TESTRAIL_PROJECT_ID);
        String endpoint = readString("endpoint", TESTRAIL_ENDPOINT);
        String logFile = readString("logFile");
        String username = readString("username");
        String password = readString("password");
        Integer runId = readInteger("runId", null);
        Integer suiteId = readInteger("suiteId", null);
        String runName = readString("runName", null);
        boolean checkIds = readBool("checkIds", false);

        if (runId == null && (suiteId == null || runName == null)) {
            fatal("Pass \"runId\" to upload results to existent test run or " +
                    "Pass \"suiteId\" and \"runName\" to create test run beforehand.");
        }

        Map<Integer, Boolean> contents = readLogFile(logFile);
        if (contents.isEmpty()) {
            fatal("Nothing to upload: check log file contents");
        }

        TestRail testRail = TestRail.builder(endpoint, username, password)
                .applicationName("RideAustin")
                .build();

        boolean valid = true;
        if (checkIds) {
            List<CaseField> caseFields = testRail.caseFields().list().execute();
            List<Case> cases = testRail.cases().list(projectId, suiteId, caseFields).execute();

            for (Integer id : contents.keySet()) {
                if (cases.stream().noneMatch(aCase -> id.equals(aCase.getId()))) {
                    System.out.println("Test case ID not found: " + id);
                    valid = false;
                }

            }
        }

        if (!valid) {
            fatal("There are invalid IDs, exit.");
        }

        List<Status> statuses = testRail.statuses().list().execute();
        Integer passedId = null;
        Integer failedId = null;
        for (Status status : statuses) {
            if (status.getName().equals(TESTRAIL_PASSED_STATUS)) {
                passedId = status.getId();
            } else if (status.getName().equals(TESTRAIL_FAILED_STATUS)) {
                failedId = status.getId();
            }
        }

        if (passedId == null || failedId == null) {
            fatal("TestRail statuses not found: should be " + TESTRAIL_PASSED_STATUS + " and " + TESTRAIL_FAILED_STATUS);
        }

        Run run;
        if (runId == null) {
            run = testRail.runs()
                    .add(projectId, new Run().setName(runName).setSuiteId(suiteId).setProjectId(projectId))
                    .execute();
        } else {
            run = testRail.runs().get(runId).execute();
        }

        List<ResultField> resultFields = testRail.resultFields().list().execute();
        List<Result> results = new ArrayList<>();

        for (Map.Entry<Integer, Boolean> entry : contents.entrySet()) {
            results.add(new Result()
              .setStatusId(entry.getValue() ? passedId : failedId)
              .setCaseId(entry.getKey()));
        }

        testRail.results().addForCases(run.getId(), results, resultFields).execute();
    }

    private static Map<Integer, Boolean> readLogFile(String path) throws IOException {
        Map<Integer, Boolean> result = new LinkedHashMap<>();
        File input = new File(path);
        if (!input.exists()) {
            fatal("Log file not found at path: " + path);
        }
        try (FileReader fr = new FileReader(input); BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = PATTERN.matcher(line);
                if (matcher.matches()) {
                    boolean passed = "PASSED".equals(matcher.group(1));
                    String casesStr = matcher.group(3);
                    if (casesStr == null || casesStr.isEmpty()) {
                        continue;
                    }
                    Arrays.stream(casesStr.split(","))
                            .map(TestCasesUploader::readTestCaseId)
                            .filter(Objects::nonNull)
                            .forEach(id -> {
                                // TODO: some suites have multiple tests with same ID
                                // TODO: three options to consider:
                                // TODO: 1) rewrite test cases
                                // TODO: 2) upload multiple results per test case
                                // TODO: 3) upload the worst result (currenty implemented)
                                if (!result.containsKey(id) || !passed) {
                                    result.put(id, passed);
                                }
                            });
                }
            }
        }
        return result;
    }

    private static Integer readTestCaseId(final String value) {
        try {
            return Integer.valueOf(value.startsWith("C") ? value.substring(1) : value);
        } catch (Exception e) {
            log.error(String.format("Can't parse test case ID: \"%s\"", value), e);
        }
        return null;
    }

    private static Integer readInteger(String propertyName, Integer defValue) {
        Integer value = Integer.getInteger(propertyName);
        if (value == null) {
            return defValue;
        }
        return value;
    }

    private static String readString(String propertyName) throws IllegalArgumentException {
        String value = System.getProperty(propertyName);
        if (value == null) {
            fatal("Property not defined: \"" + propertyName + "\"");
        }
        return value;
    }

    private static String readString(String propertyName, String defValue) {
        String value = System.getProperty(propertyName);
        if (value == null) {
            return defValue;
        }
        return value;
    }

    private static boolean readBool(String propertyName, boolean defValue) {
        try {
            return Boolean.getBoolean(propertyName);
        } catch (Exception e) {
            log.error("Error reading boolean", e);
            return defValue;
        }
    }

    private static void fatal(String message) {
        System.out.println(message);
        System.exit(1);
    }

}
