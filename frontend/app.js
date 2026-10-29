
const API = "http://localhost:8090/api";
const TOKEN_URL = "http://localhost:8090/dev/token";

let invoices = [];
let usageRows = [];
let allocations = [];
let workflows = [];
let selectedInvoiceId = null;
let token = "";
let tokenRole = "";

const $ = id => document.getElementById(id);

const money = value =>
    "₹" + Number(value || 0).toLocaleString("en-IN", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

function notice(message, type = "success") {
    const element = $("notice");

    if (!element) {
        alert(message);
        return;
    }

    element.textContent = message;
    element.className = "notice " + type;
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function clearNotice() {
    if ($("notice")) {
        $("notice").className = "notice hidden";
    }
}

function esc(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

async function getToken(role = "facilityManager") {
    const response = await fetch(
        `${TOKEN_URL}?role=${encodeURIComponent(role)}`
    );

    if (!response.ok) {
        throw new Error(`Token request failed: HTTP ${response.status}`);
    }

    const text = (await response.text()).trim();
    let data;

    try {
        data = JSON.parse(text);
    } catch {
        data = text;
    }

    if (typeof data === "string") {
        token = data;
    } else {
        token = data.token || data.accessToken || data.access_token || "";
    }

    if (!token) {
        throw new Error("The token endpoint returned no token.");
    }

    tokenRole = role;
    return token;
}

async function request(path, options = {}, role = "facilityManager") {
    if (!token || tokenRole !== role) {
        await getToken(role);
    }

    const send = () => fetch(API + path, {
        ...options,
        headers: {
            ...(options.body
                ? { "Content-Type": "application/json" }
                : {}),
            ...(options.headers || {}),
            Authorization: `Bearer ${token}`
        }
    });

    let response = await send();

    if (response.status === 401) {
        token = "";
        await getToken(role);
        response = await send();
    }

    const text = await response.text();
    let body;

    try {
        body = text ? JSON.parse(text) : {};
    } catch {
        body = { message: text };
    }

    if (!response.ok) {
        throw new Error(
            body.message ||
            body.error ||
            `HTTP ${response.status} for ${path}`
        );
    }

    return body;
}

function normalizeInvoice(invoice) {
    return {
        id: invoice.id,
        invoiceNumber: invoice.invoiceNumber || invoice.invoiceId || "",
        provider: invoice.provider || "",
        period: invoice.period || invoice.billingPeriod || "",
        totalAmount: Number(invoice.totalAmount || 0),
        commonAreaPercent: Number(invoice.commonAreaPercent || 0),
        status: invoice.status || "PENDING"
    };
}

async function refreshAll() {
    clearNotice();

    try {
        const invoiceData = await request("/invoices");

        invoices = (Array.isArray(invoiceData) ? invoiceData : [])
            .map(normalizeInvoice);

        if (!invoices.some(
            item => String(item.id) === String(selectedInvoiceId)
        )) {
            selectedInvoiceId = invoices.length
                ? invoices[0].id
                : null;
        }

        if ($("invoiceCount")) {
            $("invoiceCount").textContent = invoices.length;
        }

        renderInvoices();
        renderSummary();

        await loadUsage();
        renderAllocations(allocations);
        await loadEvents();

        clearNotice();
    } catch (error) {
        notice("Could not load data: " + error.message, "error");
    }
}

function renderInvoices() {
    const host = $("invoiceList");
    if (!host) return;

    if (!invoices.length) {
        host.innerHTML =
            '<div class="empty">No invoices found.</div>';
        return;
    }

    host.innerHTML = invoices.map(invoice => `
        <div class="invoice-card ${
            String(invoice.id) === String(selectedInvoiceId)
                ? "selected"
                : ""
        }" data-id="${esc(invoice.id)}">
            <div>
                <strong>${esc(invoice.invoiceNumber)}</strong>
                <small>${esc(invoice.provider)} · ${esc(invoice.period)}
                · ${invoice.commonAreaPercent}% common area</small>
            </div>
            <div class="amount">
                ${money(invoice.totalAmount)}
                <small>${esc(invoice.status)}</small>
            </div>
        </div>
    `).join("");

    host.querySelectorAll("[data-id]").forEach(element => {
        element.addEventListener("click", () => {
            selectedInvoiceId = element.dataset.id;
            renderInvoices();
            renderSummary();
        });
    });
}

async function loadUsage() {
    const data = await request("/usage");

    usageRows = (Array.isArray(data) ? data : []).map(row => ({
        id: row.id,
        departmentCode: row.departmentCode || "",
        departmentName: row.department || row.departmentName || "",
        meterUnits: Number(row.usageUnits ?? row.meterUnits ?? 0)
    }));

    renderUsage(usageRows);
}

function usageRow(code = "", name = "", units = "") {
    return `
        <div class="dept-row">
            <label>Code
                <input class="dept-code" value="${esc(code)}"
                    placeholder="ENG">
            </label>
            <label>Department
                <input class="dept-name" value="${esc(name)}" required>
            </label>
            <label>Meter units
                <input class="dept-units" type="number" min="0"
                    step="0.001" value="${esc(units)}" required>
            </label>
            <button class="remove-btn" type="button">×</button>
        </div>
    `;
}

function renderUsage(rows) {
    const host = $("usageRows");
    if (!host) return;

    host.innerHTML = rows.length
        ? rows.map(row => usageRow(
            row.departmentCode,
            row.departmentName,
            row.meterUnits
        )).join("")
        : '<div class="empty">No department usage found.</div>';

    host.querySelectorAll(".remove-btn").forEach(button => {
        button.addEventListener("click", () => {
            button.closest(".dept-row").remove();
        });
    });
}

function renderAllocations(rows) {
    const tbody = $("allocationRows");
    const foot = $("allocationFoot");

    if (!tbody || !foot) return;

    if (!rows.length) {
        tbody.innerHTML =
            '<tr><td colspan="5" class="empty">' +
            'Calculate costs using Allocate costs.</td></tr>';

        foot.innerHTML = "";

        if ($("reconcileBadge")) {
            $("reconcileBadge").className = "badge muted";
            $("reconcileBadge").textContent = "Not allocated";
        }

        return;
    }

    const latest = rows[rows.length - 1];

    const departments = latest.departmentAllocations
        ? Object.entries(latest.departmentAllocations)
        : [
            ["Engineering", latest.engineeringCost],
            ["HR", latest.hrCost],
            ["Operations", latest.operationsCost]
        ];

    tbody.innerHTML = departments.map(([name, value]) => `
        <tr>
            <td><strong>${esc(name)}</strong></td>
            <td>—</td>
            <td>—</td>
            <td>—</td>
            <td><strong>${money(value)}</strong></td>
        </tr>
    `).join("");

    foot.innerHTML = `
        <tr>
            <td colspan="4">Allocated total / invoice total</td>
            <td>${money(latest.allocatedTotal)} /
                ${money(latest.totalAmount)}</td>
        </tr>
    `;

    if ($("reconcileBadge")) {
        $("reconcileBadge").className =
            "badge " + (latest.reconciled ? "good" : "muted");

        $("reconcileBadge").textContent =
            latest.reconciled ? "✓ Reconciled" : "Check total";
    }
}

async function loadEvents() {
    const host = $("eventList");
    if (!host) return;

    const data = await request("/workflows");
    workflows = Array.isArray(data) ? data : [];

    host.innerHTML = workflows.length
        ? workflows.map(event => `
            <div class="event-item">
                <div class="event-icon">✓</div>
                <div>
                    <strong>${esc(event.eventType)} ·
                        ${esc(event.workflowStatus)}</strong>
                    <p>Invoice: ${esc(event.invoiceId || "—")}</p>
                    <p>${esc(event.details || "")}</p>
                </div>
            </div>
        `).join("")
        : '<div class="empty">No workflow records yet.</div>';
}

function renderSummary() {
    const invoice = invoices.find(
        item => String(item.id) === String(selectedInvoiceId)
    );

    if ($("selectedTotal")) {
        $("selectedTotal").textContent =
            invoice ? money(invoice.totalAmount) : "₹—";
    }

    if ($("selectedInvoiceLabel")) {
        $("selectedInvoiceLabel").textContent =
            invoice ? invoice.invoiceNumber : "Choose an invoice";
    }

    if ($("selectedStatus")) {
        $("selectedStatus").textContent =
            invoice ? invoice.status : "—";
    }
}

if ($("refreshBtn")) {
    $("refreshBtn").addEventListener("click", refreshAll);
}

if ($("eventsBtn")) {
    $("eventsBtn").addEventListener("click", () => {
        loadEvents().catch(error => notice(error.message, "error"));
    });
}

if ($("addDeptBtn")) {
    $("addDeptBtn").addEventListener("click", () => {
        if (!$("usageRows")) return;

        $("usageRows").insertAdjacentHTML("beforeend", usageRow());

        $("usageRows").lastElementChild
            ?.querySelector(".remove-btn")
            ?.addEventListener("click", event => {
                event.currentTarget.closest(".dept-row").remove();
            });
    });
}

if ($("usageForm")) {
    $("usageForm").addEventListener("submit", event => {
        event.preventDefault();
        notice(
            "Usage editing is not supported by the current Usage Service API.",
            "error"
        );
    });
}

if ($("invoiceForm")) {
    $("invoiceForm").addEventListener("submit", event => {
        event.preventDefault();
        notice(
            "Invoice creation is not supported by the current Invoice Service API.",
            "error"
        );
    });
}

if ($("allocateBtn")) {
    $("allocateBtn").addEventListener("click", async () => {
        const invoice = invoices.find(
            item => String(item.id) === String(selectedInvoiceId)
        );

        if (!invoice) {
            notice("Select an invoice first.", "error");
            return;
        }

        const button = $("allocateBtn");
        if (button.disabled) return;

        button.disabled = true;

        try {
            const result = await request(
                "/allocations/calculate",
                {
                    method: "POST",
                    body: JSON.stringify({
                        invoiceId: invoice.invoiceNumber,
                        totalAmount: invoice.totalAmount,
                        commonAreaPercent: invoice.commonAreaPercent
                    })
                },
                "accountant"
            );

            allocations = [result];
            renderAllocations(allocations);

            // Workflow records are created by the Kafka consumer.
            // Do not POST to /workflows/update here.
            await loadEvents();

            notice(
                `Allocation calculated: ${money(result.allocatedTotal)}. ` +
                `Reconciled: ${result.reconciled}.`,
                "success"
            );
        } catch (error) {
            notice("Allocation failed: " + error.message, "error");
        } finally {
            button.disabled = false;
        }
    });
}

refreshAll();
