document.addEventListener('DOMContentLoaded', () => {
  const btnSettings = document.getElementById('btn-settings');
  const settingsMenu = document.getElementById('settings-menu');
  const btnLogoutTrigger = document.getElementById('btn-logout-trigger');
  const modalLogout = document.getElementById('logout-modal');
  const btnCancelLogout = document.getElementById('btn-cancel-logout');
  
  const cardDuelo = document.getElementById('card-duelo');
  const modalDuelo = document.getElementById('modal-duelo');
  const closeDuelo = document.getElementById('close-duelo');

  if (btnSettings && settingsMenu) {
    btnSettings.addEventListener('click', (e) => {
      e.stopPropagation();
      settingsMenu.classList.toggle('show');
    });

    document.addEventListener('click', (e) => {
      if (!settingsMenu.contains(e.target)) {
        settingsMenu.classList.remove('show');
      }
    });
  }

  if (btnLogoutTrigger && modalLogout && btnCancelLogout) {
    btnLogoutTrigger.addEventListener('click', () => {
      settingsMenu.classList.remove('show');
      modalLogout.classList.remove('hidden');
    });

    btnCancelLogout.addEventListener('click', () => {
      modalLogout.classList.add('hidden');
    });
  }

  if (cardDuelo && modalDuelo && closeDuelo) {
    cardDuelo.addEventListener('click', () => {
      modalDuelo.classList.remove('hidden');
    });

    closeDuelo.addEventListener('click', () => {
      modalDuelo.classList.add('hidden');
    });

    modalDuelo.addEventListener('click', (e) => {
      if (e.target === modalDuelo) {
        modalDuelo.classList.add('hidden');
      }
    });
  }
});