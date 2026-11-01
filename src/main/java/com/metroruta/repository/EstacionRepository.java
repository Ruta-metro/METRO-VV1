package com.metroruta.repository;

import com.metroruta.model.Estacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstacionRepository extends JpaRepository<Estacion, Long> {

    Optional<Estacion> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}
