document.addEventListener('DOMContentLoaded', async () => {

  // =========================
  // CONFIG API
  // =========================

  const API_BASE = 'https://noemi-goateed-lavonia.ngrok-free.dev';

  class ApiService {

    static async request(path, options = {}) {

      try {

        const response = await fetch(`${API_BASE}${path}`, {
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
          },
          ...options
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

        return {
          ok: false,
          status: 500,
          error: 'Erro de conexão com o servidor.'
        };
      }
    }

    static post(path, body) {

      return this.request(path, {
        method: 'POST',
        body: JSON.stringify(body)
      });
    }

    static get(path) {

      return this.request(path, {
        method: 'GET'
      });
    }
  }

  const Api = {

    auth: {

      async entrar(nomeUsuario, senha) {

        return ApiService.post('/entrar', {
          nomeUsuario,
          senha
        });
      },

      async cadastrar(nomeUsuario, senha) {

        return ApiService.post('/cadastrar', {
          nomeUsuario,
          senha
        });
      },

      async sessao() {

        return ApiService.get('/sessao');
      },

      async sair() {

        return ApiService.post('/sair', {});
      }
    }
  };

  // =========================
  // ELEMENTOS
  // =========================

  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');

  const authTitle = document.getElementById('auth-title');
  const authForm = document.getElementById('auth-form');

  const btnSubmit = document.getElementById('btn-submit');
  const authMessage = document.getElementById('auth-message');

  let isLoginMode = true;

  // =========================
  // VERIFICAR SESSÃO
  // =========================

  try {

    const { ok } = await Api.auth.sessao();

    if (ok) {

      window.location.replace('home.html');
      return;
    }

  } catch (error) {

    console.error('Erro ao verificar sessão:', error);
  }

  // =========================
  // TROCAR MODO
  // =========================

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

  // =========================
  // MOSTRAR MENSAGEM
  // =========================

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

  // =========================
  // EVENTOS DAS TABS
  // =========================

  tabLogin.addEventListener('click', (e) => {

    e.preventDefault();

    switchMode(true);
  });

  tabRegister.addEventListener('click', (e) => {

    e.preventDefault();

    switchMode(false);
  });

  // =========================
  // LOGIN / CADASTRO
  // =========================

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

      if (isLoginMode) {

        resposta = await Api.auth.entrar(
          nomeUsuario,
          senha
        );

      } else {

        resposta = await Api.auth.cadastrar(
          nomeUsuario,
          senha
        );
      }

      if (!resposta.ok) {

        showMessage(
          resposta.error || 'Erro no servidor.'
        );

        return;
      }

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