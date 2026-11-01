package com.metroruta.repository;

import com.metroruta.model.Conexion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConexionRepository extends JpaRepository<Conexion, Long> {

    // para borrar las conexiones de una estacion cuando se elimina
    void deleteByOrigenIdOrDestinoId(Long origenId, Long destinoId);
}
