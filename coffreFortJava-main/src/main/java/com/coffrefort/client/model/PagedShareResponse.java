package com.coffrefort.client.model;

import java.util.List;

public class PagedShareResponse {
    private List<ShareItem> shares;
    private int total;
    private int offset;
    private int limit;

    public List<ShareItem> getShares() {return shares;}
    public int getTotal() {return total;}
    public int getOffset() {return offset;}
    public int getLimit() {return limit;}
}
