package com.capitalone.dashboard.client.testexecution;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.util.concurrent.Promise;
import com.capitalone.dashboard.client.JiraXRayRestClientImpl;
import com.capitalone.dashboard.client.JiraXRayRestClientSupplier;
import com.capitalone.dashboard.client.api.domain.TestExecution;
import com.capitalone.dashboard.client.api.domain.TestRun;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

@RunWith(MockitoJUnitRunner.class)
public class TestExecutionRestClientImplTest {
    @Mock
    private AbstractAsynchronousRestClient abstractAsynchronousRestClient;
    @Mock
    private JiraXRayRestClientImpl restClient;
    @Mock
    private TestExecution testExecution;
    @Mock
    private DisposableHttpClient httpClient;

    private final JiraXRayRestClientSupplier restClientSupplier=new JiraXRayRestClientSupplier();



    @Before
    public final void init() throws Exception {
        MockitoAnnotations.initMocks(this);

    }
    @Test
    public void TestExecutionRestClientImplGetTests() throws Exception{
        testExecution = new TestExecution(URI.create(""), "EME-4644", 1977l);
        TestExecution.Test test = new TestExecution.Test(URI.create(""),"EA-3403",28775L,1,TestRun.Status.PASS);
        TestExecutionRestClientImpl mock = Mockito.spy(new TestExecutionRestClientImpl(URI.create(""),httpClient));

        //Iterable<TestExecution.Test> test = restClient.getTestExecutionClient().getTests(testExecution).claim();

        try {
            Iterable<TestExecution.Test> result = mock.getTests(testExecution).claim();
            Mockito.when(mock.getTests(testExecution).claim()).thenReturn((Iterable<TestExecution.Test>) test);
            Assert.assertNotNull(result);



        }catch(Exception e){

        }


    }




}
