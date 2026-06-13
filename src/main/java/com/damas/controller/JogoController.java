package com.damas.controller;

import com.damas.dto.MovimentoDTO;
import com.damas.model.Tabuleiro;
import com.damas.service.JogoService;
import com.damas.service.MovimentoService;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.damas.service.LeiMaioriaService;

@RestController
@RequestMapping("/jogo")
public class JogoController {

    @Autowired
    private JogoService jogoService;

    @Autowired
    private MovimentoService movimentoService;

    @Autowired
    private LeiMaioriaService leiMaioriaService;

    // =====================================================
    // TABULEIRO
    // =====================================================

    @GetMapping("/tabuleiro")
    public int[][] tabuleiro() {

        Tabuleiro tabuleiro = jogoService.getTabuleiro();

        return tabuleiro.getTabuleiro();
    }

    // =====================================================
    // TURNO
    // =====================================================

    @GetMapping("/turno")
    public String turno() {

        if (jogoService.getJogadorAtual() == 1) {

            return "Jogador Vermelho";
        }

        return "Jogador Preto";
    }

    // =====================================================
    // VITÓRIA
    // =====================================================

    @GetMapping("/vitoria")
    public String vitoria() {

        String vencedor = jogoService.verificarVitoria(
                jogoService.getTabuleiro());

        if (vencedor == null) {

            return "Partida em andamento";
        }

        return vencedor;
    }

    // =====================================================
    // MOVER PEÇA
    // =====================================================

    @PostMapping("/mover")
    public int[][] mover(
            @RequestBody MovimentoDTO movimento) {

        Tabuleiro tabuleiro = jogoService.getTabuleiro();

        // =====================================================
        // CAPTURA EM SEQUÊNCIA OBRIGATÓRIA
        // =====================================================

        if (jogoService.getLinhaCapturaObrigatoria() != null) {

            if (movimento.getOrigemLinha() != jogoService.getLinhaCapturaObrigatoria()
                    ||
                    movimento.getOrigemColuna() != jogoService.getColunaCapturaObrigatoria()) {

                return tabuleiro.getTabuleiro();
            }
        }

        boolean capturaEmSequencia = jogoService.getLinhaCapturaObrigatoria() != null;

        boolean valido = movimentoService.movimentoValido(
                tabuleiro,
                movimento.getOrigemLinha(),
                movimento.getOrigemColuna(),
                movimento.getDestinoLinha(),
                movimento.getDestinoColuna(),
                jogoService.getJogadorAtual(),
                capturaEmSequencia);

        if (!valido) {

            return tabuleiro.getTabuleiro();
        }

        boolean capturou = movimentoService.capturarPeca(
                tabuleiro,
                movimento.getOrigemLinha(),
                movimento.getOrigemColuna(),
                movimento.getDestinoLinha(),
                movimento.getDestinoColuna());

        movimentoService.mover(
                tabuleiro,
                movimento.getOrigemLinha(),
                movimento.getOrigemColuna(),
                movimento.getDestinoLinha(),
                movimento.getDestinoColuna());

        movimentoService.verificarDama(
                tabuleiro);

        // captura em sequência
        boolean continuar = false;

        if (capturou) {

            continuar = movimentoService.podeCapturarNovamente(
                    tabuleiro,
                    movimento.getDestinoLinha(),
                    movimento.getDestinoColuna());

            if (continuar) {

                jogoService.definirCapturaObrigatoria(
                        movimento.getDestinoLinha(),
                        movimento.getDestinoColuna());
            }
        }

        if (!continuar) {

            jogoService.limparCapturaObrigatoria();

            jogoService.trocarTurno();
        }

        return tabuleiro.getTabuleiro();
    }

    @GetMapping("/captura-obrigatoria")
    public int[] capturaObrigatoria() {

        System.out.println(
                "CAPTURA OBRIGATORIA -> "
                        + jogoService.getLinhaCapturaObrigatoria()
                        + ","
                        + jogoService.getColunaCapturaObrigatoria());

        if (jogoService.getLinhaCapturaObrigatoria() == null) {

            return new int[] { -1, -1 };
        }

        return new int[] {
                jogoService.getLinhaCapturaObrigatoria(),
                jogoService.getColunaCapturaObrigatoria()
        };
    }

    // =====================================================
    // MOVIMENTOS POSSÍVEIS
    // =====================================================

    @GetMapping("/movimentos")
    public int[][] movimentos(
            @RequestParam int linha,
            @RequestParam int coluna) {

        Tabuleiro tabuleiro = jogoService.getTabuleiro();

        int[][] movimentos = new int[64][2];

        int quantidade = 0;

        boolean capturaEmSequencia = jogoService.getLinhaCapturaObrigatoria() != null;

        for (int destinoLinha = 0; destinoLinha < 8; destinoLinha++) {

            for (int destinoColuna = 0; destinoColuna < 8; destinoColuna++) {

                boolean valido = movimentoService.movimentoValido(
                        tabuleiro,
                        linha,
                        coluna,
                        destinoLinha,
                        destinoColuna,
                        jogoService.getJogadorAtual(),
                        capturaEmSequencia);

                if (valido) {

                    int peca = tabuleiro.getTabuleiro()[linha][coluna];

                    if (peca == 3 || peca == 4) {

                    }

                    movimentos[quantidade][0] = destinoLinha;
                    movimentos[quantidade][1] = destinoColuna;

                    quantidade++;
                }
            }
        }

        int[][] resultado = new int[quantidade][2];

        for (int i = 0; i < quantidade; i++) {

            resultado[i][0] = movimentos[i][0];

            resultado[i][1] = movimentos[i][1];
        }

        return resultado;
    }

    // =====================================================
    // REINICIAR JOGO
    // =====================================================

    @PostMapping("/reiniciar")
    public int[][] reiniciar() {

        jogoService.reiniciarJogo();

        return jogoService
                .getTabuleiro()
                .getTabuleiro();
    }
}