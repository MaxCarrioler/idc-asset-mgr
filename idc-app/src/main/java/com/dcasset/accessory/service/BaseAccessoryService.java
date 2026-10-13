package com.dcasset.accessory.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

public abstract class BaseAccessoryService<E, D> {

    protected abstract JpaRepository<E, Long> getRepository();

    protected abstract Supplier<E> entitySupplier();

    protected abstract void copyDtoToEntity(D dto, E entity);

    protected abstract void copyEntityToDto(E entity, D dto);

    protected abstract List<E> findByServerId(Long serverId);

    protected abstract String notFoundMessage(Long id);

    public List<D> listByServerId(Long serverId) {
        return findByServerId(serverId).stream().map(this::toDto).toList();
    }

    public D getById(Long id) {
        return toDto(findEntity(id));
    }

    @Transactional
    public D create(D dto) {
        E entity = entitySupplier().get();
        copyDtoToEntity(dto, entity);
        setIdNull(entity);
        return toDto(getRepository().save(entity));
    }

    @Transactional
    public D update(Long id, D dto) {
        E entity = findEntity(id);
        copyDtoToEntity(dto, entity);
        return toDto(getRepository().save(entity));
    }

    @Transactional
    public void delete(Long id) {
        getRepository().delete(findEntity(id));
    }

    protected E findEntity(Long id) {
        return getRepository().findById(id)
                .orElseThrow(() -> new IllegalArgumentException(notFoundMessage(id)));
    }

    protected D toDto(E entity) {
        D dto = newDto();
        copyEntityToDto(entity, dto);
        return dto;
    }

    protected abstract D newDto();

    protected abstract void setIdNull(E entity);
}
