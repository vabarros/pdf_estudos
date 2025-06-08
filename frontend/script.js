// script.js

const API_BASE_URL = 'http://localhost:8080/api'; // Certifique-se de que esta URL está correta

// === Variáveis de Estado do Quiz ===
let currentQuizQuestions = []; // Armazena a lista de perguntas para a sessão atual
let currentQuestionIndex = 0; // Índice da pergunta atualmente exibida
let currentSessionId = null; // Para armazenar o ID da sessão retornado pelo backend (se usar sessões)
let isOptionSelected = false; // Flag para garantir que só uma opção seja clicada por pergunta

// === Referências aos elementos HTML ===
const quizSetup = document.getElementById('quiz-setup');
const quizSection = document.getElementById('quiz-section');
const questionText = document.getElementById('question-text');
const optionsContainer = document.getElementById('options-container');
const generateQuizBtn = document.getElementById('generate-quiz-btn'); // ID corrigido
const fileInput = document.getElementById('file-input'); // ID corrigido
const questionCountInput = document.getElementById('question-count'); // ID corrigido
const quizMessage = document.getElementById('quiz-message');
const nextQuestionBtn = document.getElementById('next-question-btn');
const generateMoreBtn = document.getElementById('generate-more-btn');
const resetButton = document.getElementById('reset-button');
const startPageInput = document.getElementById('startPage'); // Novo input
const endPageInput = document.getElementById('endPage');     // Novo input

// === Event Listeners ===

// Listener para o botão "Gerar Quiz" (iniciar o quiz)
generateQuizBtn.addEventListener('click', async () => {
    const file = fileInput.files[0]; // Usando fileInput corretamente
    const count = parseInt(questionCountInput.value);
    const startPage = startPageInput.value ? parseInt(startPageInput.value) : null;
    const endPage = endPageInput.value ? parseInt(endPageInput.value) : null;

    if (!file) {
        alert('Por favor, selecione um arquivo PDF.');
        return;
    }
    if (isNaN(count) || count <= 0) {
        alert('Por favor, insira um número válido de perguntas.');
        return;
    }

    // Esconde a tela de setup e mostra uma mensagem de carregamento
    quizSetup.style.display = 'none';
    quizSection.style.display = 'block';
    questionText.textContent = "Gerando quiz, por favor aguarde...";
    optionsContainer.innerHTML = '';
    nextQuestionBtn.style.display = 'none';
    generateMoreBtn.style.display = 'none';
    resetButton.style.display = 'block';

    // Estilos para a mensagem de carregamento
    quizMessage.textContent = "Gerando quiz, por favor aguarde...";
    quizMessage.style.color = "#0056b3"; // Azul para carregamento
    quizMessage.style.backgroundColor = "#e6f7ff";
    quizMessage.style.borderColor = "#91d5ff";
    quizMessage.style.border = "1px solid";


    try {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('count', count);
        if (startPage !== null) {
            formData.append('startPage', startPage);
        }
        if (endPage !== null) {
            formData.append('endPage', endPage);
        }

        // Requisição para o endpoint correto: /api/generate-quiz
        const response = await fetch(`${API_BASE_URL}/generate-quiz`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorText = await response.text(); // Tenta ler como texto para depuração
            // Tenta parsear como JSON se possível para obter mais detalhes do erro
            try {
                const errorData = JSON.parse(errorText);
                throw new Error(`Erro do servidor: ${response.status} - ${errorData.error || errorData.message || response.statusText}`);
            } catch (jsonError) {
                // Se não for JSON, apenas usa o texto
                throw new Error(`Erro do servidor: ${response.status} - ${errorText || response.statusText}`);
            }
        }

        const data = await response.json();
        console.log('Dados do quiz recebidos:', data);

        // === INICIALIZAÇÃO DO ESTADO DO QUIZ ===
        // currentSessionId = data.sessionId; // A nova implementação com LLM pode não retornar sessionId
        currentQuizQuestions = data; // O backend agora retorna diretamente a lista de perguntas
        currentQuestionIndex = 0; // Começa na primeira pergunta
        isOptionSelected = false; // Reseta o flag de opção selecionada

        if (currentQuizQuestions && currentQuizQuestions.length > 0) {
            displayQuestion(); // Exibe a primeira pergunta
            quizMessage.textContent = ''; // Limpa qualquer mensagem de carregamento
            quizMessage.style.border = "none"; // Remove a borda
        } else {
            questionText.textContent = "Nenhuma pergunta gerada para este PDF.";
            quizMessage.textContent = "Tente um PDF diferente ou aumente o número de perguntas.";
            quizMessage.style.color = "orange";
            quizMessage.style.backgroundColor = "#fff3cd";
            quizMessage.style.borderColor = "#ffc107";
            quizMessage.style.border = "1px solid";
            generateMoreBtn.style.display = 'none';
            nextQuestionBtn.style.display = 'none';
        }


    } catch (error) {
        console.error('Erro ao gerar quiz:', error);
        alert('Ocorreu um erro ao gerar o quiz: ' + error.message);
        // Exibe mensagem de erro no quizMessage
        quizMessage.textContent = `Erro ao gerar quiz: ${error.message}. Verifique o console do backend.`;
        quizMessage.style.color = "red";
        quizMessage.style.backgroundColor = "#f8d7da";
        quizMessage.style.borderColor = "#dc3545";
        quizMessage.style.border = "1px solid";
        // Volta para a tela de setup em caso de erro
        quizSection.style.display = 'none';
        quizSetup.style.display = 'block';
    }
});

