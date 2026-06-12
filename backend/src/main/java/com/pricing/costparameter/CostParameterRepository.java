package com.pricing.costparameter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CostParameterRepository extends JpaRepository<CostParameter, Long> {

    Optional<CostParameter> findByParametro(String parametro);

    List<CostParameter> findAllByOrderByParametroAsc();
}
