package com.acme.biblio.ga.api;

import com.acme.biblio.ga.domain.Libro;
import com.acme.biblio.ga.repository.LibroRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroRepository libroRepo;

    public LibroController(LibroRepository libroRepo) {
        this.libroRepo = libroRepo;
    }

    @GetMapping
    public List<Libro> findAll() {
        return libroRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> findById(@PathVariable("id") String libroId) {
        return libroRepo.findById(libroId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
