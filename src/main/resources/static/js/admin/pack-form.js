import { api } from "./api.js";
import {
    $,
    $$,
    FALLBACK_IMAGE,
    cardRarities,
    confirmDelete,
    escapeHtml,
    fillSelect,
    formatEnum,
    imageForRarity,
    numberValue,
    setBusy,
    showToast,
    slugPreview,
} from "./ui.js";

const state = {
    id: new URLSearchParams(location.search).get("id"),
    cards: [],
    options: {},
    currentPack: null,
    selectedIds: new Set(),
};

const fields = {
    pageTitle: $("#pageTitle"),
    modeBadge: $("#modeBadge"),
    idPreview: $("#idPreview"),
    nome: $("#nome"),
    descricao: $("#descricao"),
    imageUrl: $("#imageUrl"),
    cost: $("#cost"),
    cardsPerPack: $("#cardsPerPack"),
    active: $("#active"),
    selectedCount: $("#selectedCount"),
    cardSearch: $("#cardSearch"),
    cardTypeFilter: $("#cardTypeFilter"),
    cardRarityFilter: $("#cardRarityFilter"),
    cardStatusFilter: $("#cardStatusFilter"),
    cardsPicker: $("#cardsPicker"),
    deleteButton: $("#deleteButton"),
};

const preview = {
    image: $("#previewImage"),
    meta: $("#previewMeta"),
    name: $("#previewName"),
    description: $("#previewDescription"),
};

document.addEventListener("DOMContentLoaded", init);

async function init() {
    wireEvents();
    await loadInitialData();
}

function wireEvents() {
    $("#packForm").addEventListener("submit", save);
    fields.deleteButton.addEventListener("click", deleteCurrent);
    [fields.nome, fields.descricao, fields.imageUrl, fields.cost, fields.cardsPerPack].forEach((element) => {
        element.addEventListener("input", updatePreview);
    });
    [fields.cardSearch, fields.cardTypeFilter, fields.cardRarityFilter, fields.cardStatusFilter].forEach((element) => {
        element.addEventListener("input", renderCardsPicker);
        element.addEventListener("change", renderCardsPicker);
    });
}

