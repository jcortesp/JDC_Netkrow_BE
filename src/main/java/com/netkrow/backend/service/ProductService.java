package com.netkrow.backend.service;

import com.netkrow.backend.dto.ProductDTO;
import com.netkrow.backend.model.Product;
import com.netkrow.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<ProductDTO> list() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ProductDTO get(Long id) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        return toDTO(p);
    }

    public ProductDTO create(ProductDTO dto) {
        if (dto.getCode() == null || dto.getCode().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código es obligatorio");
        if (repo.existsByCode(dto.getCode()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese código");
        Product p = toEntity(new Product(), dto);
        return toDTO(repo.save(p));
    }

    public ProductDTO update(Long id, ProductDTO dto) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        // Si cambia el código, validar unicidad
        if (dto.getCode() != null && !dto.getCode().equals(p.getCode()) && repo.existsByCode(dto.getCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese código");
        }
        p = toEntity(p, dto);
        return toDTO(repo.save(p));
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        repo.deleteById(id);
    }

    // Mapeos
    private ProductDTO toDTO(Product p) {
        ProductDTO d = new ProductDTO();
        d.setProductId(p.getProductId());
        d.setCategory(p.getCategory());
        d.setType(p.getType());
        d.setName(p.getName());
        d.setPrice(p.getPrice());
        d.setCode(p.getCode());
        d.setBrand(p.getBrand());
        d.setStatus(p.getStatus());
        return d;
    }

    private Product toEntity(Product p, ProductDTO d) {
        if (d.getCategory() != null) p.setCategory(d.getCategory());
        if (d.getType() != null) p.setType(d.getType());
        if (d.getName() != null) p.setName(d.getName());
        if (d.getPrice() != null) p.setPrice(d.getPrice());
        if (d.getCode() != null) p.setCode(d.getCode());
        if (d.getBrand() != null) p.setBrand(d.getBrand());
        if (d.getStatus() != null) p.setStatus(d.getStatus());
        return p;
    }
}
