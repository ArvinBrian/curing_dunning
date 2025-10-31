package com.example.curingdunning.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.curingdunning.dto.ApplyActionDTO;
import com.example.curingdunning.dto.CuringActionDTO;
import com.example.curingdunning.entity.ActionStatus;
import com.example.curingdunning.entity.ActionType;
import com.example.curingdunning.entity.CuringAction;
import com.example.curingdunning.entity.Customer;
import com.example.curingdunning.entity.DunningEvent;
import com.example.curingdunning.repository.CuringActionRepository;
import com.example.curingdunning.repository.DunningEventRepository;

import jakarta.transaction.Transactional;

@Service
public class CuringActionService {

    @Autowired
    private DunningEventRepository eventRepo;

    @Autowired
    private CuringActionRepository actionRepo;

    @Autowired
    private DunningEventService eventService;

    @Transactional
    public CuringActionDTO applyAction(ApplyActionDTO dto) {
        // fetch event
        DunningEvent event = eventRepo.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Customer customer = event.getCustomer();

        // create curing action
        CuringAction action = new CuringAction(event, customer, dto.getActionType());
        action.setStatus(ActionStatus.INITIATED);
        action.setCreatedAt(LocalDateTime.now());
        CuringAction saved = actionRepo.save(action);

        // perform business logic
        boolean success = performCuringLogic(dto.getActionType(), event, customer);

        if (success) {
            saved.setStatus(ActionStatus.COMPLETED);
            saved.setResolvedAt(LocalDateTime.now());
            actionRepo.save(saved);

         // FIX: Pass the original Dunning Rule ID from the DunningEvent
            Long originalRuleId = event.getAppliedRule().getId(); // <-- CORRECT ID
            
            // resolve the event and reset negative status
            eventService.markResolved(event.getId(), originalRuleId); // <-- FIXED CALL} else {
            
        } else {
        	saved.setStatus(ActionStatus.FAILED);
            actionRepo.save(saved);
        }

        return toDto(saved);
    }

    private boolean performCuringLogic(ActionType type, DunningEvent event, Customer customer) {
        // Example placeholder logic:
        // - MAKE_PAYMENT -> check payment success
        // - UPLOAD_PROOF -> verify document
        // - USER_RECHARGE -> mark prepaid cured
        return true;
    }

    private CuringActionDTO toDto(CuringAction action) {
        return new CuringActionDTO(
                action.getActionId(),
                action.getDunningEvent().getId(),
                action.getCustomer().getCustomerId(),
                action.getDunningEvent().getServiceName(),
                action.getActionType(),
                action.getStatus(),
                action.getCreatedAt(),
                action.getResolvedAt()
        );
    }

    public List<CuringActionDTO> getAvailableActionsForCustomer(Long customerId) {
        return actionRepo.findByCustomerCustomerId(customerId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

