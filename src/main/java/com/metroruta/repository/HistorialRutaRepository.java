package com.metroruta.repository;

import com.metroruta.model.HistorialRuta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialRutaRepository extends JpaRepository<HistorialRuta, Long> {

    // las 20 consultas mas recientes de un usuario
    List<HistorialRuta> findTop20ByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
