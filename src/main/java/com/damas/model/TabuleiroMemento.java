package com.damas.model;

public class TabuleiroMemento {
    private final int[][] estadoSalvo;
    private final int jogadorAtual;
    private final Integer linhaCapturaObrigatoria;
    private final Integer colunaCapturaObrigatoria;

    public TabuleiroMemento(int[][] matrizAtual, int jogadorAtual, Integer linhaCaptura, Integer colunaCaptura) {
        // Copia a matriz
        estadoSalvo = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                estadoSalvo[i][j] = matrizAtual[i][j];
            }
        }
        // Salva as regras do jogo no momento da jogada
        this.jogadorAtual = jogadorAtual;
        this.linhaCapturaObrigatoria = linhaCaptura;
        this.colunaCapturaObrigatoria = colunaCaptura;
    }

    public int[][] getEstadoSalvo() { return estadoSalvo; }
    public int getJogadorAtual() { return jogadorAtual; }
    public Integer getLinhaCapturaObrigatoria() { return linhaCapturaObrigatoria; }
    public Integer getColunaCapturaObrigatoria() { return colunaCapturaObrigatoria; }
}