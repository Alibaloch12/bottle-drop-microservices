"use strict";

/*
 * Bar Management UI
 *
 * The page is served by the same Spring Boot application as
 * the REST API. Therefore, all URLs are relative and no CORS
 * configuration is required.
 *
 * User and role checks have been removed from Bar Management.
 * Public Bar CRUD endpoints no longer send X-User-Id.
 * Internal simulation endpoints still send X-Internal-Api-Key.
 */

const state = {
    bars: [],
    selectedBar: null
};

const elements = {
    configForm:
        document.getElementById("config-form"),

    actorId:
        document.getElementById("actor-id"),

    internalApiKey:
        document.getElementById("internal-api-key"),

    serviceStatus:
        document.getElementById("service-status"),

    serviceStatusDot:
        document.getElementById("service-status-dot"),

    message:
        document.getElementById("message"),

    refreshBars:
        document.getElementById("refresh-bars"),

    barsBody:
        document.getElementById("bars-body"),

    barForm:
        document.getElementById("bar-form"),

    barFormTitle:
        document.getElementById("bar-form-title"),

    barId:
        document.getElementById("bar-id"),

    barName:
        document.getElementById("bar-name"),

    barDescription:
        document.getElementById("bar-description"),

    barLocation:
        document.getElementById("bar-location"),

    barSubmit:
        document.getElementById("bar-submit"),

    barReset:
        document.getElementById("bar-reset"),

    selectedPlaceholder:
        document.getElementById("selected-placeholder"),

    selectedContent:
        document.getElementById("selected-content"),

    selectedBarTitle:
        document.getElementById("selected-bar-title"),

    selectedBarMeta:
        document.getElementById("selected-bar-meta"),

    stocksBody:
        document.getElementById("stocks-body"),

    stockForm:
        document.getElementById("stock-form"),

    stockFormTitle:
        document.getElementById("stock-form-title"),

    stockEditId:
        document.getElementById("stock-edit-id"),

    stockTypeId:
        document.getElementById("stock-type-id"),

    stockName:
        document.getElementById("stock-name"),

    stockPrice:
        document.getElementById("stock-price"),

    stockQuantity:
        document.getElementById("stock-quantity"),

    stockThreshold:
        document.getElementById("stock-threshold"),

    stockSubmit:
        document.getElementById("stock-submit"),

    stockReset:
        document.getElementById("stock-reset"),

    availabilityForm:
        document.getElementById("availability-form"),

    availabilityTypeId:
        document.getElementById("availability-type-id"),

    availabilityOutput:
        document.getElementById("availability-output"),

    saleForm:
        document.getElementById("sale-form"),

    salePurchaseId:
        document.getElementById("sale-purchase-id"),

    saleVisitorId:
        document.getElementById("sale-visitor-id"),

    saleTypeId:
        document.getElementById("sale-type-id"),

    saleQuantity:
        document.getElementById("sale-quantity"),

    generatePurchaseId:
        document.getElementById("generate-purchase-id"),

    saleOutput:
        document.getElementById("sale-output"),

    deliveryForm:
        document.getElementById("delivery-form"),

    deliveryRequestId:
        document.getElementById("delivery-request-id"),

    /*
     * Optional field. If your HTML does not have this input,
     * the UI will use the Bar replenishment request ID as a
     * simulated warehouseReplenishmentId.
     */
    deliveryWarehouseRequestId:
        document.getElementById("delivery-warehouse-request-id"),

    deliveryTypeId:
        document.getElementById("delivery-type-id"),

    deliveryQuantity:
        document.getElementById("delivery-quantity"),

    deliveryOutput:
        document.getElementById("delivery-output"),

    refreshRequests:
        document.getElementById("refresh-requests"),

    requestsBody:
        document.getElementById("requests-body")
};

document.addEventListener(
    "DOMContentLoaded",
    initialize
);

/* =========================================================
   INITIALIZATION
   ========================================================= */

