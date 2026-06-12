package com.pricing.notebook;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotebookRepository extends JpaRepository<Notebook, Long> {

    Optional<Notebook> findByNome(String nome);

    List<Notebook> findAllByOrderByNomeAsc();
}
