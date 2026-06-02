document.addEventListener('DOMContentLoaded', async () => {

  // =========================================
  // CONFIGURAÇÃO API
  // =========================================

  const API_BASE = 'http://localhost:8080';
  const TOKEN_KEY = 'multiversal_token';
  const USER_KEY = 'multiversal_user';

  class ApiService {

    static async request(path, options = {}) {

      try {

        const token = localStorage.getItem(TOKEN_KEY);
        const response = await fetch(`${API_BASE}${path}`, {
          method: options.method || 'GET',

          headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
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

        return ApiService.post('/api/auth/login', {
          username: nomeUsuario,
          password: senha
        });
      },

      async cadastrar(nomeUsuario, senha) {

        return ApiService.post('/api/auth/register', {
          username: nomeUsuario,
          password: senha
        });
      },

      async sessao() {

        return ApiService.get('/api/auth/me');
      },

      async sair() {

        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        return { ok: true };
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

      localStorage.setItem(TOKEN_KEY, resposta.data.token || '');
      localStorage.setItem(USER_KEY, resposta.data.username || nomeUsuario.toLowerCase());

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
