package com.mall.common.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
public class Option<K, V> {

    private K label;

    private V value;

    public Option(){}

    public Option(K label, V value) {
        this.value = value;
        this.label = label;
    }

    public Option(K label, V value, List<Option<K, V>> children) {
        this.value = value;
        this.label = label;
    }

    public Option(K label, V value, String tag) {
        this.value = value;
        this.label = label;
    }
}
