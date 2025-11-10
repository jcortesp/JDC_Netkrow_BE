package com.netkrow.backend.service;

import com.netkrow.backend.dto.SaleDTO;
import com.netkrow.backend.model.Product;
import com.netkrow.backend.model.Sale;
import com.netkrow.backend.repository.ProductRepository;
import com.netkrow.backend.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private final SaleRepository saleRepo;
    private final ProductRepository productRepo;

    public SaleService(SaleRepository saleRepo, ProductRepository productRepo) {
        this.saleRepo = saleRepo;
        this.productRepo = productRepo;
    }

    public SaleDTO create(SaleDTO dto) {
        if (dto.getProductId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId es obligatorio");
        Product product = productRepo.findById(dto.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto inválido"));

        if (dto.getSaleDate() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "saleDate es obligatorio");

        if (dto.getTransactionType() == null || dto.getTransactionType().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transactionType es obligatorio");

        if (dto.getChannel() == null || dto.getChannel().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "channel es obligatorio");

        if (dto.getSaleValue() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "saleValue es obligatorio");

        if (dto.getPaymentMethod() == null || dto.getPaymentMethod().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paymentMethod es obligatorio");

        Sale s = new Sale();
        s.setSaleDate(dto.getSaleDate());
        s.setRemisionVenta(dto.getRemisionVenta());
        s.setTransactionType(dto.getTransactionType());
        s.setProduct(product);
        s.setChannel(dto.getChannel());
        s.setUnitQty(dto.getUnitQty() != null ? dto.getUnitQty() : 1);
        s.setSaleValue(dto.getSaleValue());
        s.setPaymentMethod(dto.getPaymentMethod());

        s = saleRepo.save(s);
        dto.setSaleId(s.getSaleId());
        return dto;
    }

    public List<SaleDTO> list() {
        return saleRepo.findAll().stream().map(s -> {
            SaleDTO d = new SaleDTO();
            d.setSaleId(s.getSaleId());
            d.setSaleDate(s.getSaleDate());
            d.setRemisionVenta(s.getRemisionVenta());
            d.setTransactionType(s.getTransactionType());
            d.setProductId(s.getProduct().getProductId());
            d.setChannel(s.getChannel());
            d.setUnitQty(s.getUnitQty());
            d.setSaleValue(s.getSaleValue());
            d.setPaymentMethod(s.getPaymentMethod());
            return d;
        }).collect(Collectors.toList());
    }
}
