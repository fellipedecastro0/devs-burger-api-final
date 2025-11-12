package br.com.devsburger.api.dto;
import java.math.BigDecimal;


public record ItemPedidoResponseDTO(
        Long itemPedidoId,
        String nomeProduto,
        int quantidade,
        BigDecimal precoUnitario,
        String imagemUrl
) {}