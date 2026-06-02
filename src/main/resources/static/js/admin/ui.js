export const FALLBACK_IMAGE =
    "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='480' height='360' viewBox='0 0 480 360'%3E%3Crect width='480' height='360' fill='%23241913'/%3E%3Cpath d='M86 275h308L302 154l-55 68-39-48z' fill='%23b9342f'/%3E%3Ccircle cx='165' cy='122' r='34' fill='%23d59a2b'/%3E%3Ctext x='240' y='322' text-anchor='middle' font-family='Arial' font-size='26' fill='%23fffaf1'%3EMultiversal TCG%3C/text%3E%3C/svg%3E";

export const $ = (selector, root = document) => root.querySelector(selector);
export const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];

export function showToast(message, type = "ok") {
    let toast = $("#toast");
    if (!toast) {
        toast = document.createElement("div");
        toast.id = "toast";
        toast.className = "toast";
        toast.setAttribute("role", "status");
        document.body.append(toast);
    }
    toast.textContent = message;
    toast.dataset.type = type;
    toast.classList.add("show");
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => toast.classList.remove("show"), 3200);
}

export function setBusy(isBusy) {
    $$("button, input, select, textarea").forEach((element) => {
        if (element.dataset.locked === "true") return;
        element.disabled = isBusy;
    });
}

export function confirmDelete(name) {
    return window.confirm(`Excluir permanentemente "${name}"?`);
}

export function formatEnum(value) {
    return String(value || "")
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

export function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

export function fillSelect(select, values = [], emptyLabel = "Nenhum") {
    select.innerHTML = "";
    if (emptyLabel !== null) {
        select.append(new Option(emptyLabel, ""));
    }
    values.forEach((value) => select.append(new Option(formatEnum(value), value)));
}

export function cardRarities(card) {
    return card && card.rarities && card.rarities.length
        ? card.rarities
        : [card && card.rarity].filter(Boolean);
}

export function imageForRarity(card, rarity) {
    if (card && card.rarityImageUrls && rarity && card.rarityImageUrls[rarity]) {
        return card.rarityImageUrls[rarity];
    }
    return (card && card.imageUrl) || FALLBACK_IMAGE;
}

export function numberValue(input) {
    const value = Number(input.value);
    return Number.isFinite(value) ? value : 0;
}

export function slugPreview(value) {
    return String(value || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, "_")
        .replace(/^_+|_+$/g, "");
}

export function parseJsonArray(value, label) {
    if (!value || !value.trim()) return [];
    try {
        const parsed = JSON.parse(value);
        if (!Array.isArray(parsed)) throw new Error();
        return parsed;
    } catch {
        throw new Error(`${label} deve ser um JSON de array valido.`);
    }
}
