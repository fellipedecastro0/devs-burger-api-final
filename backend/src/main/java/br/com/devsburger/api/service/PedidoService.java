package br.com.devsburger.api.service;

import br.com.devsburger.api.dto.ItemPedidoRequestDTO;
import br.com.devsburger.api.dto.PedidoRequestDTO;
import br.com.devsburger.api.dto.ItemPedidoResponseDTO;
import br.com.devsburger.api.dto.PedidoResponseDTO;
import br.com.devsburger.api.entity.ItemPedido;
import br.com.devsburger.api.entity.Pedido;
import br.com.devsburger.api.entity.Produto;
import br.com.devsburger.api.entity.StatusPedido;
import br.com.devsburger.api.repository.ItemPedidoRepository;
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

    // --- SEU MÉTODO criarPedido ORIGINAL (COM SUGESTÃO DE MELHORIA E CÁLCULO DO TOTAL) ---
    @Transactional
    public Pedido criarPedido(PedidoRequestDTO dto) {
        Pedido pedido = new Pedido();
        pedido.setNomeCliente(dto.nomeCliente());
        pedido.setDtPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.EM_PREPARO); // Defina um status inicial

        List<ItemPedido> itensDoPedido = new ArrayList<>();
        BigDecimal valorTotalCalculado = BigDecimal.ZERO;

        // Itera sobre os itens recebidos no DTO
        for (ItemPedidoRequestDTO itemDTO : dto.itens()) {
            // Busca o produto no banco
            Produto produto = produtoRepository.findById(itemDTO.produtoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + itemDTO.produtoId()));

            // Cria a entidade ItemPedido
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setProduto(produto);
            itemPedido.setQuantidade(itemDTO.quantidade());
            itemPedido.setPrecoUnitario(produto.getPreco()); // Pega o preço ATUAL do produto
            itemPedido.setPedido(pedido); // Associa este item ao pedido que estamos criando

            itensDoPedido.add(itemPedido); // Adiciona na lista temporária

            // Calcula o subtotal deste item e soma ao total geral
            valorTotalCalculado = valorTotalCalculado.add(
                    produto.getPreco().multiply(new BigDecimal(itemDTO.quantidade()))
            );
        }

        // Associa a lista de entidades ItemPedido ao Pedido
        pedido.setItens(itensDoPedido);
        // Define o valor total calculado na entidade Pedido (IMPORTANTE para a Opção A)
        pedido.setValorTotal(valorTotalCalculado);

        // Salva o Pedido. Se CascadeType.ALL estiver configurado, os ItensPedido serão salvos juntos.
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        return pedidoSalvo;
    }


    // --- SEUS MÉTODOS EXISTENTES ---
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


    // --- ADICIONE ESTES NOVOS MÉTODOS DE CONVERSÃO ---

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
        List<ItemPedidoResponseDTO> itensDTO = pedido.getItens() // Pega a lista de itens da entidade Pedido
                .stream() // Inicia o processamento da lista
                .map(this::converterParaItemResponseDTO) // Aplica o método de conversão a cada item
                .toList(); // Coleta os resultados em uma nova lista (Java 16+)
        // Se usar Java < 16, substitua .toList() por: .collect(Collectors.toList());

        // Retorna um novo DTO de resposta preenchido com os dados da entidade Pedido
        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getNomeCliente(),
                pedido.getDtPedido(),
                pedido.getStatus(),
                // Pega o valor total que foi calculado e salvo na entidade Pedido (Opção A)
                pedido.getValorTotal() != null ? pedido.getValorTotal() : BigDecimal.ZERO,
                itensDTO // Inclui a lista de DTOs dos itens
        );
    }

    // Método PÚBLICO que busca por ID e já retorna o DTO (para o PedidoWebController usar)
    public Optional<PedidoResponseDTO> buscarPorIdComoDTO(Long id) {
        return pedidoRepository.findById(id) // Busca a entidade Pedido pelo ID
                .map(this::converterParaResponseDTO); // Se encontrar (Optional não vazio), aplica a conversão para DTO
        // Se não encontrar, retorna um Optional vazio
    }

    // Método PÚBLICO opcional: Lista todos os pedidos já convertidos para DTOs
    public List<PedidoResponseDTO> listarTodosComoDTO() {
        return pedidoRepository.findAll() // Busca todas as entidades Pedido
                .stream() // Inicia o processamento da lista
                .map(this::converterParaResponseDTO) // Aplica a conversão a cada Pedido
                .toList(); // Coleta em uma lista de DTOs (Java 16+)
    }


}