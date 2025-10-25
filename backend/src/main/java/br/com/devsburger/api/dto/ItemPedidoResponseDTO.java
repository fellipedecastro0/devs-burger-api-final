package br.com.devsburger.api.dto;
import java.math.BigDecimal;


public record ItemPedidoResponseDTO(
        String nomeProduto, // Mais útil que o ID para exibição
        int quantidade,
        BigDecimal precoUnitario
) {}