async function initialize() {
    restoreConfiguration();
    bindEvents();
    generatePurchaseId();

    await checkHealth();
    await loadBars();
}

function bindEvents() {
    elements.configForm.addEventListener(
        "submit",
        saveConfiguration
    );

    elements.refreshBars.addEventListener(
        "click",
        loadBars
    );

    elements.barForm.addEventListener(
        "submit",
        submitBarForm
    );

    elements.barReset.addEventListener(
        "click",
        resetBarForm
    );

    elements.stockForm.addEventListener(
        "submit",
        submitStockForm
    );

    elements.stockReset.addEventListener(
        "click",
        resetStockForm
    );

    elements.availabilityForm.addEventListener(
        "submit",
        checkAvailability
    );

    elements.saleForm.addEventListener(
        "submit",
        recordSale
    );

    elements.generatePurchaseId.addEventListener(
        "click",
        generatePurchaseId
    );

    elements.deliveryForm.addEventListener(
        "submit",
        receiveDelivery
    );

    elements.refreshRequests.addEventListener(
        "click",
        loadReplenishmentRequests
    );
}

/* =========================================================
   CONFIGURATION
   ========================================================= */

function restoreConfiguration() {
    /*
     * X-User-Id is no longer required. The field may still
     * exist in the HTML, but this script does not use it.
     */
    if (elements.actorId) {
        elements.actorId.value = "";
        elements.actorId.disabled = true;
        elements.actorId.placeholder = "Not required";
    }

    elements.internalApiKey.value =
        localStorage.getItem("bar.internalApiKey")
        ?? "dev-internal-key";
}

async function saveConfiguration(event) {
    event.preventDefault();

    localStorage.setItem(
        "bar.internalApiKey",
        elements.internalApiKey.value.trim()
    );

    showMessage(
        "Configuration saved.",
        "success"
    );

    await loadBars();
}

/* =========================================================
   HEALTH CHECK
   ========================================================= */

async function checkHealth() {
    elements.serviceStatusDot.classList.remove(
        "online",
        "offline"
    );

    try {
        const response = await fetch(
            "/actuator/health"
        );

        const data = await response.json();

        if (response.ok && data.status === "UP") {
            elements.serviceStatus.textContent =
                "Bar service online";

            elements.serviceStatusDot.classList.add(
                "online"
            );

            return;
        }

        throw new Error(
            "Health endpoint did not return UP."
        );

    } catch (error) {
        elements.serviceStatus.textContent =
            "Bar service offline";

        elements.serviceStatusDot.classList.add(
            "offline"
        );
    }
}

/* =========================================================
   HTTP HELPERS
   ========================================================= */

function internalHeaders() {
    const apiKey =
        elements.internalApiKey.value.trim();

    if (!apiKey) {
        throw new Error(
            "Enter the internal API key first."
        );
    }

    return {
        "X-Internal-Api-Key": apiKey
    };
}

async function apiRequest(
    url,
    {
        method = "GET",
        body = null,
        authorization = "none"
    } = {}
) {
    const headers = {};

    if (authorization === "internal") {
        Object.assign(
            headers,
            internalHeaders()
        );
    }

    if (body !== null) {
        headers["Content-Type"] =
            "application/json";
    }

    const response = await fetch(url, {
        method: method,
        headers: headers,
        body:
            body === null
                ? undefined
                : JSON.stringify(body)
    });

    if (response.status === 204) {
        return null;
    }

    const contentType =
        response.headers.get("content-type") || "";

    let responseBody;

    if (
        contentType.indexOf("application/json") !== -1 ||
        contentType.indexOf("application/problem+json") !== -1
    ) {
        responseBody = await response.json();
    } else {
        responseBody = await response.text();
    }

    if (!response.ok) {
        let detail = null;

        if (
            responseBody &&
            typeof responseBody === "object"
        ) {
            detail =
                responseBody.detail ||
                responseBody.message ||
                responseBody.title;
        }

        if (!detail) {
            detail =
                responseBody ||
                "HTTP " + response.status;
        }

        throw new Error(
            typeof detail === "string"
                ? detail
                : JSON.stringify(detail)
        );
    }

    return responseBody;
}

