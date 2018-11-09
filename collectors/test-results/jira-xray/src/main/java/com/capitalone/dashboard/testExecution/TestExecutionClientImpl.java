package com.capitalone.dashboard.testExecution;

import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.capitalone.dashboard.TestResultSettings;
import com.capitalone.dashboard.api.domain.Example;
import com.capitalone.dashboard.api.domain.TestExecution;
import com.capitalone.dashboard.api.domain.TestRun;
import com.capitalone.dashboard.api.domain.TestStep;
import com.capitalone.dashboard.core.async.AsyncXrayJiraRestClient;
import com.capitalone.dashboard.core.async.XrayRestAsyncRestClientFactory;
import com.capitalone.dashboard.model.*;
import com.capitalone.dashboard.repository.FeatureRepository;
import com.capitalone.dashboard.repository.TestResultCollectorRepository;
import com.capitalone.dashboard.repository.TestResultRepository;
import com.capitalone.dashboard.util.FeatureCollectorConstants;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TestExecutionClientImpl implements TestExecutionClient {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TestExecutionClientImpl.class);
    private final TestResultSettings testResultSettings = new TestResultSettings();
    private final TestResultRepository testResultRepository;
    private final TestResultCollectorRepository testResultCollectorRepository;
    private final FeatureRepository featureRepository;
    private AsyncXrayJiraRestClient restClient;
    private final XrayRestAsyncRestClientFactory factory=new XrayRestAsyncRestClientFactory();
    private final String uriLocation="";
    private final String username="";
    private final String password= "";

    public TestExecutionClientImpl(TestResultRepository testResultRepository, TestResultCollectorRepository testResultCollectorRepository, FeatureRepository featureRepository) {
        this.testResultRepository = testResultRepository;
        this.testResultCollectorRepository = testResultCollectorRepository;
        this.featureRepository = featureRepository;
    }


    public int updateTestResultInformation() {
        int count = 0;
        int pageSize = testResultSettings.getPageSize();

//        updateStatuses();

        boolean hasMore = true;
        for (int i = 0; hasMore; i += pageSize) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Obtaining story information starting at index " + i + "...");
            }
            long queryStart = System.currentTimeMillis();
            List<Feature> tests = featureRepository.getStoryByType("Test Execution");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Story information query took " + (System.currentTimeMillis() - queryStart) + " ms");
            }

            if (tests != null && !tests.isEmpty()) {
                updateMongoInfo(tests);
                LOGGER.info("***************** NUMBER OF TESTS: " + tests.size());
                count += tests.size();
            }

            LOGGER.info("Loop i " + i + " pageSize " + tests.size());

            // will result in an extra call if number of results == pageSize
            // but I would rather do that then complicate the jira client implementation
            if (tests == null || tests.size() > pageSize) {
                hasMore = false;
                break;
            }
        }

        return count;
    }

    /**
     * Updates the MongoDB with a JSONArray received from the source system
     * back-end with story-based data.
     *
     * @param currentPagedTestExecutions
     *            A list response of Jira issues from the source system
     */
    @SuppressWarnings({ "PMD.AvoidDeeplyNestedIfStmts", "PMD.NPathComplexity" })
    private void updateMongoInfo(List<Feature> currentPagedTestExecutions) {


        LOGGER.info("\n IN updateMongoInfo Method");
        LOGGER.info("\n TEST Execution SIZE: " + currentPagedTestExecutions.size());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Size of paged Jira response: " + (currentPagedTestExecutions == null? 0 : currentPagedTestExecutions.size()));
        }

        if (currentPagedTestExecutions != null) {
            List<TestResult> testResultsToSave = new ArrayList<>();
//            ObjectId jiraXRayFeatureId = testResultCollectorRepository.findByName(FeatureCollectorConstants.JIRA_XRAY).getId();

            for (Feature testExec : currentPagedTestExecutions) {

                TestResult testResult = new TestResult();

//                testResult.setCollectorItemId(jiraXRayFeatureId);
                testResult.setDescription(testExec.getsName());
                LOGGER.info("\n TEST Execution Name: " + testExec.getsName());
                LOGGER.info("\n TEST Execution Number: " + testExec.getsNumber());
                LOGGER.info("\n TEST Execution ID: " + testExec.getsId());

                testResult.setTargetAppName(testExec.getsProjectName());
                testResult.setType(TestSuiteType.Manual);
               try {
                    TestExecution testExecution = new TestExecution(new URI(testExec.getsUrl()), testExec.getsNumber(), Long.parseLong(testExec.getsId()));
                    LOGGER.info("\n TEST Execution KEY: " + testExecution.getKey());
                    testResult.setUrl(testExecution.getSelf().toString());
                   restClient= (AsyncXrayJiraRestClient) factory.createWithBasicHttpAuthentication(new URI(uriLocation),username,password);

                    Iterable<TestExecution.Test> tests = restClient.getTestExecutionClient().getTests(testExecution).claim();

                    int totalCount = (int) tests.spliterator().getExactSizeIfKnown();
                    LOGGER.info("\n TOTAL TESTS: " + totalCount);

                   Map<String,Integer> failandpasscount = this.getFailAndPassTestCount(testExec, tests);
                   int failCount = failandpasscount.get("FAIL_COUNT");
                   int passCount = failandpasscount.get("PASS_COUNT");

                   System.out.println("======="+failCount);
                   System.out.println("====="+passCount);


                   List<TestCapability> capabilities = new ArrayList<>();
                   TestCapability capability = new TestCapability();
                   capability.setDescription(testExec.getsName());
                   capability.setTotalTestSuiteCount(1);
                   capability.setType(TestSuiteType.Manual);
                   List<TestSuite> testSuites = new ArrayList<>();
                   TestSuite testSuite = new TestSuite();

                   testSuite.setDescription(testExec.getsName());
                   testSuite.setType(TestSuiteType.Manual);

                   testSuite.setTotalTestCaseCount(totalCount);
                   testSuite.setFailedTestCaseCount(failCount);
                   testSuite.setSuccessTestCaseCount(passCount);

                   int skipCount = totalCount - (failCount + passCount);
                   testSuite.setSkippedTestCaseCount(skipCount);

                   if(failCount > 0) {
                       capability.setStatus(TestCaseStatus.Failure);
                       testResult.setResultStatus("Failure");
                       testSuite.setStatus(TestCaseStatus.Failure);
                       testResult.setFailureCount(1);
                       capability.setFailedTestSuiteCount(1);
                   } else if (totalCount == passCount){
                       capability.setStatus(TestCaseStatus.Success);
                       testResult.setResultStatus("Success");
                       testSuite.setStatus(TestCaseStatus.Success);
                       testResult.setSuccessCount(1);
                       capability.setSuccessTestSuiteCount(1);
                   } else {
                       capability.setStatus(TestCaseStatus.Skipped);
                       testResult.setResultStatus("Skipped");
                       testSuite.setStatus(TestCaseStatus.Skipped);
                       testResult.setSkippedCount(1);
                       capability.setSkippedTestSuiteCount(1);
                   }
                   testSuite.setTestCases(this.getTestCases(tests,testExec));
                   testSuites.add(testSuite);
                   capability.setTestSuites(testSuites);
                   capabilities.add(capability);
                   testResult.setTestCapabilities(capabilities);
               } catch (URISyntaxException u) {
                    LOGGER.error("URI Syntax Invalid");
               }
                testResultsToSave.add(testResult);
            }

            // Saving back to MongoDB
            testResultRepository.save(testResultsToSave);
        }
    }
    private List<TestCase> getTestCases(Iterable<TestExecution.Test> tests, Feature testExec) {
        List<TestCase> testCases = new ArrayList<>();

        for (TestExecution.Test test : tests) {
            TestCase testCase = new TestCase();

            try {
               // TestRun testRun = new TestRun(new URI(""), test.getKey(), test.getId());
                TestRun testRun = restClient.getTestRunClient().getTestRun(testExec.getsNumber(), test.getKey()).claim();

                testCase.setId(testRun.getId().toString());
                testCase.setDescription(test.toString());
                int totalSteps = (int) testRun.getSteps().spliterator().getExactSizeIfKnown();
                Map<String,Integer> failandpassSteps = this.getFailAndPassStepCount(testRun);
                int failSteps = failandpassSteps.get("FAILSTEP_COUNT");
                int passSteps = failandpassSteps.get("PASSSTEP_COUNT");
                int skipSteps = failandpassSteps.get("SKIPSTEP_COUNT");
                int unknownSteps = failandpassSteps.get("UNKNOWNSTEP_COUNT");
                testCase.setTotalTestStepCount(totalSteps);
                testCase.setFailedTestStepCount(failSteps);
                testCase.setSuccessTestStepCount(passSteps);
                testCase.setSkippedTestStepCount(skipSteps);
                testCase.setUnknownStatusCount(unknownSteps);
                if(failSteps > 0) {
                    testCase.setStatus(TestCaseStatus.Failure);
                } else if (skipSteps > 0){
                    testCase.setStatus(TestCaseStatus.Skipped);
                } else if(passSteps > 0){
                    testCase.setStatus(TestCaseStatus.Success);
                } else {
                    testCase.setStatus(TestCaseStatus.Unknown);
                }

                testCase.setTestSteps(this.getTestSteps(testRun));

            } catch (Exception e) {

            }
            testCases.add(testCase);
        }

        return testCases;
    }

    private List<TestCaseStep> getTestSteps(TestRun testRun) {

        List<TestCaseStep> testSteps = new ArrayList<>();

        for (TestStep testStep : testRun.getSteps()) {
            TestCaseStep testCaseStep = new TestCaseStep();

            testCaseStep.setId(testStep.getId().toString());
            testCaseStep.setDescription(testStep.getStep().getRaw());
            if (testStep.getStatus().toString().equals("PASS")) {
                testCaseStep.setStatus(TestCaseStatus.Success);
            } else if (testStep.getStatus().toString().equals("FAIL")) {
                testCaseStep.setStatus(TestCaseStatus.Failure);
            } else {
                testCaseStep.setStatus(TestCaseStatus.Skipped);
            }
            testSteps.add(testCaseStep);
        }



        return testSteps;
    }


    private Map<String,Integer> getFailAndPassTestCount(Feature testExec ,Iterable<TestExecution.Test> tests) {

        Map<String,Integer> map = new HashMap<String,Integer>(4);
        int failCount = 0;
        int passCount = 0;
        int skipCount = 0;
        int unknownCount = 0;

        for (TestExecution.Test test : tests) {
            try {
                TestRun testRun = restClient.getTestRunClient().getTestRun(testExec.getsNumber(), test.getKey()).claim();
                if (testRun.getStatus().toString().equals("FAIL")) {
                    failCount++;
                    //System.out.println("*****************Failcount"+count);
                }else if (testRun.getStatus().toString().equals("PASS")) {
                    passCount++;
                    //System.out.println("*****************passCount"+count);
                }else if (testRun.getStatus().toString().equals("SKIP")){
                    skipCount++;
                }else{
                    unknownCount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        map.put("FAIL_COUNT",failCount);
        map.put("PASS_COUNT",passCount);
        map.put("SKIP_COUNT",skipCount);
        map.put("UNKNOWN_COUNT",unknownCount);
        return map;
    }
    private Map<String,Integer> getFailAndPassStepCount(TestRun testRun) {
        Map<String,Integer> map = new HashMap<>(4);
        int failStepCount = 0, passStepCount = 0, skipStepCount = 0, unknownStepCount = 0;
        long start = System.currentTimeMillis();
        for (TestStep testStep : testRun.getSteps()) {

            if (testStep.getStatus().toString().equals("PASS")) {
                passStepCount++;
            } else if (testStep.getStatus().toString().equals("FAIL")) {
                failStepCount++;
            } else if (testStep.getStatus().equals("SKIP")){
                skipStepCount++;
            } else{
                unknownStepCount++;
            }
        }
        map.put("FAILSTEP_COUNT",failStepCount);
        map.put("PASSSTEP_COUNT",passStepCount);
        map.put("SKIPSTEP_COUNT",skipStepCount);
        map.put("UNKNOWNSTEP_COUNT",unknownStepCount);

        return map;
    }

    /**
     * Retrieves the maximum change date for a given query.
     *
     * @return A list object of the maximum change date
     */
    public String getMaxChangeDate() {
        String data = null;

        try {
            List<Feature> response = featureRepository
                    .findTopByCollectorIdAndChangeDateGreaterThanOrderByChangeDateDesc(
                            testResultCollectorRepository.findByName(FeatureCollectorConstants.JIRA_XRAY).getId(),
                            testResultSettings.getDeltaStartDate());
            if ((response != null) && !response.isEmpty()) {
                data = response.get(0).getChangeDate();
            }
        } catch (Exception e) {
            LOGGER.error("There was a problem retrieving or parsing data from the local "
                    + "repository while retrieving a max change date\nReturning null", e);
        }

        return data;
    }
}
