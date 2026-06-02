document.addEventListener('DOMContentLoaded', async () => {

  // =========================================
  // CONFIGURAÇÃO API
  // =========================================

  const API_BASE = 'https://noemi-goateed-lavonia.ngrok-free.dev';

  class ApiService {

    static async request(path, options = {}) {

      try {

        const response = await fetch(`${API_BASE}${path}`, {
          method: options.method || 'GET',

          credentials: 'include',

          headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
          },

          body: options.body
        });

        let data = {};

        try {
          data = await response.json();
        } catch (_) {}

        return {
          ok: response.ok,
          status: response.status,
          data,
          error: data.erro || data.message
        };

      } catch (error) {

        console.error(error);

        return {
          ok: false,
          status: 500,
          error: 'Erro de conexão com o servidor.'
        };
      }
    }

    static get(path) {

      return this.request(path);
    }

    static post(path, body) {

      return this.request(path, {
        method: 'POST',
        body: JSON.stringify(body)
      });
    }
  }

  // =========================================
  // API AUTH
  // =========================================

  const Api = {

    auth: {

      async entrar(nomeUsuario, senha) {

        return ApiService.post('/api/auth/entrar', {
          nomeUsuario,
          senha
        });
      },

      async cadastrar(nomeUsuario, senha) {

        return ApiService.post('/api/auth/cadastrar', {
          nomeUsuario,
          senha
        });
      },

      async sessao() {

        return ApiService.get('/api/auth/sessao');
      },

      async sair() {

        return ApiService.post('/api/auth/sair', {});
      }
    }
  };

  // =========================================
  // ELEMENTOS
  // =========================================

  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');

  const authTitle = document.getElementById('auth-title');
  const authForm = document.getElementById('auth-form');

  const btnSubmit = document.getElementById('btn-submit');

  const authMessage = document.getElementById('auth-message');

  let isLoginMode = true;

  // =========================================
  // VERIFICAR SESSÃO
  // =========================================

  try {

    const resposta = await Api.auth.sessao();

    if (resposta.ok) {

      window.location.replace('home.html');
      return;
    }

  } catch (error) {

    console.error('Erro ao verificar sessão:', error);
  }

  // =========================================
  // TROCAR LOGIN/CADASTRO
  // =========================================

  function switchMode(isLogin) {

    isLoginMode = isLogin;

    authMessage.classList.add('hidden');

    authForm.reset();

    if (isLogin) {

      tabLogin.classList.add('active');
      tabRegister.classList.remove('active');

      authTitle.textContent = 'ENTRAR';
      btnSubmit.textContent = 'Acessar Multiverso';

    } else {

      tabRegister.classList.add('active');
      tabLogin.classList.remove('active');

      authTitle.textContent = 'CADASTRAR';
      btnSubmit.textContent = 'Forjar Aliança';
    }
  }

  // =========================================
  // MOSTRAR MENSAGEM
  // =========================================

  function showMessage(msg, isError = true) {

    authMessage.textContent = msg;

    authMessage.classList.remove(
      'hidden',
      'error',
      'success'
    );

    authMessage.classList.add(
      isError ? 'error' : 'success'
    );
  }

  // =========================================
  // EVENTOS DAS ABAS
  // =========================================

  tabLogin.addEventListener('click', (e) => {

    e.preventDefault();

    switchMode(true);
  });

  tabRegister.addEventListener('click', (e) => {

    e.preventDefault();

    switchMode(false);
  });

  // =========================================
  // ROLAGEM AUTOMÁTICA (TECLADO MOBILE)
  // =========================================
  
  const inputs = document.querySelectorAll('.input-group input');
  inputs.forEach(input => {
    input.addEventListener('focus', function() {
      // Pequeno atraso para dar tempo ao teclado virtual do celular abrir
      setTimeout(() => {
        this.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }, 300);
    });
  });

  // =========================================
  // LOGIN / CADASTRO
  // =========================================

  authForm.addEventListener('submit', async (e) => {

    e.preventDefault();

    authMessage.classList.add('hidden');

    const nomeUsuario = document
      .getElementById('username')
      .value
      .trim();

    const senha = document
      .getElementById('password')
      .value;

    if (!nomeUsuario || !senha) {

      showMessage('Preencha todos os campos.');
      return;
    }

    btnSubmit.disabled = true;
    btnSubmit.style.opacity = '0.5';

    try {

      let resposta;

      // LOGIN
      if (isLoginMode) {

        resposta = await Api.auth.entrar(
          nomeUsuario,
          senha
        );

      }

      // CADASTRO
      else {

        resposta = await Api.auth.cadastrar(
          nomeUsuario,
          senha
        );
      }

      // ERRO
      if (!resposta.ok) {

        showMessage(
          resposta.error || 'Erro no servidor.'
        );

        return;
      }

      // SUCESSO
      showMessage(
        isLoginMode
          ? 'Acesso concedido!'
          : 'Cadastro realizado com sucesso!',
        false
      );

      setTimeout(() => {

        window.location.href = 'home.html';

      }, 1000);

    } catch (error) {

      console.error(error);

      showMessage(
        'Erro de conexão com o servidor.'
      );

    } finally {

      btnSubmit.disabled = false;
      btnSubmit.style.opacity = '1';
    }
  });
});