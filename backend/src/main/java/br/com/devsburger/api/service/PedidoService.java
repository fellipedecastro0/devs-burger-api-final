package br.com.devsburger.api.service;

import br.com.devsburger.api.dto.*;
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
import jakarta.servlet.http.HttpSession; // <<< ADICIONADO


@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Transactional
    public Pedido adicionarItemAoCarrinho(Long produtoId, HttpSession session) {

        // 1. Acha o produto
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + produtoId));

        // 2. Pega a etiqueta "pedidoId" da Sessão
        Long pedidoId = (Long) session.getAttribute("pedidoId");
        Pedido pedido;

        // 3. Se não tem etiqueta (pedidoId == null), é o PRIMEIRO clique.
        if (pedidoId == null) {
            // Cria um novo Pedido (carrinho) no banco
            pedido = new Pedido();
            pedido.setStatus(StatusPedido.EM_PREPARO);
            pedido.setDtPedido(LocalDateTime.now());
            pedido = pedidoRepository.save(pedido);
            // Cola a etiqueta "pedidoId" na sessão
            session.setAttribute("pedidoId", pedido.getId());

        } else {
            // Se JÁ tem etiqueta, TENTA buscar o Pedido no banco
            Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);

            // 4. <<< AQUI ESTÁ A CORREÇÃO >>>
            if (pedidoOpt.isEmpty()) {
                // BUG: O carrinho "lembra" de um Pedido 1, MAS o banco foi limpo.
                // SOLUÇÃO: Criamos um novo pedido e "resetamos" a etiqueta na sessão.

                System.out.println("Sessão 'stale' detectada. Criando novo pedido.");
                pedido = new Pedido(); // Cria um novo
                pedido.setStatus(StatusPedido.EM_PREPARO);
                pedido.setDtPedido(LocalDateTime.now());
                pedido = pedidoRepository.save(pedido); // Salva (ele vai pegar um novo ID)

                // Atualiza a etiqueta na sessão para o NOVO ID.
                session.setAttribute("pedidoId", pedido.getId());

            } else {
                // O Pedido foi encontrado. Vida normal.
                pedido = pedidoOpt.get();
            }
        }

        // 5. Agora temos o Pedido. Vamos ver se o produto JÁ está nele.
        Optional<ItemPedido> itemExistente = pedido.getItens().stream()
                .filter(item -> item.getProduto().getId().equals(produtoId))
                .findFirst();

        if (itemExistente.isPresent()) {
            // Se SIM: Apenas aumenta a quantidade
            ItemPedido item = itemExistente.get();
            item.setQuantidade(item.getQuantidade() + 1);
            itemPedidoRepository.save(item);
        } else {
            // Se NÃO: Cria um novo ItemPedido e o adiciona ao Pedido
            ItemPedido novoItem = new ItemPedido();
            novoItem.setProduto(produto);
            novoItem.setQuantidade(1);
            novoItem.setPrecoUnitario(produto.getPreco());
            novoItem.setPedido(pedido);

            pedido.getItens().add(novoItem);
            itemPedidoRepository.save(novoItem);
        }

        // 6. Recalcula o valor total
        recalcularValorTotal(pedido);

        // 7. Salva o Pedido (carrinho) atualizado no banco
        return pedidoRepository.save(pedido);
    }

    private void recalcularValorTotal(Pedido pedido) {
        BigDecimal valorTotalCalculado = BigDecimal.ZERO;

        // Itera sobre a lista de itens DENTRO da entidade Pedido
        for (ItemPedido item : pedido.getItens()) {
            // Garante que temos dados válidos para calcular
            if (item.getPrecoUnitario() != null && item.getQuantidade() > 0) {
                valorTotalCalculado = valorTotalCalculado.add(
                        item.getPrecoUnitario().multiply(new BigDecimal(item.getQuantidade()))
                );
            }
        }

        pedido.setValorTotal(valorTotalCalculado);
        pedido.setSubtotal(valorTotalCalculado); // Por enquanto, subtotal = total. Sem frete.
    }

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


    // Método  para converter ItemPedido (Entidade) -> ItemPedidoResponseDTO
    // Método para converter ItemPedido (Entidade) -> ItemPedidoResponseDTO
    private ItemPedidoResponseDTO converterParaItemResponseDTO(ItemPedido itemPedido) {

        // Valores padrão
        String nomeProduto = "Produto Inválido";
        String imagemUrl = "logo.png";
        BigDecimal preco = BigDecimal.ZERO;

        // Se o produto existir, pega os dados reais
        if (itemPedido.getProduto() != null) {
            nomeProduto = itemPedido.getProduto().getNome();
            imagemUrl = itemPedido.getProduto().getImagemUrl();
        }

        // Pega o preço (se existir)
        if (itemPedido.getPrecoUnitario() != null) {
            preco = itemPedido.getPrecoUnitario();
        }


        return new ItemPedidoResponseDTO(
                itemPedido.getId(),
                nomeProduto,
                itemPedido.getQuantidade(),
                preco,
                imagemUrl
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


    @Transactional
    public Pedido finalizarPedido(Long pedidoId, PedidoCheckoutDTO dadosCheckout) {
        // 1. Busca o Pedido (carrinho) que estávamos montando
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + pedidoId));

        // 2. Atualiza o Pedido com os dados do formulário
        pedido.setCep(dadosCheckout.cep());
        pedido.setEndereco(dadosCheckout.endereco());
        pedido.setNumero(dadosCheckout.numero());
        pedido.setBairro(dadosCheckout.bairro());
        pedido.setCidade(dadosCheckout.cidade());
        pedido.setEstado(dadosCheckout.estado());

        pedido.setStatus(StatusPedido.PRONTO);

        // 4. Salva o pedido finalizado no banco
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void removerItemDoCarrinho(Long itemPedidoId) {

        // 1. Tenta encontrar o ItemPedido no banco
        Optional<ItemPedido> itemOpt = itemPedidoRepository.findById(itemPedidoId);

        // 2. Se não encontrar (ex: usuário clicou duas vezes), não faz nada.
        if (itemOpt.isEmpty()) {
            System.out.println("Não foi possível remover: Item " + itemPedidoId + " não encontrado.");
            return;
        }

        ItemPedido itemParaRemover = itemOpt.get();

        // 3. Pega o "Pedido-pai" (o carrinho) antes de deletar o item
        Pedido pedido = itemParaRemover.getPedido();

        // 4. Remove o item da lista de itens do "Pedido-pai"
        // (Isso é crucial para o recalcularValorTotal funcionar)
        pedido.getItens().remove(itemParaRemover);

        // 5. Deleta o item do banco de dados
        itemPedidoRepository.delete(itemParaRemover);

        // 6. RECALCULA O TOTAL! (Nós já temos esse método!)
        recalcularValorTotal(pedido);

        // 7. Salva o "Pedido-pai" com o novo total
        pedidoRepository.save(pedido);

        System.out.println("Item " + itemPedidoId + " removido. Novo total: " + pedido.getValorTotal());

    }
    @Transactional
    public void atualizarQuantidadeItem(Long itemPedidoId, int novaQuantidade) {

        // 1. Garante que a quantidade não é zero ou negativa.
        if (novaQuantidade < 1) {
            // Se for, apenas remove o item.
            removerItemDoCarrinho(itemPedidoId);
            return;
        }

        // 2. Tenta encontrar o ItemPedido no banco
        ItemPedido item = itemPedidoRepository.findById(itemPedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Item de pedido não encontrado: " + itemPedidoId));

        // 3. ATUALIZA a quantidade
        item.setQuantidade(novaQuantidade);

        // 4. Salva o item atualizado
        itemPedidoRepository.save(item);

        // 5. RECALCULA O TOTAL!
        // (Pegamos o "Pedido-pai" e chamamos o método que JÁ TEMOS)
        Pedido pedido = item.getPedido();
        recalcularValorTotal(pedido);

        // 6. Salva o "Pedido-pai" com o novo total
        pedidoRepository.save(pedido);

        System.out.println("Item " + itemPedidoId + " atualizado para Qtd: " + novaQuantidade + ". Novo total: " + pedido.getValorTotal());
    }
}