/* =========================================================
   BAR CRUD
   ========================================================= */

async function loadBars() {
    try {
        state.bars = await apiRequest(
            "/api/v1/bars"
        );

        renderBars();

        if (state.selectedBar) {
            const stillExists =
                state.bars.some(
                    bar =>
                        bar.id ===
                        state.selectedBar.id
                );

            if (stillExists) {
                await selectBar(
                    state.selectedBar.id
                );
            } else {
                clearSelectedBar();
            }
        }

    } catch (error) {
        showMessage(
            error.message,
            "error"
        );
    }
}

function renderBars() {
    elements.barsBody.replaceChildren();

    if (!state.bars || state.bars.length === 0) {
        elements.barsBody.appendChild(
            createEmptyRow(
                4,
                "No bars have been created."
            )
        );

        return;
    }

    for (const bar of state.bars) {
        const row =
            document.createElement("tr");

        if (
            state.selectedBar &&
            state.selectedBar.id === bar.id
        ) {
            row.classList.add("selected-row");
        }

        row.appendChild(
            createCell(bar.name)
        );

        row.appendChild(
            createCell(bar.location)
        );

        row.appendChild(
            createCell(
                String(bar.stocks?.length ?? 0)
            )
        );

        const actionsCell =
            document.createElement("td");

        const actions =
            document.createElement("div");

        actions.className =
            "table-actions";

        actions.append(
            createButton(
                "Select",
                "secondary compact",
                () => selectBar(bar.id)
            ),

            createButton(
                "Edit",
                "secondary compact",
                () => beginBarEdit(bar)
            ),

            createButton(
                "Delete",
                "danger compact",
                () => deleteBar(bar)
            )
        );

        actionsCell.appendChild(actions);
        row.appendChild(actionsCell);

        elements.barsBody.appendChild(row);
    }
}

async function selectBar(barId) {
    try {
        state.selectedBar =
            await apiRequest(
                `/api/v1/bars/${barId}`
            );

        renderSelectedBar();
        renderBars();

        await loadReplenishmentRequests();

        const firstStock =
            state.selectedBar.stocks?.[0];

        if (firstStock) {
            elements.saleTypeId.value =
                firstStock.beverageTypeId;

            elements.availabilityTypeId.value =
                firstStock.beverageTypeId;
        }

    } catch (error) {
        showMessage(
            error.message,
            "error"
        );
    }
}

async function submitBarForm(event) {
    event.preventDefault();

    const barId =
        elements.barId.value.trim();

    const request = {
        name:
            elements.barName.value.trim(),

        description:
            elements.barDescription.value.trim(),

        location:
            elements.barLocation.value.trim()
    };

    try {
        let savedBar;

        if (barId) {
            savedBar = await apiRequest(
                `/api/v1/bars/${barId}`,
                {
                    method: "PUT",
                    body: request
                }
            );

            showMessage(
                "Bar updated.",
                "success"
            );

        } else {
            savedBar = await apiRequest(
                "/api/v1/bars",
                {
                    method: "POST",
                    body: request
                }
            );

            showMessage(
                "Bar created.",
                "success"
            );
        }

        resetBarForm();

        await loadBars();
        await selectBar(savedBar.id);

    } catch (error) {
        showMessage(
            error.message,
            "error"
        );
    }
}

function beginBarEdit(bar) {
    elements.barId.value =
        bar.id;

    elements.barName.value =
        bar.name;

    elements.barDescription.value =
        bar.description ?? "";

    elements.barLocation.value =
        bar.location;

    elements.barFormTitle.textContent =
        "Edit bar";

    elements.barSubmit.textContent =
        "Update bar";

    elements.barForm.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}

