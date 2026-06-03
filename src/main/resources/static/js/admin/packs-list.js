import { api } from "./api.js";
import {
    $,
    FALLBACK_IMAGE,
    confirmDelete,
    escapeHtml,
    setBusy,
    showToast,
} from "./ui.js";

const state = {
    packs: [],
};

const els = {
    packsList: $("#packsList"),
    packsCount: $("#packsCount"),
    search: $("#searchPacks"),
    status: $("#filterStatus"),
};

document.addEventListener("DOMContentLoaded", init);

async function init() {
    wireEvents();
    await loadPacks();
}

function wireEvents() {
    [els.search, els.status].forEach((element) => {
        element.addEventListener("input", render);
        element.addEventListener("change", render);
    });
}

async function loadPacks() {
    els.packsList.innerHTML = `<p class="loading">Carregando pacotes...</p>`;
    try {
        state.packs = await api.packs();
        render();
    } catch (error) {
        els.packsList.innerHTML = `<p class="empty">Nao foi possivel carregar os pacotes.</p>`;
        showToast(error.message, "error");
    }
}

function filteredPacks() {
    const query = els.search.value.trim().toLowerCase();
    const status = els.status.value;
    return state.packs.filter((pack) => {
        const haystack = [pack.nome, pack.id, pack.descricao].join(" ").toLowerCase();
        return (!query || haystack.includes(query))
            && (!status || (status === "active" ? pack.active : !pack.active));
    });
}

function render() {
    const packs = filteredPacks();
    els.packsCount.textContent = `${packs.length} pacote${packs.length === 1 ? "" : "s"}`;

    if (!packs.length) {
        els.packsList.innerHTML = `<p class="empty">Nenhum pacote encontrado.</p>`;
        return;
    }

    els.packsList.innerHTML = packs.map(packTemplate).join("");
    els.packsList.querySelectorAll("[data-delete]").forEach((button) => {
        button.addEventListener("click", () => deletePack(button.dataset.delete));
    });
    els.packsList.querySelectorAll("img").forEach((image) => {
        image.addEventListener("error", () => {
            image.src = FALLBACK_IMAGE;
        });
    });
}

function packTemplate(pack) {
    return `
        <article class="library-card">
            <img alt="" src="${escapeHtml(pack.imageUrl || FALLBACK_IMAGE)}">
            <div class="library-card-body">
                <div class="badges">
                    <span class="badge ${pack.active ? "active" : "inactive"}">${pack.active ? "Ativo" : "Inativo"}</span>
                    <span class="badge type">${Number(pack.cost || 0)} moedas</span>
                    <span class="badge">${Number(pack.cardsPerPack || 0)} cartas</span>
                </div>
                <div>
                    <h3>${escapeHtml(pack.nome || "Sem nome")}</h3>
                    <p class="card-id">${escapeHtml(pack.id || "")}</p>
                </div>
                <p class="muted">${escapeHtml(pack.descricao || "Sem descricao.")}</p>
                <div class="meta-line">
                    <span>${Number(pack.cardCount || 0)} no pool</span>
                </div>
                <div class="card-actions">
                    <a class="button secondary small" href="/admin-pack.html?id=${encodeURIComponent(pack.id)}">Editar</a>
                    <button class="danger small" type="button" data-delete="${escapeHtml(pack.id)}">Excluir</button>
                </div>
            </div>
        </article>
    `;
}

async function deletePack(id) {
    const pack = state.packs.find((item) => item.id === id);
    if (!pack || !confirmDelete(pack.nome || id)) return;

    setBusy(true);
    try {
        await api.deletePack(id);
        state.packs = state.packs.filter((item) => item.id !== id);
        render();
        showToast("Pacote excluido.");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}
