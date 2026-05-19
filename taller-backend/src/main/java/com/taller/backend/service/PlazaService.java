package com.taller.backend.service;

import com.taller.backend.exception.BadRequestException;
import com.taller.backend.exception.ResourceNotFoundException;
import com.taller.backend.model.Plaza;
import com.taller.backend.repository.PlazaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlazaService {

    private final PlazaRepository plazaRepository;

    public PlazaService(PlazaRepository plazaRepository) {
        this.plazaRepository = plazaRepository;
    }

    public List<Plaza> findAll() {
        return plazaRepository.findAll();
    }

    public Plaza findById(Integer id) {
        return plazaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plaza", id));
    }

    public List<Plaza> findByEstado(Plaza.Estado estado) {
        return plazaRepository.findByEstado(estado);
    }

    @Transactional
    public Plaza create(String numero, String descripcion) {
        if (plazaRepository.existsByNumero(numero)) {
            throw new BadRequestException("Ya existe una plaza con el número: " + numero);
        }
        Plaza plaza = new Plaza();
        plaza.setNumero(numero);
        plaza.setDescripcion(descripcion);
        plaza.setEstado(Plaza.Estado.LIBRE);
        return plazaRepository.save(plaza);
    }

    @Transactional
    public Plaza update(Integer id, String descripcion, Plaza.Estado estado) {
        Plaza plaza = findById(id);
        if (descripcion != null) plaza.setDescripcion(descripcion);
        if (estado != null) plaza.setEstado(estado);
        return plazaRepository.save(plaza);
    }

    @Transactional
    public void delete(Integer id) {
        if (!plazaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plaza", id);
        }
        plazaRepository.deleteById(id);
    }
}