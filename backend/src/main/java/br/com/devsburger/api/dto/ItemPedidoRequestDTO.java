package br.com.devsburger.api.dto;
//  representa um item no carrinho do cliente.
public record ItemPedidoRequestDTO(Long produtoId, int quantidade) {
}