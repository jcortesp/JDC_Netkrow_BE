package com.netkrow.backend.service;

import com.netkrow.backend.model.RCARecord;
import com.netkrow.backend.repository.RCARecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RCARecordService {
    @Autowired
    private RCARecordRepository repo;

    public RCARecord save(RCARecord r) {
        return repo.save(r);
    }

    public List<RCARecord> search(String query) {
        return repo.findByErrorCodeContainingIgnoreCaseOrOmsComponentContainingIgnoreCaseOrTagsContainingIgnoreCase(query, query, query);
    }

    public List<RCARecord> listAll() {
        return repo.findAll();
    }
}
