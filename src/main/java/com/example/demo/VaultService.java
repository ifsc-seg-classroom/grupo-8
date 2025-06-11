package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class VaultService {

    @Value("${spring.cloud.vault.uri}")
    private String VAULT_URI;
    @Value("${spring.cloud.vault.token}")
    private String VAULT_TOKEN;
    @Value("${spring.mail.host}")
    private String mailHost;
    @Value("${spring.mail.port}")
    private int mailPort;
    @Value("${spring.mail.username}")
    private String mailUsername;
    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String smtpAuth;
    @Value("${spring.mail.properties.mail.starttls.enable}")
    private boolean starttlsEnable;
    private JavaMailSender javaMailSender;
    private boolean isFirstTime = true;

    @PostConstruct
    public void init() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(getApiKey().replace("API KEY: ", ""));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        this.javaMailSender = mailSender;
    }

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

        return "API KEY: " + inside2.values().iterator().next() + "\n";
    }

    public Map<String, Object> getMetadata() {
        Map<String, Object> api = getVaultSecret();
        Map<String, Object> inside1 = (Map<String, Object>) api.get("data");
        Map<String, Object> inside2 = (Map<String, Object>) inside1.get("metadata");
        return inside2;
    }

    public String sendEmail(EmailRequestDTO requestDTO) throws MessagingException {
        if (!isFirstTime) {
            return "Eu disse pra nao clicar. Agora tu vai ter que fazer o [ ./gradlew bootRun ] denovo";
        }

        MimeMessage message = javaMailSender.createMimeMessage();
        message.setSubject(requestDTO.subject());
        message.setText(requestDTO.content());
        message.setFrom(new InternetAddress(mailUsername));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(requestDTO.emailAddress()));

        javaMailSender.send(message);
        this.isFirstTime = false;
        return String.format("Email enviado para: %s. Verifique sua caixa de entrada.", requestDTO.emailAddress());
    }
}