import { api } from "./api.js";
import {
    $,
    $$,
    FALLBACK_IMAGE,
    cardRarities,
    confirmDelete,
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
    options: {},
    cards: [],
    currentCard: null,
};

const fields = {
    pageTitle: $("#pageTitle"),
    modeBadge: $("#modeBadge"),
    idPreview: $("#idPreview"),
    nome: $("#nome"),
    descricao: $("#descricao"),
    cardType: $("#cardType"),
    universo: $("#universo"),
    active: $("#active"),
    tipo: $("#tipo"),
    atk: $("#atk"),
    def: $("#def"),
    evolucaoId: $("#evolucaoId"),
    efeito: $("#efeito"),
    trigger: $("#trigger"),
    tipoAlvo: $("#tipoAlvo"),
    custoAura: $("#custoAura"),
    valor: $("#valor"),
    duracao: $("#duracao"),
    baseMonsterId: $("#baseMonsterId"),
    evolvedMonsterId: $("#evolvedMonsterId"),
    primaryRarityPreview: $("#primaryRarityPreview"),
    imageRarity: $("#imageRarity"),
    imageFile: $("#imageFile"),
    uploadButton: $("#uploadButton"),
    deleteButton: $("#deleteButton"),
};

const sections = {
    monster: $("#monsterSection"),
    effect: $("#effectSection"),
    trigger: $("#triggerField"),
    evolution: $("#evolutionSection"),
};

const lists = {
    rarities: $("#raritiesList"),
    rarityImages: $("#rarityImagesList"),
    attacks: $("#attacksList"),
};

const preview = {
    image: $("#previewImage"),
    type: $("#previewType"),
    name: $("#previewName"),
    description: $("#previewDescription"),
    atk: $("#previewAtk"),
    def: $("#previewDef"),
};

document.addEventListener("DOMContentLoaded", init);

async function init() {
    wireEvents();
    await loadInitialData();
}

function wireEvents() {
    $("#cardForm").addEventListener("submit", save);
    $("#addAttackButton").addEventListener("click", () => addAttackRow());
    fields.uploadButton.addEventListener("click", uploadImage);
    fields.deleteButton.addEventListener("click", deleteCurrent);
    fields.cardType.addEventListener("change", syncTypeSections);
    fields.nome.addEventListener("input", updatePreview);
    fields.descricao.addEventListener("input", updatePreview);
    fields.atk.addEventListener("input", updatePreview);
    fields.def.addEventListener("input", updatePreview);
}

