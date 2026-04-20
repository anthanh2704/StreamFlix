// StreamFlix demo frontend — talks to the Spring Boot backend

const API_BASE = "http://localhost:8080/api";
let credentials = null;          // { username, basic: "Basic base64(...)" }
let currentVideoId = null;

// Utilities
const $ = (q) => document.querySelector(q);
const $$ = (q) => document.querySelectorAll(q);

function toast(msg, cls = "") {
    const t = $("#toast");
    t.textContent = msg;
    t.className = "toast show " + cls;
    setTimeout(() => t.className = "toast", 2500);
}
function showModal(id) { $("#" + id).classList.add("open"); }
function hideModal(id) { $("#" + id).classList.remove("open"); }

async function http(method, path, body) {
    const headers = { "Content-Type": "application/json" };
    if (credentials) headers.Authorization = credentials.basic;
    const res = await fetch(API_BASE + path, {
        method, headers,
        body: body ? JSON.stringify(body) : undefined,
    });
    let data = null;
    try { data = await res.json(); } catch { }
    if (!res.ok) {
        const msg = data?.message || res.statusText;
        throw new Error(msg);
    }
    return data;
}

function setAuth(username, password) {
    credentials = { username, basic: "Basic " + btoa(username + ":" + password) };
    $("#authStatus").textContent = "Signed in as " + username;
    $("#signInBtn").hidden = true;
    $("#registerBtn").hidden = true;
    $("#signOutBtn").hidden = false;
    $("#commentBox").hidden = false;
}
function clearAuth() {
    credentials = null;
    $("#authStatus").textContent = "Not signed in";
    $("#signInBtn").hidden = false;
    $("#registerBtn").hidden = false;
    $("#signOutBtn").hidden = true;
    $("#commentBox").hidden = true;
}

// API calls
const api = {
    async signIn() {
        const user = $("#liUser").value.trim();
        const pass = $("#liPass").value;
        if (!user || !pass) return toast("Enter username & password", "error");
        // try it by pulling /auth/me with basic creds
        credentials = { username: user, basic: "Basic " + btoa(user + ":" + pass) };
        try {
            const r = await http("GET", "/auth/me");
            setAuth(user, pass);
            hideModal("signInModal");
            toast("Welcome, " + r.data.username, "success");
            render();
        } catch (e) {
            credentials = null;
            toast("Sign in failed: " + e.message, "error");
        }
    },

    async register() {
        const body = {
            username: $("#regUser").value.trim(),
            email: $("#regEmail").value.trim(),
            password: $("#regPass").value,
            fullName: $("#regFull").value.trim() || null,
            country: $("#regCountry").value.trim() || null,
        };
        try {
            await http("POST", "/auth/register", body);
            hideModal("registerModal");
            setAuth(body.username, body.password);
            toast("Registered — you're signed in!", "success");
            render();
        } catch (e) {
            toast("Register failed: " + e.message, "error");
        }
    },

    async listVideos() { return (await http("GET", "/videos?page=0&size=24")).data; },
    async trending() { return (await http("GET", "/videos/trending?days=30&limit=12")).data; },
    async recommended() { return (await http("GET", "/videos/recommendations?limit=12")).data; },
    async history() { return (await http("GET", "/users/me/history?page=0&size=20")).data; },
    async channels() { return (await http("GET", "/channels")).data; },
    async search(q) { return (await http("GET", "/videos/search?q=" + encodeURIComponent(q) + "&size=24")).data; },

    async openVideo(id) {
        currentVideoId = id;
        const video = (await http("GET", "/videos/" + id)).data;
        $("#pTitle").textContent = video.title;
        $("#pChannel").textContent = "📺 " + (video.channelName || "—");
        $("#pStats").textContent =
            `${video.viewsCount} views • 👍 ${video.likesCount} • 👎 ${video.dislikesCount}`;
        $("#pDesc").textContent = video.description || "(no description)";
        showModal("playerModal");
        api.loadComments(id);
    },

    async loadComments(videoId) {
        const list = $("#commentList");
        list.innerHTML = '<p class="muted">Loading...</p>';
        try {
            const page = (await http("GET", `/comments/video/${videoId}`)).data;
            const items = page.content || [];
            if (!items.length) { list.innerHTML = '<p class="muted">No comments yet.</p>'; return; }
            list.innerHTML = items.map(c =>
                `<div class="comment">
                   <div class="comment-head"><strong>${c.username}</strong> • ${new Date(c.createdAt).toLocaleString()}</div>
                   <div>${escapeHtml(c.content)}</div>
                 </div>`
            ).join("");
        } catch (e) {
            list.innerHTML = '<p class="muted">Comments unavailable.</p>';
        }
    },

    async postComment(videoId) {
        const content = $("#newComment").value.trim();
        if (!content) return;
        try {
            await http("POST", `/comments/video/${videoId}`, { content, parentCommentId: null });
            $("#newComment").value = "";
            api.loadComments(videoId);
            toast("Posted!", "success");
        } catch (e) { toast(e.message, "error"); }
    },

    async react(videoId, kind) {
        if (!credentials) return toast("Sign in first", "error");
        try {
            const r = await http("POST", `/videos/${videoId}/${kind.toLowerCase()}`);
            toast("Reaction: " + r.data, "success");
        } catch (e) { toast(e.message, "error"); }
    },

    async recordWatch(videoId) {
        if (!credentials) return toast("Sign in first", "error");
        try {
            await http("POST", `/videos/${videoId}/watch`,
                { watchDuration: 120, progressPct: 50.0, deviceType: "WEB" });
            toast("View recorded (trigger incremented views_count)", "success");
        } catch (e) { toast(e.message, "error"); }
    },
};

