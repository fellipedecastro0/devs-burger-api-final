package br.com.devsburger.api.dto;


public record PedidoCheckoutDTO(
        String cep,
        String endereco,
        String numero,
        String bairro,
        String cidade,
        String estado,
        String complemento,
        String formaPagamento
) {
}