function resetBarForm() {
    elements.barForm.reset();

    elements.barId.value = "";

    elements.barFormTitle.textContent =
        "Create bar";

    elements.barSubmit.textContent =
        "Create bar";
}

async function deleteBar(bar) {
    const confirmed =
        window.confirm(
            `Delete "${bar.name}" and all of its stock entries?`
        );

    if (!confirmed) {
        return;
    }

    try {
        await apiRequest(
            `/api/v1/bars/${bar.id}`,
            {
                method: "DELETE"
            }
        );

        if (
            state.selectedBar &&
            state.selectedBar.id === bar.id
        ) {
            clearSelectedBar();
        }

        showMessage(
            "Bar deleted.",
            "success"
        );

        await loadBars();

    } catch (error) {
        showMessage(
            error.message,
            "error"
        );
    }
}

/* =========================================================
   SELECTED BAR AND STOCK TABLE
   ========================================================= */

function renderSelectedBar() {
    const bar = state.selectedBar;

    if (!bar) {
        clearSelectedBar();
        return;
    }

    elements.selectedPlaceholder.classList.add(
        "hidden"
    );

    elements.selectedContent.classList.remove(
        "hidden"
    );

    elements.selectedBarTitle.textContent =
        bar.name;

    elements.selectedBarMeta.textContent =
        `${bar.location} · ${
            bar.description ||
            "No description"
        }`;

    elements.stocksBody.replaceChildren();

    if (!bar.stocks || bar.stocks.length === 0) {
        elements.stocksBody.appendChild(
            createEmptyRow(
                7,
                "No beverage stock entries exist."
            )
        );

        return;
    }

    for (const stock of bar.stocks) {
        const row =
            document.createElement("tr");

        row.appendChild(
            createCell(stock.beverageName)
        );

        row.appendChild(
            createCell(
                String(stock.beverageTypeId)
            )
        );

        const priceCell =
            createCell(
                formatMoney(stock.price)
            );

        priceCell.classList.add("price");

        row.appendChild(priceCell);

        row.appendChild(
            createCell(
                String(stock.quantity)
            )
        );

        row.appendChild(
            createCell(
                String(stock.restockThreshold)
            )
        );

        const statusCell =
            document.createElement("td");

        const badge =
            document.createElement("span");

        badge.className =
            `status-badge ${
                stock.belowThreshold
                    ? "warning"
                    : "ok"
            }`;

        badge.textContent =
            stock.belowThreshold
                ? "Restock needed"
                : "Stock OK";

        statusCell.appendChild(badge);
        row.appendChild(statusCell);

        const actionsCell =
            document.createElement("td");

        const actions =
            document.createElement("div");

        actions.className =
            "table-actions";

        actions.append(
            createButton(
                "Edit",
                "secondary compact",
                () => beginStockEdit(stock)
            ),

            createButton(
                "Remove",
                "danger compact",
                () => removeStock(stock)
            )
        );

        actionsCell.appendChild(actions);
        row.appendChild(actionsCell);

        elements.stocksBody.appendChild(row);
    }
}

function clearSelectedBar() {
    state.selectedBar = null;

    elements.selectedPlaceholder.classList.remove(
        "hidden"
    );

    elements.selectedContent.classList.add(
        "hidden"
    );

    elements.selectedBarTitle.textContent =
        "Selected bar";

    elements.selectedBarMeta.textContent =
        "Select a bar from the table.";

    elements.requestsBody.replaceChildren(
        createEmptyRow(
            8,
            "Select a bar to view replenishment requests."
        )
    );

    resetStockForm();
}

/* =========================================================
   STOCK CRUD
   ========================================================= */

