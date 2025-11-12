package br.com.devsburger.api.controller;

import br.com.devsburger.api.dto.ItemPedidoResponseDTO;
import br.com.devsburger.api.dto.PedidoCheckoutDTO;
import br.com.devsburger.api.dto.PedidoResponseDTO;
import br.com.devsburger.api.entity.Pedido;
import br.com.devsburger.api.entity.Produto;
import br.com.devsburger.api.repository.ProdutoRepository;
import br.com.devsburger.api.service.PedidoService; // <<< IMPORTADO
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession; // <<< IMPORTADO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping; // <<< IMPORTADO
import org.springframework.web.bind.annotation.RequestParam; // <<< IMPORTADO

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private ProdutoRepository produtoRepository;

    // "Contratando" o PedidoService
    @Autowired
    private PedidoService pedidoService;

    @GetMapping("/")
    public String mostrarHomePage(Model model) {
        List<Produto> produtosReais = produtoRepository.findAll();
        model.addAttribute("produtos", produtosReais);
        return "home";
    }

    // MÉTODO ATUALIZADO (com HttpSession)
    @PostMapping("/adicionarCarrinho")
    public String adicionarAoCarrinho(@RequestParam("produtoId") Long idDoProduto,
                                      HttpSession session) { // <-- Pedimos a Sessão

        System.out.println("ADICIONANDO PRODUTO ID: " + idDoProduto);

        // O "Porteiro" (Controller) chama o "Mágico" (Service)
        // para fazer todo o trabalho sujo
        pedidoService.adicionarItemAoCarrinho(idDoProduto, session);

        // Redireciona o usuário para a página de checkout
        return "redirect:/checkout";
    }

    // MÉTODO ATUALIZADO (com HttpSession)
    @GetMapping("/checkout")
    public String mostrarCheckout(Model model, HttpSession session) { // <-- Pedimos a Sessão

        // Pergunta para a Sessão qual é o ID do Pedido (carrinho) atual
        Long pedidoId = (Long) session.getAttribute("pedidoId");

        if (pedidoId == null) {
            // Se não tem ID, é um carrinho vazio
            System.out.println("Mostrando carrinho VAZIO (pedidoId nulo na sessao)");
            model.addAttribute("carrinho", criarCarrinhoVazio());
        } else {
            // Se TEM ID, busca o Pedido no banco e converte para DTO
            System.out.println("Mostrando carrinho para o PEDIDO ID: " + pedidoId);
            PedidoResponseDTO carrinhoDto = pedidoService.buscarPorIdComoDTO(pedidoId)
                    .orElse(criarCarrinhoVazio()); // Se der erro, mostra vazio

            model.addAttribute("carrinho", carrinhoDto);
        }

        return "checkout";
    }

    @GetMapping("/success")
    public String mostrarSuccess(@RequestParam("id") Long pedidoId, Model model) {

        // 1. Busca o Pedido finalizado no banco (usando o DTO)
        PedidoResponseDTO pedidoDto = pedidoService.buscarPorIdComoDTO(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado")); // Lança erro se o ID for inválido

        // 2. Envia o DTO (a "pasta") para o success.html
        model.addAttribute("pedido", pedidoDto);

        return "success";
    }


    private PedidoResponseDTO criarCarrinhoVazio() {
        return new PedidoResponseDTO(
                null, null, null, null, BigDecimal.ZERO,
                new ArrayList<ItemPedidoResponseDTO>(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, null, null, null, null
        );
    }

    /*
     * MÉTODO "CONFIRMAR PEDIDO" ATUALIZADO
     * Ele agora chama o Service, limpa o carrinho e redireciona com o ID
     */
    @PostMapping("/confirmarPedido")
    public String confirmarPedido(
            @ModelAttribute PedidoCheckoutDTO dadosDoFormulario,
            HttpSession session) {

        // 1. Pega o ID do pedido que está no "carrinho"
        Long pedidoId = (Long) session.getAttribute("pedidoId");

        if (pedidoId == null) {
            // Carrinho expirou ou não existe. Volta para home.
            return "redirect:/";
        }

        // 2. CHAMA O SERVICE para finalizar o pedido no banco
        Pedido pedidoFinalizado = pedidoService.finalizarPedido(pedidoId, dadosDoFormulario);

        // 3. LIMPA O CARRINHO!
        // (Para o usuário poder fazer um novo pedido)
        session.removeAttribute("pedidoId");

        // 4. Redireciona para a página de sucesso, passando o ID do pedido
        return "redirect:/success?id=" + pedidoFinalizado.getId();
    }

    @PostMapping("/removerItem")
    public String removerItemDoCarrinho(@RequestParam("itemPedidoId") Long idDoItem) {

        System.out.println("REMOVENDO ITEM DE ID: " + idDoItem);

        // 1. Chamamos o "mágico" (que vamos criar agora)
        pedidoService.removerItemDoCarrinho(idDoItem);

        // 2. Redireciona o usuário DE VOLTA para o checkout
        return "redirect:/checkout";
    }

}
