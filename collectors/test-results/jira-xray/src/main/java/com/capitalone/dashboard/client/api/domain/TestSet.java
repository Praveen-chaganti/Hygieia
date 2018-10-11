package com.capitalone.dashboard.client.api.domain;

import java.net.URI;

public class TestSet extends VersionableIssue<TestSet> {

    public TestSet(URI self, String key, Long id) {
        super(self, key, id);
    }

    @Override
    public TestSet clone() throws CloneNotSupportedException {
        TestSet myTestSet=new TestSet(getSelf(),getKey(),getId());

        return null;
    }
}
