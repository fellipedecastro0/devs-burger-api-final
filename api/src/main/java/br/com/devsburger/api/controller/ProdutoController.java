
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
@RequestMapping("/produtos") // Todos os endpoints aqui dentro começarão com /produtos

public class ProdutoController {

    @Autowired
    private ProdutoRepository repository;

    // métodos do CRUD virão aqui dentro

    // primeiro metodo "R" do CRUD (Read/Ler)

    @GetMapping // é o cliente dizendo "Quero VER o cardápio"
    public List<Produto> listarTodos() {
        return repository.findAll();
    }

    @GetMapping("/{id}") //O cliente está fazendo um pedido de LEITURA/BUSCAS
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id ) {
        return repository.findById(id)
                .map(produto -> ResponseEntity.ok(produto))
                .orElse(ResponseEntity.notFound().build());
    }

    // C CRIAR
    @PostMapping //é o cliente dizendo "Quero ADICIONAR este novo item ao cardápio".
    public ResponseEntity<Produto> criarProduto(@RequestBody Produto produto) {
        Produto produtoSalvo = repository.save(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoSalvo); // Prepare uma resposta profissional ('201 Criado)
    }

    //U UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizarProduto(@PathVariable Long id, @RequestBody Produto produtoAtualizado) {
        // Passo 1: Busca o produto pelo ID. O resultado é um Optional<Produto>.
        return repository.findById(id)
                // Passo 2: SE o Optional contiver um produto, execute o código abaixo.
                .map(produtoExistente -> {
                    // Atualiza os campos do produto que veio do banco.
                    produtoExistente.setNome(produtoAtualizado.getNome());
                    produtoExistente.setPreco(produtoAtualizado.getPreco());

                    // Salva o produto atualizado no banco e retorna uma resposta 200 OK.
                    return ResponseEntity.ok(repository.save(produtoExistente));
                })
                // Passo 3: SENÃO (se o Optional estiver vazio), retorna um 404 Not Found.
                .orElse(ResponseEntity.notFound().build());
    }

    // D DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable long id) {
        if (!repository.existsById(id)) {
            // se o produto nao existe retornamos 404 not found
            return ResponseEntity.notFound().build();

        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
