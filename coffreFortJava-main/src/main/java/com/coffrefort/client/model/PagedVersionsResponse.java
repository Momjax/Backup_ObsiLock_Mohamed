package com.coffrefort.client.model;

import java.util.List;

public class PagedVersionsResponse {
    private List<VersionEntry> versions;
    private int total;
    private int offset;
    private int limit;

    public List<VersionEntry> getVersions() { return versions; }
    public int getTotal() { return total; }
    public int getOffset() { return offset; }
    public int getLimit() { return limit; }

    public void setVersions(List<VersionEntry> versions) { this.versions = versions; }
    public void setTotal(int total) { this.total = total; }
    public void setOffset(int offset) { this.offset = offset; }
    public void setLimit(int limit) { this.limit = limit; }
}
