const JSON_HEADERS = { "Content-Type": "application/json" };
const API_BASE = resolveApiBase();

function resolveApiBase() {
    const configured = localStorage.getItem("adminApiBaseUrl") || localStorage.getItem("apiBaseUrl");
    if (configured) {
        return configured.replace(/\/+$/, "");
    }

    const runningOutsideBackend = window.location.protocol === "file:"
        || (window.location.hostname && window.location.port && window.location.port !== "8080");
    return runningOutsideBackend ? "http://localhost:8080" : "";
}

function authHeaders() {
    const token = localStorage.getItem("token") || localStorage.getItem("authToken");
    return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(path, options = {}) {
    const { auth = true, ...fetchOptions } = options;
    const response = await fetch(`${API_BASE}${path}`, {
        ...fetchOptions,
        headers: {
            ...(fetchOptions.body instanceof FormData ? {} : JSON_HEADERS),
            ...(auth ? authHeaders() : {}),
            ...(fetchOptions.headers || {}),
        },
    });

    const text = await response.text();
    const data = text ? JSON.parse(text) : null;
    if (!response.ok) {
        throw new Error((data && (data.erro || data.message)) || `Erro HTTP ${response.status}`);
    }
    return data;
}

export const api = {
    cards: () => request("/api/cards"),
    options: () => request("/api/cards/options"),
    card: (id) => request(`/api/cards/${encodeURIComponent(id)}`),
    createCard: (payload) => request("/api/cards", {
        method: "POST",
        body: JSON.stringify(payload),
    }),
    updateCard: (id, payload) => request(`/api/cards/${encodeURIComponent(id)}`, {
        method: "PUT",
        body: JSON.stringify(payload),
    }),
    deleteCard: (id) => request(`/api/cards/${encodeURIComponent(id)}`, {
        method: "DELETE",
    }),
    packs: () => request("/api/packs"),
    pack: (id) => request(`/api/packs/${encodeURIComponent(id)}`),
    createPack: (payload) => request("/api/packs", {
        method: "POST",
        body: JSON.stringify(payload),
    }),
    updatePack: (id, payload) => request(`/api/packs/${encodeURIComponent(id)}`, {
        method: "PUT",
        body: JSON.stringify(payload),
    }),
    deletePack: (id) => request(`/api/packs/${encodeURIComponent(id)}`, {
        method: "DELETE",
    }),
    emotes: () => request("/api/emotes", { auth: false }),
    createEmote: (payload) => request("/api/emotes", {
        method: "POST",
        auth: false,
        body: JSON.stringify(payload),
    }),
    updateEmote: (id, payload) => request(`/api/emotes/${encodeURIComponent(id)}`, {
        method: "PUT",
        auth: false,
        body: JSON.stringify(payload),
    }),
    deleteEmote: (id) => request(`/api/emotes/${encodeURIComponent(id)}`, {
        method: "DELETE",
        auth: false,
    }),
    uploadEmoteGif: (id, file, nome = "") => {
        const formData = new FormData();
        formData.append("file", file);
        const query = nome ? `?nome=${encodeURIComponent(nome)}` : "";
        return request(`/api/emotes/${encodeURIComponent(id)}/gif${query}`, {
            method: "POST",
            auth: false,
            body: formData,
        });
    },
    uploadImage: (id, rarity, file) => {
        const formData = new FormData();
        formData.append("file", file);
        return request(`/api/cards/${encodeURIComponent(id)}/image?rarity=${encodeURIComponent(rarity || "")}`, {
            method: "POST",
            body: formData,
        });
    },
};
