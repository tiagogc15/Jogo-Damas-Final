let origemLinha = null;
let origemColuna = null;

let casaSelecionada = null;
let movimentosPossiveis = [];
let jogoEncerrado = false;

const tabuleiroDiv =
    document.getElementById("tabuleiro");

const btnDesfazer = document.getElementById("btn-desfazer");

// =====================================================
// CARREGAR TABULEIRO
// =====================================================

async function carregarTabuleiro() {

    const resposta =
        await fetch("/jogo/tabuleiro");

    const tabuleiro =
        await resposta.json();

    desenharTabuleiro(tabuleiro);

    carregarTurno();

    carregarVitoria();

    // NOVO: Sempre atualiza o estado do botão ao recarregar a tela
    atualizarBotaoDesfazer();
}

// =====================================================
// DESENHAR TABULEIRO
// =====================================================

function desenharTabuleiro(tabuleiro) {

    tabuleiroDiv.innerHTML = "";

    for (let linha = 0;
        linha < 8;
        linha++) {

        for (let coluna = 0;
            coluna < 8;
            coluna++) {

            const casa =
                document.createElement("div");

            casa.classList.add("casa");

            // =====================================================
            // COR DA CASA
            // =====================================================

            if ((linha + coluna) % 2 == 0) {

                casa.classList.add("branca");

            } else {

                casa.classList.add("preta");
            }

            const valor =
                tabuleiro[linha][coluna];

            // =====================================================
            // PEÇAS
            // =====================================================

            if (valor !== 0) {

                const peca =
                    document.createElement("div");

                peca.classList.add("peca");

                // vermelha
                if (valor === 1 ||
                    valor === 3) {

                    peca.classList.add("vermelha");
                }

                // preta
                if (valor === 2 ||
                    valor === 4) {

                    peca.classList.add("preta-peca");
                }

                // dama
                if (valor === 3 ||
                    valor === 4) {

                    peca.classList.add("dama");
                }

                casa.appendChild(peca);
            }

            // =====================================================
            // CLIQUE
            // =====================================================

            casa.addEventListener("click", () => {

                clicarCasa(
                    linha,
                    coluna,
                    tabuleiro);
            });

            tabuleiroDiv.appendChild(casa);
        }
    }
}

// =====================================================
// TURNO
// =====================================================

async function carregarTurno() {

    const resposta =
        await fetch("/jogo/turno");

    const texto =
        await resposta.text();

    document.getElementById("turno")
        .innerText = texto;
}

// =====================================================
// VITÓRIA
// =====================================================

async function carregarVitoria() {

    const resposta =
        await fetch("/jogo/vitoria");

    const texto =
        await resposta.text();

    document.getElementById("vitoria")
        .innerText = texto;

    if (texto !== "Partida em andamento") {

        jogoEncerrado = true;
    }
}
// =====================================================
// DESFAZER JOGADA
// =====================================================
async function desfazerJogada() {
    await fetch("/jogo/desfazer", {
        method: "POST"
    });

    // Limpa a seleção atual para evitar bugs visuais
    origemLinha = null;
    origemColuna = null;
    if (casaSelecionada) {
        casaSelecionada.classList.remove("selecionada");
        casaSelecionada = null;
    }
    limparMovimentos();

    // Recarrega o tabuleiro com a matriz antiga que o backend devolveu
    carregarTabuleiro();
}

// =====================================================
// ATUALIZAR STATUS DO BOTÃO DESFAZER (NOVA FUNÇÃO)
// =====================================================
async function atualizarBotaoDesfazer() {
    if (!btnDesfazer) return; // Segurança caso o ID esteja diferente no HTML

    const resposta = await fetch("/jogo/pode-desfazer");
    const podeDesfazer = await resposta.json();

    // Se NÃO puder desfazer (podeDesfazer === false), ativa o disabled do HTML
    btnDesfazer.disabled = !podeDesfazer;
}

// =====================================================
// REINICIAR
// =====================================================

async function reiniciarJogo() {

    await fetch("/jogo/reiniciar", {

        method: "POST"
    });

    // limpar seleção
    origemLinha = null;
    origemColuna = null;
    casaSelecionada = null;

    jogoEncerrado = false;
    carregarTabuleiro();
}

