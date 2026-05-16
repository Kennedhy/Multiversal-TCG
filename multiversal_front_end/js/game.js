document.addEventListener('DOMContentLoaded', () => {
  const btnOpenSurrender = document.getElementById('btn-open-surrender');
  const modalSurrender = document.getElementById('surrender-modal');
  const btnCancelSurrender = document.getElementById('btn-cancel-surrender');

  if (btnOpenSurrender && modalSurrender && btnCancelSurrender) {
    btnOpenSurrender.addEventListener('click', () => {
      modalSurrender.classList.remove('hidden');
    });

    btnCancelSurrender.addEventListener('click', () => {
      modalSurrender.classList.add('hidden');
    });
  }
});

// Lógica para alternar fases visualmente
const btnPass = document.getElementById('btn-pass-phase');
const phases = document.querySelectorAll('.phase-badge');
let currentPhaseIndex = 2; // Começa na Main 1 (index 2)

if (btnPass) {
  btnPass.addEventListener('click', () => {
    // Remove active da fase atual
    phases[currentPhaseIndex].classList.remove('active');
    
    // Avança para a próxima
    currentPhaseIndex = (currentPhaseIndex + 1) % phases.length;
    
    // Adiciona active na nova fase
    phases[currentPhaseIndex].classList.add('active');
    
    // Pequeno efeito de brilho ao clicar
    gsap.fromTo(".game-phase-bar", 
      { boxShadow: "0 0 0px var(--accent-glow)" }, 
      { boxShadow: "0 0 30px var(--accent-glow)", duration: 0.3, yoyo: true, repeat: 1 }
    );
  });
}
document.addEventListener('DOMContentLoaded', () => {
  let timeLeft = 50;
  const timerElement = document.getElementById('game-timer');
  const timerPill = document.querySelector('.timer-pill');

  const countdown = setInterval(() => {
    timeLeft--;
    timerElement.textContent = `${timeLeft}s`;

    if (timeLeft <= 10) {
      timerPill.classList.add('low-time');
    }

    if (timeLeft <= 0) {
      clearInterval(countdown);
      // Lógica opcional for passar de fase automaticamente
      document.getElementById('btn-pass-phase').click();
    }
  }, 1000);

  // Resetar o timer quando o botão for clicado
  document.getElementById('btn-pass-phase').addEventListener('click', () => {
    timeLeft = 50;
    timerElement.textContent = `50s`;
    timerPill.classList.remove('low-time');
  });
});