const JSON_HEADERS = { "Content-Type": "application/json" };

function authHeaders() {
    const token = localStorage.getItem("token") || localStorage.getItem("authToken");
    return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(path, options = {}) {
    const response = await fetch(path, {
        ...options,
        headers: {
            ...(options.body instanceof FormData ? {} : JSON_HEADERS),
            ...authHeaders(),
            ...(options.headers || {}),
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
    uploadImage: (id, rarity, file) => {
        const formData = new FormData();
        formData.append("file", file);
        return request(`/api/cards/${encodeURIComponent(id)}/image?rarity=${encodeURIComponent(rarity || "")}`, {
            method: "POST",
            body: formData,
        });
    },
};