async function submitStockForm(event) {
    event.preventDefault();

    if (!state.selectedBar) {
        showMessage(
            "Select a bar first.",
            "error"
        );

        return;
    }

    const editingId =
        elements.stockEditId.value.trim();

    try {
        let updatedBar;

        if (editingId) {
            updatedBar = await apiRequest(
                `/api/v1/bars/${state.selectedBar.id}` +
                `/stocks/${editingId}`,
                {
                    method: "PUT",

                    body: {
                        beverageName:
                            elements.stockName.value.trim(),

                        price:
                            elements.stockPrice.value.trim(),

                        quantity:
                            Number(
                                elements.stockQuantity.value
                            ),

                        restockThreshold:
                            Number(
                                elements.stockThreshold.value
                            )
                    }
                }
            );

            showMessage(
                "Stock entry updated.",
                "success"
            );

        } else {
            updatedBar = await apiRequest(
                `/api/v1/bars/${state.selectedBar.id}/stocks`,
                {
                    method: "POST",

                    body: {
                        beverageTypeId:
                            Number(
                                elements.stockTypeId.value
                            ),

                        beverageName:
                            elements.stockName.value.trim(),

                        price:
                            elements.stockPrice.value.trim(),

                        initialQuantity:
                            Number(
                                elements.stockQuantity.value
                            ),

                        restockThreshold:
                            Number(
                                elements.stockThreshold.value
                            )
                    }
                }
            );

            showMessage(
                "Stock entry added.",
                "success"
            );
        }

        state.selectedBar = updatedBar;

        resetStockForm();

        await loadBars();
        await selectBar(updatedBar.id);

    } catch (error) {
        showMessage(
            error.message,
            "error"
        );
    }
}

function beginStockEdit(stock) {
    elements.stockEditId.value =
        stock.beverageTypeId;

    elements.stockTypeId.value =
        stock.beverageTypeId;

    elements.stockName.value =
        stock.beverageName;

    elements.stockPrice.value =
        normalizePriceInput(stock.price);

    elements.stockQuantity.value =
        stock.quantity;

    elements.stockThreshold.value =
        stock.restockThreshold;

    elements.stockTypeId.disabled = true;

    elements.stockFormTitle.textContent =
        `Edit ${stock.beverageName} stock`;

    elements.stockSubmit.textContent =
        "Update stock";

    elements.stockForm.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}

function resetStockForm() {
    elements.stockForm.reset();

    elements.stockEditId.value = "";

    elements.stockTypeId.disabled = false;

    elements.stockPrice.value = "0.00";
    elements.stockQuantity.value = "0";
    elements.stockThreshold.value = "20";

    elements.stockFormTitle.textContent =
        "Add beverage stock";

    elements.stockSubmit.textContent =
        "Add stock";
}

async function removeStock(stock) {
    if (!state.selectedBar) {
        return;
    }

    const confirmed =
        window.confirm(
            `Remove ${stock.beverageName} from this bar?`
        );

    if (!confirmed) {
        return;
    }

    try {
        const updatedBar =
            await apiRequest(
                `/api/v1/bars/${state.selectedBar.id}` +
                `/stocks/${stock.beverageTypeId}`,
                {
                    method: "DELETE"
                }
            );

        state.selectedBar = updatedBar;

        showMessage(
            "Stock entry removed.",
            "success"
        );

        await loadBars();
        await selectBar(updatedBar.id);

    } catch (error) {
        showMessage(
            error.message,
            "error"
        );
    }
}

/* =========================================================
   AVAILABILITY
   ========================================================= */

async function checkAvailability(event) {
    event.preventDefault();

    if (!state.selectedBar) {
        showMessage(
            "Select a bar first.",
            "error"
        );

        return;
    }

    try {
        const beverageTypeId =
            Number(
                elements.availabilityTypeId.value
            );

        const result = await apiRequest(
            `/api/v1/bars/${state.selectedBar.id}` +
            `/availability/${beverageTypeId}`,
            {
                authorization: "internal"
            }
        );

        elements.availabilityOutput.textContent =
            JSON.stringify(
                result,
                null,
                2
            );

    } catch (error) {
        elements.availabilityOutput.textContent =
            error.message;

        showMessage(
            error.message,
            "error"
        );
    }
}

/* =========================================================
   SALE SIMULATION
   ========================================================= */

