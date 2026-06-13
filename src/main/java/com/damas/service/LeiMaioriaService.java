package com.damas.service;

import com.damas.model.Tabuleiro;
import org.springframework.stereotype.Service;

@Service
public class LeiMaioriaService {

    // =====================================================
    // MAIOR SEQUÊNCIA DE CAPTURA
    // =====================================================

    private int[][] copiarMatriz(
            int[][] original) {

        int[][] copia = new int[8][8];

        for (int linha = 0; linha < 8; linha++) {

            for (int coluna = 0; coluna < 8; coluna++) {

                copia[linha][coluna] = original[linha][coluna];
            }
        }

        return copia;
    }

    private Tabuleiro simularCaptura(
            Tabuleiro tabuleiro,
            int origemLinha,
            int origemColuna,
            int destinoLinha,
            int destinoColuna,
            int linhaCapturada,
            int colunaCapturada) {

        Tabuleiro copia = new Tabuleiro(
                copiarMatriz(
                        tabuleiro.getTabuleiro()));

        copia.removerPeca(
                linhaCapturada,
                colunaCapturada);

        copia.moverPeca(
                origemLinha,
                origemColuna,
                destinoLinha,
                destinoColuna);

        return copia;
    }

    private int calcularCapturaDama(
            Tabuleiro tabuleiro,
            int linha,
            int coluna) {

        int[][] matriz = tabuleiro.getTabuleiro();

        int peca = matriz[linha][coluna];

        int maior = 0;

        int[][] direcoes = {
                { -1, -1 },
                { -1, 1 },
                { 1, -1 },
                { 1, 1 }
        };

        for (int[] dir : direcoes) {

            int linhaAtual = linha + dir[0];
            int colunaAtual = coluna + dir[1];

            boolean encontrouInimigo = false;

            int linhaInimigo = -1;
            int colunaInimigo = -1;

            while (linhaAtual >= 0 &&
                    linhaAtual < 8 &&
                    colunaAtual >= 0 &&
                    colunaAtual < 8) {

                int valor = matriz[linhaAtual][colunaAtual];

                // peça aliada bloqueia
                if (peca == 3 &&
                        (valor == 1 || valor == 3)) {

                    break;
                }

                if (peca == 4 &&
                        (valor == 2 || valor == 4)) {

                    break;
                }

                // encontrou inimigo
                boolean inimigo = false;

                if (peca == 3) {

                    inimigo = valor == 2 ||
                            valor == 4;
                }

                if (peca == 4) {

                    inimigo = valor == 1 ||
                            valor == 3;
                }

                if (inimigo) {

                    // segundo inimigo na mesma diagonal
                    if (encontrouInimigo) {

                        break;
                    }

                    encontrouInimigo = true;

                    linhaInimigo = linhaAtual;
                    colunaInimigo = colunaAtual;
                }

                // casa vazia depois do inimigo
                if (valor == 0 &&
                        encontrouInimigo) {

                    Tabuleiro copia = simularCaptura(
                            tabuleiro,
                            linha,
                            coluna,
                            linhaAtual,
                            colunaAtual,
                            linhaInimigo,
                            colunaInimigo);

                    int sequencia = 1 +
                            calcularCapturaDama(
                                    copia,
                                    linhaAtual,
                                    colunaAtual);

                    maior = Math.max(
                            maior,
                            sequencia);
                }

                linhaAtual += dir[0];
                colunaAtual += dir[1];
            }
        }

        return maior;
    }

