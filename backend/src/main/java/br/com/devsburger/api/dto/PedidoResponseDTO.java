package br.com.devsburger.api.dto;

import br.com.devsburger.api.entity.StatusPedido; // Importe o Enum
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public record PedidoResponseDTO(
        Long id,
        String nomeCliente,
        LocalDateTime dtPedido,
        StatusPedido status,
        BigDecimal valorTotal,
        List<ItemPedidoResponseDTO> itens
) {}