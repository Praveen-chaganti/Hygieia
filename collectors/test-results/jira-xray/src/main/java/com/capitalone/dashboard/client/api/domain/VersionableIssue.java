package com.capitalone.dashboard.client.api.domain;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;

import java.net.URI;

public abstract class VersionableIssue<T> extends BasicIssue implements Versionable<T> {
    private int version=0;
    private T oldVersion=null;

    public VersionableIssue(URI self, String key, Long id) {
        super(self, key, id);
    }

    public T getOldVersion() {
        return oldVersion;
    }

    public void setOldVersion(T oldVersion) {
    if(this.version==0) {
        this.oldVersion = oldVersion;
        this.version=1;
    }
    }

    public int getVersion() {
        return version;
    }

    public abstract T clone() throws CloneNotSupportedException;
}