    public int calcularMaiorSequenciaCaptura(
            Tabuleiro tabuleiro,
            int linha,
            int coluna) {

        int[][] matriz = tabuleiro.getTabuleiro();

        int peca = matriz[linha][coluna];

        if (peca == 3 || peca == 4) {

            return calcularCapturaDama(
                    tabuleiro,
                    linha,
                    coluna);
        }

        // sem peça
        if (peca == 0) {

            return 0;
        }

        int maior = 0;

        int[][] direcoes = {
                { -2, -2 },
                { -2, 2 },
                { 2, -2 },
                { 2, 2 }
        };

        for (int[] dir : direcoes) {

            int destinoLinha = linha + dir[0];

            int destinoColuna = coluna + dir[1];

            int linhaMeio = linha + dir[0] / 2;

            int colunaMeio = coluna + dir[1] / 2;

            if (destinoLinha < 0 ||
                    destinoLinha >= 8 ||
                    destinoColuna < 0 ||
                    destinoColuna >= 8) {

                continue;
            }

            if (matriz[destinoLinha][destinoColuna] != 0) {

                continue;
            }

            int inimigo = matriz[linhaMeio][colunaMeio];

            boolean captura = false;

            if (peca == 1 || peca == 3) {

                captura = inimigo == 2 ||
                        inimigo == 4;
            }

            if (peca == 2 || peca == 4) {

                captura = inimigo == 1 ||
                        inimigo == 3;
            }

            if (!captura) {

                continue;
            }

            Tabuleiro copia = simularCaptura(
                    tabuleiro,
                    linha,
                    coluna,
                    destinoLinha,
                    destinoColuna,
                    linhaMeio,
                    colunaMeio);

            int sequencia = 1 +
                    calcularMaiorSequenciaCaptura(
                            copia,
                            destinoLinha,
                            destinoColuna);

            maior = Math.max(
                    maior,
                    sequencia);
        }

        return maior;
    }

    public int maiorCapturaJogador(
            Tabuleiro tabuleiro,
            int jogador) {

        int maior = 0;

        int[][] matriz = tabuleiro.getTabuleiro();

        for (int linha = 0; linha < 8; linha++) {

            for (int coluna = 0; coluna < 8; coluna++) {

                int peca = matriz[linha][coluna];

                if (jogador == 1 &&
                        peca != 1 &&
                        peca != 3) {

                    continue;
                }

                if (jogador == 2 &&
                        peca != 2 &&
                        peca != 4) {

                    continue;
                }

                maior = Math.max(
                        maior,
                        calcularMaiorSequenciaCaptura(
                                tabuleiro,
                                linha,
                                coluna));
            }
        }

        return maior;
    }

    public boolean possuiMaiorCaptura(
            Tabuleiro tabuleiro,
            int jogador,
            int linha,
            int coluna) {

        int maior = maiorCapturaJogador(
                tabuleiro,
                jogador);

        int capturas = calcularMaiorSequenciaCaptura(
                tabuleiro,
                linha,
                coluna);

        return capturas == maior;
    }

    public boolean mantemSequenciaMaxima(
            Tabuleiro tabuleiro,
            int origemLinha,
            int origemColuna,
            int destinoLinha,
            int destinoColuna,
            int linhaCapturada,
            int colunaCapturada) {

        Tabuleiro copia = simularCaptura(
                tabuleiro,
                origemLinha,
                origemColuna,
                destinoLinha,
                destinoColuna,
                linhaCapturada,
                colunaCapturada);

        int restante = calcularMaiorSequenciaCaptura(
                copia,
                destinoLinha,
                destinoColuna);

        int atual = calcularMaiorSequenciaCaptura(
                tabuleiro,
                origemLinha,
                origemColuna);

        return restante == (atual - 1);
    }

    public int capturasRestantesAposJogada(
            Tabuleiro tabuleiro,
            int origemLinha,
            int origemColuna,
            int destinoLinha,
            int destinoColuna,
            int linhaCapturada,
            int colunaCapturada) {

        Tabuleiro copia = simularCaptura(
                tabuleiro,
                origemLinha,
                origemColuna,
                destinoLinha,
                destinoColuna,
                linhaCapturada,
                colunaCapturada);

        return calcularMaiorSequenciaCaptura(
                copia,
                destinoLinha,
                destinoColuna);
    }
}