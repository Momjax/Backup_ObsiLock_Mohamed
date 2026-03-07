package com.coffrefort.client.model;

import java.util.List;

public class PagedFilesResponse {
    private List<FileEntry> files;
    private int total;
    private int offset;
    private int limit;

    public List<FileEntry> getFiles() { return files; }
    public int getTotal() { return total; }
    public int getOffset() { return offset; }
    public int getLimit() { return limit; }

    public void setFiles(List<FileEntry> files) { this.files = files; }
    public void setTotal(int total) { this.total = total; }
    public void setOffset(int offset) { this.offset = offset; }
    public void setLimit(int limit) { this.limit = limit; }
}