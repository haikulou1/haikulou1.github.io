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
 * <p>边界：空数组返回空数组；单元素原样返回；{@code arr} 为 null 返回 400；
 * {@code arr} 长度超过 {@value MAX_ARRAY_LENGTH} 返回 400（防 OOM/DoS）。
 */
@RestController
@RequestMapping("/api")
public class QuickSortController {

    /** 入参数组长度上限，防止超大数组导致 OOM/DoS。 */
    private static final int MAX_ARRAY_LENGTH = 10000;

    /**
     * 对入参数组执行快速排序（升序）。
     *
     * @param request 排序请求，包含整型数组 {@code arr}
     * @return 排序后的升序数组包装为 {@link SortResponse}
     */
    @PostMapping("/quick-sort")
    public SortResponse quickSort(@RequestBody SortRequest request) {
        if (request.getArr() == null) {
            throw new IllegalArgumentException("arr must not be null");
        }
        if (request.getArr().length > MAX_ARRAY_LENGTH) {
            throw new IllegalArgumentException("arr length must not exceed " + MAX_ARRAY_LENGTH);
        }
        int[] arr = request.getArr().clone();   // 不修改入参对象
        quickSort(arr, 0, arr.length - 1);
        return new SortResponse(arr);
    }

    /**
     * 原地快速排序（升序）：分治 + 三数取中 Lomuto 分区。
     *
     * @param arr  待排序数组（原地修改）
     * @param low  分区下界（含）
     * @param high 分区上界（含）
     */
    private static void quickSort(int[] arr, int low, int high) {
        if (low >= high) {
            return;
        }
        int p = partition(arr, low, high);
        quickSort(arr, low, p - 1);
        quickSort(arr, p + 1, high);
    }

    /**
     * Lomuto 分区：以「三数取中」选择基准，规避已排序/逆序输入触发的最坏递归深度（G14）。
     *
     * @param arr  待分区数组
     * @param low  分区下界（含）
     * @param high 分区上界（含）
     * @return 基准最终落位下标
     */
    private static int partition(int[] arr, int low, int high) {
        int mid = medianOfThreeIndex(arr, low, high);
        swap(arr, mid, high);
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    /**
     * 三数取中：比较首、中、尾元素，返回其中值元素的下标，作为基准候选。
     *
     * @param arr  数组
     * @param low  下界
     * @param high 上界
     * @return 三者中值元素的下标
     */
    private static int medianOfThreeIndex(int[] arr, int low, int high) {
        int mid = low + (high - low) / 2;
        // 保证返回「中间值」的下标：通过两两比较排序 low/mid/high
        if (arr[low] > arr[mid]) {
            swap(arr, low, mid);
        }
        if (arr[low] > arr[high]) {
            swap(arr, low, high);
        }
        if (arr[mid] > arr[high]) {
            swap(arr, mid, high);
        }
        // 此时 arr[mid] 为三者的中值
        return mid;
    }

    /**
     * 交换数组中两个位置的元素。
     *
     * @param arr 数组
     * @param i   位置 i
     * @param j   位置 j
     */
    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}
