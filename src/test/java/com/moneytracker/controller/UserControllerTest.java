package com.moneytracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneytracker.dto.BalanceUpdateRequest;
import com.moneytracker.entity.Savings;
import com.moneytracker.entity.User;
import com.moneytracker.repository.SavingsRepository;
import com.moneytracker.repository.UserRepository;
import com.moneytracker.security.JwtUtils;
import com.moneytracker.security.UserDetailsImpl;
import com.moneytracker.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SavingsRepository savingsRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @BeforeEach
    void setUp() {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L,
                "testuser",
                "test@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getBalance_shouldReturnUserBalance() throws Exception {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .balance(new BigDecimal("1500.75"))
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/user/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500.75));
    }

    @Test
    void updateBalance_shouldUpdateAndReturnNewBalance() throws Exception {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .balance(new BigDecimal("100.00"))
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BalanceUpdateRequest request = new BalanceUpdateRequest();
        request.setAmount(new BigDecimal("500.00"));

        mockMvc.perform(put("/api/user/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.00));
    }

    @Test
    void updateBalance_shouldRejectNegativeAmount() throws Exception {
        BalanceUpdateRequest request = new BalanceUpdateRequest();
        request.setAmount(new BigDecimal("-10.00"));

        mockMvc.perform(put("/api/user/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferToSavings_shouldTransferAmountAndReturnUpdatedBalance() throws Exception {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .balance(new BigDecimal("1000.00"))
                .build();

        Savings savings = Savings.builder()
                .id(1L)
                .name("Emergency Fund")
                .currentAmount(new BigDecimal("100.00"))
                .targetAmount(new BigDecimal("1000.00"))
                .user(user)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(savingsRepository.findById(1L)).thenReturn(Optional.of(savings));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(savingsRepository.save(any(Savings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/user/transfer-to-savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 200.00, \"savingsId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800.00))
                .andExpect(jsonPath("$.savings.currentAmount").value(300.00));
    }

    @Test
    void transferToSavings_shouldRejectInsufficientBalance() throws Exception {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .balance(new BigDecimal("50.00"))
                .build();

        Savings savings = Savings.builder()
                .id(1L)
                .name("Emergency Fund")
                .currentAmount(new BigDecimal("0.00"))
                .targetAmount(new BigDecimal("1000.00"))
                .user(user)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(savingsRepository.findById(1L)).thenReturn(Optional.of(savings));

        mockMvc.perform(post("/api/user/transfer-to-savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 200.00, \"savingsId\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }
}
