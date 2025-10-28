
package br.com.devsburger.api.controller;
import br.com.devsburger.api.entity.Produto;
import br.com.devsburger.api.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.List;


@RestController // transforma uma classe Java normal em um ponto de atendimento de API.
@RequestMapping("/produtos")

public class ProdutoController {

    @Autowired
    private ProdutoRepository repository;

    // primeiro metodo "R" do CRUD (Read/Ler)

    @GetMapping
    public List<Produto> listarTodos() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id ) {
        return repository.findById(id)
                .map(produto -> ResponseEntity.ok(produto))
                .orElse(ResponseEntity.notFound().build());
    }

    // C CRIAR
    @PostMapping //é o cliente dizendo "Quero ADICIONAR este novo item ao cardápio".
    public ResponseEntity<Produto> criarProduto(@RequestBody Produto produto) {
        Produto produtoSalvo = repository.save(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoSalvo);
    }

    //U UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizarProduto(@PathVariable Long id, @RequestBody Produto produtoAtualizado) {
        return repository.findById(id)
                .map(produtoExistente -> {
                    produtoExistente.setNome(produtoAtualizado.getNome());
                    produtoExistente.setPreco(produtoAtualizado.getPreco());

                    return ResponseEntity.ok(repository.save(produtoExistente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // D DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();

        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
