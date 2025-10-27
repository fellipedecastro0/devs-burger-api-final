package br.com.devsburger.api.dto;

import br.com.devsburger.api.entity.StatusPedido;

// Este record serve apenas para carregar o novo status na requisição.
public record AtualizacaoStatusPedidoDTO(StatusPedido status) {
}
