package com.metroruta.repository;

import com.metroruta.model.Linea;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineaRepository extends JpaRepository<Linea, Long> {

    Optional<Linea> findByNombre(String nombre);
}
