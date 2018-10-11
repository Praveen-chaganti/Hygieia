package com.capitalone.dashboard.client.api.domain;

public interface Versionable<T> {

    public T getOldVersion();
    public void setOldVersion(T oldVersion);

    public int getVersion();
}
