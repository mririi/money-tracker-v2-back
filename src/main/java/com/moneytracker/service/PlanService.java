package com.moneytracker.service;

import com.moneytracker.dto.PlanRequest;
import com.moneytracker.entity.Plan;
import com.moneytracker.entity.PlanStatus;
import com.moneytracker.entity.User;
import com.moneytracker.repository.PlanRepository;
import com.moneytracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Plan> getAllPlans() {
        Long userId = getCurrentUserId();
        return planRepository.findByUserId(userId);
    }

    public Plan getPlanById(Long id) {
        Long userId = getCurrentUserId();
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        if (!plan.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return plan;
    }

    public Plan createPlan(PlanRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Plan plan = Plan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .savedAmount(request.getSavedAmount())
                .targetDate(request.getTargetDate())
                .status(PlanStatus.ACTIVE)
                .user(user)
                .build();
        return planRepository.save(plan);
    }

    public Plan updatePlan(Long id, PlanRequest request) {
        Plan plan = getPlanById(id);
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setTargetAmount(request.getTargetAmount());
        plan.setSavedAmount(request.getSavedAmount());
        plan.setTargetDate(request.getTargetDate());

        if (plan.getSavedAmount().compareTo(plan.getTargetAmount()) >= 0) {
            plan.setStatus(PlanStatus.COMPLETED);
        }

        return planRepository.save(plan);
    }

    public void deletePlan(Long id) {
        Plan plan = getPlanById(id);
        planRepository.delete(plan);
    }

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
