package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class VaultService {

    @Value("${spring.cloud.vault.uri}")
    private String VAULT_URI;
    @Value("${spring.cloud.vault.token}")
    private String VAULT_TOKEN;

    public Map<String, Object> getVaultSecret() {
        String path = "/v1/secret/data/api-keys/producao";
        Map<String, Object> responseMap = new HashMap<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(VAULT_URI + path);
            request.addHeader("X-Vault-Token", VAULT_TOKEN);

            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            responseMap = mapper.convertValue(root, Map.class);
        } catch (Exception e) {
            responseMap.put("error", e.getMessage());
        }

        return responseMap;
    }

    public String getApiKey() {
        Map<String, Object> api = getVaultSecret();
        Map<String, Object> inside1 = (Map<String, Object>) api.get("data");
        Map<String, Object> inside2 = (Map<String, Object>) inside1.get("data");

        return "API KEY: " + inside2.get("key") + "\n";
    }

    public Map<String, Object> getMetadata() {
        Map<String, Object> api = getVaultSecret();
        Map<String, Object> inside1 = (Map<String, Object>) api.get("data");
        Map<String, Object> inside2 = (Map<String, Object>) inside1.get("metadata");
        return inside2;
    }
}