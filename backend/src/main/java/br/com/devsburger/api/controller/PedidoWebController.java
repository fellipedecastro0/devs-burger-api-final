// Arquivo: src/main/java/br/com/devsburger/api/controller/PedidoWebController.java
package br.com.devsburger.api.controller;

// --- Imports Necessários ---
import br.com.devsburger.api.dto.PedidoResponseDTO; // O DTO que criamos para a resposta
import br.com.devsburger.api.service.PedidoService; // O serviço que busca os dados
import org.springframework.beans.factory.annotation.Autowired; // Para injetar o service
import org.springframework.stereotype.Controller; // Para indicar que é um controller web
import org.springframework.ui.Model; // Para passar dados para o HTML
import org.springframework.web.bind.annotation.GetMapping; // Para mapear a URL GET
import org.springframework.web.bind.annotation.PathVariable; // Para pegar o ID da URL
import org.springframework.web.bind.annotation.RequestMapping; // Para definir o prefixo da URL

import java.util.Optional; // Para lidar com a possibilidade do pedido não existir

@Controller // Indica que esta classe lida com requisições web e retorna nomes de templates
@RequestMapping("/pedido") // Todas as URLs aqui dentro começarão com /pedido
public class PedidoWebController {

    // Injeta a instância do PedidoService para podermos usá-la
    @Autowired
    private PedidoService pedidoService;

    // Mapeia requisições GET para /pedido/acompanhar/{id} (ex: /pedido/acompanhar/123)
    @GetMapping("/acompanhar/{id}")
    public String acompanharPedido(@PathVariable Long id, Model model) {

        // Usa o service para buscar o pedido pelo ID, já convertendo para o DTO de resposta
        Optional<PedidoResponseDTO> pedidoDtoOptional = pedidoService.buscarPorIdComoDTO(id);

        // Verifica se o pedido (como DTO) foi encontrado
        if (pedidoDtoOptional.isPresent()) {
            // Se sim, pega o DTO de dentro do Optional
            PedidoResponseDTO pedidoDto = pedidoDtoOptional.get();

            // Adiciona o DTO ao Model (a "mala" de dados para o HTML)
            // O nome "pedidoDto" será usado no template Thymeleaf
            model.addAttribute("pedidoDto", pedidoDto);

            // Retorna o nome do arquivo HTML (sem .html) que deve ser renderizado
            // O Spring/Thymeleaf vai procurar por "acompanhamento-pedido.html"
            // na pasta src/main/resources/templates/
            return "acompanhamento-pedido";

        } else {
            // Se o Optional estava vazio (pedido não encontrado),
            // retorna o nome de um template HTML de erro
            // (Você precisa criar este arquivo: erro-pedido-nao-encontrado.html)
            return "erro-pedido-nao-encontrado";
        }
    }
}