async function loadInitialData() {
    setBusy(true);
    try {
        const [options, cards] = await Promise.all([api.options(), api.cards()]);
        state.options = options;
        state.cards = cards;
        hydrateOptions();

        if (state.id) {
            const card = await api.card(state.id);
            loadCard(card);
        } else {
            resetForCreate();
        }
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}

function hydrateOptions() {
    fillSelect(fields.cardType, state.options.cardTypes || [], null);
    fillSelect(fields.tipo, state.options.tipos || [], "Nenhum");
    fillSelect(fields.evolucaoId, [], "Sem evolucao");
    fillSelect(fields.efeito, state.options.efeitos || [], "Nenhum");
    fillSelect(fields.trigger, state.options.triggers || [], "Nenhum");
    fillSelect(fields.tipoAlvo, state.options.tipos || [], "Nenhum");
    fillSelect(fields.baseMonsterId, [], "Escolha o monstro base");
    fillSelect(fields.evolvedMonsterId, [], "Escolha o monstro evoluido");
    renderRarityChips(["COMUM"]);
    refreshMonsterSelects();
}

function resetForCreate() {
    state.id = null;
    state.currentCard = null;
    fields.pageTitle.textContent = "Nova carta";
    fields.modeBadge.textContent = "Criando";
    fields.idPreview.textContent = "Sera gerado ao salvar";
    fields.nome.value = "";
    fields.descricao.value = "";
    fields.cardType.value = "MONSTRO";
    fields.universo.value = "";
    fields.active.checked = true;
    fields.tipo.value = "";
    fields.atk.value = 0;
    fields.def.value = 0;
    fields.evolucaoId.value = "";
    fields.efeito.value = "";
    fields.trigger.value = "";
    fields.tipoAlvo.value = "";
    fields.custoAura.value = 0;
    fields.valor.value = 0;
    fields.duracao.value = 0;
    fields.baseMonsterId.value = "";
    fields.evolvedMonsterId.value = "";
    lists.attacks.innerHTML = "";
    addAttackRow();
    renderRarityChips(["COMUM"]);
    setEditOnlyButtons(false);
    syncTypeSections();
    updatePreview();
}

function loadCard(card) {
    state.id = card.id;
    state.currentCard = card;
    fields.pageTitle.textContent = `Editar ${card.nome || "carta"}`;
    fields.modeBadge.textContent = "Editando";
    fields.idPreview.textContent = card.id || "";
    fields.nome.value = card.nome || "";
    fields.descricao.value = card.descricao || "";
    fields.cardType.value = card.cardType || "MONSTRO";
    fields.universo.value = card.universo || "";
    fields.active.checked = Boolean(card.active);
    fields.tipo.value = card.tipo || "";
    fields.atk.value = card.atk || 0;
    fields.def.value = card.def || 0;
    fields.efeito.value = card.efeito || "";
    fields.trigger.value = card.trigger || "";
    fields.tipoAlvo.value = card.tipoAlvo || "";
    fields.custoAura.value = card.custoAura || 0;
    fields.valor.value = card.valor || 0;
    fields.duracao.value = card.duracao || 0;

    refreshMonsterSelects(card);
    fields.evolucaoId.value = card.evolucaoId || "";
    fields.baseMonsterId.value = card.baseMonsterId || "";
    fields.evolvedMonsterId.value = card.evolvedMonsterId || "";

    lists.attacks.innerHTML = "";
    (card.ataques || []).forEach(addAttackRow);
    if (!lists.attacks.children.length) addAttackRow();

    renderRarityChips(cardRarities(card));
    renderRarityImages(card.rarityImageUrls || {}, card.imageUrl || "");
    setEditOnlyButtons(true);
    syncTypeSections();
    updatePreview();
}

function setEditOnlyButtons(isEditing) {
    fields.deleteButton.disabled = !isEditing;
    fields.deleteButton.dataset.locked = String(!isEditing);
    fields.uploadButton.disabled = !isEditing;
    fields.uploadButton.dataset.locked = String(!isEditing);
}

function syncTypeSections() {
    const type = fields.cardType.value;
    sections.monster.classList.toggle("hidden", type !== "MONSTRO");
    sections.effect.classList.toggle("hidden", type !== "MAGIA" && type !== "ARMADILHA");
    sections.trigger.classList.toggle("hidden", type !== "ARMADILHA");
    sections.evolution.classList.toggle("hidden", type !== "EVOLUCAO");
    updatePreview();
}

function refreshMonsterSelects(selected = {}) {
    const monsters = state.cards
        .filter((card) => card.cardType === "MONSTRO" && card.id !== state.id)
        .sort((a, b) => (a.nome || "").localeCompare(b.nome || "", "pt-BR"));

    fillCardSelect(fields.evolucaoId, monsters, "Sem evolucao", selected.evolucaoId);
    fillCardSelect(fields.baseMonsterId, monsters, "Escolha o monstro base", selected.baseMonsterId);
    fillCardSelect(fields.evolvedMonsterId, monsters, "Escolha o monstro evoluido", selected.evolvedMonsterId);
}

function fillCardSelect(select, cards, emptyLabel, selectedValue = "") {
    select.innerHTML = "";
    select.append(new Option(emptyLabel, ""));
    cards.forEach((card) => select.append(new Option(`${card.nome} (${card.id})`, card.id)));
    if (selectedValue && !cards.some((card) => card.id === selectedValue)) {
        select.append(new Option(`${selectedValue} (nao encontrado)`, selectedValue));
    }
    select.value = selectedValue || "";
}

function renderRarityChips(selected = ["COMUM"]) {
    const selectedSet = new Set(selected && selected.length ? selected : ["COMUM"]);
    lists.rarities.innerHTML = "";
    (state.options.rarities || []).forEach((rarity) => {
        const label = document.createElement("label");
        label.className = "chip";
        label.innerHTML = `
            <input type="checkbox" value="${rarity}" ${selectedSet.has(rarity) ? "checked" : ""}>
            ${formatEnum(rarity)}
        `;
        label.querySelector("input").addEventListener("change", () => {
            if (!readRarities().length) {
                label.querySelector("input").checked = true;
                showToast("Escolha pelo menos uma raridade.", "error");
            }
            renderRarityImages(readRarityImages(), primaryImage());
            updatePreview();
        });
        lists.rarities.append(label);
    });
    renderRarityImages(readRarityImages(), primaryImage());
}

function renderRarityImages(seed = {}, fallback = "") {
    const rarities = readRarities();
    lists.rarityImages.innerHTML = "";
    fields.imageRarity.innerHTML = "";

    rarities.forEach((rarity, index) => {
        const row = document.createElement("div");
        row.className = "rarity-image-row";
        row.innerHTML = `
            <label>
                Arte ${formatEnum(rarity)}
                <input data-rarity-image="${rarity}" maxlength="500" placeholder="/uploads/cards/${rarity.toLowerCase()}.png">
            </label>
            <button class="secondary small" type="button">Preview</button>
        `;
        row.querySelector("input").value = seed[rarity] || (index === 0 ? fallback : "");
        row.querySelector("input").addEventListener("input", updatePreview);
        row.querySelector("button").addEventListener("click", () => {
            preview.image.src = row.querySelector("input").value.trim() || FALLBACK_IMAGE;
        });
        lists.rarityImages.append(row);
        fields.imageRarity.append(new Option(formatEnum(rarity), rarity));
    });
}

function addAttackRow(attack = {}) {
    const row = document.createElement("div");
    row.className = "repeat-item";
    row.innerHTML = `
        <div class="grid three">
            <label>Nome <input data-field="nome" maxlength="90" placeholder="Ex: Corte solar"></label>
            <label>Custo aura <input data-field="custoAura" type="number" min="0" step="1" value="0"></label>
            <label>Bonus ATK <input data-field="bonusAtk" type="number" step="1" value="0"></label>
        </div>
        <div class="grid three">
            <label>Status <select data-field="statusAplicado"></select></label>
            <label>Duracao <input data-field="duracaoStatus" type="number" min="0" step="1" value="0"></label>
            <label class="field-label">Acao <button class="secondary small" type="button">Remover</button></label>
        </div>
    `;
    fillSelect(row.querySelector("[data-field='statusAplicado']"), state.options.status || [], "Nenhum");
    setRow(row, "nome", attack.nome || "");
    setRow(row, "custoAura", attack.custoAura || 0);
    setRow(row, "bonusAtk", attack.bonusAtk || 0);
    setRow(row, "statusAplicado", attack.statusAplicado || "");
    setRow(row, "duracaoStatus", attack.duracaoStatus || 0);
    row.querySelector("button").addEventListener("click", () => row.remove());
    lists.attacks.append(row);
}

function buildPayload() {
    const type = fields.cardType.value;
    const rarities = readRarities();
    const rarityImageUrls = readRarityImages();

    return {
        id: state.id || "",
        nome: fields.nome.value.trim(),
        descricao: fields.descricao.value.trim(),
        imageUrl: rarityImageUrls[rarities[0]] || "",
        rarityImageUrls,
        cardType: type,
        rarity: rarities[0] || "COMUM",
        rarities,
        tipo: fields.tipo.value,
        universo: fields.universo.value.trim(),
        atk: numberValue(fields.atk),
        def: numberValue(fields.def),
        evolucaoId: fields.evolucaoId.value,
        efeito: fields.efeito.value,
        trigger: fields.trigger.value,
        custoAura: numberValue(fields.custoAura),
        valor: numberValue(fields.valor),
        duracao: numberValue(fields.duracao),
        tipoAlvo: fields.tipoAlvo.value,
        baseMonsterId: fields.baseMonsterId.value,
        evolvedMonsterId: fields.evolvedMonsterId.value,
        ataques: type === "MONSTRO" ? readAttacks() : [],
        regras: state.currentCard && state.currentCard.regras ? state.currentCard.regras : [],
        active: fields.active.checked,
        deckCopies: state.currentCard ? state.currentCard.deckCopies || 0 : 0,
    };
}

async function save(event) {
    event.preventDefault();

    let payload;
    try {
        payload = buildPayload();
    } catch (error) {
        showToast(error.message, "error");
        return;
    }

    setBusy(true);
    try {
        const saved = state.id
            ? await api.updateCard(state.id, payload)
            : await api.createCard(payload);
        state.id = saved.id;
        state.currentCard = saved;
        state.cards = state.cards.filter((card) => card.id !== saved.id);
        state.cards.push(saved);
        history.replaceState(null, "", `/admin-card.html?id=${encodeURIComponent(saved.id)}`);
        loadCard(saved);
        showToast("Carta salva.");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}

async function uploadImage() {
    if (!state.id) {
        showToast("Salve a carta antes de enviar imagem.", "error");
        return;
    }
    if (!fields.imageFile.files[0]) {
        showToast("Escolha uma imagem primeiro.", "error");
        return;
    }

    const rarity = fields.imageRarity.value || primaryRarity();
    setBusy(true);
    try {
        const result = await api.uploadImage(state.id, rarity, fields.imageFile.files[0]);
        const input = lists.rarityImages.querySelector(`[data-rarity-image='${rarity}']`);
        if (input) input.value = result.imageUrl;
        fields.imageFile.value = "";
        updatePreview();
        showToast(`Imagem ${formatEnum(rarity)} enviada em WebP.`);
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}

async function deleteCurrent() {
    if (!state.currentCard || !confirmDelete(state.currentCard.nome || state.id)) return;
    setBusy(true);
    try {
        await api.deleteCard(state.id);
        showToast("Carta excluida.");
        window.location.href = "/admin.html";
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}

function readRarities() {
    return $$("input:checked", lists.rarities).map((input) => input.value);
}

function readRarityImages() {
    const images = {};
    $$("[data-rarity-image]", lists.rarityImages).forEach((input) => {
        if (input.value.trim()) images[input.dataset.rarityImage] = input.value.trim();
    });
    return images;
}

function primaryRarity() {
    return readRarities()[0] || "COMUM";
}

function primaryImage() {
    return readRarityImages()[primaryRarity()] || (state.currentCard && state.currentCard.imageUrl) || "";
}

function readAttacks() {
    return $$(".repeat-item", lists.attacks)
        .map((row) => ({
            nome: getRow(row, "nome").trim(),
            custoAura: numberValue(row.querySelector("[data-field='custoAura']")),
            bonusAtk: numberValue(row.querySelector("[data-field='bonusAtk']")),
            statusAplicado: getRow(row, "statusAplicado") || null,
            duracaoStatus: numberValue(row.querySelector("[data-field='duracaoStatus']")),
        }))
        .filter((attack) => attack.nome);
}

function updatePreview() {
    const rarity = primaryRarity();
    fields.primaryRarityPreview.textContent = rarity;
    preview.name.textContent = fields.nome.value.trim() || "Nova carta";
    preview.description.textContent = fields.descricao.value.trim() || "Preencha o formulario para visualizar.";
    preview.type.textContent = fields.cardType.value || "MONSTRO";
    preview.atk.textContent = `ATK ${numberValue(fields.atk)}`;
    preview.def.textContent = `DEF ${numberValue(fields.def)}`;
    preview.image.src = readRarityImages()[rarity] || FALLBACK_IMAGE;

    if (!state.id) {
        fields.idPreview.textContent = slugPreview(fields.nome.value) || "Sera gerado ao salvar";
    }
}

function getRow(row, field) {
    const input = row.querySelector(`[data-field='${field}']`);
    return input ? input.value : "";
}

function setRow(row, field, value) {
    const input = row.querySelector(`[data-field='${field}']`);
    if (input) input.value = value;
}
