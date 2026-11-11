package com.eduneko.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class GeneradorContenidoIAService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String modelo;

    public GeneradorContenidoIAService(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.api.url}") String apiUrl,
            @Value("${openai.api.model}") String modelo,
            ObjectMapper objectMapper) {

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        this.objectMapper = objectMapper;
        this.modelo = modelo;
    }

    public String generarTexto(String instrucciones, String contenido) {

        Map<String, Object> solicitud = Map.of(
                "model", modelo,
                "instructions", instrucciones,
                "input", contenido
        );

        long inicio = System.nanoTime();

        String respuesta = restClient.post()
        .contentType(MediaType.APPLICATION_JSON)
        .body(solicitud)
        .retrieve()
        .body(String.class);

        // Calcular el tiempo transcurrido.
        double tiempoSegundos =
                (System.nanoTime() - inicio) / 1_000_000_000.0;

        // Mostrar el resultado en la consola del backend.
        System.out.println("===== RENDIMIENTO IA - EDUNEKO =====");
        System.out.println("Modelo: " + modelo);
        System.out.printf(
                java.util.Locale.US,
                "Tiempo de respuesta: %.2f segundos%n",
                tiempoSegundos
        );
        System.out.println("===================================");

        try {
            JsonNode raiz = objectMapper.readTree(respuesta);
            JsonNode uso = raiz.path("usage");

        if (!uso.isMissingNode() && !uso.isNull()) {

            long tokensEntrada = uso.path("input_tokens").asLong();
            long tokensSalida = uso.path("output_tokens").asLong();
            long tokensTotales = uso.path("total_tokens").asLong();

            double costoEstimado =
                    (tokensEntrada * 0.40 / 1_000_000.0)
                    + (tokensSalida * 1.60 / 1_000_000.0);

            System.out.println("===== CONSUMO OPENAI - EDUNEKO =====");
            System.out.println("Modelo: " + modelo);
            System.out.println("Tokens entrada: " + tokensEntrada);
            System.out.println("Tokens salida: " + tokensSalida);
            System.out.println("Tokens totales: " + tokensTotales);
            System.out.printf(
                    java.util.Locale.US,
                    "Costo estimado: US$%.6f%n",
                    costoEstimado
            );
            System.out.println("==================================");
        }
            StringBuilder texto = new StringBuilder();

            JsonNode salidas = raiz.path("output");

            for (JsonNode salida : salidas) {
                if (!"message".equals(salida.path("type").asText())) {
                    continue;
                }

                for (JsonNode parte : salida.path("content")) {
                    if ("output_text".equals(parte.path("type").asText())) {
                        texto.append(parte.path("text").asText());
                    }
                }
            }

            if (texto.isEmpty()) {
                throw new IllegalStateException(
                        "OpenAI no devolvio contenido de texto."
                );
            }

            return texto.toString();

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException(
                    "No fue posible interpretar la respuesta de OpenAI.", e
            );
        }
    }
}
