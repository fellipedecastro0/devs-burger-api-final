package br.com.devsburger.api.service;

import br.com.devsburger.api.dto.ItemPedidoRequestDTO;
import br.com.devsburger.api.dto.PedidoRequestDTO;
import br.com.devsburger.api.dto.ItemPedidoResponseDTO;
import br.com.devsburger.api.dto.PedidoResponseDTO;
import br.com.devsburger.api.entity.ItemPedido;
import br.com.devsburger.api.entity.Pedido;
import br.com.devsburger.api.entity.Produto;
import br.com.devsburger.api.entity.StatusPedido;
import br.com.devsburger.api.repository.PedidoRepository;
import br.com.devsburger.api.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Transactional
    public Pedido criarPedido(PedidoRequestDTO dto) {
        Pedido pedido = new Pedido();
        pedido.setNomeCliente(dto.nomeCliente());
        pedido.setDtPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.EM_PREPARO); // Defina um status inicial

        List<ItemPedido> itensDoPedido = new ArrayList<>();
        BigDecimal valorTotalCalculado = BigDecimal.ZERO;

        for (ItemPedidoRequestDTO itemDTO : dto.itens()) {
            Produto produto = produtoRepository.findById(itemDTO.produtoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + itemDTO.produtoId()));

            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setProduto(produto);
            itemPedido.setQuantidade(itemDTO.quantidade());
            itemPedido.setPrecoUnitario(produto.getPreco());
            itemPedido.setPedido(pedido);

            itensDoPedido.add(itemPedido);

            valorTotalCalculado = valorTotalCalculado.add(
                    produto.getPreco().multiply(new BigDecimal(itemDTO.quantidade()))
            );
        }


        pedido.setItens(itensDoPedido);

        pedido.setValorTotal(valorTotalCalculado);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        return pedidoSalvo;
    }


    //MÉTODOS EXISTENTES
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional
    public Optional<Pedido> atualizarStatus(Long id, StatusPedido novoStatus) {
        return pedidoRepository.findById(id)
                .map(pedidoEncontrado -> {
                    pedidoEncontrado.setStatus(novoStatus);
                    // O @Transactional cuida do save automaticamente ao fim do método
                    return pedidoEncontrado;
                });
    }

    public boolean deletarPedido(Long id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            return true;
        }
        return false;
    }


    // Método PRIVADO para converter ItemPedido (Entidade) -> ItemPedidoResponseDTO
    private ItemPedidoResponseDTO converterParaItemResponseDTO(ItemPedido itemPedido) {
        // Verifica se o produto associado existe para evitar NullPointerException
        String nomeProduto = (itemPedido.getProduto() != null) ? itemPedido.getProduto().getNome() : "Produto Inválido";
        // Verifica se o preço unitário existe
        BigDecimal preco = (itemPedido.getPrecoUnitario() != null) ? itemPedido.getPrecoUnitario() : BigDecimal.ZERO;

        return new ItemPedidoResponseDTO(
                nomeProduto,
                itemPedido.getQuantidade(),
                preco
        );
    }

    // Método PÚBLICO para converter Pedido (Entidade) -> PedidoResponseDTO
    public PedidoResponseDTO converterParaResponseDTO(Pedido pedido) {
        // Converte a lista de entidades ItemPedido para uma lista de DTOs
        List<ItemPedidoResponseDTO> itensDTO = pedido.getItens()
                .stream() // Inicia o processamento da lista
                .map(this::converterParaItemResponseDTO) // Aplica o método de conversão a cada item
                .toList();

        // Retorna um novo DTO de resposta preenchido com os dados da entidade Pedido
        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getNomeCliente(),
                pedido.getDtPedido(),
                pedido.getStatus(),
                pedido.getValorTotal() != null ? pedido.getValorTotal() : BigDecimal.ZERO,
                itensDTO,
                pedido.getSubtotal() != null ? pedido.getSubtotal() : BigDecimal.ZERO, // subtotal
                pedido.getFrete() != null ? pedido.getFrete() : BigDecimal.ZERO,     // frete
                pedido.getValorTotal() != null ? pedido.getValorTotal() : BigDecimal.ZERO, // total
                pedido.getCep(),
                pedido.getEndereco(),
                pedido.getNumero(),
                pedido.getBairro(),
                pedido.getCidade(),
                pedido.getEstado()
        );
    }

    // Método PÚBLICO que busca por ID e já retorna o DTO (para o PedidoWebController usar)
    public Optional<PedidoResponseDTO> buscarPorIdComoDTO(Long id) {
        return pedidoRepository.findById(id)
                .map(this::converterParaResponseDTO);
        // Se não encontrar, retorna um Optional vazio
    }

    //  Lista todos os pedidos já convertidos para DTOs
    public List<PedidoResponseDTO> listarTodosComoDTO() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }


}