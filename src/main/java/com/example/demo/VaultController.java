package com.example.demo;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/vault")
public class VaultController {

    private final VaultService vaultService;

    public VaultController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @GetMapping("/full-secret")
    public Map<String, Object> getSecret() {
        return vaultService.getVaultSecret();
    }

    @GetMapping("/api-key")
    public String getApiKey() {
        return vaultService.getApiKey();
    }

    @GetMapping("/metadata")
    public Map<String, Object> getMetadata() {
        return vaultService.getMetadata();
    }
    
    @PostMapping("/send-email")
    public String sendEmail(
            @RequestBody @Valid EmailRequestDTO requestDTO
    ) throws MessagingException {
        return vaultService.sendEmail(requestDTO);
    }
}