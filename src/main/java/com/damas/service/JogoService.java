package com.damas.service;

import com.damas.model.Tabuleiro;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class JogoService {

    @Autowired
    private MovimentoService movimentoService;

    private Tabuleiro tabuleiro;

    private int jogadorAtual = 1;

    private Integer linhaCapturaObrigatoria = null;

    private Integer colunaCapturaObrigatoria = null;

    public JogoService() {
        tabuleiro = new Tabuleiro();
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public void reiniciarJogo() {
        tabuleiro = new Tabuleiro();
        jogadorAtual = 1;
        limparCapturaObrigatoria();
    }

    // =====================================================
    // VERIFICAR VITÓRIA
    // =====================================================

    public String verificarVitoria(Tabuleiro tabuleiro) {
        int vermelhas = 0;
        int pretas = 0;

        for (int linha = 0; linha < 8; linha++) {
            for (int coluna = 0; coluna < 8; coluna++) {
                int valor = tabuleiro.getTabuleiro()[linha][coluna];

                if (valor == 1 || valor == 3) {
                    vermelhas++;
                }

                if (valor == 2 || valor == 4) {
                    pretas++;
                }
            }
        }

        if (vermelhas == 0) {
            return "Jogador Preto venceu!";
        }

        if (pretas == 0) {
            return "Jogador Vermelho venceu!";
        }

        // =====================================================
        // BLOQUEIO DE MOVIMENTO
        // =====================================================

        if (!possuiMovimentos(tabuleiro, 1)) {
            return "Jogador Preto venceu!";
        }

        if (!possuiMovimentos(tabuleiro, 2)) {
            return "Jogador Vermelho venceu!";
        }

        return null;
    }

    // =====================================================
    // TURNO
    // =====================================================

    public int getJogadorAtual() {
        return jogadorAtual;
    }

    // 🔴 NOVO MÉTODO: Necessário para o Controller restaurar de quem era a vez na hora de Desfazer
    public void setJogadorAtual(int jogadorAtual) {
        this.jogadorAtual = jogadorAtual;
    }

    public void trocarTurno() {
        if (jogadorAtual == 1) {
            jogadorAtual = 2;
        } else {
            jogadorAtual = 1;
        }
    }

    // =====================================================
    // POSSUI MOVIMENTOS
    // =====================================================

    public boolean possuiMovimentos(Tabuleiro tabuleiro, int jogador) {
        int[][] matriz = tabuleiro.getTabuleiro();

        for (int linha = 0; linha < 8; linha++) {
            for (int coluna = 0; coluna < 8; coluna++) {
                int peca = matriz[linha][coluna];

                if (jogador == 1) {
                    if (peca != 1 && peca != 3) {
                        continue;
                    }
                }

                if (jogador == 2) {
                    if (peca != 2 && peca != 4) {
                        continue;
                    }
                }

                for (int destinoLinha = 0; destinoLinha < 8; destinoLinha++) {
                    for (int destinoColuna = 0; destinoColuna < 8; destinoColuna++) {

                        boolean valido = movimentoService.movimentoValido(
                                tabuleiro,
                                linha,
                                coluna,
                                destinoLinha,
                                destinoColuna,
                                jogador,
                                false);

                        if (valido) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public Integer getLinhaCapturaObrigatoria() {
        return linhaCapturaObrigatoria;
    }

    public Integer getColunaCapturaObrigatoria() {
        return colunaCapturaObrigatoria;
    }

    public void definirCapturaObrigatoria(int linha, int coluna) {
        linhaCapturaObrigatoria = linha;
        colunaCapturaObrigatoria = coluna;
    }

    public void limparCapturaObrigatoria() {
        linhaCapturaObrigatoria = null;
        colunaCapturaObrigatoria = null;
    }
}