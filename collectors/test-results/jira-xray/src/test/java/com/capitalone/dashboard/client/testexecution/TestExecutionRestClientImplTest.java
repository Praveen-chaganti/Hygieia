package com.capitalone.dashboard.client.testexecution;

import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.capitalone.dashboard.client.api.domain.TestExecution;
import com.capitalone.dashboard.client.api.domain.TestRun;
import com.capitalone.dashboard.client.core.json.TestArrayJsonParser;
import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TestExecutionRestClientImpl.class)
public class TestExecutionRestClientImplTest {

    @Mock
    private TestExecution testExecution;
    @Mock
    private DisposableHttpClient httpClient;

    @Before
    public final void init() throws Exception {
        MockitoAnnotations.initMocks(this);

    }
    @Test
    public void getTests() throws Exception{
        testExecution = new TestExecution(URI.create(""), "EME-4644", 1977l);
        TestExecution.Test test =  new TestExecution.Test(URI.create(""),"EA-3403",28775L,1,TestRun.Status.PASS);
        TestExecutionRestClientImpl mock = PowerMockito.spy(new TestExecutionRestClientImpl(URI.create(""),httpClient));
        Promise p =new Promise() {
            @Override
            public Object claim() {
                return test;
            }

            @Override
            public Promise done(Effect effect) {
                return null;
            }

            @Override
            public Promise fail(Effect effect) {
                return null;
            }

            @Override
            public Promise then(FutureCallback futureCallback) {
                return null;
            }

            @Override
            public Promise map(Function function) {
                return null;
            }

            @Override
            public Promise flatMap(Function function) {
                return null;
            }

            @Override
            public Promise recover(Function function) {
                return null;
            }

            @Override
            public Promise fold(Function function, Function function1) {
                return null;
            }

            @Override
            public void addListener(Runnable runnable, Executor executor) {

            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public Object get() throws InterruptedException, ExecutionException {
                return null;
            }

            @Override
            public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };
        PowerMockito.doReturn(p).when(mock,"getAndParse",Matchers.any(URI.class),Matchers.any(TestArrayJsonParser.class));
        Promise<Iterable<TestExecution.Test>> testResult= mock.getTests(testExecution);
        Assert.assertNotNull(testResult);
        }




}
