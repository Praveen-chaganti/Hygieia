package com.capitalone.dashboard.model;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a collection of test suites that have been executed. This may include
 * a unit test run, a security scan, static analysis, functional tests,
 * manual acceptance tests or bug reports.
 *
 * Possible Collectors:
 *  Hudson (in scope)
 *  ALM
 *  Various build system test results
 *
 */
@Document(collection="test_results")
@CompoundIndexes({
        @CompoundIndex(name = "test_results_collItemId_ts_idx", def = "{'collectorItemId' : 1, 'timestamp': -1}")
})
public class TestResult extends BaseModel {
    /**
     * ID of {@link CollectorItem}
     */
    private ObjectId collectorItemId;

    /**
     * Creation timestamp
     */
    private long timestamp;

    /**
     * Identifies this test result in the source system
     */
    private String executionId;

    //Object ID of the build event - if say build happens via Jenkins
    private ObjectId buildId;
    /**
     * Description of the test result that might make sense to a human
     */
    private String description;

    /**
     * Optional URL to test results
     */
    private String url;

    /**
     * Start test execution time {@link java.util.Date#getTime()}
     */
    private long startTime;

    /**
     * End test execution time {@link java.util.Date#getTime()}
     */
    private long endTime;

    /**
     * Test duration in milliseconds
     */
    private long duration;

    /**
     * Count of test cases that failed in all test suites
     */
    private int failureCount;

    /**
     * Count of test cases that generated an error in all test suites
     */
    private int successCount;

    /**
     * Count of test cases that were skipped in all test suites
     */
    private int skippedCount;

    /**
     * The total number of test cases in all test suites
     */
    private int totalCount;

    private int unknownStatusCount;

    /**
     * Type of test
     */
    private TestSuiteType type;

    private String targetAppName;
    private String targetEnvName;

    private String resultStatus;


    /**
     * Collection of {@link TestSuite}s generated by this TestResult.
     */
    private Collection<TestCapability> testCapabilities = new ArrayList<>();

    public ObjectId getCollectorItemId() {
        return collectorItemId;
    }

    public void setCollectorItemId(ObjectId collectorItemId) {
        this.collectorItemId = collectorItemId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public int getUnknownStatusCount() {
        return unknownStatusCount;
    }

    public void setUnknownStatusCount(int unknownStatusCount) {
        this.unknownStatusCount = unknownStatusCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public Collection<TestCapability> getTestCapabilities() {
        return testCapabilities;
    }

    public void setTestCapabilities(Collection<TestCapability> testCapabilities) { this.testCapabilities = testCapabilities; }

    public ObjectId getBuildId() {
        return buildId;
    }

    public void setBuildId(ObjectId buildId) {
        this.buildId = buildId;
    }

    public TestSuiteType getType() {
        return type;
    }

    public void setType(TestSuiteType type) {
        this.type = type;
    }

    public String getTargetAppName() {
        return targetAppName;
    }

    public void setTargetAppName(String targetAppName) {
        this.targetAppName = targetAppName;
    }

    public String getTargetEnvName() {
        return targetEnvName;
    }

    public void setTargetEnvName(String targetEnvName) {
        this.targetEnvName = targetEnvName;
    }


    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }


}
