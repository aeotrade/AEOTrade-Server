package com.aeotrade.server.chain.config;

/**
 * @Author yewei
 * @Date 2022/5/31 18:42
 * @Description:
 * @Version 1.0
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String column;
    private boolean asc = true;

    public static OrderItem asc(String column) {
        return build(column, true);
    }

    public static OrderItem desc(String column) {
        return build(column, false);
    }

    public static List<OrderItem> ascs(String... columns) {
        return (List)Arrays.stream(columns).map(OrderItem::asc).collect(Collectors.toList());
    }

    public static List<OrderItem> descs(String... columns) {
        return (List)Arrays.stream(columns).map(OrderItem::desc).collect(Collectors.toList());
    }

    private static OrderItem build(String column, boolean asc) {
        return new OrderItem(column, asc);
    }

    public String getColumn() {
        return this.column;
    }

    public boolean isAsc() {
        return this.asc;
    }

    public void setColumn(final String column) {
        this.column = column;
    }

    public void setAsc(final boolean asc) {
        this.asc = asc;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof OrderItem)) {
            return false;
        } else {
            OrderItem other = (OrderItem)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.isAsc() != other.isAsc()) {
                return false;
            } else {
                Object this$column = this.getColumn();
                Object other$column = other.getColumn();
                if (this$column == null) {
                    if (other$column != null) {
                        return false;
                    }
                } else if (!this$column.equals(other$column)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof OrderItem;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.isAsc() ? 79 : 97);
        Object $column = this.getColumn();
        result = result * 59 + ($column == null ? 43 : $column.hashCode());
        return result;
    }

    public String toString() {
        return "OrderItem(column=" + this.getColumn() + ", asc=" + this.isAsc() + ")";
    }

    public OrderItem() {
    }

    public OrderItem(final String column, final boolean asc) {
        this.column = column;
        this.asc = asc;
    }
}