// Rendering
function escapeHtml(s) {
    return (s || "").replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
function videoCard(v) {
    return `<div class="card" onclick="api.openVideo(${v.videoId})">
      <div class="thumb">🎬</div>
      <div class="card-body">
        <div class="card-title">${escapeHtml(v.title)}</div>
        <div class="card-meta"><span>${escapeHtml(v.channelName || '')}</span>
          <span>${v.viewsCount ?? 0} views</span></div>
      </div>
    </div>`;
}

let currentView = "home";

async function render() {
    const main = $("#main");
    main.innerHTML = '<p class="muted">Loading...</p>';
    try {
        if (currentView === "home") {
            const page = await api.listVideos();
            const items = page.content || [];
            main.innerHTML = `<h2>Browse Videos</h2>
                              <div class="grid">${items.map(videoCard).join("") || '<p class="empty">No videos yet.</p>'}</div>`;
        }
        else if (currentView === "trending") {
            const items = await api.trending();
            main.innerHTML = `<h2>🔥 Trending</h2>
                              <div class="grid">${items.map(videoCard).join("") || '<p class="empty">Nothing trending.</p>'}</div>`;
        }
        else if (currentView === "recommended") {
            if (!credentials) { main.innerHTML = '<p class="empty">Sign in to see personalised recommendations.</p>'; return; }
            const items = await api.recommended();
            main.innerHTML = `<h2>✨ Recommended For You</h2>
                              <div class="grid">${items.map(videoCard).join("") || '<p class="empty">Watch more videos to get recommendations.</p>'}</div>`;
        }
        else if (currentView === "history") {
            if (!credentials) { main.innerHTML = '<p class="empty">Sign in to see your history.</p>'; return; }
            const page = await api.history();
            const items = page.content || [];
            main.innerHTML = `<h2>📜 Watch History</h2>
                              <div class="grid">${items.map(h =>
                videoCard(h.video).replace('</div></div>',
                    `<div class="muted" style="font-size:12px;margin-top:4px">
                                       Watched ${new Date(h.watchedAt).toLocaleString()}</div></div></div>`))
                    .join("") || '<p class="empty">No history yet.</p>'}</div>`;
        }
        else if (currentView === "channels") {
            const items = await api.channels();
            main.innerHTML = `<h2>📺 Channels</h2>
                              <div class="grid">${items.map(c =>
                `<div class="channel-card">
                                     <h3>${escapeHtml(c.name)}</h3>
                                     <div class="muted">by ${escapeHtml(c.ownerUsername || '')}</div>
                                     <div class="stats">${c.subscriberCount} subscribers</div>
                                     <p>${escapeHtml(c.description || '')}</p>
                                   </div>`).join("") || '<p class="empty">No channels.</p>'}</div>`;
        }
    } catch (e) {
        main.innerHTML = `<p class="empty">⚠️ ${escapeHtml(e.message)}<br/><small>Make sure the backend is running on <code>http://localhost:8080</code>.</small></p>`;
    }
}

// Wire up events
$$(".tab").forEach(t => t.addEventListener("click", () => {
    $$(".tab").forEach(x => x.classList.remove("active"));
    t.classList.add("active");
    currentView = t.dataset.view;
    render();
}));

$("#signInBtn").onclick = () => showModal("signInModal");
$("#registerBtn").onclick = () => showModal("registerModal");
$("#signOutBtn").onclick = () => { clearAuth(); render(); toast("Signed out"); };

let searchTimer;
$("#search").addEventListener("input", (e) => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(async () => {
        const q = e.target.value.trim();
        if (!q) { render(); return; }
        try {
            const page = await api.search(q);
            const items = page.content || [];
            $("#main").innerHTML = `<h2>Search: "${escapeHtml(q)}"</h2>
                                    <div class="grid">${items.map(videoCard).join("") || '<p class="empty">Nothing matched.</p>'}</div>`;
        } catch (e) { toast(e.message, "error"); }
    }, 300);
});

render();
