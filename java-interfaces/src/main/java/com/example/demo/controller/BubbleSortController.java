package com.example.demo.controller;

import com.example.demo.dto.SortRequest;
import com.example.demo.dto.SortResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 冒泡排序接口。
 * <p>
 * POST /api/bubble-sort
 * 入参：{@code {"arr":[5,2,8,1,9]}}
 * 出参：{@code {"sorted":[1,2,5,8,9]}}
 *
 * <p>算法约束：必须为冒泡排序（相邻元素两两比较交换），升序；
 * 不得替换为 {@link java.util.Arrays#sort} 等内置排序。
 *
 * <p>边界：空数组返回空数组；单元素原样返回；{@code arr} 为 null 返回 400。
 */
@RestController
@RequestMapping("/api")
public class BubbleSortController {

    @PostMapping("/bubble-sort")
    public SortResponse bubbleSort(@RequestBody SortRequest request) {
        if (request.getArr() == null) {
            throw new IllegalArgumentException("arr must not be null");
        }
        int[] arr = request.getArr().clone();   // 不修改入参对象
        bubbleSort(arr);
        return new SortResponse(arr);
    }

    /**
     * 原地冒泡排序（升序）：相邻元素两两比较交换。
     * 加入「本轮无交换则提前结束」优化，结果仍为标准冒泡排序。
     */
    private static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    int tmp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = tmp;
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }
}
