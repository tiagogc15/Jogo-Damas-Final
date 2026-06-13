package com.damas.service;

import com.damas.model.Tabuleiro;
import com.damas.model.TabuleiroMemento;
import org.springframework.stereotype.Service;
import java.util.Stack;

@Service
public class HistoricoService {
    private Stack<TabuleiroMemento> historico = new Stack<>();

    // Alterado: Agora recebe os dados do turno e captura
    public void salvarEstado(Tabuleiro tabuleiro, int jogadorAtual, Integer linhaCaptura, Integer colunaCaptura) {
        historico.push(new TabuleiroMemento(tabuleiro.getTabuleiro(), jogadorAtual, linhaCaptura, colunaCaptura));
    }

    // Alterado: Retorna o Memento (a foto completa) em vez de um boolean
    public TabuleiroMemento desfazer(Tabuleiro tabuleiro) {
        if (!historico.isEmpty()) {
            TabuleiroMemento ultimaFoto = historico.pop();
            // Restaura fisicamente as peças no tabuleiro
            tabuleiro.restaurar(ultimaFoto);
            return ultimaFoto; // Devolve para o Controller restaurar o resto
        }
        return null;
    }

    public boolean isVazio() {
        return historico.isEmpty();
    }

    public void limpar() {
        historico.clear();
    }
}