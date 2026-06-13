package com.damas.controller;

import com.damas.dto.MovimentoDTO;
import com.damas.model.Tabuleiro;
import com.damas.model.TabuleiroMemento; // <-- IMPORT ADICIONADO AQUI
import com.damas.service.HistoricoService;
import com.damas.service.JogoService;
import com.damas.service.MovimentoService;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/jogo")
public class JogoController {

    @Autowired
    private JogoService jogoService;

    @Autowired
    private MovimentoService movimentoService;

    @Autowired
    private HistoricoService historicoService;

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

        System.out.println("================================");
        System.out.println("JOGADA RECEBIDA PELO CONTROLLER");
        System.out.println("Origem: " + movimento.getOrigemLinha() + "," + movimento.getOrigemColuna());
        System.out.println("Destino: " + movimento.getDestinoLinha() + "," + movimento.getDestinoColuna());
        System.out.println("Captura obrigatoria: " + jogoService.getLinhaCapturaObrigatoria() + "," + jogoService.getColunaCapturaObrigatoria());
        System.out.println("================================");

        // =====================================================
        // CAPTURA EM SEQUÊNCIA OBRIGATÓRIA
        // =====================================================

        if (jogoService.getLinhaCapturaObrigatoria() != null) {
            if (movimento.getOrigemLinha() != jogoService.getLinhaCapturaObrigatoria()
                    || movimento.getOrigemColuna() != jogoService.getColunaCapturaObrigatoria()) {
                return tabuleiro.getTabuleiro();
            }
        }

        System.out.println("================================");
        System.out.println("JOGADA DO USUARIO");
        System.out.println("Origem: " + movimento.getOrigemLinha() + "," + movimento.getOrigemColuna());
        System.out.println("Destino: " + movimento.getDestinoLinha() + "," + movimento.getDestinoColuna());
        System.out.println("================================");

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
            System.out.println("MOVIMENTO REJEITADO");
            return tabuleiro.getTabuleiro();
        }

        // 🔴 NOVA CHAMADA: Salva a foto completa (Matriz + Turno + Captura)
        historicoService.salvarEstado(
                tabuleiro,
                jogoService.getJogadorAtual(),
                jogoService.getLinhaCapturaObrigatoria(),
                jogoService.getColunaCapturaObrigatoria()
        );

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

        movimentoService.verificarDama(tabuleiro);

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
        System.out.println("CAPTURA OBRIGATORIA -> " + jogoService.getLinhaCapturaObrigatoria() + "," + jogoService.getColunaCapturaObrigatoria());

        if (jogoService.getLinhaCapturaObrigatoria() == null) {
            return new int[] { -1, -1 };
        }

        return new int[] {
                jogoService.getLinhaCapturaObrigatoria(),
                jogoService.getColunaCapturaObrigatoria()
        };
    }

    // =====================================================
    // DESFAZER JOGADA
    // =====================================================

    // 🔴 ALTERADO: Agora restaura as informações do Memento usando a nova lógica
    @PostMapping("/desfazer")
    public int[][] desfazer() {
        Tabuleiro tabuleiro = jogoService.getTabuleiro();

        // Tenta desfazer e pega a fotografia completa salva
        TabuleiroMemento mementoRestaurado = historicoService.desfazer(tabuleiro);

        if (mementoRestaurado != null) {
            // 1. Devolve o turno para quem estava jogando na hora da foto
            jogoService.setJogadorAtual(mementoRestaurado.getJogadorAtual());

            // 2. Se a pessoa estava no meio de um combo de capturas, devolve o estado do combo
            if (mementoRestaurado.getLinhaCapturaObrigatoria() != null) {
                jogoService.definirCapturaObrigatoria(
                        mementoRestaurado.getLinhaCapturaObrigatoria(),
                        mementoRestaurado.getColunaCapturaObrigatoria()
                );
            } else {
                jogoService.limparCapturaObrigatoria();
            }
            System.out.println("JOGADA DESFEITA COM SUCESSO");
        } else {
            System.out.println("NÃO HÁ JOGADAS PARA DESFAZER - TURNO MANTIDO");
        }

        return tabuleiro.getTabuleiro();
    }

    // =====================================================
    // VERIFICAR SE PODE DESFAZER
    // =====================================================
    @GetMapping("/pode-desfazer")
    public boolean podeDesfazer() {
        return !historicoService.isVazio();
    }

    // =====================================================
    // REINICIAR JOGO
    // =====================================================

    @PostMapping("/reiniciar")
    public int[][] reiniciar() {
        jogoService.reiniciarJogo();
        historicoService.limpar(); // Evita que o usuário desfaça jogadas da partida anterior!

        return jogoService.getTabuleiro().getTabuleiro();
    }

    // =====================================================
    // MOVIMENTOS VÁLIDOS
    // =====================================================

    @GetMapping("/movimentos-validos")
    public java.util.List<int[]> movimentosValidos(
            @RequestParam int linha,
            @RequestParam int coluna) {

        java.util.List<int[]> validos = new java.util.ArrayList<>();
        Tabuleiro tabuleiro = jogoService.getTabuleiro();
        int jogadorAtual = jogoService.getJogadorAtual();
        boolean capturaEmSequencia = jogoService.getLinhaCapturaObrigatoria() != null;

        for (int dLinha = 0; dLinha < 8; dLinha++) {
            for (int dColuna = 0; dColuna < 8; dColuna++) {

                boolean valido = movimentoService.movimentoValido(
                        tabuleiro, linha, coluna, dLinha, dColuna, jogadorAtual, capturaEmSequencia);

                if (valido) {
                    validos.add(new int[]{dLinha, dColuna});
                }
            }
        }
        return validos;
    }
}