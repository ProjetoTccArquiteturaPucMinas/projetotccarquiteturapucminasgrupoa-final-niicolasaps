package com.example.marketplace.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.example.marketplace.model.CategoriaProduto;
import com.example.marketplace.model.ItemCarrinho;
import com.example.marketplace.model.Produto;
import com.example.marketplace.model.ResumoCarrinho;
import com.example.marketplace.model.SelecaoCarrinho;
import com.example.marketplace.repository.ProdutoRepository;

@Service
public class ServicoCarrinho {

    private final ProdutoRepository repositorioProdutos;

    public ServicoCarrinho(ProdutoRepository repositorioProdutos) {
        this.repositorioProdutos = repositorioProdutos;
    }

    public ResumoCarrinho construirResumo(List<SelecaoCarrinho> selecoes) {
        List<ItemCarrinho> itens = new ArrayList<>();
        int qtdTotal = 0;

        for (SelecaoCarrinho s : selecoes) {
            Produto p = repositorioProdutos.buscarPorId(s.getProdutoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
            itens.add(new ItemCarrinho(p, s.getQuantidade()));
            qtdTotal += s.getQuantidade();
        }

        BigDecimal subtotal = itens.stream()
            .map(ItemCarrinho::calcularSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Declaracao explicita para evitar erro de escopo
        BigDecimal percentualDesconto = BigDecimal.ZERO;
        
        if (qtdTotal >= 4) {
            percentualDesconto = new BigDecimal("0.10");
        } else if (qtdTotal == 3) {
            percentualDesconto = new BigDecimal("0.07");
        } else if (qtdTotal == 2) {
            percentualDesconto = new BigDecimal("0.05");
        }

        BigDecimal vDescCat = BigDecimal.ZERO;
        for (ItemCarrinho i : itens) {
            BigDecimal pCat = obterDesc(i.getProduto().getCategoria());
            vDescCat = vDescCat.add(i.calcularSubtotal().multiply(pCat));
        }

        // Variaveis calculadas logo antes do return
        BigDecimal valorDescontoTotal = subtotal.multiply(percentualDesconto).add(vDescCat);
        BigDecimal valorFinalTotal = subtotal.subtract(valorDescontoTotal);

        return new ResumoCarrinho(itens, subtotal, percentualDesconto, valorDescontoTotal, valorFinalTotal);
    }

    private BigDecimal obterDesc(CategoriaProduto c) {
        if (c == null) return BigDecimal.ZERO;
        switch (c) {
            case CAPINHA: return new BigDecimal("0.03");
            case CARREGADOR: return new BigDecimal("0.05");
            case FONE: return new BigDecimal("0.03");
            case PELICULA: return new BigDecimal("0.02");
            case SUPORTE: return new BigDecimal("0.02");
            default: return BigDecimal.ZERO;
        }
    }
}
