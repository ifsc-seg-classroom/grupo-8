package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}