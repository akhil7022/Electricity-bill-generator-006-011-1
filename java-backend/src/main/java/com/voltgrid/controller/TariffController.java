package com.voltgrid.controller;

import com.voltgrid.model.Tariff;
import com.voltgrid.repository.TariffRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tariffs")
@CrossOrigin(origins = "*")
public class TariffController {

    private final TariffRepository tariffRepo;

    public TariffController(TariffRepository tariffRepo) {
        this.tariffRepo = tariffRepo;
    }

    @GetMapping
    public List<Tariff> getAll() {
        return tariffRepo.findAll();
    }

    @PostMapping
    public ResponseEntity<Tariff> create(@RequestBody Tariff tariff) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tariffRepo.save(tariff));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tariff> update(@PathVariable int id, @RequestBody Tariff tariff) {
        return tariffRepo.update(id, tariff)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        tariffRepo.delete(id);
        return ResponseEntity.noContent().build();
    }
}
