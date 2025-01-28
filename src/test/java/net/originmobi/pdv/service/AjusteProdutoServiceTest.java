package net.originmobi.pdv.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import net.originmobi.pdv.enumerado.ajuste.AjusteStatus;
import net.originmobi.pdv.model.Ajuste;
import net.originmobi.pdv.model.AjusteProduto;
import net.originmobi.pdv.model.Produto;
import net.originmobi.pdv.model.ProdutoEstoque;
import net.originmobi.pdv.repository.AjusteProdutoRepository;

class AjusteProdutoServiceTest {

    @InjectMocks
    private AjusteProdutoService service;

    @Mock
    private AjusteProdutoRepository ajusteProdutoRepository;

    @Mock
    private ProdutoService produtoService;

    @Mock
    private AjusteService ajusteService;  

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testListaProdutosAjuste() {
        Long codAjuste = 1L;
        List<AjusteProduto> produtos = new ArrayList<>();
        produtos.add(new AjusteProduto());
        when(ajusteProdutoRepository.findByAjusteCodigoEquals(codAjuste)).thenReturn(produtos);

        List<AjusteProduto> result = service.listaProdutosAjuste(codAjuste);

        assertEquals(produtos, result);
        verify(ajusteProdutoRepository).findByAjusteCodigoEquals(codAjuste);
    }

    @Test
    void testBuscaProdAjust() {
        Long codAjuste = 1L;
        Long codProd = 2L;
        int expected = 1;
        when(ajusteProdutoRepository.buscaProdAjuste(codAjuste, codProd)).thenReturn(expected);

        int result = service.buscaProdAjust(codAjuste, codProd);

        assertEquals(expected, result);
        verify(ajusteProdutoRepository).buscaProdAjuste(codAjuste, codProd);
    }

    @Test
    void testAddProdutoSuccess() {
        Long codAjuste = 1L;
        Long codProd = 2L;
        int qtdAlteracao = 5;

        Ajuste ajuste = new Ajuste();
        ajuste.setStatus(AjusteStatus.APROCESSAR);
        Produto produto = new Produto();
        ProdutoEstoque estoque = new ProdutoEstoque();
        estoque.setQtd(10);
        produto.setEstoque(estoque);

        when(ajusteService.busca(codAjuste)).thenReturn(Optional.of(ajuste));
        when(produtoService.busca(codProd)).thenReturn(produto);
        when(ajusteProdutoRepository.buscaProdAjuste(codAjuste, codProd)).thenReturn(0);

        String result = service.addProduto(codAjuste, codProd, qtdAlteracao);

        assertEquals("Ajuste processado com sucesso", result);
        verify(ajusteProdutoRepository).insereProduto(codAjuste, codProd, 10, qtdAlteracao, 15);
    }