async function loadInitialData() {
    setBusy(true);
    try {
        const [options, cards] = await Promise.all([api.options(), api.cards()]);
        state.options = options;
        state.cards = cards;
        hydrateOptions();

        if (state.id) {
            const pack = await api.pack(state.id);
            loadPack(pack);
        } else {
            resetForCreate();
        }
        renderCardsPicker();
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}

function hydrateOptions() {
    fillSelect(fields.cardTypeFilter, state.options.cardTypes || [], "Todos");
    fillSelect(fields.cardRarityFilter, state.options.rarities || [], "Todas");
}

function resetForCreate() {
    state.id = null;
    state.currentPack = null;
    state.selectedIds = new Set();
    fields.pageTitle.textContent = "Novo pacote";
    fields.modeBadge.textContent = "Criando";
    fields.idPreview.textContent = "Sera gerado ao salvar";
    fields.nome.value = "";
    fields.descricao.value = "";
    fields.imageUrl.value = "";
    fields.cost.value = 100;
    fields.cardsPerPack.value = 5;
    fields.active.checked = true;
    setEditOnlyButtons(false);
    updatePreview();
    updateSelectedCount();
}

function loadPack(pack) {
    state.id = pack.id;
    state.currentPack = pack;
    state.selectedIds = new Set(pack.cardIds || []);
    fields.pageTitle.textContent = `Editar ${pack.nome || "pacote"}`;
    fields.modeBadge.textContent = "Editando";
    fields.idPreview.textContent = pack.id || "";
    fields.nome.value = pack.nome || "";
    fields.descricao.value = pack.descricao || "";
    fields.imageUrl.value = pack.imageUrl || "";
    fields.cost.value = pack.cost || 0;
    fields.cardsPerPack.value = pack.cardsPerPack || 5;
    fields.active.checked = Boolean(pack.active);
    setEditOnlyButtons(true);
    updatePreview();
    updateSelectedCount();
}

function setEditOnlyButtons(isEditing) {
    fields.deleteButton.disabled = !isEditing;
    fields.deleteButton.dataset.locked = String(!isEditing);
}

function renderCardsPicker() {
    const cards = filteredCards();
    if (!cards.length) {
        fields.cardsPicker.innerHTML = `<p class="empty">Nenhuma carta encontrada.</p>`;
        return;
    }

    fields.cardsPicker.innerHTML = cards.map(cardRow).join("");
    fields.cardsPicker.querySelectorAll("[data-card-id]").forEach((input) => {
        input.addEventListener("change", () => {
            if (input.checked) {
                state.selectedIds.add(input.value);
            } else {
                state.selectedIds.delete(input.value);
            }
            updateSelectedCount();
        });
    });
    fields.cardsPicker.querySelectorAll("img").forEach((image) => {
        image.addEventListener("error", () => {
            image.src = FALLBACK_IMAGE;
        });
    });
}

function filteredCards() {
    const query = fields.cardSearch.value.trim().toLowerCase();
    const type = fields.cardTypeFilter.value;
    const rarity = fields.cardRarityFilter.value;
    const status = fields.cardStatusFilter.value;
    return state.cards.filter((card) => {
        const rarities = cardRarities(card);
        const haystack = [
            card.nome,
            card.id,
            card.cardType,
            card.tipo,
            card.universo,
            ...(rarities || []),
        ].join(" ").toLowerCase();
        return (!query || haystack.includes(query))
            && (!type || card.cardType === type)
            && (!rarity || rarities.includes(rarity))
            && (!status || (status === "active" ? card.active : !card.active));
    });
}

function cardRow(card) {
    const checked = state.selectedIds.has(card.id) ? "checked" : "";
    const rarities = cardRarities(card);
    const imageUrl = imageForRarity(card, card.rarity || rarities[0]);
    return `
        <label class="selection-item">
            <input data-card-id="${escapeHtml(card.id)}" type="checkbox" value="${escapeHtml(card.id)}" ${checked}>
            <img alt="" src="${escapeHtml(imageUrl)}">
            <span>
                <strong>${escapeHtml(card.nome || "Sem nome")}</strong>
                <small>${escapeHtml(card.id || "")}</small>
                <small>${escapeHtml(card.cardType || "CARTA")} · ${escapeHtml(rarities.map(formatEnum).join(", ") || "Sem raridade")}</small>
            </span>
            <em class="${card.active ? "ok-text" : "danger-text"}">${card.active ? "Ativa" : "Inativa"}</em>
        </label>
    `;
}

function buildPayload() {
    return {
        id: state.id || "",
        nome: fields.nome.value.trim(),
        descricao: fields.descricao.value.trim(),
        imageUrl: fields.imageUrl.value.trim(),
        cost: numberValue(fields.cost),
        cardsPerPack: Math.max(1, numberValue(fields.cardsPerPack)),
        active: fields.active.checked,
        cardIds: [...state.selectedIds],
    };
}

async function save(event) {
    event.preventDefault();
    const payload = buildPayload();
    if (!payload.cardIds.length) {
        showToast("Escolha pelo menos uma carta para o pacote.", "error");
        return;
    }

    setBusy(true);
    try {
        const saved = state.id
            ? await api.updatePack(state.id, payload)
            : await api.createPack(payload);
        state.id = saved.id;
        state.currentPack = saved;
        state.selectedIds = new Set(saved.cardIds || []);
        history.replaceState(null, "", `/admin-pack.html?id=${encodeURIComponent(saved.id)}`);
        loadPack(saved);
        renderCardsPicker();
        showToast("Pacote salvo.");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}

async function deleteCurrent() {
    if (!state.currentPack || !confirmDelete(state.currentPack.nome || state.id)) return;
    setBusy(true);
    try {
        await api.deletePack(state.id);
        showToast("Pacote excluido.");
        window.location.href = "/admin-packs.html";
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}

function updateSelectedCount() {
    fields.selectedCount.textContent = `${state.selectedIds.size} selecionada${state.selectedIds.size === 1 ? "" : "s"}`;
}

function updatePreview() {
    preview.name.textContent = fields.nome.value.trim() || "Novo pacote";
    preview.description.textContent = fields.descricao.value.trim() || "Selecione cartas para montar o pacote.";
    preview.meta.textContent = `${numberValue(fields.cost)} moedas · ${Math.max(1, numberValue(fields.cardsPerPack))} cartas`;
    preview.image.src = fields.imageUrl.value.trim() || FALLBACK_IMAGE;

    if (!state.id) {
        fields.idPreview.textContent = slugPreview(fields.nome.value) || "Sera gerado ao salvar";
    }
}
