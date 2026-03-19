package com.netkrow.backend.controller;

import com.netkrow.backend.dto.QueryRequest;
import com.netkrow.backend.model.RCARecord;
import com.netkrow.backend.service.OracleQueryService;
import com.netkrow.backend.service.RCARecordService;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rca")
public class RCARecordController {

    private final RCARecordService service;
    private final OracleQueryService oracleQueryService;

    public RCARecordController(RCARecordService service, OracleQueryService oracleQueryService) {
        this.service = service;
        this.oracleQueryService = oracleQueryService;
    }

    @PostMapping
    public RCARecord create(@RequestBody RCARecord r) {
        return service.save(r);
    }

    @GetMapping("/search")
    public List<RCARecord> search(@RequestParam String q) {
        return service.search(q);
    }

    @GetMapping
    public List<RCARecord> listAll() {
        return service.listAll();
    }

    @PostMapping("/query")
    public List<Map<String, Object>> executeOracleQuery(@RequestBody QueryRequest req) throws SQLException {
        return oracleQueryService.executeQuery(req);
    }

    @PostMapping("/backtrace")
    public Map<String, Object> backtrace(@RequestBody Map<String, String> req) throws SQLException {
        String search = req.get("search");
        if (search == null || search.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe enviar el campo 'search'.");
        }
        int maxDepth = 15;
        if (req.containsKey("maxDepth")) {
            try { maxDepth = Integer.parseInt(req.get("maxDepth")); } catch (Exception ignore) {}
        }
        return oracleQueryService.backtrace(search.trim(), maxDepth);
    }
}
