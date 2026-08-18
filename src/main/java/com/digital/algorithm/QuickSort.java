package com.digital.algorithm;

import java.util.Arrays;

/**
 * 快速排序算法实现（基于比较的泛型版本）。
 *
 * <p>采用原地分区（in-place partition），以中间元素作为基准（pivot），
 * 避免对已排序或逆序输入退化到 O(n²) 的最坏情况。</p>
 *
 * <p>时间复杂度：平均 O(n log n)，最坏 O(n²)。
 * 空间复杂度：O(log n)（递归调用栈）。</p>
 *
 * @author DTCoder
 * @date 2025/08/18
 */
public final class QuickSort {

    /**
     * 对传入数组进行原地的升序快速排序。
     *
     * @param array 待排序数组，元素须实现 {@link Comparable}
     * @param <T>   元素类型，必须实现 {@link Comparable}
     * @throws IllegalArgumentException 当 array 为 null 时
     */
    public static <T extends Comparable<? super T>> void sort(T[] array) {
        if (array == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        quickSort(array, 0, array.length - 1);
    }

    /**
     * 对数组指定区间 [low, high] 执行快速排序。
     *
     * @param array 待排序数组
     * @param low   区间下界（含）
     * @param high  区间上界（含）
     * @param <T>   元素类型
     */
    private static <T extends Comparable<? super T>> void quickSort(T[] array, int low, int high) {
        if (low >= high) {
            return;
        }
        int pivotIndex = partition(array, low, high);
        quickSort(array, low, pivotIndex - 1);
        quickSort(array, pivotIndex + 1, high);
    }

    /**
     * 以中间元素为基准对区间 [low, high] 进行分区。
     *
     * <p>分区完成后，基准左侧元素均不大于基准，右侧元素均不小于基准。</p>
     *
     * @param array 待分区数组
     * @param low   区间下界（含）
     * @param high  区间上界（含）
     * @param <T>   元素类型
     * @return 分区完成后基准元素的最终下标
     */
    private static <T extends Comparable<? super T>> int partition(T[] array, int low, int high) {
        // 选取中间元素作为基准并交换到区间末端，避免对有序输入的最坏退化
        int mid = low + (high - low) / 2;
        swap(array, mid, high);

        T pivot = array[high];
        int i = low;
        for (int j = low; j < high; j++) {
            if (array[j].compareTo(pivot) <= 0) {
                swap(array, i, j);
                i++;
            }
        }
        swap(array, i, high);
        return i;
    }

    /**
     * 交换数组中两个位置的元素。
     *
     * @param array 目标数组
     * @param a     位置 a
     * @param b     位置 b
     * @param <T>   元素类型
     */
    private static <T> void swap(T[] array, int a, int b) {
        if (a == b) {
            return;
        }
        T temp = array[a];
        array[a] = array[b];
        array[b] = temp;
    }

    /**
     * 私有构造方法，禁止实例化工具类。
     */
    private QuickSort() {
    }

    // ==================== 入口演示 ====================

    /**
     * 可执行入口：对示例数组排序并打印结果。
     *
     * @param args 启动参数（未使用）
     */
    public static void main(String[] args) {
        Integer[] array = {5, 3, 8, 1, 9, 2, 7, 4, 6};
        sort(array);
        System.out.println(Arrays.toString(array));
    }
}
