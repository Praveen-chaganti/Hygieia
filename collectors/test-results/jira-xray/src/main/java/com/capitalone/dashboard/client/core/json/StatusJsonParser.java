package com.capitalone.dashboard.client.core.json;

import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.capitalone.dashboard.client.api.domain.TestRun;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Created by lucho on 17/08/16.
 */
public class StatusJsonParser implements JsonObjectParser<TestRun.Status> {
    public TestRun.Status parse(JSONObject jsonObject) throws JSONException {
        throw new IllegalArgumentException("NOT IMPLEMENTED YET");
    }
}
