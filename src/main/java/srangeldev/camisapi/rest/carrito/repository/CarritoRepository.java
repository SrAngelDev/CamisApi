package srangeldev.camisapi.rest.carrito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import srangeldev.camisapi.rest.carrito.models.Carrito;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    // Buscar carrito por usuario
    Optional<Carrito> findByUsuarioId(Long usuarioId);

    // Verificar si existe un carrito activo para un usuario
    boolean existsByUsuarioIdAndActivoTrue(Long usuarioId);

    // Buscar carritos activos
    List<Carrito> findByActivoTrue();

    // Buscar carritos por usuario y estado
    List<Carrito> findByUsuarioIdAndActivo(Long usuarioId, Boolean activo);

    // Buscar carrito más reciente de un usuario
    Optional<Carrito> findTopByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    // Eliminar carritos inactivos antiguos
    @Query("DELETE FROM Carrito c WHERE c.activo = false AND c.fechaCreacion < :fecha")
    void deleteInactiveCarritosBefore(@Param("fecha") java.time.LocalDateTime fecha);

    // Contar carritos activos por usuario
    Long countByUsuarioIdAndActivoTrue(Long usuarioId);

    // Buscar carritos por rango de fechas
    @Query("SELECT c FROM Carrito c WHERE c.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<Carrito> findCarritosByFechaRange(@Param("fechaInicio") java.time.LocalDateTime fechaInicio,
                                           @Param("fechaFin") java.time.LocalDateTime fechaFin);
}
