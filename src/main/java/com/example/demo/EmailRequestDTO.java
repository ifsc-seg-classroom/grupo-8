package com.example.demo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmailRequestDTO(
        @NotNull @Size(max = 900) String subject,
        @NotNull @Size(max = 900)  String content,
        @Email @NotNull @Size(max = 300) String emailAddress
) {
}
