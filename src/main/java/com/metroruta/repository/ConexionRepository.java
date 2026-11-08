package com.metroruta.repository;

import com.metroruta.model.Conexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ConexionRepository extends JpaRepository<Conexion, Long> {

    // para borrar las conexiones de una estacion cuando se elimina
    @Transactional
    void deleteByOrigenIdOrDestinoId(Long origenId, Long destinoId);
}