async function recordSale(event) {
    event.preventDefault();

    if (!state.selectedBar) {
        showMessage(
            "Select a bar first.",
            "error"
        );

        return;
    }

    try {
        const result = await apiRequest(
            `/api/v1/bars/${state.selectedBar.id}/sales`,
            {
                method: "POST",
                authorization: "internal",

                body: {
                    purchaseId:
                        Number(
                            elements.salePurchaseId.value
                        ),

                    visitorId:
                        Number(
                            elements.saleVisitorId.value
                        ),

                    items: [
                        {
                            beverageTypeId:
                                Number(
                                    elements.saleTypeId.value
                                ),

                            quantity:
                                Number(
                                    elements.saleQuantity.value
                                )
                        }
                    ]
                }
            }
        );

        elements.saleOutput.textContent =
            JSON.stringify(
                result,
                null,
                2
            );

        showMessage(
            `Sale recorded. Total price: ${
                formatMoney(result.totalPrice)
            }`,
            "success"
        );

        generatePurchaseId();

        await selectBar(
            state.selectedBar.id
        );

    } catch (error) {
        elements.saleOutput.textContent =
            error.message;

        showMessage(
            error.message,
            "error"
        );
    }
}

function generatePurchaseId() {
    /*
     * Backend now expects Long IDs, not UUIDs.
     * This creates a browser-generated numeric demo ID.
     */
    const generatedId =
        Math.floor(
            Date.now() * 1000 +
            Math.random() * 999
        );

    elements.salePurchaseId.value =
        String(generatedId);
}

/* =========================================================
   REPLENISHMENT REQUESTS
   ========================================================= */

async function loadReplenishmentRequests() {
    if (!state.selectedBar) {
        elements.requestsBody.replaceChildren(
            createEmptyRow(
                8,
                "Select a bar to view replenishment requests."
            )
        );

        return;
    }

    try {
        const requests = await apiRequest(
            `/api/v1/bars/${state.selectedBar.id}` +
            "/replenishment-requests"
        );

        renderReplenishmentRequests(
            requests
        );

    } catch (error) {
        showMessage(
            error.message,
            "error"
        );
    }
}

function renderReplenishmentRequests(
    requests
) {
    elements.requestsBody.replaceChildren();

    if (!requests || requests.length === 0) {
        elements.requestsBody.appendChild(
            createEmptyRow(
                8,
                "No replenishment requests exist."
            )
        );

        return;
    }

    for (const request of requests) {
        const row =
            document.createElement("tr");

        const idCell =
            createCell(request.id);

        idCell.classList.add("monospace");

        row.appendChild(idCell);

        row.appendChild(
            createCell(
                request.beverageName
            )
        );

        row.appendChild(
            createCell(
                String(
                    request.beverageTypeId
                )
            )
        );

        const priceCell =
            createCell(
                formatMoney(request.price)
            );

        priceCell.classList.add("price");

        row.appendChild(priceCell);

        row.appendChild(
            createCell(
                String(
                    request.quantityRequested
                )
            )
        );

        const statusCell =
            document.createElement("td");

        const statusBadge =
            document.createElement("span");

        statusBadge.className =
            `status-badge ${
                replenishmentStatusClass(
                    request.status
                )
            }`;

        statusBadge.textContent =
            request.status;

        statusCell.appendChild(
            statusBadge
        );

        row.appendChild(statusCell);

        row.appendChild(
            createCell(
                formatDate(
                    request.requestedAt
                )
            )
        );

        const actionCell =
            document.createElement("td");

        const canDeliver =
            request.status === "REQUESTED" ||
            request.status === "ACCEPTED";

        if (canDeliver) {
            actionCell.appendChild(
                createButton(
                    "Use for delivery",
                    "secondary compact",
                    () =>
                        prepareDelivery(
                            request
                        )
                )
            );
        } else {
            actionCell.textContent = "—";
        }

        row.appendChild(actionCell);

        elements.requestsBody.appendChild(
            row
        );
    }
}

