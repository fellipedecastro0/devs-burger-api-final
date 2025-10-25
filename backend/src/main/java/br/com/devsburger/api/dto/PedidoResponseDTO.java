package br.com.devsburger.api.dto;

import br.com.devsburger.api.entity.StatusPedido; // Importe o Enum
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Representa o resumo do pedido que será ENVIADO de volta ao cliente
public record PedidoResponseDTO(
        Long id,
        String nomeCliente,
        LocalDateTime dtPedido, // Ou você pode formatar como String aqui se preferir
        StatusPedido status,
        BigDecimal valorTotal, // Calculado no backend
        List<ItemPedidoResponseDTO> itens // Lista com os resumos dos itens
) {}