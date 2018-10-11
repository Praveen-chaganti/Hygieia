package com.capitalone.dashboard.client.testexecution;

import com.atlassian.util.concurrent.Promise;
import com.capitalone.dashboard.client.api.domain.TestExecution;

public interface TestExecutionRestClient {
    Promise<Iterable<TestExecution.Test>> getTests(TestExecution key);
    public Promise<Void> setTests(TestExecution testExec);
    Promise<Void> removeTest(TestExecution testExecKey, TestExecution.Test testKey);

}
