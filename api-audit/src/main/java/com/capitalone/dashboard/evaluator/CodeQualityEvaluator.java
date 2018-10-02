package com.capitalone.dashboard.evaluator;

import com.capitalone.dashboard.common.CommonCodeReview;
import com.capitalone.dashboard.model.AuditException;
import com.capitalone.dashboard.model.CodeQuality;
import com.capitalone.dashboard.model.CodeQualityMetric;
import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorItemConfigHistory;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.Dashboard;
import com.capitalone.dashboard.repository.CodeQualityRepository;
import com.capitalone.dashboard.repository.CollItemConfigHistoryRepository;
import com.capitalone.dashboard.repository.CommitRepository;
import com.capitalone.dashboard.response.CodeQualityAuditResponse;
import com.capitalone.dashboard.status.CodeQualityAuditStatus;
import com.capitalone.dashboard.model.CodeQualityMetricType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Iterator;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Component
public class CodeQualityEvaluator extends Evaluator<CodeQualityAuditResponse> {

    private final CodeQualityRepository codeQualityRepository;
    private final CommitRepository commitRepository;
    private final CollItemConfigHistoryRepository collItemConfigHistoryRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeQualityEvaluator.class);


    @Autowired
    public CodeQualityEvaluator(CodeQualityRepository codeQualityRepository, CommitRepository commitRepository, CollItemConfigHistoryRepository collItemConfigHistoryRepository) {
        this.codeQualityRepository = codeQualityRepository;
        this.commitRepository = commitRepository;
        this.collItemConfigHistoryRepository = collItemConfigHistoryRepository;
    }


    @Override
    public Collection<CodeQualityAuditResponse> evaluate(Dashboard dashboard, long beginDate, long endDate, Map<?, ?> data) throws AuditException {

        List<CollectorItem> codeQualityItems = getCollectorItems(dashboard, "codeanalysis", CollectorType.CodeQuality);
        if (CollectionUtils.isEmpty(codeQualityItems)) {
            throw new AuditException("No code quality job configured", AuditException.NO_COLLECTOR_ITEM_CONFIGURED);
        }

        List<CollectorItem> repoItems = getCollectorItems(dashboard, "repo", CollectorType.SCM);

        Map<String, List<CollectorItem>> repoData = new HashMap<>();
        repoData.put("repos", repoItems);

        return codeQualityItems.stream().map(item -> evaluate(item, beginDate, endDate, repoData)).collect(Collectors.toList());
    }


    @Override
    public CodeQualityAuditResponse evaluate(CollectorItem collectorItem, long beginDate, long endDate, Map<?, ?> data) {
        List<CollectorItem> repoItems;
        if (!MapUtils.isEmpty(data) &&
                (data.get("repos") instanceof List) &&
                !CollectionUtils.isEmpty(Collections.singleton(data.get("repos"))) &&
                (!CollectionUtils.isEmpty((List) data.get("repos"))) &&
                (((List) data.get("repos")).get(0) instanceof CollectorItem)) {
            repoItems = (List<CollectorItem>) data.get("repos");

        } else {
            repoItems = new ArrayList<>();
        }
        return getStaticAnalysisResponse(collectorItem, repoItems, beginDate, endDate);
    }


    /**
     * Reusable method for constructing the CodeQualityAuditResponse object for a
     *
     * @return CodeQualityAuditResponse
     */
    private CodeQualityAuditResponse getStaticAnalysisResponse(CollectorItem collectorItem, List<CollectorItem> repoItems, long beginDate, long endDate) {
        CodeQualityAuditResponse codeQualityAuditResponse = new CodeQualityAuditResponse();
        if (collectorItem == null) return getNotConfigured();
        if (!isProjectIdValid(collectorItem)) return getErrorResponse(collectorItem);
        List<CodeQuality> codeQualities = codeQualityRepository.findByCollectorItemIdAndTimestampIsBetweenOrderByTimestampDesc(collectorItem.getId(), beginDate - 1, endDate + 1);
        ObjectMapper mapper = new ObjectMapper();

        if (CollectionUtils.isEmpty(codeQualities)) {

            return codeQualityDetailsForMissingStatus(collectorItem);
        } else {
            codeQualityAuditResponse.addAuditStatus(CodeQualityAuditStatus.CODE_QUALITY_CHECK_IS_CURRENT);
        }

        CodeQuality returnQuality = codeQualities.get(0);
        codeQualityAuditResponse.setUrl(returnQuality.getUrl());
        codeQualityAuditResponse.setCodeQuality(returnQuality);
        codeQualityAuditResponse.setLastExecutionTime(returnQuality.getTimestamp());
        List<String> metricNames = new ArrayList<>();
        for(CodeQualityMetric metricsName : returnQuality.getMetrics()){
            metricNames.add(metricsName.getName());
        }
        //If metric names not contain at least one of these {blocker_violations, critical_violations, test_success_density, coverage or new_coverage}
        if (!(metricNames.contains("blocker_violations") || metricNames.contains("critical_violations") || metricNames.contains("test_success_density")
                    || metricNames.contains("coverage") || metricNames.contains("new_coverage"))) {
                codeQualityAuditResponse.addAuditStatus(CodeQualityAuditStatus.CODE_QUALITY_AUDIT_FAIL);
        }
        for (CodeQualityMetric metric : returnQuality.getMetrics()){
            if (metric.getStatus() != null){
                /*this applies for sonar 5 style data for quality_gate_details
                 Set audit statuses if the threshold is met based on the status field of code_quality metric*/
                this.auditStatusWhenQualityGateDetailsNotFound(metric, codeQualityAuditResponse);
            }
            if (StringUtils.equalsIgnoreCase(metric.getName(), "quality_gate_details")) {
                codeQualityAuditResponse.addAuditStatus(CodeQualityAuditStatus.CODE_QUALITY_GATES_FOUND);
                if (metric.getStatus() == null) {
                    // this applies for sonar 6.7 style data for quality_gate_details
                    TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
                            };
                    Map<String, String> values;
                    try {
                        values = mapper.readValue((String) metric.getValue(), typeRef);
                        try {
                            JSONObject qualityGateDetails = (JSONObject) new JSONParser().parse(metric.getValue().toString());
                            JSONArray conditions = (JSONArray) qualityGateDetails.get("conditions");
                            Iterator itr = conditions.iterator();
                            // Iterate through the quality_gate conditions
                            while (itr.hasNext()) {
                                Map condition = ((Map) itr.next());
                                // Set audit statuses for Thresholds found if they are defined in quality_gate_details metric
                                this.auditStatusWhenQualityGateDetailsFound(condition, codeQualityAuditResponse);
                            }
                        } catch (ParseException e) {
                            LOGGER.error("Error in CodeQualityEvaluator.getStaticAnalysisResponse() - Unable to parse quality_gate metrics - " + e.getMessage());
                        }
                    } catch (IOException e){
                        LOGGER.error("Error in CodeQualityEvaluator.getStaticAnalysisResponse() - Unable to parse quality_gate metrics - " + e.getMessage());
                    }
                }
            }
        }
        codeQualityAuditResponse.addAuditStatus((StringUtils.containsIgnoreCase(codeQualityAuditResponse.getAuditStatuses().toString(), "CODE_QUALITY_AUDIT_FAIL"))?
                        CodeQualityAuditStatus.CODE_QUALITY_AUDIT_FAIL:CodeQualityAuditStatus.CODE_QUALITY_AUDIT_OK);
        List<CollectorItemConfigHistory> configHistories = getProfileChanges(returnQuality, beginDate, endDate);
        if (CollectionUtils.isEmpty(configHistories)) {
            codeQualityAuditResponse.addAuditStatus(CodeQualityAuditStatus.QUALITY_PROFILE_VALIDATION_AUDIT_NO_CHANGE);
            return codeQualityAuditResponse;
        }

        Set<String> codeAuthors = CommonCodeReview.getCodeAuthors(repoItems, beginDate, endDate, commitRepository);
        List<String> overlap = configHistories.stream().map(CollectorItemConfigHistory::getUserID).filter(codeAuthors::contains).collect(Collectors.toList());
        codeQualityAuditResponse.addAuditStatus(!CollectionUtils.isEmpty(overlap) ? CodeQualityAuditStatus.QUALITY_PROFILE_VALIDATION_AUDIT_FAIL : CodeQualityAuditStatus.QUALITY_PROFILE_VALIDATION_AUDIT_OK);

        return codeQualityAuditResponse;
    }

    private List<CollectorItemConfigHistory> getProfileChanges (CodeQuality codeQuality,long beginDate, long endDate)
    {
        return collItemConfigHistoryRepository
                .findByCollectorItemIdAndTimestampIsBetweenOrderByTimestampDesc(codeQuality.getCollectorItemId(), beginDate - 1, endDate + 1);
    }

    private CodeQualityAuditResponse getErrorResponse(CollectorItem codeQualityCollectorItem){
        CodeQualityAuditResponse missingInputResponse = new CodeQualityAuditResponse();
        missingInputResponse.addAuditStatus(CodeQualityAuditStatus.COLLECTOR_ITEM_ERROR);
        missingInputResponse.setLastUpdated(codeQualityCollectorItem.getLastUpdated());
        missingInputResponse.setUrl((String)codeQualityCollectorItem.getOptions().get("instanceUrl"));
        missingInputResponse.setMessage("Unable to collect scan results at this point - check Sonar project exist");
        missingInputResponse.setName((String)codeQualityCollectorItem.getOptions().get("projectName"));
        return  missingInputResponse;
    }

    private CodeQualityAuditResponse getNotConfigured(){
        CodeQualityAuditResponse notConfigured = new CodeQualityAuditResponse();
        notConfigured.addAuditStatus(CodeQualityAuditStatus.CODE_QUALITY_NOT_CONFIGURED);
        notConfigured.setMessage("Unable to register in Hygieia, Code Quality widget not configured invalid Sonar project reference");
        return  notConfigured;
    }

    /**
     *It will return response when codeQuality details not found in given date range.
     * @param codeQualityCollectorItem
     * @return missing details audit-response.
     */
    private CodeQualityAuditResponse codeQualityDetailsForMissingStatus(CollectorItem codeQualityCollectorItem ){
        CodeQualityAuditResponse detailsMissing =new CodeQualityAuditResponse();
        detailsMissing.addAuditStatus(CodeQualityAuditStatus.CODE_QUALITY_DETAIL_MISSING);
        detailsMissing.setLastUpdated(codeQualityCollectorItem.getLastUpdated());
        List<CodeQuality> codeQualities = codeQualityRepository.findByCollectorItemIdOrderByTimestampDesc(codeQualityCollectorItem.getId());
        for(CodeQuality returnCodeQuality:codeQualities) {
            detailsMissing.setUrl(returnCodeQuality.getUrl());
            detailsMissing.setName(returnCodeQuality.getName());
            detailsMissing.setLastExecutionTime(returnCodeQuality.getTimestamp());
        }
        return detailsMissing;
    }

    /*
     * This method will return audit statuses in preventive mode.
     * */
    private void auditStatusWhenQualityGateDetailsFound(Map condition, CodeQualityAuditResponse codeQualityAuditResponse) {
        if (StringUtils.equalsIgnoreCase(condition.get("metric").toString(), CodeQualityMetricType.BLOCKER_VIOLATIONS.getType())) {
            codeQualityAuditResponse.addAuditStatus(CodeQualityAuditStatus.CODE_QUALITY_THRESHOLD_BLOCKER_FOUND);
            codeQualityAuditResponse.addAuditStatus((StringUtils.equalsIgnoreCase(condition.get("level").toString(), "OK")? CodeQualityAuditStatus.CODE_QUALITY_THRESHOLD_BLOCKER_MET : CodeQualityAuditStatus.CODE_QUALITY_AUDIT_FAIL));
        } else if (StringUtils.equalsIgnoreCase(condition.get("metric").toString(), CodeQualityMetricType.CRITICAL_VIOLATIONS.getType())) {
            codeQualityAuditResponse.addAuditStatus(CodeQualityAuditStatus.CODE_QUALITY_THRESHOLD_CRITICAL_FOUND);
            codeQualityAuditResponse.addAuditStatus((StringUtils.equalsIgnoreCase(condition.get("level").toString(), "OK")? CodeQualityAuditStatus.CODE_QUALITY_THRESHOLD_CRITICAL_MET : CodeQualityAuditStatus.CODE_QUALITY_AUDIT_FAIL));
        } else if (StringUtils.equalsIgnoreCase(condition.get("metric").toString(), CodeQualityMetricType.UNIT_TEST.getType())) {
            codeQualityAuditResponse.addAuditStatus(CodeQualityAuditStatus.CODE_QUALITY_THRESHOLD_UNIT_TEST_FOUND);
            codeQualityAuditResponse.addAuditStatus((StringUtils.equalsIgnoreCase(condition.get("level").toString(), "OK")? CodeQualityAuditStatus.CODE_QUALITY_THRESHOLD_UNIT_TEST_MET : CodeQualityAuditStatus.CODE_QUALITY_AUDIT_FAIL));
        } else if (StringUtils.equalsIgnoreCase(condition.get("metric").toString(), CodeQualityMetricType.NEW_COVERAGE.getType())
                || StringUtils.equalsIgnoreCase(condition.get("metric").toString(), CodeQualityMetricType.COVERAGE.getType())) {
            codeQualityAuditResponse.addAuditStatus(CodeQualityAuditStatus.CODE_QUALITY_THRESHOLD_CODE_COVERAGE_FOUND);
            codeQualityAuditResponse.addAuditStatus((StringUtils.equalsIgnoreCase(condition.get("level").toString(), "OK")? CodeQualityAuditStatus.CODE_QUALITY_THRESHOLD_CODE_COVERAGE_MET : CodeQualityAuditStatus.CODE_QUALITY_AUDIT_FAIL));
        }

    }

    private void auditStatusWhenQualityGateDetailsNotFound(CodeQualityMetric metric, CodeQualityAuditResponse codeQualityAuditResponse) {
        if (StringUtils.equalsIgnoreCase(metric.getName(), CodeQualityMetricType.BLOCKER_VIOLATIONS.getType())) {
            codeQualityAuditResponse.addAuditStatus((StringUtils.equalsIgnoreCase(metric.getStatus().toString(), "OK")? CodeQualityAuditStatus.CODE_QUALITY_THRESHOLD_BLOCKER_MET:CodeQualityAuditStatus.CODE_QUALITY_AUDIT_FAIL));
        } else if (StringUtils.equalsIgnoreCase(metric.getName(), CodeQualityMetricType.CRITICAL_VIOLATIONS.getType())) {
            codeQualityAuditResponse.addAuditStatus((StringUtils.equalsIgnoreCase(metric.getStatus().toString(), "OK")? CodeQualityAuditStatus.CODE_QUALITY_THRESHOLD_CRITICAL_MET:CodeQualityAuditStatus.CODE_QUALITY_AUDIT_FAIL));
        } else if (StringUtils.equalsIgnoreCase(metric.getName(), CodeQualityMetricType.UNIT_TEST.getType())) {
            codeQualityAuditResponse.addAuditStatus((StringUtils.equalsIgnoreCase(metric.getStatus().toString(), "OK")? CodeQualityAuditStatus.CODE_QUALITY_THRESHOLD_UNIT_TEST_MET:CodeQualityAuditStatus.CODE_QUALITY_AUDIT_FAIL));
        } else if (StringUtils.equalsIgnoreCase(metric.getName(), CodeQualityMetricType.COVERAGE.getType())
                || StringUtils.equalsIgnoreCase(metric.getName(), CodeQualityMetricType.NEW_COVERAGE.getType())) {
            codeQualityAuditResponse.addAuditStatus((StringUtils.equalsIgnoreCase(metric.getStatus().toString(), "OK")? CodeQualityAuditStatus.CODE_QUALITY_THRESHOLD_CODE_COVERAGE_MET:CodeQualityAuditStatus.CODE_QUALITY_AUDIT_FAIL));
        }
    }

    private boolean isProjectIdValid(CollectorItem codeQualityCollectorItem) {
        return Optional.ofNullable(codeQualityCollectorItem.getOptions().get("projectId")).isPresent();
    }
}