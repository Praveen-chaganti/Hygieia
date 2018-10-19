package com.capitalone.dashboard.service;

import com.capitalone.dashboard.common.TestUtils;
import com.capitalone.dashboard.config.FongoConfig;
import com.capitalone.dashboard.config.TestConfig;
import com.capitalone.dashboard.model.AuditResult;
import com.capitalone.dashboard.model.AuditType;
import com.capitalone.dashboard.repository.AuditResultRepository;
import com.capitalone.dashboard.testutil.GsonUtil;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class, FongoConfig.class})
@DirtiesContext
public class AuditResultServiceTest {

    @Autowired
    private AuditResultService auditResultService;
    @Autowired
    private AuditResultRepository auditResultRepository;

    @Before
    public void loadStuff() throws IOException{
        TestUtils.loadAuditResults(auditResultRepository);
    }

    @Test
    public void loadAuditResultByTitle() throws IOException{
        Iterable<AuditResult> actual =  auditResultService.getAuditResultsByDBoardTitle("Sample");
        List<ObjectId> dashboardIds =new ArrayList<>();
        actual.forEach(auditResult ->{
            auditResult.getDashboardTitle().equalsIgnoreCase("");
            Assert.assertEquals(true, auditResult.getDashboardTitle().equalsIgnoreCase("Sample"));
            dashboardIds.add(auditResult.getDashboardId());
        });
        Assert.assertEquals(2, dashboardIds.size());
    }

    @Test
    public void loadAuditResultsByServAndAppNames(){
        Iterable<AuditResult> actual = auditResultService.getAuditResultsByServAndAppNames("ASV", "BAP");
        actual.forEach(auditResult -> {
            Assert.assertEquals("BAP", auditResult.getConfigItemBusAppName());
            Assert.assertEquals("ASV", auditResult.getConfigItemBusServName());
        });
    }

    @Test
    public void loadAuditResultsByServAndAppNamesAndAuditTypes(){
        Iterable<AuditResult> actual = auditResultService.getAuditResultsByServAndAppNamesAndAuditType("ASV", "BAP", AuditType.TEST_RESULT);
        actual.forEach(auditResult -> {
            Assert.assertEquals("BAP", auditResult.getConfigItemBusAppName());
            Assert.assertEquals("ASV", auditResult.getConfigItemBusServName());
            Assert.assertEquals(AuditType.TEST_RESULT, auditResult.getAuditType());
        });
    }

    @Test
    public void loadAuditResultsById() throws IOException {
        AuditResult actual = getActualResponse(auditResultService.getAuditResult(new ObjectId("5bbcd5efeee33c09f4e97569")));
        AuditResult expected = getExpectedReviewResponse("AuditResults.json");
        assertDashboardAudit(actual, expected);
        System.out.println();

    }
    @Test
    public void loadAuditResultsByAuditType(){
        Pageable pageable = new PageRequest(0, 2);

        Iterable<AuditResult> actual = auditResultService.getAuditResultsByAuditType(AuditType.CODE_QUALITY, pageable);
        actual.forEach(auditResult -> {

            Assert.assertEquals(AuditType.CODE_QUALITY, auditResult.getAuditType());
        });
        Assert.assertEquals(2,Iterables.size(actual));
    }

    @Test
    public void loadAuditResultsByALL(){
        Pageable pageable =new PageRequest(0, 4);
        List<ObjectId> dashboardIDs =new ArrayList<>();
        Iterable<AuditResult> actual =auditResultService.getAuditResultsAll(pageable);
        actual.forEach(auditResult ->{
            dashboardIDs.add(auditResult.getId());
        });
        Assert.assertEquals(4, dashboardIDs.size());
    }

    @Test
    public void getAuditResultsByLineOfBus(){
        Pageable pageable = new PageRequest(0, 4);
        Iterable<AuditResult> actual =auditResultService.getAuditResultsByLineOfBus("Bus", pageable);
        actual.forEach(auditResult -> {
            Assert.assertEquals("Bus", auditResult.getLineOfBusiness());
        });
        Assert.assertEquals(4,Iterables.size(actual));
    }

    @Test
    public void getAuditResultsByLineOfBusAndAuditType(){
        Pageable pageable = new PageRequest(0, 2);
        Iterable<AuditResult> actual =auditResultService.getAuditResultsByLineOfBusAndAuditType("Bus", AuditType.CODE_QUALITY, pageable);
        actual.forEach(auditResult -> {
            Assert.assertEquals("Bus", auditResult.getLineOfBusiness());
            Assert.assertEquals(AuditType.CODE_QUALITY,auditResult.getAuditType() );
        });
        Assert.assertEquals(2,Iterables.size(actual));
    }




    private String getExpectedJSON(String fileName) throws IOException {
        String path = "./expected/" + fileName;
        URL fileUrl = Resources.getResource(path);
        return IOUtils.toString(fileUrl);
    }

    private <T extends AuditResult> AuditResult getExpectedReviewResponse (String fileName) throws IOException {
        Gson gson = GsonUtil.getGson();
        return gson.fromJson(getExpectedJSON(fileName), new TypeToken<AuditResult>(){}.getType());
    }

    private <T extends AuditResult> AuditResult getActualResponse(AuditResult response) {
        Gson gson = GsonUtil.getGson();
        return gson.fromJson(gson.toJson(response), new TypeToken<AuditResult>(){}.getType());
    }

    private void assertDashboardAudit(AuditResult lhs, AuditResult rhs) {
        assertThat(lhs.getId()).isEqualTo(rhs.getId());
        assertThat(lhs.getDashboardId()).isEqualTo(rhs.getDashboardId());
        assertThat(lhs.getDashboardTitle()).isEqualTo(rhs.getDashboardTitle());
        assertThat(lhs.getAuditDetails()).isEqualTo(rhs.getAuditDetails());
        assertThat(lhs.getAuditType()).isEqualTo(rhs.getAuditType());
        assertThat(lhs.getAuditTypeStatus()).isEqualTo(rhs.getAuditTypeStatus());
        assertThat(lhs.getConfigItemBusAppName()).isEqualTo(rhs.getConfigItemBusAppName());
        assertThat(lhs.getConfigItemBusAppOwner()).isEqualTo(rhs.getConfigItemBusAppOwner());
        assertThat(lhs.getConfigItemBusServName()).isEqualTo(rhs.getConfigItemBusServName());
        assertThat(lhs.getConfigItemBusServOwner()).isEqualTo(rhs.getConfigItemBusServOwner());
    }


}