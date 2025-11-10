package br.com.devsburger.api.dto;
import java.math.BigDecimal;


public record ItemPedidoResponseDTO(
        String nomeProduto,
        int quantidade,
        BigDecimal precoUnitario,
        String imagemUrl
) {}