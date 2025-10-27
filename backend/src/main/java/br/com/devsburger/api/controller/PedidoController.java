package br.com.devsburger.api.controller;
import br.com.devsburger.api.dto.AtualizacaoStatusPedidoDTO;
import br.com.devsburger.api.dto.PedidoRequestDTO;
import br.com.devsburger.api.entity.Pedido;
import br.com.devsburger.api.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService; // Agora injetamos o Service, e não o Repository!

    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody PedidoRequestDTO dto) {
        Pedido pedidoSalvo = pedidoService.criarPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoSalvo);
    }

    @GetMapping
    public List<Pedido> listar() {
        return pedidoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        Optional<Pedido> pedido = pedidoService.buscarPorId(id);

        if (pedido.isPresent()) {
            return ResponseEntity.ok(pedido.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // Chamamos o service e guardamos a resposta (true ou false)
        boolean foiDeletado = pedidoService.deletarPedido(id);

        if (foiDeletado) {
            // Se foi deletado com sucesso, retornamos a resposta padrão para delete: 204 No Content
            return ResponseEntity.noContent().build();
        } else {
            // Se o service retornou false, significa que não encontrou o pedido. Retornamos 404.
            return ResponseEntity.notFound().build();
        }
    }

    // Adicione este método dentro da classe PedidoController

    @PatchMapping("/{id}/status")
    public ResponseEntity<Pedido> atualizarStatus(@PathVariable Long id, @RequestBody AtualizacaoStatusPedidoDTO dto) {
        return pedidoService.atualizarStatus(id, dto.status())
                .map(pedidoAtualizado -> ResponseEntity.ok(pedidoAtualizado)) // Se o service encontrou e atualizou, retorna 200 OK
                .orElse(ResponseEntity.notFound().build()); // Se não encontrou, retorna 404 Not Found
    }
}
