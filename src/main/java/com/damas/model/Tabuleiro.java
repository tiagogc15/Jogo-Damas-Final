package com.damas.model;

public class Tabuleiro {

    private int[][] tabuleiro = new int[8][8];

    public Tabuleiro() {

        iniciarPecas();
    }

    public Tabuleiro(int[][] matriz) {

        for (int linha = 0; linha < 8; linha++) {

            for (int coluna = 0; coluna < 8; coluna++) {

                this.tabuleiro[linha][coluna] = matriz[linha][coluna];
            }
        }
    }

    public void iniciarPecas() {

        // Jogador 1
        for (int linha = 0; linha < 3; linha++) {

            for (int coluna = 0; coluna < 8; coluna++) {

                if ((linha + coluna) % 2 == 1) {

                    tabuleiro[linha][coluna] = TipoPeca.BRANCA;
                }
            }
        }

        // Jogador 2
        for (int linha = 5; linha < 8; linha++) {

            for (int coluna = 0; coluna < 8; coluna++) {

                if ((linha + coluna) % 2 == 1) {

                    tabuleiro[linha][coluna] = TipoPeca.PRETA;
                }
            }
        }
    }

    public int[][] getTabuleiro() {

        return tabuleiro;
    }

    public void moverPeca(
            int origemLinha,
            int origemColuna,
            int destinoLinha,
            int destinoColuna) {

        int peca = tabuleiro[origemLinha][origemColuna];

        tabuleiro[origemLinha][origemColuna] = TipoPeca.VAZIO;

        tabuleiro[destinoLinha][destinoColuna] = peca;
    }

    public void removerPeca(int linha, int coluna) {

        tabuleiro[linha][coluna] = TipoPeca.VAZIO;
    }

    // Restaura o Memento (Volta no tempo)
    public void restaurar(TabuleiroMemento memento) {
        int[][] estadoAntigo = memento.getEstadoSalvo();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                this.tabuleiro[i][j] = estadoAntigo[i][j];
            }
        }
    }
}