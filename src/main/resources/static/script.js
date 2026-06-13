let origemLinha = null;
let origemColuna = null;

let casaSelecionada = null;
let movimentosPossiveis = [];
let jogoEncerrado = false;

const tabuleiroDiv =
    document.getElementById("tabuleiro");

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

    const elementoVitoria =
        document.getElementById("vitoria");

    elementoVitoria.innerText = texto;

    if (texto !== "Partida em andamento") {

        jogoEncerrado = true;

        elementoVitoria.style.color =
            "#28a745";

        elementoVitoria.style.fontSize =
            "40px";

        elementoVitoria.style.fontWeight =
            "bold";

        elementoVitoria.style.textAlign =
            "center";

        elementoVitoria.style.marginTop =
            "20px";

    } else {

        elementoVitoria.style.color =
            "black";

        elementoVitoria.style.fontSize =
            "24px";

        elementoVitoria.style.fontWeight =
            "normal";
    }
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
// MOSTRAR MOVIMENTOS
// =====================================================

async function mostrarMovimentosPossiveis(
    linha,
    coluna) {

    limparMovimentos();

    movimentosPossiveis = [];

    const resposta =
        await fetch(
            `/jogo/movimentos?linha=${linha}&coluna=${coluna}`);

    const movimentos =
        await resposta.json();

    movimentos.forEach(movimento => {

        const destinoLinha =
            movimento[0];

        const destinoColuna =
            movimento[1];

        movimentosPossiveis.push({

            linha: destinoLinha,
            coluna: destinoColuna
        });

        const casa =
            document.querySelectorAll(".casa")
            [destinoLinha * 8 + destinoColuna];

        casa.classList
            .add("movimento-possivel");
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

async function clicarCasa(
    linha,
    coluna,
    tabuleiro) {

    if (jogoEncerrado) {

        return;
    }

    const valor =
        tabuleiro[linha][coluna];

    // =====================================================
    // SELECIONAR PEÇA
    // =====================================================

    if (origemLinha === null &&
        valor !== 0) {

        origemLinha = linha;
        origemColuna = coluna;

        casaSelecionada =
            document.querySelectorAll(".casa")
            [linha * 8 + coluna];

        casaSelecionada.classList
            .add("selecionada");

        console.log("Peça selecionada");

        await mostrarMovimentosPossiveis(
            linha,
            coluna);

        return;
    }

    // =====================================================
    // MOVER PEÇA
    // =====================================================

    if (origemLinha !== null) {

        await fetch("/jogo/mover", {

            method: "POST",

            headers: {
                "Content-Type":
                    "application/json"
            },

            body: JSON.stringify({

                origemLinha:
                    origemLinha,

                origemColuna:
                    origemColuna,

                destinoLinha:
                    linha,

                destinoColuna:
                    coluna
            })
        });

        // remover destaque
        if (casaSelecionada) {

            casaSelecionada.classList
                .remove("selecionada");
        }

        const capturaObrigatoria =
            await obterCapturaObrigatoria();

        if (capturaObrigatoria[0] === -1) {

            origemLinha = null;
            origemColuna = null;
            casaSelecionada = null;

            await carregarTabuleiro();

        } else {

            origemLinha =
                capturaObrigatoria[0];

            origemColuna =
                capturaObrigatoria[1];

            await carregarTabuleiro();

            casaSelecionada =
                document.querySelectorAll(".casa")
                [origemLinha * 8 + origemColuna];

            casaSelecionada.classList
                .add("selecionada");

            await mostrarMovimentosPossiveis(
                origemLinha,
                origemColuna);
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