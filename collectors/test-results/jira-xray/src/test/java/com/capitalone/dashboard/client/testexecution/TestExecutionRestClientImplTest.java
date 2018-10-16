package com.capitalone.dashboard.client.testexecution;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.util.concurrent.Promise;
import com.capitalone.dashboard.client.JiraXRayRestClientImpl;
import com.capitalone.dashboard.client.JiraXRayRestClientSupplier;
import com.capitalone.dashboard.client.api.domain.TestExecution;
import com.capitalone.dashboard.client.api.domain.TestRun;
import com.capitalone.dashboard.client.core.PluginConstants;
import com.capitalone.dashboard.client.core.json.TestArrayJsonParser;
import com.capitalone.dashboard.client.core.json.gen.TestExecUpdateJsonGenerator;
import com.capitalone.dashboard.util.FeatureCollectorConstants;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.beans.binding.When;
import org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.commons.collections4.iterators.IteratorIterable;
import org.codehaus.jettison.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertNotNull;

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
    @Mock
    private TestArrayJsonParser testArrayJsonParser;
    @Mock
    private TestExecUpdateJsonGenerator testExecUpdateJsonGenerator;
    //@InjectMocks
    private  TestExecutionRestClientImpl testExecutionRestClient;



    @Before
    public final void init() throws Exception {
       MockitoAnnotations.initMocks(this);
       // restClient = (JiraXRayRestClientImpl) restClientSupplier.get();

        //Mockito.when(restClient.getTestExecutionClient().getTests(testExecution).claim()).thenReturn(null);
    }

    @Test
    public void TestExecutionRestClientImplGetTests() throws Exception{
        testExecution = new TestExecution(URI.create(""), "EME-4644", 1977l);
        TestExecution.Test test = new TestExecution.Test(URI.create(""),"EA-3403",28775L,1,TestRun.Status.PASS);
        TestExecutionRestClientImpl mock = Mockito.spy(new TestExecutionRestClientImpl(URI.create(""),httpClient));

        //Iterable<TestExecution.Test> test = restClient.getTestExecutionClient().getTests(testExecution).claim();
       //Mockito.when(mock.getAndParse(Matchers.anyObject(),Matchers.anyObject())).thenReturn((Promise<Object>) test);
        try {
            Promise<Iterable<TestExecution.Test>> result = mock.getTests(testExecution);
            System.out.println("hi"+result.claim());


        }catch(Exception e){

        }


    }




}
