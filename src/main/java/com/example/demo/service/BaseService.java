package com.example.demo.service;

import com.example.demo.model.BaseEntity;
import com.example.demo.repository.InMemoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public abstract class BaseService<T extends BaseEntity> {

    protected final InMemoryRepository<T> repository;

    protected BaseService(InMemoryRepository<T> repository) {
        this.repository = repository;
    }

    protected void beforeCreate(T item) {
    }

    protected void beforeDelete(Long id) {
    }

    protected abstract void patch(T target, T data);

    public List<T> getAll() {
        return repository.findAll();
    }

    public T getById(Long id) {
        T item = repository.findById(id);
        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return item;
    }

    public T create(T item) {
        item.id = null;
        beforeCreate(item);
        return repository.save(item);
    }

    public T update(Long id, T data) {
        T item = getById(id);
        patch(item, data);
        return repository.save(item);
    }

    public void delete(Long id) {
        getById(id);
        beforeDelete(id);
        repository.deleteById(id);
    }
}