package com.capitalone.dashboard.client;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.capitalone.dashboard.client.test.TestRestClient;
import com.capitalone.dashboard.client.testexecution.TestExecutionRestClient;
import com.capitalone.dashboard.client.testrun.TestRunRestClient;
import com.capitalone.dashboard.client.testset.TestSetRestClient;

public interface JiraXRayRestClient extends JiraRestClient {

    public TestRestClient getTestClient();
    public TestExecutionRestClient getTestExecutionClient();
    public TestRunRestClient getTestRunClient();
    public TestSetRestClient getTestSetClient();

}
