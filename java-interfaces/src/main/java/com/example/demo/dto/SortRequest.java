package com.example.demo.dto;

/**
 * 排序接口请求体，用于 bubble-sort 与 quick-sort 接口。
 * <p>
 * 契约：{@code {"arr":[3,1,2]}}（整数数组）。
 */
public class SortRequest {

    private int[] arr;

    public int[] getArr() {
        return arr;
    }

    public void setArr(int[] arr) {
        this.arr = arr;
    }
}
