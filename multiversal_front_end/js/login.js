document.addEventListener('DOMContentLoaded', () => {
  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');
  const authTitle = document.getElementById('auth-title');
  const authForm = document.getElementById('auth-form');
  const btnSubmit = document.getElementById('btn-submit');
  const authMessage = document.getElementById('auth-message');
  
  const API_BASE_URL = 'http://localhost:8080/api/auth';
  let isLoginMode = true;

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

  function showMessage(msg, isError = true) {
    authMessage.textContent = msg;
    authMessage.classList.remove('hidden', 'error', 'success');
    if (isError) {
      authMessage.classList.add('error');
    } else {
      authMessage.classList.add('success');
    }
  }

  tabLogin.addEventListener('click', (e) => {
    e.preventDefault();
    switchMode(true);
  });

  tabRegister.addEventListener('click', (e) => {
    e.preventDefault();
    switchMode(false);
  });

  authForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const nomeUsuario = document.getElementById('username').value;
    const senha = document.getElementById('password').value;
    const endpoint = isLoginMode ? '/entrar' : '/cadastrar';
    
    btnSubmit.disabled = true;
    btnSubmit.style.opacity = '0.5';

    try {
      const response = await fetch(API_BASE_URL + endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({ nomeUsuario: nomeUsuario, senha: senha })
      });

      const data = await response.json();

      if (!response.ok) {
        showMessage(data.erro || 'Erro na comunicação com o servidor.', true);
      } else {
        showMessage(isLoginMode ? 'Acesso concedido!' : 'Cadastro realizado com sucesso!', false);
        setTimeout(() => {
          window.location.href = 'home.html';
        }, 1000);
      }
    } catch (error) {
      showMessage('Erro de conexão. Verifique se o servidor está rodando.', true);
    } finally {
      btnSubmit.disabled = false;
      btnSubmit.style.opacity = '1';
    }
  });
});