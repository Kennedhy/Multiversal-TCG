document.addEventListener('DOMContentLoaded', () => {
  const poolGrid = document.getElementById('pool-grid');
  const deckGrid = document.getElementById('deck-grid');
  const inspector = document.getElementById('card-inspector');
  const btnCloseInspector = document.getElementById('close-inspector');
  const btnAddDeck = document.getElementById('btn-add-deck');
  const deckCountEl = document.getElementById('deck-count');
  const searchInput = document.getElementById('search-card');
  const filterSelect = document.getElementById('filter-type');
  const sortSelect = document.getElementById('sort-type');
  const btnPrevCard = document.getElementById('btn-prev-card');
  const btnNextCard = document.getElementById('btn-next-card');

  let poolCards = [
    { id: 1, name: "Aedes Egípcio", type: "Monster", atk: 1800, def: 600, img: "assets/Aedes_egicpio.png", desc: "Uma praga vindo direto do antigo egito para dominar o multiverso." },
    { id: 2, name: "Canarinho Pistola", type: "Monster", atk: 2500, def: 2100, img: "assets/Canarinho.png", desc: "O Canário brasileiro que vem em busca de vitórias em todas as dimensões." },
    { id: 3, name: "Diabo Rubro", type: "Monster", atk: 1800, def: 1400, img: "assets/Diabo_rubro.png", desc: "O mestre das artes ocultas para enfrentar qualquer ameaça." },
    { id: 4, name: "Dragão do Bairro Industrial", type: "Monster", atk: 2400, def: 1900, img: "assets/Dragão_bairro_industrial.png", desc: "O dragão que habita o bairro industrial e protege suas terras." },
    { id: 5, name: "Gengar", type: "Monster", atk: 2500, def: 2100, img: "assets/Gengar.png", desc: "O espírito que é considerado um mestre das sombras." },
    { id: 6, name: "Mago Negro", type: "Monster", atk: 1900, def: 1700, img: "assets/Mago_negro.png", desc: "O mestre das artes ocultas para enfrentar qualquer ameaça." },
    { id: 7, name: "Mula sem Cabeça", type: "Monster", atk: 1700, def: 1400, img: "assets/Mula.png", desc: "Uma fera amaldiçoada que ataca tudo que vê pela frente." },
    { id: 8, name: "Tremendão da Serra", type: "Monster", atk: 2100, def: 2000, img: "assets/Tremendao.png", desc: "O gigante que habita a serra e protege suas terras." }
  ];

  let deckCards = [];
  let currentFilteredCards = [];
  let currentSelectedCard = null;
  let isViewingDeck = false;

  function renderGrid(container, cards, isDeck) {
    container.innerHTML = '';
    for (let i = 0; i < cards.length; i++) {
      const card = cards[i];
      const div = document.createElement('div');
      div.className = 'card-item';
      div.innerHTML = `<img src="${card.img}" alt="${card.name}">`;
      
      div.addEventListener('click', () => {
        showInspector(card, isDeck);
      });

      container.appendChild(div);
    }
  }

  function updateDeckCount() {
    if (deckCountEl) {
      deckCountEl.textContent = deckCards.length + " / 60";
    }
  }

  function showInspector(card, isDeck) {
    currentSelectedCard = card;
    isViewingDeck = isDeck;

    document.getElementById('inspector-img').src = card.img;
    document.getElementById('inspector-name').textContent = card.name;
    document.getElementById('inspector-stats').textContent = "[" + card.type + "] ATK/ " + card.atk + " DEF/ " + card.def;
    document.getElementById('inspector-desc').textContent = card.desc;
    
    if (isDeck) {
      btnAddDeck.textContent = "Remover do Deck";
      btnAddDeck.style.backgroundColor = "rgba(239, 68, 68, 0.15)";
      btnAddDeck.style.borderColor = "rgba(239, 68, 68, 0.4)";
      btnAddDeck.style.color = "#ef4444";
    } else {
      btnAddDeck.textContent = "Adicionar ao Deck";
      btnAddDeck.style.backgroundColor = ""; 
      btnAddDeck.style.borderColor = "";
      btnAddDeck.style.color = "";
    }

    let activeList = isDeck ? deckCards : currentFilteredCards;
    let index = -1;
    for (let i = 0; i < activeList.length; i++) {
      if (activeList[i].id === card.id) {
        index = i;
        break;
      }
    }

    btnPrevCard.disabled = (index <= 0);
    btnNextCard.disabled = (index >= activeList.length - 1 || index === -1);

    inspector.classList.remove('hidden');
  }

  btnCloseInspector.addEventListener('click', () => {
    inspector.classList.add('hidden');
    currentSelectedCard = null;
  });

  inspector.addEventListener('click', (e) => {
    if (e.target === inspector) {
      inspector.classList.add('hidden');
      currentSelectedCard = null;
    }
  });

  btnPrevCard.addEventListener('click', () => {
    if (!currentSelectedCard) return;
    let activeList = isViewingDeck ? deckCards : currentFilteredCards;
    let index = -1;
    for (let i = 0; i < activeList.length; i++) {
      if (activeList[i].id === currentSelectedCard.id) {
        index = i;
        break;
      }
    }
    if (index > 0) {
      showInspector(activeList[index - 1], isViewingDeck);
    }
  });

  btnNextCard.addEventListener('click', () => {
    if (!currentSelectedCard) return;
    let activeList = isViewingDeck ? deckCards : currentFilteredCards;
    let index = -1;
    for (let i = 0; i < activeList.length; i++) {
      if (activeList[i].id === currentSelectedCard.id) {
        index = i;
        break;
      }
    }
    if (index !== -1 && index < activeList.length - 1) {
      showInspector(activeList[index + 1], isViewingDeck);
    }
  });

  btnAddDeck.addEventListener('click', () => {
    if (!currentSelectedCard) return;

    if (isViewingDeck) {
      let newDeck = [];
      for (let i = 0; i < deckCards.length; i++) {
        if (deckCards[i].id !== currentSelectedCard.id) {
          newDeck.push(deckCards[i]);
        }
      }
      deckCards = newDeck;
      poolCards.push(currentSelectedCard);
    } else {
      if (deckCards.length < 60) {
        deckCards.push(currentSelectedCard);
        let newPool = [];
        for (let i = 0; i < poolCards.length; i++) {
          if (poolCards[i].id !== currentSelectedCard.id) {
            newPool.push(poolCards[i]);
          }
        }
        poolCards = newPool;
      } else {
        alert("O deck já está cheio!");
        return;
      }
    }

    poolCards.sort((a, b) => a.id - b.id);
    
    filterPoolAndRender();
    renderGrid(deckGrid, deckCards, true);
    updateDeckCount();
    
    inspector.classList.add('hidden');
    currentSelectedCard = null;
  });

  function filterPoolAndRender() {
    const searchTerm = searchInput.value.toLowerCase();
    const typeFilter = filterSelect.value.toLowerCase();
    const sortValue = sortSelect.value;

    let filtered = [];
    for (let i = 0; i < poolCards.length; i++) {
      const card = poolCards[i];
      const matchesSearch = card.name.toLowerCase().includes(searchTerm);
      const matchesType = typeFilter === 'all' || card.type.toLowerCase() === typeFilter;
      if (matchesSearch && matchesType) {
        filtered.push(card);
      }
    }

    filtered.sort((a, b) => {
      if (sortValue === 'az') return a.name.localeCompare(b.name);
      if (sortValue === 'za') return b.name.localeCompare(a.name);
      if (sortValue === 'atk-high') return b.atk - a.atk;
      if (sortValue === 'atk-low') return a.atk - b.atk;
      if (sortValue === 'def-high') return b.def - a.def;
      if (sortValue === 'def-low') return a.def - b.def;
      return a.id - b.id;
    });

    currentFilteredCards = filtered;
    renderGrid(poolGrid, currentFilteredCards, false);
  }

  searchInput.addEventListener('input', filterPoolAndRender);
  filterSelect.addEventListener('change', filterPoolAndRender);
  sortSelect.addEventListener('change', filterPoolAndRender);

  filterPoolAndRender();
  renderGrid(deckGrid, deckCards, true);
  updateDeckCount();
});