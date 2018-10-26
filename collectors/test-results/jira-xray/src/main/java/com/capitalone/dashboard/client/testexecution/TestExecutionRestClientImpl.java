package com.capitalone.dashboard.client.testexecution;

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousSearchRestClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.util.concurrent.Promise;
import com.capitalone.dashboard.client.api.domain.TestRun;
import com.capitalone.dashboard.client.api.domain.TestStep;
import com.capitalone.dashboard.model.Feature;
import com.capitalone.dashboard.model.TestResult;
import com.capitalone.dashboard.model.TestCapability;
import com.capitalone.dashboard.model.TestSuite;
import com.capitalone.dashboard.model.TestSuiteType;
import com.capitalone.dashboard.model.TestCase;
import com.capitalone.dashboard.model.TestCaseStatus;
import com.capitalone.dashboard.model.TestCaseStep;
import com.capitalone.dashboard.repository.FeatureRepository;
import com.capitalone.dashboard.repository.TestResultCollectorRepository;
import com.capitalone.dashboard.repository.TestResultRepository;
import com.capitalone.dashboard.util.ClientUtil;
import com.capitalone.dashboard.util.DateUtil;
import com.capitalone.dashboard.util.FeatureCollectorConstants;
import com.capitalone.dashboard.util.TestResultSettings;
import com.google.common.base.Function;
import com.capitalone.dashboard.client.api.domain.TestExecution;
import com.capitalone.dashboard.client.core.PluginConstants;
import com.capitalone.dashboard.client.core.json.TestArrayJsonParser;
import com.capitalone.dashboard.client.core.json.gen.TestExecUpdateJsonGenerator;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestExecutionRestClientImpl extends AbstractAsynchronousRestClient implements TestExecutionRestClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecutionRestClientImpl.class);
    private static final ClientUtil TOOLS = ClientUtil.getInstance();
    private URI baseUri;
    private final  TestArrayJsonParser testsParser=new TestArrayJsonParser();
    private final static TestExecUpdateJsonGenerator execUpdateGenerator=new TestExecUpdateJsonGenerator();
    private final TestResultSettings testResultSettings = new TestResultSettings();
    private final TestResultRepository testResultRepository;
    private final TestResultCollectorRepository testResultCollectorRepository;
    private final FeatureRepository featureRepository;

    private SearchRestClient searchRestClient=null;

    private final DateFormat SETTINGS_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public TestExecutionRestClientImpl(URI serverUri, DisposableHttpClient httpClient, TestResultCollectorRepository testResultCollectorRepository, TestResultRepository testResultRepository, FeatureRepository featureRepository){
        super(httpClient);
        baseUri = UriBuilder.fromUri(serverUri).path("/rest/raven/{restVersion}/api/").build(PluginConstants.XRAY_REST_VERSION);
        searchRestClient=new AsynchronousSearchRestClient(UriBuilder.fromUri(serverUri).path("rest/api/latest/").build(new Object[0]),httpClient);

//        this.testResultSettings = testResultSettings;
        this.testResultCollectorRepository = testResultCollectorRepository;
        this.featureRepository = featureRepository;
        this.testResultRepository = testResultRepository;
    }

    /**
     * Explicitly updates queries for the source system, and initiates the
     * update to MongoDB from those calls.
     */
    public int updateTestExecutionInformation() {
        int count = 0;
        testResultSettings.setPageSize(5);
        int pageSize = testResultSettings.getPageSize();
        System.out.println(pageSize);

//        updateStatuses();

        boolean hasMore = true;
        for (int i = 0; hasMore; i += pageSize) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Obtaining story information starting at index " + i + "...");
            }
            long queryStart = System.currentTimeMillis();
            List<Feature> tests = featureRepository.getStoryByType("Test Execution");
            System.out.println(tests);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Story information query took " + (System.currentTimeMillis() - queryStart) + " ms");
            }

            if (tests != null && !tests.isEmpty()) {
                updateMongoInfo(tests);
                count += tests.size();
            }

            LOGGER.info("Loop i " + i + " pageSize " + tests.size());

            // will result in an extra call if number of results == pageSize
            // but I would rather do that then complicate the jira client implementation
            if (tests == null || tests.size() < pageSize) {
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
        final TestResultSettings testResultSettings = new TestResultSettings();


        LOGGER.info("\n IN updateMongoInfo Method");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Size of paged Jira response: " + (currentPagedTestExecutions == null? 0 : currentPagedTestExecutions.size()));
        }

        if (currentPagedTestExecutions != null) {
            List<TestResult> testResultsToSave = new ArrayList<>();
           // ObjectId jiraXRayFeatureId = testResultCollectorRepository.findByName(FeatureCollectorConstants.JIRA_XRAY).getId();
            ObjectId jiraXRayFeatureId = new ObjectId("abcdef0123456789abcdef01");
            for (Feature testExec : currentPagedTestExecutions) {

                TestResult testResult = new TestResult();

                testResult.setCollectorItemId(jiraXRayFeatureId);
                testResult.setDescription(testExec.getsName());
                testResult.setTargetAppName(testExec.getsProjectName());
                testResult.setType(TestSuiteType.Manual);
                try {
                   // TestExecution testExecution = new TestExecution(new URI(""), testExec.getsNumber(), Long.parseLong(testExec.getsId()));
                    TestExecution testExecution = new TestExecution(URI.create(""), "EME-4644", 1977l);
                    TestExecution.Test test =  new TestExecution.Test(URI.create(""),"EA-3403",28775L,1,TestRun.Status.PASS);
                    testResult.setUrl(testExecution.getSelf().toString());

                    Iterable<TestExecution.Test> tests = this.getTests(testExecution).claim();

                    int totalCount = (int) tests.spliterator().getExactSizeIfKnown();
                    int failCount = this.getTestCount(tests, "FAIL");
                    int passCount = this.getTestCount(tests, "PASS");
                    testResult.setTotalCount(totalCount);
                    testResult.setFailureCount(failCount);
                    testResult.setSuccessCount(passCount);

                    int skipCount = totalCount - (failCount + passCount);
                    testResult.setSkippedCount(skipCount);

                    if(failCount > 0) {
                        testResult.setResultStatus(TestCaseStatus.Failure.toString());
                    } else if (totalCount == passCount){
                        testResult.setResultStatus(TestCaseStatus.Success.toString());
                    } else {
                        testResult.setResultStatus(TestCaseStatus.Skipped.toString());
                    }

                    testResult.setTestCapabilities(this.getCapabilities(tests, testExec));
                } catch (Exception e) {
                    LOGGER.error("URI Syntax Invalid");
                }
                testResultsToSave.add(testResult);
            }

            // Saving back to MongoDB
            testResultRepository.save(testResultsToSave);
        }
    }

    private List<TestCapability> getCapabilities(Iterable<TestExecution.Test> tests, Feature testExec) {
        List<TestCapability> capabilities = new ArrayList<>();
        TestCapability capability = new TestCapability();
        capability.setDescription(testExec.getsName());

        int totalCount = (int) tests.spliterator().getExactSizeIfKnown();
        int failCount = this.getTestCount(tests, "FAIL");
        int passCount = this.getTestCount(tests, "PASS");
        capability.setTotalTestSuiteCount(1);
        capability.setType(TestSuiteType.Manual);

        if(failCount > 0) {
            capability.setStatus(TestCaseStatus.Failure);
            capability.setFailedTestSuiteCount(1);
        } else if (totalCount == passCount){
            capability.setStatus(TestCaseStatus.Success);
            capability.setSuccessTestSuiteCount(1);
        } else {
            capability.setStatus(TestCaseStatus.Skipped);
            capability.setSkippedTestSuiteCount(1);
        }
        capability.setTestSuites(this.getTestSuites(tests, testExec));
        capabilities.add(capability);

        return capabilities;
    }

    private List<TestSuite> getTestSuites(Iterable<TestExecution.Test> tests, Feature testExec) {
        List<TestSuite> testSuites = new ArrayList<>();
        TestSuite testSuite = new TestSuite();

        testSuite.setDescription(testExec.getsName());
        testSuite.setType(TestSuiteType.Manual);
        int totalCount = (int) tests.spliterator().getExactSizeIfKnown();
        int failCount = this.getTestCount(tests, "FAIL");
        int passCount = this.getTestCount(tests, "PASS");
        testSuite.setTotalTestCaseCount(totalCount);
        testSuite.setFailedTestCaseCount(failCount);
        testSuite.setSuccessTestCaseCount(passCount);

        int skipCount = totalCount - (failCount + passCount);
        testSuite.setSkippedTestCaseCount(skipCount);

        if(failCount > 0) {
            testSuite.setStatus(TestCaseStatus.Failure);
        } else if (totalCount == passCount){
            testSuite.setStatus(TestCaseStatus.Success);
        } else {
            testSuite.setStatus(TestCaseStatus.Skipped);
        }

        testSuite.setTestCases(this.getTestCases(tests));
        testSuites.add(testSuite);

        return testSuites;
    }

    private List<TestCase> getTestCases(Iterable<TestExecution.Test> tests) {
        List<TestCase> testCases = new ArrayList<>();

        for (TestExecution.Test test : tests) {
            TestCase testCase = new TestCase();

            try {
                TestRun testRun = new TestRun(new URI(""), test.getKey(), test.getId());

                testCase.setId(testRun.getId().toString());
                testCase.setDescription(test.toString());
                int totalSteps = (int) testRun.getSteps().spliterator().getExactSizeIfKnown();
                int failSteps = this.getStepCount(testRun, "FAIL");
                int passSteps = this.getStepCount(testRun, "PASS");
                int skipSteps = totalSteps - (failSteps + passSteps);
                testCase.setTotalTestStepCount(totalSteps);
                testCase.setFailedTestStepCount(failSteps);
                testCase.setSuccessTestStepCount(passSteps);
                testCase.setSkippedTestStepCount(skipSteps);
                if(failSteps > 0) {
                    testCase.setStatus(TestCaseStatus.Failure);
                } else if (totalSteps == passSteps){
                    testCase.setStatus(TestCaseStatus.Success);
                } else {
                    testCase.setStatus(TestCaseStatus.Skipped);
                }

                testCase.setTestSteps(this.getTestSteps(testRun));

            } catch (URISyntaxException u) {
                LOGGER.error("URI Syntax Invalid");
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
            if (testStep.getStatus().equals("PASS")) {
                testCaseStep.setStatus(TestCaseStatus.Success);
            } else if (testStep.getStatus().equals("FAIL")) {
                testCaseStep.setStatus(TestCaseStatus.Failure);
            } else {
                testCaseStep.setStatus(TestCaseStatus.Skipped);
            }
            testSteps.add(testCaseStep);
        }



        return testSteps;
    }

    private int getTestCount(Iterable<TestExecution.Test> tests, String statusType) {
        int count = 0;

        for (TestExecution.Test test : tests) {
            try {
                TestRun testRun = new TestRun(new URI(""), test.getKey(), test.getId());
                if (statusType == "FAIL") {
                    if (testRun.getStatus().equals("FAIL")) {
                        count++;
                    }
                } else if (statusType == "PASS") {
                    if (testRun.getStatus().equals("PASS")) {
                        count++;
                    }
                }
            } catch (URISyntaxException u) {
                LOGGER.error("URI Syntax Invalid");
            }
        }

        return count;
    }

    private int getStepCount(TestRun testRun, String statusType) {
        int count = 0;

        for (TestStep testStep : testRun.getSteps()) {
            if (statusType == "FAIL") {
                if (testStep.getStatus().equals("FAIL")) {
                    count++;
                }
            } else if (statusType == "PASS") {
                if (testStep.getStatus().equals("PASS")) {
                    count++;
                }
            }
        }

        return count;
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

    private String getChangeDateMinutePrior(String changeDateISO) {
        int priorMinutes = this.testResultSettings.getScheduledPriorMin();
        return DateUtil.toISODateRealTimeFormat(DateUtil.getDatePriorToMinutes(
                DateUtil.fromISODateTimeFormat(changeDateISO), priorMinutes));
    }

    public Promise<Iterable<TestExecution.Test>> getTests(TestExecution testExecution) {
        UriBuilder uriBuilder= UriBuilder.fromUri(baseUri);
        uriBuilder.path("testexec").path("{isssue-key}").path("test");
        return this.getAndParse(uriBuilder.build(testExecution.getKey()),this.testsParser);
    }


    /**
     * Adds/Removes the test associated with this test execution
     * @param testExec
     * @return
     */
    public Promise<Void> setTests(TestExecution testExec) {
        UriBuilder uriBuilder= UriBuilder.fromUri(baseUri);
        LOGGER.info("\n IN setTests Method");
        this.updateMongoInfo(new ArrayList<>());

        uriBuilder.path("testexec").path("{isssue-key}").path("test");
        return this.postAndParse(uriBuilder.build(testExec.getKey()), testExec, execUpdateGenerator, new JsonObjectParser<Void>() {
            public Void parse(JSONObject jsonObject) throws JSONException {
                return null;
            }
        });
    }

    /**
     * Removes a test from the test execution identified by his test key
     * @param testExecKey
     * @param testKey
     * @return
     */
    public Promise<Void> removeTest(TestExecution testExecKey, TestExecution.Test testKey) {
        UriBuilder uriBuilder= UriBuilder.fromUri(baseUri);
        uriBuilder.path("testexec").path("{isssue-key}").path("test").path("{ŧest-key");
        return this.delete(uriBuilder.build(testExecKey.getKey(),testKey.getKey()));
    }


    /**
     * Method who queries the JQL testTestExecution() from the X-RAY plugin. Returning the test-executions related to this test identified by his key.
     * @param test Test containin the test key to search for.
     * @return A list of test execution promises which runs this test.
     */
    // TODO: MOVE THIS METHOD TO A BOUNDARY
    public Promise<Iterable<TestExecution>> get(TestExecution.Test test){
        if(test.getKey()==null){
            throw new IllegalArgumentException("KEY NOT SET");
        }
        Promise<SearchResult> searchResultPromise= searchRestClient.searchJql("issue in testTestExecutions(\""+test.getKey()+"\") ");
        return searchResultPromise.map(new Function<SearchResult, Iterable<TestExecution>>() {
            public Iterable<TestExecution> apply(@Nullable SearchResult searchResult) {
                ArrayList<TestExecution> testExceList=new ArrayList<TestExecution>();
                for(Issue testExcecIssue : searchResult.getIssues()){
                    testExceList.add(new TestExecution(testExcecIssue.getSelf(),testExcecIssue.getKey(),testExcecIssue.getId()));
                }
                return testExceList;
            }
        });
    }
}
