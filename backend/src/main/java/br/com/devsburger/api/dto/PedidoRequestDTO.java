package br.com.devsburger.api.dto;
import java.util.List;


public record PedidoRequestDTO(
        String nomeCliente,
        List<ItemPedidoRequestDTO> itens
) {

}