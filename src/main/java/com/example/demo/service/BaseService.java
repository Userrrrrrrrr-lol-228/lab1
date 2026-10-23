package com.example.demo.service;

import com.example.demo.model.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public abstract class BaseService<T extends BaseEntity> {

    protected final JpaRepository<T, Long> repository;

    protected BaseService(JpaRepository<T, Long> repository) {
        this.repository = repository;
    }

    protected void beforeCreate(T item) {
    }

    protected abstract void patch(T target, T data);

    public List<T> getAll() {
        return repository.findAll();
    }

    public T getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
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
        repository.deleteById(id); // связанные записи удаляет БД (ON DELETE CASCADE)
    }
}
