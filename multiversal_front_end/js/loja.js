document.addEventListener('DOMContentLoaded', () => {
  // =========================================
  // GESTÃO DE MENU E LOGOUT
  // =========================================
  const btnSettings = document.getElementById('btn-settings');
  const settingsMenu = document.getElementById('settings-menu');
  const btnLogoutTrigger = document.getElementById('btn-logout-trigger');
  const modalLogout = document.getElementById('logout-modal');
  const btnCancelLogout = document.getElementById('btn-cancel-logout');
  
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

  // =========================================
  // SISTEMA DE CRÉDITOS (MOEDAS)
  // =========================================
  const coinDisplay = document.getElementById('user-coins');
  
  // Inicializa moedas no localStorage se não existirem
  if (localStorage.getItem('multiversal_coins') === null) {
    localStorage.setItem('multiversal_coins', '1500');
  }
  
  function updateCoinDisplay() {
    const currentCoins = parseInt(localStorage.getItem('multiversal_coins') || '0', 10);
    if (coinDisplay) {
      coinDisplay.textContent = currentCoins.toLocaleString('pt-BR');
    }
  }
  
  updateCoinDisplay();

  // =========================================
  // BANCOS DE CARTAS POR BOOSTER
  // =========================================
  const rarityRates = {
    'comum': '60%',
    'raro': '25%',
    'epico': '10%',
    'lendario': '5%'
  };

  const cardPools = {
    regional: [
      { name: "Mula sem Cabeça", img: "assets/Mula.png", rarity: "raro" },
      { name: "Tremendão da Serra", img: "assets/Tremendao.png", rarity: "comum" }
    ],
    cientifico: [
      { name: "Aedes Egípcio", img: "assets/Aedes_egicpio.png", rarity: "epico" }
    ],
    pokemon: [
      { name: "Gengar", img: "assets/Gengar.png", rarity: "lendario" }
    ],
    mitico: [
      { name: "Mago Negro", img: "assets/Mago_negro.png", rarity: "lendario" },
      { name: "Dragão do Bairro Industrial", img: "assets/Dragão_bairro_industrial.png", rarity: "epico" },
      { name: "Diabo Rubro", img: "assets/Diabo_rubro.png", rarity: "raro" }
    ],
    esportivo: [
      { name: "Canarinho Pistola", img: "assets/Canarinho.png", rarity: "epico" }
    ]
  };

  // Simulação: Preenche o pacote regional com 50 cartas para testar o layout de rolagem
  const raritiesArray = ['comum', 'raro', 'epico', 'lendario'];
  for (let i = 3; i <= 50; i++) {
    const randomRarity = raritiesArray[Math.floor(Math.random() * raritiesArray.length)];
    cardPools.regional.push({
      name: `Entidade Oculta #${i}`,
      img: "assets/Mula.png",
      rarity: randomRarity
    });
  }

  // =========================================
  // SISTEMA DE COMPRA E ABERTURA DE PACOTES
  // =========================================
  const packCards = document.querySelectorAll('.pack-card');
  const modalOpening = document.getElementById('modal-pack-opening');
  const tcgCards = document.querySelectorAll('.tcg-card');
  const btnCollect = document.getElementById('btn-collect');
  
  // Modal de Confirmação
  const modalConfirmPurchase = document.getElementById('modal-confirm-purchase');
  const confirmPurchaseText = document.getElementById('confirm-purchase-text');
  const btnCancelPurchase = document.getElementById('btn-cancel-purchase');
  const btnConfirmPurchase = document.getElementById('btn-confirm-purchase');
  
  let flippedCount = 0;
  let pendingPurchase = null;

  // Elementos do Modal de Detalhes
  const modalPackDetails = document.getElementById('modal-pack-details');
  const btnCloseDetails = document.getElementById('btn-close-details');
  const btnDetailsBuy = document.getElementById('btn-details-buy');

  packCards.forEach(pack => {
    pack.addEventListener('click', () => {
      const packId = pack.getAttribute('data-pack-id');
      const price = parseInt(pack.getAttribute('data-price') || '0', 10);
      const packName = pack.querySelector('.pack-name').textContent;
      const packDesc = pack.querySelector('.pack-desc').textContent;
      const packImgSrc = pack.querySelector('.pack-art img').src;

      // Populate Modal Detalhes
      document.getElementById('details-pack-img').src = packImgSrc;
      document.getElementById('details-pack-name').textContent = packName;
      document.getElementById('details-pack-desc').textContent = packDesc;
      document.getElementById('details-pack-price').textContent = price;

      // Store pending purchase state
      pendingPurchase = { packId, price, packName };

      modalPackDetails.classList.remove('hidden');
    });
  });

  // Fecha modal de detalhes
  if (btnCloseDetails) {
    btnCloseDetails.addEventListener('click', () => {
      modalPackDetails.classList.add('hidden');
      pendingPurchase = null;
    });
  }

  // Comprar a partir do modal de detalhes
  if (btnDetailsBuy) {
    btnDetailsBuy.addEventListener('click', () => {
      if (pendingPurchase) {
        modalPackDetails.classList.add('hidden');
        
        // Abre confirmação de compra
        confirmPurchaseText.innerHTML = `Deseja realmente adquirir o <strong>${pendingPurchase.packName}</strong> por <strong>${pendingPurchase.price}</strong> 🪙?`;
        modalConfirmPurchase.classList.remove('hidden');
      }
    });
  }

  // Eventos do Modal de Probabilidades
  const btnViewProbabilities = document.getElementById('btn-view-probabilities');
  const modalPackProbabilities = document.getElementById('modal-pack-probabilities');
  const btnBackToDetails = document.getElementById('btn-back-to-details');
  const btnCloseProbabilities = document.getElementById('btn-close-probabilities');
  const probabilitiesCardsGrid = document.getElementById('probabilities-cards-grid');

  if (btnViewProbabilities) {
    btnViewProbabilities.addEventListener('click', (e) => {
      e.preventDefault();
      if (!pendingPurchase) return;

      const pool = cardPools[pendingPurchase.packId];
      if (!pool) return;

      // Ordenar do maior para o menor drop rate (comum > raro > epico > lendario)
      const rarityOrder = { 'comum': 1, 'raro': 2, 'epico': 3, 'lendario': 4 };
      const sortedPool = [...pool].sort((a, b) => {
        const orderA = rarityOrder[a.rarity] || 99;
        const orderB = rarityOrder[b.rarity] || 99;
        return orderA - orderB;
      });

      // Populate grid
      probabilitiesCardsGrid.innerHTML = '';
      sortedPool.forEach(card => {
        const rate = rarityRates[card.rarity] || '??%';
        const cardHtml = `
          <div class="prob-card-item">
            <img src="${card.img}" class="prob-card-img" alt="${card.name}">
            <div class="prob-card-name">${card.name}</div>
            <div class="prob-card-rarity rarity-${card.rarity}">${card.rarity}</div>
            <div class="prob-card-rate">${rate} DROP</div>
          </div>
        `;
        probabilitiesCardsGrid.insertAdjacentHTML('beforeend', cardHtml);
      });

      modalPackDetails.classList.add('hidden');
      modalPackProbabilities.classList.remove('hidden');
    });
  }

  if (btnBackToDetails) {
    btnBackToDetails.addEventListener('click', () => {
      modalPackProbabilities.classList.add('hidden');
      modalPackDetails.classList.remove('hidden');
    });
  }

  if (btnCloseProbabilities) {
    btnCloseProbabilities.addEventListener('click', () => {
      modalPackProbabilities.classList.add('hidden');
      pendingPurchase = null;
    });
  }

  // Eventos do Modal de Confirmação
  if (btnCancelPurchase && btnConfirmPurchase) {
    btnCancelPurchase.addEventListener('click', () => {
      modalConfirmPurchase.classList.add('hidden');
      pendingPurchase = null;
    });

    btnConfirmPurchase.addEventListener('click', () => {
      if (pendingPurchase) {
        const userCoins = parseInt(localStorage.getItem('multiversal_coins') || '0', 10);
        
        if (userCoins >= pendingPurchase.price) {
          // Deduz moedas
          const newCoinsBalance = userCoins - pendingPurchase.price;
          localStorage.setItem('multiversal_coins', newCoinsBalance.toString());
          updateCoinDisplay();
          
          modalConfirmPurchase.classList.add('hidden');
          
          // Inicia revelação de cartas do pacote selecionado
          startBoosterOpening(pendingPurchase.packId);
        } else {
          alert("Erro: Créditos insuficientes no momento da compra.");
          modalConfirmPurchase.classList.add('hidden');
        }
        pendingPurchase = null;
      }
    });
  }

  function startBoosterOpening(packId) {
    const pool = cardPools[packId] || cardPools.regional;
    flippedCount = 0;
    
    // Esconde o botão de coletar no início
    btnCollect.classList.add('hidden');

    // Preenche as cartas com artes aleatórias do pool correspondente
    tcgCards.forEach((card, index) => {
      card.classList.remove('flipped');
      
      // Sorteia uma carta do pool específico
      const randomIndex = Math.floor(Math.random() * pool.length);
      const chosenCard = pool[randomIndex];
      
      const imgFront = card.querySelector('.card-front img');
      if (imgFront) {
        imgFront.src = chosenCard.img;
        imgFront.alt = chosenCard.name;
      }
    });

    // Mostra o Modal de abertura
    modalOpening.classList.remove('hidden');

    // Executa uma animação de impacto de entrada nos wraps das cartas usando GSAP
    gsap.fromTo('.reveal-card-wrapper', 
      { scale: 0.3, y: 50, rotationY: 0, opacity: 0 },
      { scale: 1, y: 0, opacity: 1, duration: 0.6, stagger: 0.15, ease: 'back.out(1.7)' }
    );
  }

  // Evento de clique para virar/revelar cada carta individualmente
  tcgCards.forEach(card => {
    card.addEventListener('click', () => {
      if (!card.classList.contains('flipped')) {
        card.classList.add('flipped');
        
        // Efeito sonoro fake/impacto com rotação usando GSAP para deixar premium
        gsap.to(card, {
          scale: 1.05,
          duration: 0.1,
          yoyo: true,
          repeat: 1,
          ease: 'power2.out'
        });

        flippedCount++;
        
        // Se todas as 3 cartas do booster foram reveladas, mostra o botão de coletar
        if (flippedCount === 3) {
          setTimeout(() => {
            btnCollect.classList.remove('hidden');
            gsap.fromTo(btnCollect,
              { opacity: 0, scale: 0.8 },
              { opacity: 1, scale: 1, duration: 0.4, ease: 'back.out(1.5)' }
            );
          }, 600);
        }
      }
    });
  });

  // Botão de fechar/coletar booster
  btnCollect.addEventListener('click', () => {
    // Efeito de fechar com saída suave usando GSAP
    gsap.to('.pack-opening-container', {
      scale: 0.9,
      opacity: 0,
      duration: 0.3,
      onComplete: () => {
        modalOpening.classList.add('hidden');
        // Restaura escala normal para a próxima abertura
        gsap.set('.pack-opening-container', { scale: 1, opacity: 1 });
      }
    });
  });
});