function replenishmentStatusClass(status) {
    switch (status) {
        case "COMPLETED":
            return "ok";

        case "REJECTED":
            return "error";

        case "REQUESTED":
        case "ACCEPTED":
            return "info";

        default:
            return "warning";
    }
}

function prepareDelivery(request) {
    elements.deliveryRequestId.value =
        request.id;

    if (elements.deliveryWarehouseRequestId) {
        elements.deliveryWarehouseRequestId.value =
            request.warehouseRequestId ??
            request.id;
    }

    elements.deliveryTypeId.value =
        request.beverageTypeId;

    elements.deliveryQuantity.value =
        request.quantityRequested;

    elements.deliveryForm.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}

/* =========================================================
   DELIVERY SIMULATION
   ========================================================= */

async function receiveDelivery(event) {
    event.preventDefault();

    if (!state.selectedBar) {
        showMessage(
            "Select a bar first.",
            "error"
        );

        return;
    }

    try {
        const barReplenishmentRequestId =
            Number(
                elements.deliveryRequestId.value
            );

        const warehouseReplenishmentId =
            elements.deliveryWarehouseRequestId?.value
                ? Number(
                    elements.deliveryWarehouseRequestId.value
                )
                : barReplenishmentRequestId;

        const result = await apiRequest(
            `/api/v1/bars/${state.selectedBar.id}` +
            "/replenishment-fulfillments",
            {
                method: "POST",
                authorization: "internal",

                body: {
                    warehouseReplenishmentId:
                        warehouseReplenishmentId,

                    barReplenishmentRequestId:
                        barReplenishmentRequestId,

                    beverageTypeId:
                        Number(
                            elements.deliveryTypeId.value
                        ),

                    deliveredQuantity:
                        Number(
                            elements.deliveryQuantity.value
                        )
                }
            }
        );

        elements.deliveryOutput.textContent =
            JSON.stringify(
                result,
                null,
                2
            );

        showMessage(
            "Replenishment delivery recorded.",
            "success"
        );

        elements.deliveryForm.reset();
        elements.deliveryQuantity.value = "50";

        await selectBar(
            state.selectedBar.id
        );

    } catch (error) {
        elements.deliveryOutput.textContent =
            error.message;

        showMessage(
            error.message,
            "error"
        );
    }
}

/* =========================================================
   UI UTILITIES
   ========================================================= */

function showMessage(text, type = "info") {
    elements.message.textContent = text;

    elements.message.className =
        `message ${type}`;

    elements.message.classList.remove(
        "hidden"
    );

    window.clearTimeout(
        showMessage.timeoutId
    );

    showMessage.timeoutId =
        window.setTimeout(
            () => {
                elements.message.classList.add(
                    "hidden"
                );
            },
            6000
        );
}

function createCell(text) {
    const cell =
        document.createElement("td");

    cell.textContent =
        text ?? "";

    return cell;
}

function createEmptyRow(
    columnCount,
    message
) {
    const row =
        document.createElement("tr");

    const cell =
        document.createElement("td");

    cell.colSpan = columnCount;
    cell.textContent = message;

    row.appendChild(cell);

    return row;
}

function createButton(
    label,
    classNames,
    clickHandler
) {
    const button =
        document.createElement("button");

    button.type = "button";

    button.className =
        `button ${classNames}`;

    button.textContent = label;

    button.addEventListener(
        "click",
        clickHandler
    );

    return button;
}

function formatDate(value) {
    if (!value) {
        return "—";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString();
}

function formatMoney(value) {
    if (
        value === null ||
        value === undefined ||
        value === ""
    ) {
        return "—";
    }

    const numberValue = Number(value);

    if (Number.isNaN(numberValue)) {
        return String(value);
    }

    return numberValue.toFixed(2);
}

function normalizePriceInput(value) {
    const numberValue = Number(value);

    if (Number.isNaN(numberValue)) {
        return "0.00";
    }

    return numberValue.toFixed(2);
}