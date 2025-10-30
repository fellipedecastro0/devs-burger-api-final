package br.com.devsburger.api.dto;

import br.com.devsburger.api.entity.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        String nomeCliente,
        LocalDateTime dtPedido,
        StatusPedido status,
        BigDecimal valorTotal,
        List<ItemPedidoResponseDTO> itens,
        BigDecimal subtotal,
        BigDecimal frete,
        BigDecimal total,
        String cep,
        String endereco,
        String numero,
        String bairro,
        String cidade,
        String estado
) {}