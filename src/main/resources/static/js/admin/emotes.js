import { api } from "./api.js?v=20260603-3";
import {
    $,
    escapeHtml,
    setBusy,
    showToast,
} from "./ui.js";

const state = {
    emotes: [],
};

const els = {
    list: $("#emotesList"),
    count: $("#emotesCount"),
    form: $("#emoteForm"),
    newName: $("#newEmoteName"),
    newGifUrl: $("#newEmoteGifUrl"),
    newFile: $("#newEmoteFile"),
};

document.addEventListener("DOMContentLoaded", init);

async function init() {
    wireEvents();
    await loadEmotes();
}

function wireEvents() {
    els.form.addEventListener("submit", createEmote);
}

async function loadEmotes() {
    els.list.innerHTML = `<p class="loading">Carregando emotes...</p>`;
    try {
        state.emotes = await api.emotes();
        render();
    } catch (error) {
        els.list.innerHTML = `<p class="empty">Nao foi possivel carregar os emotes.</p>`;
        showToast(error.message, "error");
    }
}

function render() {
    els.count.textContent = `${state.emotes.length} emote${state.emotes.length === 1 ? "" : "s"}`;
    if (!state.emotes.length) {
        els.list.innerHTML = `<p class="empty">Nenhum emote encontrado.</p>`;
        return;
    }

    els.list.innerHTML = state.emotes.map(emoteTemplate).join("");
    els.list.querySelectorAll("[data-save]").forEach((button) => {
        button.addEventListener("click", () => saveUrl(button.dataset.save));
    });
    els.list.querySelectorAll("[data-upload]").forEach((button) => {
        button.addEventListener("click", () => uploadGif(button.dataset.upload));
    });
    els.list.querySelectorAll("[data-delete]").forEach((button) => {
        button.addEventListener("click", () => deleteEmote(button.dataset.delete));
    });
    els.list.querySelectorAll("[data-preview]").forEach((input) => {
        input.addEventListener("input", () => updatePreview(input.dataset.preview));
    });
}

function emoteTemplate(emote) {
    const id = escapeHtml(emote.id);
    const canDelete = !["HELLO", "GOOD_GAME", "THANKS", "WOW", "OOPS", "ANGRY"].includes(emote.id);
    return `
        <article class="library-card emote-card">
            <img class="emote-preview" data-image="${id}" alt="${escapeHtml(emote.nome || emote.id)}" src="${escapeHtml(emote.gifUrl || "")}">
            <div class="library-card-body">
                <div>
                    <h3>${escapeHtml(emote.nome || emote.id)}</h3>
                </div>
                <label>
                    URL do GIF
                    <input data-preview="${id}" data-url="${id}" value="${escapeHtml(emote.gifUrl || "")}" maxlength="500" placeholder="/uploads/emotes/${id.toLowerCase()}.gif">
                </label>
                <label>
                    Enviar GIF
                    <input data-file="${id}" type="file" accept="image/gif">
                </label>
                <div class="card-actions">
                    <button class="secondary small" type="button" data-save="${id}">Salvar URL</button>
                    <button class="small" type="button" data-upload="${id}">Enviar GIF</button>
                    ${canDelete ? `<button class="danger small" type="button" data-delete="${id}">Excluir</button>` : ""}
                </div>
            </div>
        </article>
    `;
}

function updatePreview(id) {
    const input = els.list.querySelector(`[data-url="${cssEscape(id)}"]`);
    const image = els.list.querySelector(`[data-image="${cssEscape(id)}"]`);
    if (input && image) {
        image.src = input.value.trim();
    }
}

async function saveUrl(id) {
    const input = els.list.querySelector(`[data-url="${cssEscape(id)}"]`);
    const current = state.emotes.find((emote) => emote.id === id);
    const gifUrl = input ? input.value.trim() : "";
    if (!gifUrl) {
        showToast("Informe a URL do GIF.", "error");
        return;
    }

    setBusy(true);
    try {
        const saved = await api.updateEmote(id, {
            nome: current ? current.nome : id,
            gifUrl,
        });
        replaceEmote(saved);
        render();
        showToast("Emote atualizado.");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}

async function uploadGif(id) {
    const input = els.list.querySelector(`[data-file="${cssEscape(id)}"]`);
    const file = input && input.files ? input.files[0] : null;
    if (!file) {
        showToast("Escolha um arquivo GIF.", "error");
        return;
    }

    setBusy(true);
    try {
        const saved = await api.uploadEmoteGif(id, file);
        replaceEmote(saved);
        render();
        showToast("GIF enviado.");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}

async function createEmote(event) {
    event.preventDefault();
    const nome = els.newName.value.trim();
    const id = nome;
    const gifUrl = els.newGifUrl.value.trim();
    const file = els.newFile.files ? els.newFile.files[0] : null;
    if (!nome) {
        showToast("Informe o nome do emote.", "error");
        return;
    }
    if (!gifUrl && !file) {
        showToast("Informe a URL ou escolha um arquivo GIF.", "error");
        return;
    }

    setBusy(true);
    try {
        const saved = file
            ? await api.uploadEmoteGif(id, file, nome)
            : await api.createEmote({ id, nome, gifUrl });
        upsertEmote(saved);
        els.form.reset();
        render();
        showToast("Emote adicionado.");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}

async function deleteEmote(id) {
    const current = state.emotes.find((emote) => emote.id === id);
    if (!current || !window.confirm(`Excluir o emote "${current.nome || id}"?`)) return;

    setBusy(true);
    try {
        await api.deleteEmote(id);
        state.emotes = state.emotes.filter((emote) => emote.id !== id);
        render();
        showToast("Emote excluido.");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        setBusy(false);
    }
}

function replaceEmote(saved) {
    state.emotes = state.emotes.map((emote) => emote.id === saved.id ? saved : emote);
}

function upsertEmote(saved) {
    const exists = state.emotes.some((emote) => emote.id === saved.id);
    if (exists) {
        replaceEmote(saved);
    } else {
        state.emotes = [...state.emotes, saved].sort((a, b) => a.id.localeCompare(b.id));
    }
}

function cssEscape(value) {
    if (window.CSS && CSS.escape) return CSS.escape(value);
    return String(value).replace(/"/g, '\\"');
}
