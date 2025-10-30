package br.com.devsburger.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.math.BigDecimal;
import br.com.devsburger.api.entity.Produto;
import br.com.devsburger.api.dto.PedidoResponseDTO;
import br.com.devsburger.api.dto.ItemPedidoResponseDTO;

@Controller
public class WebController {

    @GetMapping("/")
    public String mostrarHomePage(Model model) {
        model.addAttribute("produtos", new ArrayList<Produto>());
        return "home";
    }

    @GetMapping("/checkout")
    public String mostrarCheckout(Model model) {

        PedidoResponseDTO carrinhoVazio = new PedidoResponseDTO(
                null, // id
                null, // nomeCliente
                null, // dtPedido
                null, // status (pode ser null por enquanto)
                BigDecimal.ZERO, // valorTotal
                new ArrayList<ItemPedidoResponseDTO>(), // itens

                // --- PASSANDO OS VALORES ZERADOS PARA OS NOVOS CAMPOS ---
                BigDecimal.ZERO, // subtotal
                BigDecimal.ZERO, // frete
                BigDecimal.ZERO , // total
                null, // cep
                null, // endereco
                null, // numero
                null, // bairro
                null, // cidade
                null  // estado

        );

        model.addAttribute("carrinho", carrinhoVazio);
        return "checkout";
    }

    @GetMapping("/checkout-filled")
    public String mostrarCheckoutFilled(Model model) {

        PedidoResponseDTO pedidoVazio = new PedidoResponseDTO(
                null, // id
                null, // nomeCliente
                null, // dtPedido
                null, // status
                BigDecimal.ZERO, // valorTotal
                new ArrayList<ItemPedidoResponseDTO>(), // itens
                BigDecimal.ZERO, // subtotal
                BigDecimal.ZERO, // frete
                BigDecimal.ZERO,  // total
                null, // cep
                null, // endereco
                null, // numero
                null, // bairro
                null, // cidade
                null  // estado
        );

        model.addAttribute("pedido", pedidoVazio);
        return "checkout-filled";
    }


    @GetMapping("/success")
    public String mostrarSuccess() {
        return "success";
    }
}