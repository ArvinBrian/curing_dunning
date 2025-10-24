package com.example.curingdunning.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.curingdunning.entity.DunningRule;
import com.example.curingdunning.repository.DunningRuleRepository;

@Service
public class DunningRuleService {

    @Autowired
    private DunningRuleRepository repo;

    public DunningRule createRule(DunningRule rule) {
        return repo.save(rule);
    }

    public List<DunningRule> getAllRules() {
        return repo.findAll();
    }

    public DunningRule updateRule(Long id, DunningRule rule) {
        DunningRule existing = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Rule not found"));
        existing.setServiceName(rule.getServiceName());
        existing.setOverdueDays(rule.getOverdueDays());
        existing.setAction(rule.getAction());
        existing.setPriority(rule.getPriority());
        return repo.save(existing);
    }

    public void deleteRule(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Rule not found");
        }
        repo.deleteById(id);
    }
}
