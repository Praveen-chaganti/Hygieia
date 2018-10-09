package com.capitalone.dashboard.service;

import com.capitalone.dashboard.common.TestUtils;
import com.capitalone.dashboard.config.FongoConfig;
import com.capitalone.dashboard.config.TestConfig;
import com.capitalone.dashboard.model.AuditResult;
import com.capitalone.dashboard.repository.AuditResultRepository;
import com.capitalone.dashboard.response.AuditReviewResponse;
import com.capitalone.dashboard.response.DashboardReviewResponse;
import com.capitalone.dashboard.testutil.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class, FongoConfig.class})
@DirtiesContext
public class AuditResultServiceTest {

    @Autowired
    private AuditResultService auditResultService;
    @Autowired
    private AuditResultRepository auditResultRepository;

    @Before
    public void loadStuf() throws IOException{
        TestUtils.loadAuditResults(auditResultRepository);
    }

    @Test
    public void loadAuditResultsById(){
        AuditResult actual = getActualResponse(auditResultService.add

    }

    private ObjectId ObjectId(String s) {
    }

    private <T extends AuditResult> AuditResult getActualResponse (AuditResult response, Class<T> anyType) {
        Gson gson = GsonUtil.getGson();
        return gson.fromJson(gson.toJson(response), new TypeToken<DashboardReviewResponse<T>>(){}.getType());
    }


}
