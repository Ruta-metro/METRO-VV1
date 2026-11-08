package com.metroruta.repository;

import com.metroruta.model.Cierre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CierreRepository extends JpaRepository<Cierre, Long> {

    // para borrar el registro de cierres de una estacion cuando se elimina
    @Transactional
    void deleteByEstacionId(Long estacionId);
}
