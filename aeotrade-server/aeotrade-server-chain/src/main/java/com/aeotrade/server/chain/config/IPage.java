package com.aeotrade.server.chain.config;

/**
 * @Author yewei
 * @Date 2022/5/31 18:44
 * @Description:
 * @Version 1.0
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface IPage<T> extends Serializable {
    List<OrderItem> orders();

    /** @deprecated */
    @Deprecated
    default Map<Object, Object> condition() {
        return null;
    }

    default boolean optimizeCountSql() {
        return true;
    }

    default boolean isSearchCount() {
        return true;
    }

    default long offset() {
        long current = this.getCurrent();
        return current <= 1L ? 0L : (current - 1L) * this.getSize();
    }

    default Long maxLimit() {
        return null;
    }

    default long getPages() {
        if (this.getSize() == 0L) {
            return 0L;
        } else {
            long pages = this.getTotal() / this.getSize();
            if (this.getTotal() % this.getSize() != 0L) {
                ++pages;
            }

            return pages;
        }
    }

    default IPage<T> setPages(long pages) {
        return this;
    }

    /** @deprecated */
    @Deprecated
    default void hitCount(boolean hit) {
    }

    /** @deprecated */
    @Deprecated
    default boolean isHitCount() {
        return false;
    }

    List<T> getRecords();

    IPage<T> setRecords(List<T> records);

    long getTotal();

    IPage<T> setTotal(long total);

    long getSize();

    IPage<T> setSize(long size);

    long getCurrent();

    IPage<T> setCurrent(long current);



    default String countId() {
        return null;
    }

    /** @deprecated */
    @Deprecated
    default String cacheKey() {
        StringBuilder key = new StringBuilder();
        key.append(this.offset()).append(":").append(this.getSize());
        List<OrderItem> orders = this.orders();
        if (CollectionUtils.isNotEmpty(orders)) {
            Iterator var3 = orders.iterator();

            while(var3.hasNext()) {
                OrderItem item = (OrderItem)var3.next();
                key.append(":").append(item.getColumn()).append(":").append(item.isAsc());
            }
        }

        return key.toString();
    }
}