// Listener para o botão "Próxima Pergunta"
nextQuestionBtn.addEventListener('click', () => {
    currentQuestionIndex++; // Avança para a próxima pergunta
    isOptionSelected = false; // Reseta o flag para a nova pergunta
    displayQuestion(); // Exibe a nova pergunta
});

// Listener para o botão "Gerar Mais Perguntas"
// NOTA: A lógica de sessão aqui pode precisar de ajustes se o backend não retornar sessionId
// ou se cada geração de perguntas for uma requisição independente ao LLM.
generateMoreBtn.addEventListener('click', async () => {
    // Por enquanto, vamos fazer um "Gerar Mais" simples que recria a partir do PDF original
    // Isso pode ser custoso em termos de chamadas à API do LLM
    const file = fileInput.files[0];
    const count = parseInt(questionCountInput.value) || 5;
    const startPage = startPageInput.value ? parseInt(startPageInput.value) : null;
    const endPage = endPageInput.value ? parseInt(endPageInput.value) : null;

    if (!file) {
        alert('Por favor, selecione um arquivo PDF primeiro.');
        return;
    }

    quizMessage.textContent = "Gerando mais perguntas...";
    quizMessage.style.color = "black";
    quizMessage.style.backgroundColor = "#e6f7ff";
    quizMessage.style.borderColor = "#91d5ff";
    quizMessage.style.border = "1px solid";
    generateMoreBtn.disabled = true; // Desabilita o botão enquanto carrega
    nextQuestionBtn.style.display = 'none'; // Esconde o botão de próxima enquanto gera

    try {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('count', count);
        if (startPage !== null) {
            formData.append('startPage', startPage);
        }
        if (endPage !== null) {
            formData.append('endPage', endPage);
        }

        const response = await fetch(`${API_BASE_URL}/generate-quiz`, { // Chamando o mesmo endpoint de geração
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorText = await response.text();
             try {
                const errorData = JSON.parse(errorText);
                throw new Error(`Erro ao gerar mais perguntas! Status: ${response.status}: ${errorData.error || errorData.message || response.statusText}`);
            } catch (jsonError) {
                throw new Error(`Erro ao gerar mais perguntas! Status: ${response.status}: ${errorText || response.statusText}`);
            }
        }

        const data = await response.json();
        console.log('Mais perguntas recebidas:', data);

        if (data && data.length > 0) {
            currentQuizQuestions = currentQuizQuestions.concat(data); // Adiciona as novas perguntas
            // Não precisamos ajustar currentQuestionIndex explicitamente se sempre geramos novas e o quiz já estava no final.
            // A próxima chamada de displayQuestion() já pegará a nova pergunta.
            isOptionSelected = false; // Reseta o flag

            displayQuestion(); // Exibe a primeira pergunta da nova leva ou continua de onde parou
            generateMoreBtn.style.display = 'none'; // Esconde o botão novamente
            generateMoreBtn.disabled = false; // Reabilita
            quizMessage.textContent = ''; // Limpa mensagem
            quizMessage.style.border = "none";
        } else {
            quizMessage.textContent = "Não foi possível gerar mais perguntas únicas no momento.";
            quizMessage.style.color = "orange";
            quizMessage.style.backgroundColor = "#fff3cd";
            quizMessage.style.borderColor = "#ffc107";
            quizMessage.style.border = "1px solid";
            generateMoreBtn.disabled = false; // Reabilita
            nextQuestionBtn.style.display = 'none';
        }

    } catch (error) {
        console.error('Erro ao gerar mais perguntas:', error);
        alert('Ocorreu um erro ao gerar mais perguntas: ' + error.message);
        generateMoreBtn.disabled = false; // Reabilita
    }
});


// Listener para o botão "Reiniciar Quiz"
resetButton.addEventListener('click', async () => {
    // Se o backend tiver um conceito de sessão, você poderia enviar um comando de reset para ele.
    // Como a geração é stateless com o Gemini, não precisamos de um reset de sessão no backend por agora.
    // Mas mantive o código de chamada ao backend comentado caso você implemente sessões depois.
    /*
    if (currentSessionId) {
        try {
            const response = await fetch(`${API_BASE_URL}/reset-session/${currentSessionId}`, {
                method: 'POST'
            });

            if (!response.ok) {
                const errorData = await response.json();
                console.error('Erro ao resetar sessão no backend:', errorData.error || response.statusText);
                alert('Ocorreu um erro ao resetar a sessão no backend. Por favor, tente novamente.');
            } else {
                console.log('Sessão resetada no backend.');
            }
        } catch (error) {
            console.error('Erro de rede ao resetar a sessão:', error);
            alert('Erro de conexão ao tentar resetar a sessão. Por favor, verifique sua rede.');
        }
    }
    */

    // Reseta o estado do frontend
    currentQuizQuestions = [];
    currentQuestionIndex = 0;
    currentSessionId = null; // Reinicia sessionId
    isOptionSelected = false;

    // Volta para a tela de setup
    quizSection.style.display = 'none';
    quizSetup.style.display = 'block';
    questionCountInput.value = '5'; // Reseta o valor do input de perguntas
    fileInput.value = ''; // Limpa o arquivo selecionado
    startPageInput.value = ''; // Limpa páginas
    endPageInput.value = ''; // Limpa páginas
    quizMessage.textContent = ''; // Limpa mensagens
    nextQuestionBtn.style.display = 'none';
    generateMoreBtn.style.display = 'none';
    optionsContainer.innerHTML = '';
    questionText.textContent = '';
});

// === Funções de Exibição e Lógica do Quiz ===

// Função para exibir a pergunta atual
function displayQuestion() {
    if (currentQuestionIndex < currentQuizQuestions.length) {
        const question = currentQuizQuestions[currentQuestionIndex];
        questionText.textContent = question.pergunta;
        optionsContainer.innerHTML = ''; // Limpa as opções anteriores
        quizMessage.textContent = ''; // Limpa mensagens de feedback
        quizMessage.style.border = "none"; // Remove a borda da mensagem
        nextQuestionBtn.style.display = 'none'; // Esconde o botão "Próxima Pergunta" inicialmente

        // Adiciona as alternativas
        question.alternativas.forEach(option => {
            const button = document.createElement('button');
            button.textContent = option;
            button.className = 'quiz-option';
            // Usa uma função de seta para capturar o contexto correto da questão
            button.onclick = () => selectOption(button, question.correta);
            optionsContainer.appendChild(button);
        });
    } else {
        // Se não houver mais perguntas na lista atual (e o generateMoreBtn não tiver adicionado mais)
        questionText.textContent = "Você respondeu todas as perguntas disponíveis!";
        optionsContainer.innerHTML = '';
        nextQuestionBtn.style.display = 'none'; // Garante que o botão de próxima pergunta esteja escondido
        generateMoreBtn.style.display = 'block'; // Mostra o botão para gerar mais perguntas
        quizMessage.textContent = "Clique em 'Gerar Mais Perguntas' para continuar.";
        quizMessage.style.color = "black";
        quizMessage.style.backgroundColor = "#f0f0f0"; // Cor neutra
        quizMessage.style.borderColor = "#ccc";
        quizMessage.style.border = "1px solid";
    }
}

// Função para lidar com a seleção de uma opção
function selectOption(selectedButton, correctAnswer) {
    if (isOptionSelected) { // Evita múltiplos cliques na mesma pergunta
        return;
    }
    isOptionSelected = true;

    const allOptionButtons = optionsContainer.querySelectorAll('.quiz-option');
    allOptionButtons.forEach(button => {
        button.disabled = true; // Desabilita todos os botões após a seleção
        if (button.textContent === correctAnswer) {
            button.classList.add('correct'); // Marca a resposta correta
        } else if (button === selectedButton) {
            button.classList.add('incorrect'); // Marca a resposta incorreta selecionada
        }
    });

    // Feedback visual
    if (selectedButton.textContent === correctAnswer) {
        quizMessage.textContent = "Resposta Correta!";
        quizMessage.style.color = "#155724"; /* Cor de texto para correto */
        quizMessage.style.backgroundColor = "#d4edda"; /* Fundo para correto */
        quizMessage.style.borderColor = "#28a745"; /* Borda para correto */
    } else {
        quizMessage.textContent = `Resposta Incorreta. A correta era: "${correctAnswer}"`;
        quizMessage.style.color = "#721c24"; /* Cor de texto para incorreto */
        quizMessage.style.backgroundColor = "#f8d7da"; /* Fundo para incorreto */
        quizMessage.style.borderColor = "#dc3545"; /* Borda para incorreto */
    }
    quizMessage.style.border = "1px solid"; // Adiciona uma borda

    // Mostra o botão "Próxima Pergunta" após um pequeno atraso para o usuário ver o feedback
    setTimeout(() => {
        nextQuestionBtn.style.display = 'block';
    }, 500); // 0.5 segundo de atraso
}