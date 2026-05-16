document.addEventListener('DOMContentLoaded', () => {
  const poolGrid = document.getElementById('pool-grid');
  const deckGrid = document.getElementById('deck-grid');
  const inspector = document.getElementById('card-inspector');
  const btnCloseInspector = document.querySelector('.btn-close-inspector');
  
  const cardsData = [
    { id: 1, name: "Yami Yugi", type: "Monster", atk: 2500, def: 2100, img: "assets/yugi.png", desc: "O faraó lendário retornou for dominar o multiverso." },
    { id: 2, name: "Deuses Egípcios", type: "Monster", atk: 4000, def: 4000, img: "assets/gods_cards.png", desc: "A trindade divina que governa sobre todas as dimensões." },
    { id: 3, name: "Mago Negro", type: "Monster", atk: 2500, def: 2100, img: "assets/yugi_2.png", desc: "O mestre das artes ocultas for enfrentar qualquer ameaça." }
  ];

  let selectedId = null;

  function renderGrid(container, cards) {
    container.innerHTML = '';
    cards.forEach(card => {
      const div = document.createElement('div');
      div.className = 'card-item';
      div.innerHTML = `<img src="${card.img}" alt="${card.name}">`;
      
      div.addEventListener('click', () => {
        document.querySelectorAll('.card-item').forEach(c => c.classList.remove('selected'));
        div.classList.add('selected');
        selectedId = card.id;
      });

      div.addEventListener('dblclick', () => {
        showInspector(card);
      });

      container.appendChild(div);
    });
  }

  function showInspector(card) {
    document.getElementById('inspected-img').src = card.img;
    document.getElementById('inspected-name').textContent = card.name;
    document.getElementById('inspected-type').textContent = card.type;
    document.getElementById('inspected-atk-def').textContent = `ATK/ ${card.atk} DEF/ ${card.def}`;
    document.getElementById('inspected-desc').textContent = card.desc;
    inspector.classList.remove('hidden');
  }

  btnCloseInspector.addEventListener('click', () => {
    inspector.classList.add('hidden');
  });

  renderGrid(poolGrid, cardsData);
});