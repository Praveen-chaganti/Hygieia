package com.capitalone.dashboard.client.core.json;

import com.atlassian.jira.rest.client.internal.json.JsonArrayParser;
import com.capitalone.dashboard.client.api.domain.TestExecution;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import java.util.ArrayList;

public class TestArrayJsonParser implements JsonArrayParser<Iterable<TestExecution.Test>> {
    private final static TestJsonParser tParser=new TestJsonParser();

    public Iterable<TestExecution.Test> parse(JSONArray jsonArray) throws JSONException {
        ArrayList<TestExecution.Test> tests=new ArrayList<TestExecution.Test>();
        for(int i=0;i< jsonArray.length();i++){
            tests.add(tParser.parse(jsonArray.getJSONObject(i)));
        }
    return tests;
    }
}