// =====================================================
// MOSTRAR MOVIMENTOS (Consultando o Java)
// =====================================================

async function mostrarMovimentosPossiveis(linhaOrigem, colunaOrigem) {
    limparMovimentos();

    // Pergunta ao Java quais são os movimentos válidos para essa peça
    const resposta = await fetch(`/jogo/movimentos-validos?linha=${linhaOrigem}&coluna=${colunaOrigem}`);
    const movimentos = await resposta.json();

    // Pinta cada casa que o Java devolver
    movimentos.forEach(mov => {
        const destinoLinha = mov[0];
        const destinoColuna = mov[1];

        const casa = document.querySelectorAll(".casa")[destinoLinha * 8 + destinoColuna];

        // Dica visual: na dama clássica, se a peça anda 2 ou mais casas, costuma ser captura
        const diferenca = Math.abs(destinoLinha - linhaOrigem);

        if (diferenca >= 2) {
            casa.classList.add("captura-possivel"); // Borda vermelha
        } else {
            casa.classList.add("movimento-possivel"); // Borda verde limão
        }
    });
}

// =====================================================
// LIMPAR MOVIMENTOS
// =====================================================

function limparMovimentos() {

    document.querySelectorAll(".casa")
        .forEach(casa => {

            casa.classList
                .remove("movimento-possivel");

            casa.classList
                .remove("captura-possivel");
        });
}

// =====================================================
// CLIQUE NAS CASAS
// =====================================================

async function clicarCasa(linha, coluna, tabuleiro) {

    if (jogoEncerrado) {
        return;
    }

    const valor = tabuleiro[linha][coluna];

    // =====================================================
    // SELECIONAR PEÇA
    // =====================================================

    if (origemLinha === null && valor !== 0) {
        origemLinha = linha;
        origemColuna = coluna;

        casaSelecionada = document.querySelectorAll(".casa")[linha * 8 + coluna];
        casaSelecionada.classList.add("selecionada");

        console.log("Peça selecionada");

        // Chama a função para acender o tabuleiro!
        mostrarMovimentosPossiveis(linha, coluna);

        return;
    }

    // =====================================================
    // MOVER PEÇA
    // =====================================================

    if (origemLinha !== null) {

        await fetch("/jogo/mover", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                origemLinha: origemLinha,
                origemColuna: origemColuna,
                destinoLinha: linha,
                destinoColuna: coluna
            })
        });

        // Limpa as cores verde e vermelha da jogada anterior
        limparMovimentos();

        // Pergunta ao Java se a peça precisa continuar comendo
        const capturaObrigatoria = await obterCapturaObrigatoria();

        // 🔴 ALTERAÇÃO AQUI: Recarregamos o tabuleiro PRIMEIRO
        // Isso atualiza as peças na tela antes de tentarmos pintá-las novamente
        await carregarTabuleiro();

        // Se o Java disse que NÃO tem captura obrigatória (-1)
        if (capturaObrigatoria[0] === -1) {
            origemLinha = null;
            origemColuna = null;
            casaSelecionada = null;

        } else {
            // Se o Java disse que TEM captura obrigatória, mantemos a peça selecionada!
            origemLinha = capturaObrigatoria[0];
            origemColuna = capturaObrigatoria[1];

            // Como recarregamos o tabuleiro, precisamos pegar a "nova" div da casa no HTML
            casaSelecionada = document.querySelectorAll(".casa")[origemLinha * 8 + origemColuna];
            casaSelecionada.classList.add("selecionada");

            // 🔴 O SEGREDO: Chamamos a função de mostrar movimentos novamente para o combo!
            mostrarMovimentosPossiveis(origemLinha, origemColuna);
        }
    }
}

async function obterCapturaObrigatoria() {

    const resposta =
        await fetch(
            "/jogo/captura-obrigatoria");

    const dados =
        await resposta.json();

    console.log(
        "CAPTURA OBRIGATORIA:",
        dados);

    return dados;
}

// =====================================================
// INICIAR
// =====================================================

carregarTabuleiro();