    @Test
    void testAddProdutoInsertFailure() {
        Long codAjuste = 1L;
        Long codProd = 2L;
        int qtdAlteracao = 5;

        Ajuste ajuste = new Ajuste();
        ajuste.setStatus(AjusteStatus.APROCESSAR);
        Produto produto = new Produto();
        ProdutoEstoque estoque = new ProdutoEstoque();
        estoque.setQtd(10);
        produto.setEstoque(estoque);

        when(ajusteService.busca(codAjuste)).thenReturn(Optional.of(ajuste));
        when(produtoService.busca(codProd)).thenReturn(produto);
        when(ajusteProdutoRepository.buscaProdAjuste(codAjuste, codProd)).thenReturn(0);
        doThrow(RuntimeException.class).when(ajusteProdutoRepository).insereProduto(any(), any(), anyInt(), anyInt(), anyInt());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.addProduto(codAjuste, codProd, qtdAlteracao);
        });

        assertEquals("Erro ao tentar inserir produto no ajuste, chame o suporte", exception.getMessage());
    }

    @Test
    void testRemoveProdutoAlreadyProcessed() {
        Long codAjuste = 1L;
        Long codItem = 2L;

        Ajuste ajuste = new Ajuste();
        ajuste.setStatus(AjusteStatus.PROCESSADO);

        when(ajusteService.busca(codAjuste)).thenReturn(Optional.of(ajuste));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.removeProduto(codAjuste, codItem);
        });

        assertEquals("Ajuste já esta processado", exception.getMessage());
    }

    @Test
    void testRemoveProdutoFailure() {
        Long codAjuste = 1L;
        Long codItem = 2L;

        Ajuste ajuste = new Ajuste();
        ajuste.setStatus(AjusteStatus.APROCESSAR);

        when(ajusteService.busca(codAjuste)).thenReturn(Optional.of(ajuste));
        doThrow(RuntimeException.class).when(ajusteProdutoRepository).removeProduto(any(), any());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.removeProduto(codAjuste, codItem);
        });

        assertEquals("Erro ao tentar remover produto do ajuste, chame o suporte", exception.getMessage());
    }
    
    @Test
    void testAddProdutoSuccessWithZeroStock() {
        Long codAjuste = 1L;
        Long codProd = 2L;
        int qtdAlteracao = 5;

        Ajuste ajuste = new Ajuste();
        ajuste.setStatus(AjusteStatus.APROCESSAR);
        Produto produto = new Produto();
        ProdutoEstoque estoque = new ProdutoEstoque();
        estoque.setQtd(0);
        produto.setEstoque(estoque);

        when(ajusteService.busca(codAjuste)).thenReturn(Optional.of(ajuste));
        when(produtoService.busca(codProd)).thenReturn(produto);
        when(ajusteProdutoRepository.buscaProdAjuste(codAjuste, codProd)).thenReturn(0);

        String result = service.addProduto(codAjuste, codProd, qtdAlteracao);

        assertEquals("Ajuste processado com sucesso", result);
        verify(ajusteProdutoRepository).insereProduto(codAjuste, codProd, 0, qtdAlteracao, 5);
    }   
    
    @Test
    void testAddProdutoInsertError() {
        Long codAjuste = 1L;
        Long codProd = 2L;
        int qtdAlteracao = 5;

        Ajuste ajuste = new Ajuste();
        ajuste.setStatus(AjusteStatus.APROCESSAR);
        Produto produto = new Produto();
        ProdutoEstoque estoque = new ProdutoEstoque();
        estoque.setQtd(10);
        produto.setEstoque(estoque);

        when(ajusteService.busca(codAjuste)).thenReturn(Optional.of(ajuste));
        when(produtoService.busca(codProd)).thenReturn(produto);
        when(ajusteProdutoRepository.buscaProdAjuste(codAjuste, codProd)).thenReturn(0);
        doThrow(new RuntimeException("Erro ao tentar inserir produto no ajuste")).when(ajusteProdutoRepository)
            .insereProduto(anyLong(), anyLong(), anyInt(), anyInt(), anyInt());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.addProduto(codAjuste, codProd, qtdAlteracao);
        });

        assertEquals("Erro ao tentar inserir produto no ajuste, chame o suporte", exception.getMessage());
    }
    
    @Test
    void testRemoveProdutoSuccess() {
        Long codAjuste = 1L;
        Long codItem = 2L;

        Ajuste ajuste = new Ajuste();
        ajuste.setStatus(AjusteStatus.APROCESSAR);

        when(ajusteService.busca(codAjuste)).thenReturn(Optional.of(ajuste));

        String result = service.removeProduto(codAjuste, codItem);

        assertEquals("Produto removido com sucesso", result);
        verify(ajusteProdutoRepository).removeProduto(codAjuste, codItem);
    }
    
    @Test
    void testAddProdutoAjusteJaProcessado() {
        Long codAjuste = 1L;
        Long codProd = 2L;

        Ajuste ajuste = new Ajuste();
        ajuste.setStatus(AjusteStatus.PROCESSADO);

        when(ajusteService.busca(codAjuste)).thenReturn(Optional.of(ajuste));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.addProduto(codAjuste, codProd, 5);
        });

        assertEquals("Ajuste já esta processado", exception.getMessage());
    }   
    
    @Test
    void testAddProdutoProdutoNaoEncontrado() {
        Long codAjuste = 1L;
        Long codProd = 2L;
        int qtdAlteracao = 5;

        Ajuste ajuste = new Ajuste();
        ajuste.setStatus(AjusteStatus.APROCESSAR);

        when(ajusteService.busca(codAjuste)).thenReturn(Optional.of(ajuste));
        when(produtoService.busca(codProd)).thenThrow(new RuntimeException("Produto não encontrado"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.addProduto(codAjuste, codProd, qtdAlteracao);
        });

        assertEquals("Produto não encontrado", exception.getMessage());
    }    
    
    @Test
    void testAddProdutoJaExistenteNoAjuste() {
        Long codAjuste = 1L;
        Long codProd = 2L;
        int qtdAlteracao = 5;

        Ajuste ajuste = new Ajuste();
        ajuste.setStatus(AjusteStatus.APROCESSAR);
        Produto produto = new Produto();
        ProdutoEstoque estoque = new ProdutoEstoque();
        estoque.setQtd(10);
        produto.setEstoque(estoque);

        when(ajusteService.busca(codAjuste)).thenReturn(Optional.of(ajuste));
        when(produtoService.busca(codProd)).thenReturn(produto);
        when(ajusteProdutoRepository.buscaProdAjuste(codAjuste, codProd)).thenReturn(1);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.addProduto(codAjuste, codProd, qtdAlteracao);
        });

        assertEquals("Este produto já existe neste ajuste", exception.getMessage());
    }       
    
}
