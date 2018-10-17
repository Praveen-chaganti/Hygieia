package com.capitalone.dashboard.client.testrun;

import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.capitalone.dashboard.client.api.domain.TestRun;
import com.capitalone.dashboard.client.core.json.TestArrayJsonParser;
import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TestRunRestClientImpl.class)
public class TestRunRestClientImplTest {
    private final String TEST_EXEC_KEY="EME-4944";
    private final String TEST_KEY="EME-1683";
    private final long TEST_ID=507571;

    @Mock
    private DisposableHttpClient httpClient;
    @Mock
    private Promise pr;

    @Before
    public final void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestRun testRun = new TestRun(URI.create(""), "EME-1683", 507571L, TestRun.Status.PASS, null, null, "jqm884", "jqm884", null, null, null, null, null);
        Promise pr =new Promise() {
            @Override
            public Object claim() {
                return testRun;
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
    }
    @Test
    public void getTestRunsByTestExecKeyAndTestKey() throws Exception {

        TestRunRestClientImpl mock = PowerMockito.spy(new TestRunRestClientImpl(URI.create(""),httpClient));
        PowerMockito.doReturn(pr).when(mock,"getAndParse",Matchers.anyObject(),Matchers.any(TestArrayJsonParser.class));
        Promise<TestRun> testruns = mock.getTestRun(TEST_EXEC_KEY, TEST_KEY);
        Assert.assertNotNull(testruns);

    }
    @Test
    public void  getTestRunByTestRunId()throws Exception{
        TestRunRestClientImpl mock = PowerMockito.spy(new TestRunRestClientImpl(URI.create(""),httpClient));
        PowerMockito.doReturn(pr).when(mock,"getAndParse",Matchers.any(URI.class),Matchers.any(TestArrayJsonParser.class));
        Promise<TestRun> testruns = mock.getTestRun(TEST_ID);
        Assert.assertNotNull(testruns);
    }

}
