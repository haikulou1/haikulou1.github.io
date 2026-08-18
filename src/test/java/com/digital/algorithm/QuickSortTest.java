package com.digital.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * QuickSort 单元测试
 *
 * @author DTCoder
 * @date 2025/08/18
 */
class QuickSortTest {

    // ==================== sort 测试 ====================

    @Test
    @DisplayName("给定无序整型数组，排序后应为升序")
    void should_sortAscending_when_unsortedIntegerArray() {
        // Arrange (Given)
        Integer[] array = {5, 3, 8, 1, 9, 2, 7, 4, 6};

        // Act (When)
        QuickSort.sort(array);

        // Assert (Then)
        assertThat(array).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Test
    @DisplayName("给定已升序数组，排序后应保持不变")
    void should_keepOrder_when_alreadySorted() {
        // Arrange (Given)
        Integer[] array = {1, 2, 3, 4, 5};

        // Act (When)
        QuickSort.sort(array);

        // Assert (Then)
        assertThat(array).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @DisplayName("给定逆序数组，排序后应为升序")
    void should_sortAscending_when_reverseOrder() {
        // Arrange (Given)
        Integer[] array = {5, 4, 3, 2, 1};

        // Act (When)
        QuickSort.sort(array);

        // Assert (Then)
        assertThat(array).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @DisplayName("含重复元素的数组排序后应稳定升序")
    void should_sortAscending_when_duplicateElements() {
        // Arrange (Given)
        Integer[] array = {3, 1, 2, 3, 1, 2};

        // Act (When)
        QuickSort.sort(array);

        // Assert (Then)
        assertThat(array).containsExactly(1, 1, 2, 2, 3, 3);
    }

    @Test
    @DisplayName("单个元素数组排序后保持不变")
    void should_keepAsIs_when_singleElement() {
        // Arrange (Given)
        Integer[] array = {42};

        // Act (When)
        QuickSort.sort(array);

        // Assert (Then)
        assertThat(array).containsExactly(42);
    }

    @Test
    @DisplayName("空数组排序后仍为空")
    void should_keepEmpty_when_emptyArray() {
        // Arrange (Given)
        Integer[] array = {};

        // Act (When)
        QuickSort.sort(array);

        // Assert (Then)
        assertThat(array).isEmpty();
    }

    @Test
    @DisplayName("字符串数组应按字典序升序排序")
    void should_sortByNaturalOrder_when_stringArray() {
        // Arrange (Given)
        String[] array = {"banana", "apple", "cherry"};

        // Act (When)
        QuickSort.sort(array);

        // Assert (Then)
        assertThat(array).containsExactly("apple", "banana", "cherry");
    }

    @Test
    @DisplayName("负数与正数混合数组应正确升序排序")
    void should_sortAscending_when_mixedNegativeAndPositive() {
        // Arrange (Given)
        Integer[] array = {0, -3, 5, -1, 2};

        // Act (When)
        QuickSort.sort(array);

        // Assert (Then)
        assertThat(array).containsExactly(-3, -1, 0, 2, 5);
    }

    @Test
    @DisplayName("较大规模随机数组排序后应与系统排序结果一致")
    void should_matchSystemSort_when_largeRandomArray() {
        // Arrange (Given)
        Integer[] array = {9, 7, 8, 5, 6, 3, 4, 1, 2, 0,
                15, 13, 14, 11, 12, 19, 17, 18, 16, 10};
        Integer[] expected = array.clone();
        Arrays.sort(expected);

        // Act (When)
        QuickSort.sort(array);

        // Assert (Then)
        assertThat(array).containsExactly(expected);
    }

    @Test
    @DisplayName("传入 null 数组应抛出 IllegalArgumentException")
    void should_throwException_when_arrayIsNull() {
        // Act & Assert (When & Then)
        assertThatThrownBy(() -> QuickSort.sort((Integer[]) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("array");
    }
}
