package com.example.demo.repository;

import com.example.demo.model.BaseEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class InMemoryRepository<T extends BaseEntity> {

    private final Map<Long, T> items = new LinkedHashMap<>();
    private long nextId = 1;

    public List<T> findAll() {
        return new ArrayList<>(items.values());
    }

    public T findById(Long id) {
        return items.get(id);
    }

    public T save(T item) {
        if (item.id == null) {
            item.id = nextId++;
        }
        items.put(item.id, item);
        return item;
    }

    public void deleteById(Long id) {
        items.remove(id);
    }
}