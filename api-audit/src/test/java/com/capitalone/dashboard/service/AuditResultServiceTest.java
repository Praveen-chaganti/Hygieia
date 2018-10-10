package com.capitalone.dashboard.service;

import com.capitalone.dashboard.common.TestUtils;
import com.capitalone.dashboard.config.FongoConfig;
import com.capitalone.dashboard.config.TestConfig;
import com.capitalone.dashboard.model.AuditResult;
import com.capitalone.dashboard.repository.AuditResultRepository;
import com.capitalone.dashboard.testutil.GsonUtil;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.validation.constraints.AssertTrue;
import java.io.IOException;
import java.net.URL;

import static junit.framework.TestCase.assertTrue;
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
    public void loadAuditResultsById() throws IOException {
        AuditResult actual = getActualResponse(auditResultService.findById(new ObjectId("5bbcd5efeee33c09f4e97569")));
        AuditResult expected = getExpectedReviewResponse("AuditResults.json");
        assertDashboardAudit(actual, expected);
    }

    @Test
    public void loadAuditResultByTitle() throws IOException{
        Iterable<AuditResult> actual =  auditResultService.findByDashboardTitle("Sample");
        actual.forEach(auditResult ->{
            auditResult.getDashboardTitle().equalsIgnoreCase("");
            Assert.assertEquals(true, auditResult.getDashboardTitle().equalsIgnoreCase("Sample"));
        });
    }

    @Test
    public void loadAuditResultsByALL(){


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
