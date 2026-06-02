import { api } from "./api.js";
import {
    $,
    FALLBACK_IMAGE,
    cardRarities,
    confirmDelete,
    escapeHtml,
    fillSelect,
    formatEnum,
    imageForRarity,
    setBusy,
    showToast,
} from "./ui.js";

const state = {
    cards: [],
    options: {},
};

const els = {
    cardsList: $("#cardsList"),
    cardsCount: $("#cardsCount"),
    search: $("#searchCards"),
    type: $("#filterType"),
    rarity: $("#filterRarity"),
    source: $("#filterSource"),
    status: $("#filterStatus"),
};

document.addEventListener("DOMContentLoaded", init);

async function init() {
    wireEvents();
    await loadData();
}

function wireEvents() {
    [els.search, els.type, els.rarity, els.source, els.status].forEach((element) => {
        element.addEventListener("input", render);
        element.addEventListener("change", render);
    });
}

async function loadData() {
    els.cardsList.innerHTML = `<p class="loading">Carregando cartas...</p>`;
    try {
        const [options, cards] = await Promise.all([api.options(), api.cards()]);
        state.options = options;
        state.cards = cards;
        fillSelect(els.type, options.cardTypes || [], "Todos");
        fillSelect(els.rarity, options.rarities || [], "Todas");
        render();
    } catch (error) {
        els.cardsList.innerHTML = `<p class="empty">Nao foi possivel carregar as cartas.</p>`;
        showToast(error.message, "error");
    }
}

function filteredCards() {
    const query = els.search.value.trim().toLowerCase();
    const type = els.type.value;
    const rarity = els.rarity.value;
    const source = els.source.value;
    const status = els.status.value;

    return state.cards.filter((card) => {
        const rarities = cardRarities(card);
        const haystack = [
            card.nome,
            card.id,
            card.cardType,
            card.tipo,
            card.universo,
            card.rarity,
            ...(rarities || []),
        ].join(" ").toLowerCase();
        return (!query || haystack.includes(query))
            && (!type || card.cardType === type)
            && (!rarity || rarities.includes(rarity))
            && (!source || card.source === source)
            && (!status || (status === "active" ? card.active : !card.active));
    });
}

function render() {
    const cards = filteredCards();
    els.cardsCount.textContent = `${cards.length} carta${cards.length === 1 ? "" : "s"}`;

    if (!cards.length) {
        els.cardsList.innerHTML = `<p class="empty">Nenhuma carta encontrada.</p>`;
        return;
    }

    els.cardsList.innerHTML = cards.map(cardTemplate).join("");
    els.cardsList.querySelectorAll("[data-delete]").forEach((button) => {
        button.addEventListener("click", () => deleteCard(button.dataset.delete));
    });
    els.cardsList.querySelectorAll("img").forEach((image) => {
        image.addEventListener("error", () => {
            image.src = FALLBACK_IMAGE;
        });
    });
}

function cardTemplate(card) {
    const rarities = cardRarities(card);
    const primaryRarity = card.rarity || rarities[0];
    const imageUrl = imageForRarity(card, primaryRarity);
    return `
        <article class="library-card">
            <img alt="" src="${escapeHtml(imageUrl)}">
            <div class="library-card-body">
                <div class="badges">
                    <span class="badge">${escapeHtml(card.source || "CUSTOM")}</span>
                    <span class="badge type">${escapeHtml(card.cardType || "CARTA")}</span>
                    <span class="badge ${card.active ? "active" : "inactive"}">${card.active ? "Ativa" : "Inativa"}</span>
                </div>
                <div>
                    <h3>${escapeHtml(card.nome || "Sem nome")}</h3>
                    <p class="card-id">${escapeHtml(card.id || "")}</p>
                </div>
                <div class="meta-line">
                    <span>${escapeHtml(rarities.map(formatEnum).join(", ") || "Sem raridade")}</span>
                    <span>${escapeHtml(card.tipo || card.universo || "Sem tipo")}</span>
                </div>
                <div class="card-actions">
                    <a class="button secondary small" href="/admin-card.html?id=${encodeURIComponent(card.id)}">Editar</a>
                    <button class="danger small" type="button" data-delete="${escapeHtml(card.id)}">Excluir</button>
                </div>
            </div>
        </article>
    `;
}

async function deleteCard(id) {
    const card = state.cards.find((item) => item.id === id);
    if (!card || !confirmDelete(card.nome || id)) return;

    setBusy(true);
    try {
        await api.deleteCard(id);
        state.cards = state.cards.filter((item) => item.id !== id);
        render();
        showToast("Carta excluida.");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}
