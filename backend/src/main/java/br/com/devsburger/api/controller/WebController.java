package br.com.devsburger.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import java.util.ArrayList;
import java.math.BigDecimal;
import br.com.devsburger.api.entity.Produto;
import br.com.devsburger.api.dto.PedidoResponseDTO;
import br.com.devsburger.api.dto.ItemPedidoResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import br.com.devsburger.api.repository.ProdutoRepository;
import java.util.List;
// ------------------------------------

@Controller
public class WebController {

    // 1. INJETAR O REPOSITÓRIO (ASSIM COMO O PRODUTOCONTROLLER FAZ)
    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping("/")
    public String mostrarHomePage(Model model) {

        // 2. BUSCAR OS PRODUTOS REAIS DO BANCO
        List<Produto> produtosReais = produtoRepository.findAll();

        // 3. ENVIAR A LISTA REAL PARA O HTML
        model.addAttribute("produtos", produtosReais);

        return "home"; // Retorna o home.html
    }

    // O restante do seu controller continua igual...

    @GetMapping("/checkout")
    public String mostrarCheckout(Model model) {

        PedidoResponseDTO carrinhoVazio = new PedidoResponseDTO(
                null, null, null, null, BigDecimal.ZERO,
                new ArrayList<ItemPedidoResponseDTO>(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, null, null, null, null, null
        );

        model.addAttribute("carrinho", carrinhoVazio);
        return "checkout";
    }


    @GetMapping("/success")
    public String mostrarSuccess() {
        return "success";
    }
}