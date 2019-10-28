package com.gruposolux.rcivil.pdisciplinario.repository;

import com.gruposolux.rcivil.pdisciplinario.domain.MovimientoProvidencia;
import com.gruposolux.rcivil.pdisciplinario.domain.Providencia;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Spring Data  repository for the MovimientoProvidencia entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MovimientoProvidenciaRepository extends JpaRepository<MovimientoProvidencia, Long>
{
    @Query("SELECT m FROM MovimientoProvidencia m INNER JOIN m.providencia p WHERE p = :providencia " +
        "ORDER BY m.fecha asc")
    Set<MovimientoProvidencia> findByProvidencia(@Param("providencia") Providencia providencia);

//    @Query("SELECT m FROM MovimientoProvidencia m INNER JOIN m.providencia p WHERE p = :providencia " +
//        "ORDER BY m.fecha desc")
//    List<MovimientoProvidencia> findByProvidencia(@Param("providencia") Providencia providencia);

    // Set<MovimientoProvidencia> findByProvidenciaOrderByProvidenciaDesc(Providencia providencia);



//    @Query("SELECT M FROM MovimientoProvidencia m where m.providencia=:Id" )
//        List<MovimientoProvidencia> traerMovimientos(@Param("Id") Long Id);



//
//    @Query(value = "SELECT m FROM MovimientoProvidencia m where m.providencia=:Id " +
//        "and m.fecha=( select  max (m2.fecha) from MovimientoProvidencia m2)" )
//    @Query(value = "Select * from movimiento_providencia WHERE  providencia_id = :Id and fecha in (" +
//        "SELECT MAX(fecha)" +
//        "FROM movimiento_providencia" +
//        ")", nativeQuery = true )

    @Query(value = "SELECT * FROM movimiento_providencia m where m.providencia_id =:id  order by m.fecha desc limit 1", nativeQuery = true)
    MovimientoProvidencia traerMovimientos(@Param("id") Long id);



}
