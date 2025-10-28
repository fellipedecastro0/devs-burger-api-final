
package br.com.devsburger.api.controller;
import br.com.devsburger.api.dto.PedidoResponseDTO;
import br.com.devsburger.api.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/pedido")
public class PedidoWebController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping("/acompanhar/{id}")
    public String acompanharPedido(@PathVariable Long id, Model model) {

        Optional<PedidoResponseDTO> pedidoDtoOptional = pedidoService.buscarPorIdComoDTO(id);

        if (pedidoDtoOptional.isPresent()) {
            PedidoResponseDTO pedidoDto = pedidoDtoOptional.get();
            model.addAttribute("pedidoDto", pedidoDto);
            return "acompanhamento-pedido";

        } else {
            return "erro-pedido-nao-encontrado";
        }
    }
}
