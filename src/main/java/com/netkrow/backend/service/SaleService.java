package com.netkrow.backend.service;

import com.netkrow.backend.dto.SaleDTO;
import com.netkrow.backend.model.Customer;
import com.netkrow.backend.model.Product;
import com.netkrow.backend.model.Sale;
import com.netkrow.backend.repository.CustomerRepository;
import com.netkrow.backend.repository.ProductRepository;
import com.netkrow.backend.repository.SaleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private final SaleRepository saleRepo;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;

    private static final Set<String> ALLOWED_TX_TYPES = Set.of("Venta", "Alquiler");

    public SaleService(SaleRepository saleRepo,
                       ProductRepository productRepo,
                       CustomerRepository customerRepo) {
        this.saleRepo = saleRepo;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
    }

    public SaleDTO create(SaleDTO dto) {
        if (dto.getProductId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId es obligatorio");
        }
        Product product = productRepo.findById(dto.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto inválido"));

        if (dto.getRemisionVenta() == null || dto.getRemisionVenta().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "remisionVenta es obligatoria");
        }

        if (dto.getTransactionType() == null || dto.getTransactionType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transactionType es obligatorio");
        }
        if (!ALLOWED_TX_TYPES.contains(dto.getTransactionType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transactionType debe ser 'Venta' o 'Alquiler'");
        }

        if (dto.getChannel() == null || dto.getChannel().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "channel es obligatorio");
        }

        if (dto.getSaleValue() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "saleValue es obligatorio");
        }

        if (dto.getPaymentMethod() == null || dto.getPaymentMethod().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paymentMethod es obligatorio");
        }

        if (dto.getCustomerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId es obligatorio");
        }
        Customer customer = customerRepo.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente inválido"));

        // Fecha y hora: si llega en DTO, úsala; de lo contrario, ahora().
        LocalDateTime saleDt = dto.getSaleDate() != null ? dto.getSaleDate() : LocalDateTime.now();

        Sale s = new Sale();
        s.setSaleDate(saleDt);
        s.setRemisionVenta(dto.getRemisionVenta());
        s.setTransactionType(dto.getTransactionType());
        s.setProduct(product);
        s.setChannel(dto.getChannel());
        s.setUnitQty(dto.getUnitQty() != null ? dto.getUnitQty() : 1);
        s.setSaleValue(dto.getSaleValue());
        s.setPaymentMethod(dto.getPaymentMethod());
        s.setCustomer(customer);

        s = saleRepo.save(s);

        return toDTO(s);
    }

    public List<SaleDTO> listFiltered(
            String q,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String channel,
            String paymentMethod,
            String transactionType,
            Long customerId
    ) {
        // Trae con fetch join para evitar Lazy y N+1
        return saleRepo.findAllWithJoins().stream()
                .filter(s -> dateFrom == null || !s.getSaleDate().isBefore(dateFrom))
                .filter(s -> dateTo == null   || !s.getSaleDate().isAfter(dateTo))
                .filter(s -> channel == null || channel.isBlank() || channel.equalsIgnoreCase(s.getChannel()))
                .filter(s -> paymentMethod == null || paymentMethod.isBlank() || paymentMethod.equalsIgnoreCase(s.getPaymentMethod()))
                .filter(s -> transactionType == null || transactionType.isBlank() || transactionType.equalsIgnoreCase(s.getTransactionType()))
                .filter(s -> customerId == null || (s.getCustomer() != null && customerId.equals(s.getCustomer().getCustomerId())))
                .filter(s -> {
                    if (q == null || q.isBlank()) return true;
                    String qq = q.toLowerCase();

                    String customerName = "";
                    if (s.getCustomer() != null) {
                        String fn = s.getCustomer().getFirstName() != null ? s.getCustomer().getFirstName() : "";
                        String ln = s.getCustomer().getLastName() != null ? s.getCustomer().getLastName() : "";
                        customerName = (fn + " " + ln).trim().toLowerCase();
                    }

                    String productName = "";
                    if (s.getProduct() != null && s.getProduct().getName() != null) {
                        productName = s.getProduct().getName().toLowerCase();
                    }

                    String remision = s.getRemisionVenta() != null ? s.getRemisionVenta().toLowerCase() : "";
                    String ch = s.getChannel() != null ? s.getChannel().toLowerCase() : "";
                    String tx = s.getTransactionType() != null ? s.getTransactionType().toLowerCase() : "";

                    return customerName.contains(qq)
                            || productName.contains(qq)
                            || remision.contains(qq)
                            || ch.contains(qq)
                            || tx.contains(qq);
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private SaleDTO toDTO(Sale s) {
        SaleDTO d = new SaleDTO();
        d.setSaleId(s.getSaleId());
        d.setSaleDate(s.getSaleDate());
        d.setRemisionVenta(s.getRemisionVenta());
        d.setTransactionType(s.getTransactionType());

        // Null-safe product
        if (s.getProduct() != null) {
            d.setProductId(s.getProduct().getProductId());
        } else {
            d.setProductId(null);
        }

        d.setChannel(s.getChannel());
        d.setUnitQty(s.getUnitQty());
        d.setSaleValue(s.getSaleValue());
        d.setPaymentMethod(s.getPaymentMethod());

        // Null-safe customer
        if (s.getCustomer() != null) {
            d.setCustomerId(s.getCustomer().getCustomerId());
            String first = s.getCustomer().getFirstName();
            String last = s.getCustomer().getLastName();
            d.setCustomerName((first != null ? first : "") + (last != null && !last.isBlank() ? " " + last : ""));
        }
        return d;
    }
}
