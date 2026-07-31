package com.example.demo.controller;

import com.example.demo.dto.SortRequest;
import com.example.demo.dto.SortResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 快速排序接口（第三个接口，依据设计文档 U1 自主决策定案）。
 * <p>
 * POST /api/quick-sort
 * 入参：{@code {"arr":[3,1,2]}}
 * 出参：{@code {"sorted":[1,2,3]}}
 *
 * <p>与冒泡排序契约对称，复用同一 DTO，便于算法对比。
 *
 * <p>算法约束：必须为快速排序（分治、选基准、分区），升序；
 * 不得替换为 {@link java.util.Arrays#sort} 等内置排序。
 *
 * <p>边界：空数组返回空数组；单元素原样返回；{@code arr} 为 null 返回 400。
 */
@RestController
@RequestMapping("/api")
public class QuickSortController {

    @PostMapping("/quick-sort")
    public SortResponse quickSort(@RequestBody SortRequest request) {
        if (request.getArr() == null) {
            throw new IllegalArgumentException("arr must not be null");
        }
        int[] arr = request.getArr().clone();   // 不修改入参对象
        quickSort(arr, 0, arr.length - 1);
        return new SortResponse(arr);
    }

    /**
     * 原地快速排序（升序）：分治 + Lomuto 分区。
     */
    private static void quickSort(int[] arr, int low, int high) {
        if (low >= high) {
            return;
        }
        int p = partition(arr, low, high);
        quickSort(arr, low, p - 1);
        quickSort(arr, p + 1, high);
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                int tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
            }
        }
        int tmp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = tmp;
        return i + 1;
    }
}
