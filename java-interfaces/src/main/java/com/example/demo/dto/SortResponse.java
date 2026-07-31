package com.example.demo.dto;

/**
 * 排序接口响应体。
 * <p>
 * 契约：{@code {"sorted":[1,2,3]}}。
 */
public class SortResponse {

    private int[] sorted;

    public SortResponse() {
    }

    public SortResponse(int[] sorted) {
        this.sorted = sorted;
    }

    public int[] getSorted() {
        return sorted;
    }

    public void setSorted(int[] sorted) {
        this.sorted = sorted;
    }
}
