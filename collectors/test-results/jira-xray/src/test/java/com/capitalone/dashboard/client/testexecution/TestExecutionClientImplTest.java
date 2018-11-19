
package com.capitalone.dashboard.client.testexecution;

import com.capitalone.dashboard.TestResultSettings;
import com.capitalone.dashboard.api.JiraXRayRestClient;
import com.capitalone.dashboard.api.domain.TestExecution;
import com.capitalone.dashboard.api.domain.TestRun;
import com.capitalone.dashboard.api.domain.TestStep;
import com.capitalone.dashboard.core.client.JiraXRayRestClientImpl;
import com.capitalone.dashboard.core.client.JiraXRayRestClientSupplier;
import com.capitalone.dashboard.core.client.TestExecutionRestClientImpl;
import com.capitalone.dashboard.core.client.testexecution.TestExecutionClient;
import com.capitalone.dashboard.core.client.testexecution.TestExecutionClientImpl;
import com.capitalone.dashboard.core.json.util.RendereableItem;
import com.capitalone.dashboard.core.json.util.RendereableItemImpl;
import com.capitalone.dashboard.model.TestCase;
import com.capitalone.dashboard.model.TestCaseStatus;
import com.capitalone.dashboard.model.TestCaseStep;
import com.capitalone.dashboard.model.Feature;

import com.capitalone.dashboard.repository.CollectorItemRepository;
import com.capitalone.dashboard.repository.FeatureRepository;
import com.capitalone.dashboard.repository.TestResultCollectorRepository;
import com.capitalone.dashboard.repository.TestResultRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
@PrepareForTest(TestExecutionRestClientImpl.class)
public class TestExecutionClientImplTest {

    private TestResultSettings testResultSettings;
    @Mock
    private TestResultRepository testResultRepository;
    @Mock
    private TestResultCollectorRepository testResultCollectorRepository;
    @Mock
    private FeatureRepository featureRepository;
    @Mock
    private CollectorItemRepository collectorItemRepository;

    @Mock
    private JiraXRayRestClientSupplier restClientSupplier;
    @Mock
    private JiraXRayRestClientImpl restClient;


    TestExecutionClientImpl testExecutionClientimpl;

    @Before
    public final void init(){
        MockitoAnnotations.initMocks(this);
        testResultSettings = new TestResultSettings();
        testExecutionClientimpl = new TestExecutionClientImpl(testResultRepository, testResultCollectorRepository, featureRepository, collectorItemRepository, testResultSettings, restClientSupplier);
        testResultSettings.setPageSize(20);
        restClient = new Mockito().mock(JiraXRayRestClientImpl.class);
    }

    @Test
    public void updateInformation(){
        TestExecution testExecution = new TestExecution(URI.create(""), "CRM-1985", 7689L);

       Mockito.when(featureRepository.getStoryByType("Test Execution")).thenReturn(createFeature());
      // Mockito.when(restClient.getTestExecutionClient().getTests(Matchers.anyObject()).claim()).thenReturn(createTests());
        int cnt = testExecutionClientimpl.updateTestResultInformation();
        Assert.assertEquals(1, cnt);

    }


    @Test
    public void getTestCases() {
        Iterable<TestStep> testSteps = new ArrayList<>();
        TestStep testStep = new TestStep(URI.create(""), "DEF678", 1234L, 1, null, null, null, TestStep.Status.PASS);
        ((ArrayList<TestStep>) testSteps).add(testStep);
        TestRun testRun = new TestRun(URI.create("myurl.com"), "Abc123", 3456L, TestRun.Status.PASS, null, null, null, null, testSteps);
        List<TestCase> testCases = testExecutionClientimpl.getTestCases(createTests(), createFeature().get(0));
        for(TestCase testCase : testCases){
            Assert.assertEquals(null, testCase.getId());
            Assert.assertEquals("DEF678","DEF678" );
            Assert.assertEquals(0, testCase.getTotalTestStepCount());
            Assert.assertEquals(0, testCase.getSuccessTestStepCount());
            Assert.assertEquals(TestCaseStatus.Unknown, testCase.getStatus());
        }


    }

    @Test
    public void validateGetTestSteps(){
        Iterable<TestStep> testSteps = new ArrayList<>();
        RendereableItem rendereableItem = new RendereableItemImpl("hello", "");
        TestStep testStep =new TestStep(URI.create(""), "", 1234L, 1,rendereableItem, null, null, TestStep.Status.PASS);
        ((ArrayList<TestStep>) testSteps).add(testStep);
        TestRun testRun = new TestRun(URI.create("myurl.com"), "Abc123", 3456L, TestRun.Status.PASS , null, null, null, null,testSteps);

        List<TestCaseStep> testCaseSteps = testExecutionClientimpl.getTestSteps(testRun);
        for(TestCaseStep step : testCaseSteps){
            Assert.assertEquals("1234", step.getId());
            Assert.assertEquals("hello", step.getDescription());
            Assert.assertEquals(TestCaseStatus.Success, step.getStatus());
        }
    }

    private List<Feature> createFeature() {
        List<Feature> features = new ArrayList<>();
        Feature feature1 = new Feature();
        //feature1.setsTeamID("503");
        feature1.setsName("summary1001");
        feature1.setsProjectName("Hygieia");
        feature1.setsTypeName("Test Execution");
        feature1.setsNumber("CAB1985");
        feature1.setsUrl("http://myurl.com");
        feature1.setsId("123");
        feature1.setsProjectName("Hygieia");
        features.add(feature1);
        return features;
    }

    private Iterable<TestExecution.Test> createTests(){
        Iterable<TestExecution.Test> tests = new ArrayList<>();
        TestExecution.Test test1 = new TestExecution.Test(URI.create("http://URL.com"), "DEF567", 12345L);
        TestExecution.Test test2 = new TestExecution.Test(URI.create("http://myurl.com"), "FOX123", 78901L);
        ((ArrayList<TestExecution.Test>) tests).add(test1);
        ((ArrayList<TestExecution.Test>) tests).add(test2);


        return tests;
    }




}
