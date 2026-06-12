package com.damas.service;

import com.damas.model.Tabuleiro;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MovimentoService {

    @Autowired
    private LeiMaioriaService leiMaioriaService;

    public void mover(
            Tabuleiro tabuleiro,
            int origemLinha,
            int origemColuna,
            int destinoLinha,
            int destinoColuna) {

        tabuleiro.moverPeca(
                origemLinha,
                origemColuna,
                destinoLinha,
                destinoColuna);
    }

    // =====================================================
    // VERIFICAR DAMA
    // =====================================================

    public void verificarDama(
            Tabuleiro tabuleiro) {

        // Vermelha vira dama
        for (int coluna = 0; coluna < 8; coluna++) {

            if (tabuleiro.getTabuleiro()[7][coluna] == 1) {

                tabuleiro.getTabuleiro()[7][coluna] = 3;
            }
        }

        // Preta vira dama
        for (int coluna = 0; coluna < 8; coluna++) {

            if (tabuleiro.getTabuleiro()[0][coluna] == 2) {

                tabuleiro.getTabuleiro()[0][coluna] = 4;
            }
        }
    }

    // =====================================================
    // VALIDAR MOVIMENTO
    // =====================================================

    public boolean movimentoValido(
            Tabuleiro tabuleiro,
            int origemLinha,
            int origemColuna,
            int destinoLinha,
            int destinoColuna,
            int jogadorAtual,
            boolean capturaEmSequencia) {

        int[][] matriz = tabuleiro.getTabuleiro();

        // destino fora
        if (destinoLinha < 0 ||
                destinoLinha >= 8 ||
                destinoColuna < 0 ||
                destinoColuna >= 8) {

            return false;
        }

        // destino ocupado
        if (matriz[destinoLinha][destinoColuna] != 0) {

            return false;
        }

        int peca = matriz[origemLinha][origemColuna];

        // =====================================================
        // VALIDAR TURNO
        // =====================================================

        // jogador vermelho
        if (jogadorAtual == 1) {

            if (peca != 1 &&
                    peca != 3) {

                return false;
            }
        }

        // jogador preto
        if (jogadorAtual == 2) {

            if (peca != 2 &&
                    peca != 4) {

                return false;
            }
        }

        // peça inexistente
        if (peca == 0) {

            return false;
        }

        int diferencaLinha = destinoLinha - origemLinha;

        int absLinha = Math.abs(diferencaLinha);

        // =====================================================
        // CAPTURA OBRIGATÓRIA
        // =====================================================

        boolean existeCaptura = existeCapturaDisponivel(
                tabuleiro,
                jogadorAtual);

        int maiorCaptura = leiMaioriaService
                .maiorCapturaJogador(
                        tabuleiro,
                        jogadorAtual);

        int capturasDaPeca = leiMaioriaService
                .calcularMaiorSequenciaCaptura(
                        tabuleiro,
                        origemLinha,
                        origemColuna);

        // existe captura no tabuleiro
        if (existeCaptura && !capturaEmSequencia) {

            // existe peça melhor para capturar
            if (capturasDaPeca < maiorCaptura) {

                return false;
            }

            // movimento simples proibido
            if (absLinha == 1) {

                return false;
            }
        }

        int diferencaColuna = Math.abs(
                destinoColuna -
                        origemColuna);

        // movimento diagonal
        if (diferencaColuna != Math.abs(diferencaLinha)) {

            return false;
        }

        // =====================================================
        // PEÇA VERMELHA
        // =====================================================

        if (peca == 1) {

            // movimento simples
            if (absLinha == 1) {

                if (diferencaLinha != 1) {

                    return false;
                }
            }

            // captura
            if (absLinha == 2) {

                int linhaMeio = (origemLinha + destinoLinha) / 2;

                int colunaMeio = (origemColuna + destinoColuna) / 2;

                int inimigo = matriz[linhaMeio][colunaMeio];

                if (inimigo != 2 &&
                        inimigo != 4) {

                    return false;
                }
            }

            // tamanho inválido
            if (absLinha != 1 &&
                    absLinha != 2) {

                return false;
            }
        }

        // =====================================================
        // PEÇA PRETA
        // =====================================================

        if (peca == 2) {

            // movimento simples
            if (absLinha == 1) {

                if (diferencaLinha != -1) {

                    return false;
                }
            }

            // captura
            if (absLinha == 2) {

                int linhaMeio = (origemLinha + destinoLinha) / 2;

                int colunaMeio = (origemColuna + destinoColuna) / 2;

                int inimigo = matriz[linhaMeio][colunaMeio];

                if (inimigo != 1 &&
                        inimigo != 3) {

                    return false;
                }
            }

            // tamanho inválido
            if (absLinha != 1 &&
                    absLinha != 2) {

                return false;
            }
        }
        // =====================================================
        // DAMA
        // =====================================================

        if (peca == 3 || peca == 4) {

            // movimento precisa ser diagonal
            if (Math.abs(diferencaLinha) != diferencaColuna) {

                return false;
            }

            int direcaoLinha = diferencaLinha > 0 ? 1 : -1;

            int direcaoColuna = (destinoColuna - origemColuna) > 0
                    ? 1
                    : -1;

            int linhaAtual = origemLinha + direcaoLinha;

            int colunaAtual = origemColuna + direcaoColuna;

            int inimigos = 0;

            while (linhaAtual != destinoLinha &&
                    colunaAtual != destinoColuna) {

                int valor = matriz[linhaAtual][colunaAtual];

                // peça aliada
                if (peca == 3 &&
                        (valor == 1 ||
                                valor == 3)) {

                    return false;
                }

                if (peca == 4 &&
                        (valor == 2 ||
                                valor == 4)) {

                    return false;
                }

                // inimigo encontrado
                if (peca == 3 &&
                        (valor == 2 ||
                                valor == 4)) {

                    inimigos++;
                }

                if (peca == 4 &&
                        (valor == 1 ||
                                valor == 3)) {

                    inimigos++;
                }

                linhaAtual += direcaoLinha;
                colunaAtual += direcaoColuna;
            }

            // mais de um inimigo
            if (inimigos > 1) {

                return false;
            }

            return true;
        }

        return true;
    }

    // =====================================================
    // PODE CONTINUAR CAPTURANDO
    // =====================================================

    public boolean podeCapturarNovamente(
            Tabuleiro tabuleiro,
            int linha,
            int coluna) {

        int[][] matriz = tabuleiro.getTabuleiro();

        int peca = matriz[linha][coluna];

        int[][] direcoes = {
                { -1, -1 },
                { -1, 1 },
                { 1, -1 },
                { 1, 1 }
        };

        // =====================================================
        // DAMA
        // =====================================================

        if (peca == 3 || peca == 4) {

            for (int[] dir : direcoes) {

                int linhaAtual = linha + dir[0];

                int colunaAtual = coluna + dir[1];

                boolean encontrouInimigo = false;

                while (linhaAtual >= 0 &&
                        linhaAtual < 8 &&
                        colunaAtual >= 0 &&
                        colunaAtual < 8) {

                    int valor = matriz[linhaAtual][colunaAtual];

                    // vazio após inimigo
                    if (valor == 0 &&
                            encontrouInimigo) {

                        return true;
                    }

                    // peça aliada bloqueia
                    if (peca == 3 &&
                            (valor == 1 ||
                                    valor == 3)) {

                        break;
                    }

                    if (peca == 4 &&
                            (valor == 2 ||
                                    valor == 4)) {

                        break;
                    }

                    // encontrou inimigo
                    if (peca == 3 &&
                            (valor == 2 ||
                                    valor == 4)) {

                        if (encontrouInimigo) {

                            break;
                        }

                        encontrouInimigo = true;
                    }

                    if (peca == 4 &&
                            (valor == 1 ||
                                    valor == 3)) {

                        if (encontrouInimigo) {

                            break;
                        }

                        encontrouInimigo = true;
                    }

                    linhaAtual += dir[0];
                    colunaAtual += dir[1];
                }
            }

            return false;
        }

        // =====================================================
        // PEÇA COMUM
        // =====================================================

        int[][] capturas = {
                { -2, -2 },
                { -2, 2 },
                { 2, -2 },
                { 2, 2 }
        };

        for (int[] dir : capturas) {

            int destinoLinha = linha + dir[0];

            int destinoColuna = coluna + dir[1];

            int linhaMeio = linha + dir[0] / 2;

            int colunaMeio = coluna + dir[1] / 2;

            // fora
            if (destinoLinha < 0 ||
                    destinoLinha >= 8 ||
                    destinoColuna < 0 ||
                    destinoColuna >= 8) {

                continue;
            }

            // destino ocupado
            if (matriz[destinoLinha][destinoColuna] != 0) {

                continue;
            }

            int inimigo = matriz[linhaMeio][colunaMeio];

            // vermelha
            if (peca == 1) {

                if (inimigo == 2 ||
                        inimigo == 4) {

                    return true;
                }
            }

            // preta
            if (peca == 2) {

                if (inimigo == 1 ||
                        inimigo == 3) {

                    return true;
                }
            }
        }

        return false;
    }

    // =====================================================
    // CAPTURA
    // =====================================================

    public boolean capturarPeca(
            Tabuleiro tabuleiro,
            int origemLinha,
            int origemColuna,
            int destinoLinha,
            int destinoColuna) {

        boolean capturou = false;

        int[][] matriz = tabuleiro.getTabuleiro();

        int peca = matriz[origemLinha][origemColuna];

        int direcaoLinha = destinoLinha > origemLinha ? 1 : -1;

        int direcaoColuna = destinoColuna > origemColuna ? 1 : -1;

        int linhaAtual = origemLinha + direcaoLinha;

        int colunaAtual = origemColuna + direcaoColuna;

        while (linhaAtual != destinoLinha &&
                colunaAtual != destinoColuna) {

            int valor = matriz[linhaAtual][colunaAtual];

            // dama vermelha
            if (peca == 3 &&
                    (valor == 2 ||
                            valor == 4)) {

                tabuleiro.removerPeca(
                        linhaAtual,
                        colunaAtual);

                capturou = true;
            }

            // dama preta
            if (peca == 4 &&
                    (valor == 1 ||
                            valor == 3)) {

                tabuleiro.removerPeca(
                        linhaAtual,
                        colunaAtual);

                capturou = true;
            }

            // peça comum vermelha
            if (peca == 1 &&
                    (valor == 2 ||
                            valor == 4)) {

                tabuleiro.removerPeca(
                        linhaAtual,
                        colunaAtual);

                capturou = true;
            }

            // peça comum preta
            if (peca == 2 &&
                    (valor == 1 ||
                            valor == 3)) {

                tabuleiro.removerPeca(
                        linhaAtual,
                        colunaAtual);

                capturou = true;
            }

            linhaAtual += direcaoLinha;
            colunaAtual += direcaoColuna;
        }

        return capturou;
    }

    // =====================================================
    // EXISTE CAPTURA DISPONÍVEL
    // =====================================================

    public boolean existeCapturaDisponivel(
            Tabuleiro tabuleiro,
            int jogadorAtual) {

        int[][] matriz = tabuleiro.getTabuleiro();

        for (int linha = 0; linha < 8; linha++) {

            for (int coluna = 0; coluna < 8; coluna++) {

                int peca = matriz[linha][coluna];

                // jogador vermelho
                if (jogadorAtual == 1) {

                    if (peca != 1 &&
                            peca != 3) {

                        continue;
                    }
                }

                // jogador preto
                if (jogadorAtual == 2) {

                    if (peca != 2 &&
                            peca != 4) {

                        continue;
                    }
                }

                if (podeCapturarNovamente(
                        tabuleiro,
                        linha,
                        coluna)) {

                    return true;
                }
            }
        }

        return false;
    }

    // =====================================================
    // QUANTIDADE DE CAPTURAS
    // =====================================================

    // =====================================================
    // QUANTIDADE DE CAPTURAS
    // =====================================================

    public int quantidadeCapturas(
            Tabuleiro tabuleiro,
            int linha,
            int coluna) {

        int[][] matriz = tabuleiro.getTabuleiro();

        int peca = matriz[linha][coluna];

        int total = 0;

        // =====================================================
        // PEÇA COMUM
        // =====================================================

        if (peca == 1 || peca == 2) {

            int[][] capturas = {
                    { -2, -2 },
                    { -2, 2 },
                    { 2, -2 },
                    { 2, 2 }
            };

            for (int[] dir : capturas) {

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

                if (peca == 1 &&
                        (inimigo == 2 ||
                                inimigo == 4)) {

                    total++;
                }

                if (peca == 2 &&
                        (inimigo == 1 ||
                                inimigo == 3)) {

                    total++;
                }
            }
        }

        // =====================================================
        // DAMA
        // =====================================================

        if (peca == 3 || peca == 4) {

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

                while (linhaAtual >= 0 &&
                        linhaAtual < 8 &&
                        colunaAtual >= 0 &&
                        colunaAtual < 8) {

                    int valor = matriz[linhaAtual][colunaAtual];

                    if (valor == 0 &&
                            encontrouInimigo) {

                        total++;
                        break;
                    }

                    if (peca == 3 &&
                            (valor == 1 ||
                                    valor == 3)) {

                        break;
                    }

                    if (peca == 4 &&
                            (valor == 2 ||
                                    valor == 4)) {

                        break;
                    }

                    if (peca == 3 &&
                            (valor == 2 ||
                                    valor == 4)) {

                        if (encontrouInimigo) {

                            break;
                        }

                        encontrouInimigo = true;
                    }

                    if (peca == 4 &&
                            (valor == 1 ||
                                    valor == 3)) {

                        if (encontrouInimigo) {

                            break;
                        }

                        encontrouInimigo = true;
                    }

                    linhaAtual += dir[0];
                    colunaAtual += dir[1];
                }
            }
        }

        return total;
    }

    // =====================================================
    // CAPTURA MÁXIMA DO JOGADOR
    // =====================================================

    public int capturaMaximaJogador(
            Tabuleiro tabuleiro,
            int jogadorAtual) {

        int[][] matriz = tabuleiro.getTabuleiro();

        int maior = 0;

        for (int linha = 0; linha < 8; linha++) {

            for (int coluna = 0; coluna < 8; coluna++) {

                int peca = matriz[linha][coluna];

                // peças vermelhas
                if (jogadorAtual == 1) {

                    if (peca != 1 &&
                            peca != 3) {

                        continue;
                    }
                }

                // peças pretas
                if (jogadorAtual == 2) {

                    if (peca != 2 &&
                            peca != 4) {

                        continue;
                    }
                }

                int capturas = quantidadeCapturas(
                        tabuleiro,
                        linha,
                        coluna);

                if (capturas > maior) {

                    maior = capturas;
                }
            }
        }

        return maior;
    }

    // =====================================================
    // PEÇA POSSUI CAPTURA MÁXIMA
    // =====================================================

    public boolean possuiCapturaMaxima(
            Tabuleiro tabuleiro,
            int jogadorAtual,
            int linha,
            int coluna) {

        int maior = capturaMaximaJogador(
                tabuleiro,
                jogadorAtual);

        int capturasPeca = quantidadeCapturas(
                tabuleiro,
                linha,
                coluna);

        return capturasPeca == maior;
    }
}
