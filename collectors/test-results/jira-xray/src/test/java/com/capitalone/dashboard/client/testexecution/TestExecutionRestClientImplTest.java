package com.capitalone.dashboard.client.testexecution;

import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.util.concurrent.Promise;
import com.capitalone.dashboard.client.api.domain.TestExecution;
import com.capitalone.dashboard.client.api.domain.TestRun;
import com.capitalone.dashboard.client.core.json.TestArrayJsonParser;

import com.capitalone.dashboard.model.Feature;
import com.capitalone.dashboard.model.TestCapability;
import com.capitalone.dashboard.model.TestCaseStatus;
import com.capitalone.dashboard.repository.FeatureRepository;
import com.capitalone.dashboard.repository.TestResultCollectorRepository;
import com.capitalone.dashboard.repository.TestResultRepository;
import com.capitalone.dashboard.util.FeatureCollectorConstants;
import com.capitalone.dashboard.util.TestResultSettings;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TestExecutionRestClientImpl.class)
public class TestExecutionRestClientImplTest {

    @Mock
    private TestExecution testExecution;
    @Mock
    private DisposableHttpClient httpClient;
    @Mock
    private Promise pr;
    @Mock
    FeatureRepository featureRepository;
    @Mock
    TestResultRepository testResultRepository;
    @Mock
    TestResultCollectorRepository testResultCollectorRepository;
    TestResultSettings testResultSettings;



    @Before
    public final void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        testResultSettings.setPageSize(5);
        TestExecution.Test test =  new TestExecution.Test(URI.create(""),"EA-3403",28775L,1,TestRun.Status.PASS);
        PowerMockito.when(pr.claim()).thenReturn(test);
        ObjectId objectId = new ObjectId("5af11dd28902ccb2d87fcdab");
        //PowerMockito.when(testResultCollectorRepository.findByName(FeatureCollectorConstants.JIRA_XRAY).getId()).thenReturn(objectId);



    }
    @Test
    public void getTests() throws Exception{
        testExecution = new TestExecution(URI.create(""), "EME-4644", 1977l);
        TestExecutionRestClientImpl mock = PowerMockito.spy(new TestExecutionRestClientImpl(URI.create(""),httpClient,testResultCollectorRepository,testResultRepository,featureRepository));
        PowerMockito.doReturn(pr).when(mock,"getAndParse",Matchers.any(URI.class),Matchers.any(TestArrayJsonParser.class));
        Promise<Iterable<TestExecution.Test>> testResult= mock.getTests(testExecution);
        Assert.assertNotNull(testResult.claim());
        System.out.println(testResult.claim());
    }

    @Test
    public void get() throws Exception {
        testExecution = new TestExecution(URI.create(""), "EME-4644", 1977l);
        TestExecution.Test test =  new TestExecution.Test(URI.create(""),"EA-3403",28775L,1,TestRun.Status.PASS);
        try {
            TestExecutionRestClientImpl mock = PowerMockito.spy(new TestExecutionRestClientImpl(URI.create(""),httpClient,testResultCollectorRepository,testResultRepository,featureRepository));
            Promise<Iterable<TestExecution>> testResult= mock.get(test);
            Assert.assertNotNull(testResult);
        }catch (Exception e){

        }
    }

    @Test
    public void updateMongoTest_ExecInformation(){
        TestExecutionRestClientImpl mock = PowerMockito.spy(new TestExecutionRestClientImpl(URI.create(""),httpClient,testResultCollectorRepository,testResultRepository,featureRepository));
        int count = mock.updateTestExecutionInformation();
        PowerMockito.when(featureRepository.getStoryByTeamID("503")).thenReturn(createFeature());
       // PowerMockito.when(mock.)
    }

    private List<Feature> createFeature() {
        List<Feature> features = new ArrayList<>();
        Feature feature1 = new Feature();
        feature1.setsTeamID("503");
        feature1.setsName("summary1001");
        feature1.setsProjectName("Hygieia");
        features.add(feature1);
        return features;
    }


    private List<TestCapability> createTestCapability(Iterable<TestExecution.Test> test,Feature feature){
        List<TestCapability> testCapabilities = new ArrayList<>();
        TestCapability testCapability = new TestCapability();
        testCapability.setDescription("summary1001");
        testCapability.setTotalTestSuiteCount(1);
        testCapability.setStatus(TestCaseStatus.Success);
        //testCapability.setTestSuites(this.c);
        testCapabilities.add(testCapability);
        return testCapabilities;
    }







}