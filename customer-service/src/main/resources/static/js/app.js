"use strict";

/*
 * Customer UI communicates only with Customer Service.
 *
 * Customer Service then communicates with:
 * - Bar Management for the catalogue
 * - Purchase Service for order creation
 */

const CURRENCY = "EUR";

const state = {
    catalog: null,
    cart: new Map(),
    loadingCatalog: false,
    checkingOut: false
};

const elements = {
    visitorId:
        document.getElementById("visitor-id"),

    barId:
        document.getElementById("bar-id"),

    loadProductsButton:
        document.getElementById(
            "load-products-button"
        ),

    refreshProductsButton:
        document.getElementById(
            "refresh-products-button"
        ),

    catalogMessage:
        document.getElementById(
            "catalog-message"
        ),

    barName:
        document.getElementById("bar-name"),

    barLocation:
        document.getElementById(
            "bar-location"
        ),

    productList:
        document.getElementById(
            "product-list"
        ),

    cartItems:
        document.getElementById(
            "cart-items"
        ),

    cartCount:
        document.getElementById(
            "cart-count"
        ),

    cartProductCount:
        document.getElementById(
            "cart-product-count"
        ),

    cartQuantityCount:
        document.getElementById(
            "cart-quantity-count"
        ),

    cartTotal:
        document.getElementById(
            "cart-total"
        ),

    checkoutButton:
        document.getElementById(
            "checkout-button"
        ),

    clearCartButton:
        document.getElementById(
            "clear-cart-button"
        ),

    orderResult:
        document.getElementById(
            "order-result"
        ),

    toast:
        document.getElementById("toast")
};

initialize();

function initialize() {
    elements.loadProductsButton
        .addEventListener(
            "click",
            loadCatalog
        );

    elements.refreshProductsButton
        .addEventListener(
            "click",
            loadCatalog
        );

    elements.checkoutButton
        .addEventListener(
            "click",
            checkout
        );

    elements.clearCartButton
        .addEventListener(
            "click",
            clearCart
        );

    elements.barId
        .addEventListener(
            "keydown",
            event => {
                if (event.key === "Enter") {
                    loadCatalog();
                }
            }
        );

    elements.visitorId
        .addEventListener(
            "keydown",
            event => {
                if (event.key === "Enter") {
                    loadCatalog();
                }
            }
        );

    initializeBarFromQueryParameter();
    renderCart();
}

function initializeBarFromQueryParameter() {
    const parameters =
        new URLSearchParams(
            window.location.search
        );

    const queryBarId =
        parameters.get("barId");

    if (!queryBarId) {
        return;
    }

    try {
        const barId =
            parsePositiveLong(
                queryBarId,
                "Bar ID"
            );

        elements.barId.value =
            String(barId);

        loadCatalog();

    } catch (error) {
        showToast(
            error.message,
            "error"
        );
    }
}

async function loadCatalog() {
    if (state.loadingCatalog) {
        return;
    }

    let barId;

    try {
        barId = parsePositiveLong(
            elements.barId.value,
            "Bar ID"
        );
    } catch (error) {
        setCatalogMessage(
            error.message,
            "error"
        );

        return;
    }

    setCatalogLoading(true);

    try {
        const response = await fetch(
            `/api/v1/customer/bars/${barId}/catalog`,
            {
                method: "GET",
                headers: {
                    Accept: "application/json"
                }
            }
        );

        const catalog =
            await readResponse(response);

        validateCatalog(catalog);

        state.catalog = catalog;

        elements.barName.textContent =
            catalog.barName;

        elements.barLocation.textContent =
            catalog.location;

        elements.refreshProductsButton.disabled =
            false;

        updateBarQueryParameter(
            catalog.barId
        );

        reconcileCartWithCatalog();
        renderProducts();
        renderCart();

        setCatalogMessage(
            `${catalog.products.length} products loaded.`,
            "success"
        );

    } catch (error) {
        state.catalog = null;

        renderCatalogError(
            error.message
        );

        setCatalogMessage(
            error.message,
            "error"
        );

    } finally {
        setCatalogLoading(false);
    }
}

function validateCatalog(catalog) {
    if (
        !catalog
        || !Number.isSafeInteger(
            Number(catalog.barId)
        )
        || !Array.isArray(
            catalog.products
        )
    ) {
        throw new Error(
            "The server returned an invalid catalogue."
        );
    }
}

function renderProducts() {
    elements.productList.innerHTML = "";

    if (
        !state.catalog
        || state.catalog.products.length === 0
    ) {
        elements.productList.innerHTML = `
            <div class="empty-state">
                <h3>No products found</h3>
                <p>
                    This bar currently has no products.
                </p>
            </div>
        `;

        return;
    }

    for (
        const product
        of state.catalog.products
    ) {
        const card =
            createProductCard(product);

        elements.productList.appendChild(
            card
        );
    }
}

function createProductCard(product) {
    const card =
        document.createElement("article");

    const availableQuantity =
        Number(product.availableQuantity);

    const isAvailable =
        Boolean(product.available)
        && availableQuantity > 0;

    card.className =
        isAvailable
            ? "product-card"
            : "product-card product-card--unavailable";

    const currentCartQuantity =
        state.cart.get(
            Number(product.beverageTypeId)
        )?.quantity ?? 0;

    const remainingAddable =
        Math.max(
            0,
            availableQuantity
                - currentCartQuantity
        );

    card.innerHTML = `
        <div class="product-card__header">
            <div>
                <h3>
                    ${escapeHtml(product.name)}
                </h3>
            </div>

            <span
                class="
                    stock-badge
                    ${
                        isAvailable
                            ? "stock-badge--available"
                            : "stock-badge--unavailable"
                    }
                "
            >
                ${
                    isAvailable
                        ? `${availableQuantity} available`
                        : "Sold out"
                }
            </span>
        </div>

        <p class="product-price">
            ${formatMoney(product.price)}
        </p>

        <div class="product-card__actions">
            <input
                class="product-quantity"
                type="number"
                min="1"
                max="${Math.max(1, remainingAddable)}"
                value="1"
                inputmode="numeric"
                aria-label="Quantity for ${escapeHtml(product.name)}"
                ${
                    !isAvailable
                    || remainingAddable === 0
                        ? "disabled"
                        : ""
                }
            >

            <button
                class="add-to-cart-button"
                type="button"
                ${
                    !isAvailable
                    || remainingAddable === 0
                        ? "disabled"
                        : ""
                }
            >
                ${
                    remainingAddable === 0
                        ? "Maximum in cart"
                        : "Add to cart"
                }
            </button>
        </div>
    `;

    const quantityInput =
        card.querySelector(
            ".product-quantity"
        );

    const addButton =
        card.querySelector(
            ".add-to-cart-button"
        );

    addButton.addEventListener(
        "click",
        () => {
            try {
                const quantity =
                    parsePositiveInteger(
                        quantityInput.value,
                        "Quantity"
                    );

                addProductToCart(
                    product,
                    quantity
                );

            } catch (error) {
                showToast(
                    error.message,
                    "error"
                );
            }
        }
    );

    return card;
}

function addProductToCart(
    product,
    quantityToAdd
) {
    const beverageTypeId =
        Number(product.beverageTypeId);

    const availableQuantity =
        Number(product.availableQuantity);

    const existing =
        state.cart.get(
            beverageTypeId
        );

    const existingQuantity =
        existing?.quantity ?? 0;

    const nextQuantity =
        existingQuantity
            + quantityToAdd;

    if (
        nextQuantity >
        availableQuantity
    ) {
        throw new Error(
            `Only ${availableQuantity} unit(s) of ` +
            `${product.name} are currently available.`
        );
    }

    state.cart.set(
        beverageTypeId,
        {
            beverageTypeId,
            name:
                String(product.name),
            unitPrice:
                Number(product.price),
            quantity:
                nextQuantity,
            availableQuantity
        }
    );

    renderCart();
    renderProducts();

    showToast(
        `${product.name} added to the cart.`,
        "success"
    );
}

function renderCart() {
    elements.cartItems.innerHTML = "";

    const cartValues =
        Array.from(
            state.cart.values()
        );

    if (cartValues.length === 0) {
        elements.cartItems.innerHTML = `
            <div class="empty-state empty-state--small">
                <p>Your cart is empty.</p>
            </div>
        `;
    } else {
        for (const item of cartValues) {
            elements.cartItems.appendChild(
                createCartItem(item)
            );
        }
    }

    updateCartSummary();
}

function createCartItem(item) {
    const container =
        document.createElement("article");

    container.className =
        "cart-item";

    const lineTotal =
        item.unitPrice
        * item.quantity;

    container.innerHTML = `
        <div class="cart-item__heading">
            <div>
                <h3>
                    ${escapeHtml(item.name)}
                </h3>

                <div class="cart-item__price">
                    ${formatMoney(item.unitPrice)}
                    each
                </div>
            </div>

            <div class="cart-item__line-total">
                ${formatMoney(lineTotal)}
            </div>
        </div>

        <div class="cart-item__controls">
            <button
                class="quantity-button decrease-button"
                type="button"
                aria-label="Decrease quantity"
            >
                −
            </button>

            <input
                class="quantity-input"
                type="number"
                min="1"
                max="${item.availableQuantity}"
                value="${item.quantity}"
                inputmode="numeric"
                aria-label="Cart quantity for ${escapeHtml(item.name)}"
            >

            <button
                class="quantity-button increase-button"
                type="button"
                aria-label="Increase quantity"
                ${
                    item.quantity
                        >= item.availableQuantity
                        ? "disabled"
                        : ""
                }
            >
                +
            </button>

            <button
                class="remove-button"
                type="button"
            >
                Remove
            </button>
        </div>
    `;

    const decreaseButton =
        container.querySelector(
            ".decrease-button"
        );

    const increaseButton =
        container.querySelector(
            ".increase-button"
        );

    const quantityInput =
        container.querySelector(
            ".quantity-input"
        );

    const removeButton =
        container.querySelector(
            ".remove-button"
        );

    decreaseButton.addEventListener(
        "click",
        () => {
            updateCartQuantity(
                item.beverageTypeId,
                item.quantity - 1
            );
        }
    );

    increaseButton.addEventListener(
        "click",
        () => {
            updateCartQuantity(
                item.beverageTypeId,
                item.quantity + 1
            );
        }
    );

    quantityInput.addEventListener(
        "change",
        () => {
            try {
                const quantity =
                    parsePositiveInteger(
                        quantityInput.value,
                        "Quantity"
                    );

                updateCartQuantity(
                    item.beverageTypeId,
                    quantity
                );

            } catch (error) {
                quantityInput.value =
                    String(item.quantity);

                showToast(
                    error.message,
                    "error"
                );
            }
        }
    );

    removeButton.addEventListener(
        "click",
        () => {
            removeCartItem(
                item.beverageTypeId
            );
        }
    );

    return container;
}

function updateCartQuantity(
    beverageTypeId,
    quantity
) {
    const item =
        state.cart.get(
            beverageTypeId
        );

    if (!item) {
        return;
    }

    if (quantity <= 0) {
        removeCartItem(
            beverageTypeId
        );

        return;
    }

    if (
        quantity >
        item.availableQuantity
    ) {
        showToast(
            `Only ${item.availableQuantity} unit(s) of ` +
            `${item.name} are currently available.`,
            "error"
        );

        return;
    }

    state.cart.set(
        beverageTypeId,
        {
            ...item,
            quantity
        }
    );

    renderCart();
    renderProducts();
}

function removeCartItem(
    beverageTypeId
) {
    const item =
        state.cart.get(
            beverageTypeId
        );

    state.cart.delete(
        beverageTypeId
    );

    renderCart();
    renderProducts();

    if (item) {
        showToast(
            `${item.name} removed from the cart.`
        );
    }
}

function clearCart() {
    state.cart.clear();

    renderCart();
    renderProducts();

    showToast(
        "Cart cleared."
    );
}

function updateCartSummary() {
    const items =
        Array.from(
            state.cart.values()
        );

    const productCount =
        items.length;

    const totalQuantity =
        items.reduce(
            (total, item) =>
                total + item.quantity,
            0
        );

    const totalPrice =
        items.reduce(
            (total, item) =>
                total
                + item.unitPrice
                * item.quantity,
            0
        );

    elements.cartCount.textContent =
        `${totalQuantity} ${
            totalQuantity === 1
                ? "item"
                : "items"
        }`;

    elements.cartProductCount.textContent =
        String(productCount);

    elements.cartQuantityCount.textContent =
        String(totalQuantity);

    elements.cartTotal.textContent =
        formatMoney(totalPrice);

    const isEmpty =
        state.cart.size === 0;

    elements.checkoutButton.disabled =
        isEmpty
        || state.checkingOut;

    elements.clearCartButton.disabled =
        isEmpty
        || state.checkingOut;
}

async function checkout() {
    if (
        state.checkingOut
        || state.cart.size === 0
    ) {
        return;
    }

    let visitorId;
    let barId;

    try {
        visitorId =
            parsePositiveLong(
                elements.visitorId.value,
                "Visitor ID"
            );

        barId =
            parsePositiveLong(
                elements.barId.value,
                "Bar ID"
            );

    } catch (error) {
        showToast(
            error.message,
            "error"
        );

        return;
    }

    if (
        state.catalog
        && Number(state.catalog.barId)
            !== barId
    ) {
        showToast(
            "The cart belongs to another bar. " +
            "Reload the selected bar before ordering.",
            "error"
        );

        return;
    }

    const request = {
        visitorId,
        barId,

        items:
            Array.from(
                state.cart.values()
            ).map(item => ({
                beverageTypeId:
                    item.beverageTypeId,

                quantity:
                    item.quantity
            }))
    };

    setCheckoutLoading(true);

    try {
        const response = await fetch(
            "/api/v1/customer/orders",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json",

                    Accept:
                        "application/json"
                },

                body:
                    JSON.stringify(request)
            }
        );

        const order =
            await readResponse(response);

        renderOrderSuccess(order);

        state.cart.clear();

        renderCart();

        showToast(
            `Order ${order.purchaseId} completed.`,
            "success"
        );

        await loadCatalog();

        elements.orderResult
            .scrollIntoView({
                behavior: "smooth",
                block: "center"
            });

    } catch (error) {
        renderOrderError(
            error.message
        );

        showToast(
            error.message,
            "error"
        );

        /*
         * Reload the catalogue because another
         * customer may have changed the stock.
         */
        await loadCatalog();

    } finally {
        setCheckoutLoading(false);
    }
}

function renderOrderSuccess(order) {
    const orderItems =
        Array.isArray(order.items)
            ? order.items
            : [];

    elements.orderResult.className =
        "order-result order-success";

    elements.orderResult.innerHTML = `
        <div class="order-success__header">
            <div>
                <h3>Order completed</h3>

                <p>
                    Purchase ID:
                    <strong>
                        ${escapeHtml(order.purchaseId)}
                    </strong>
                </p>
            </div>

            <span class="order-status">
                ${escapeHtml(order.status)}
            </span>
        </div>

        <div class="order-items">
            ${orderItems
                .map(item => `
                    <div class="order-item">
                        <span>
                            ${escapeHtml(
                                item.beverageName
                            )}
                        </span>

                        <span>
                            ${item.quantity}
                            ×
                            ${formatMoney(
                                item.unitPrice
                            )}
                        </span>

                        <strong>
                            ${formatMoney(
                                item.lineTotal
                            )}
                        </strong>
                    </div>
                `)
                .join("")}
        </div>

        <div class="order-total">
            <span>Total</span>

            <strong>
                ${formatMoney(order.totalPrice)}
            </strong>
        </div>
    `;
}

function renderOrderError(message) {
    elements.orderResult.className =
        "order-result empty-state";

    elements.orderResult.innerHTML = `
        <h3>Order could not be completed</h3>

        <p>
            ${escapeHtml(message)}
        </p>
    `;
}

function reconcileCartWithCatalog() {
    if (!state.catalog) {
        return;
    }

    const productsById =
        new Map(
            state.catalog.products.map(
                product => [
                    Number(
                        product.beverageTypeId
                    ),
                    product
                ]
            )
        );

    for (
        const [
            beverageTypeId,
            cartItem
        ] of state.cart.entries()
    ) {
        const currentProduct =
            productsById.get(
                beverageTypeId
            );

        if (
            !currentProduct
            || !currentProduct.available
            || Number(
                currentProduct.availableQuantity
            ) <= 0
        ) {
            state.cart.delete(
                beverageTypeId
            );

            continue;
        }

        const currentAvailableQuantity =
            Number(
                currentProduct.availableQuantity
            );

        const adjustedQuantity =
            Math.min(
                cartItem.quantity,
                currentAvailableQuantity
            );

        state.cart.set(
            beverageTypeId,
            {
                beverageTypeId,
                name:
                    String(currentProduct.name),
                unitPrice:
                    Number(currentProduct.price),
                quantity:
                    adjustedQuantity,
                availableQuantity:
                    currentAvailableQuantity
            }
        );
    }
}

async function readResponse(response) {
    const contentType =
        response.headers.get(
            "content-type"
        ) ?? "";

    let body;

    if (
        contentType.includes(
            "application/json"
        )
    ) {
        body = await response.json();
    } else {
        body = await response.text();
    }

    if (!response.ok) {
        const message =
            typeof body === "object"
                ? body.detail
                    ?? body.message
                    ?? body.title
                : body;

        throw new Error(
            message
            || `Request failed with HTTP ${response.status}.`
        );
    }

    return body;
}

function setCatalogLoading(isLoading) {
    state.loadingCatalog =
        isLoading;

    elements.loadProductsButton.disabled =
        isLoading;

    elements.refreshProductsButton.disabled =
        isLoading
        || !state.catalog;

    elements.loadProductsButton.classList.toggle(
        "loading",
        isLoading
    );

    elements.loadProductsButton.textContent =
        isLoading
            ? "Loading"
            : "Load products";
}

function setCheckoutLoading(isLoading) {
    state.checkingOut =
        isLoading;

    elements.checkoutButton.classList.toggle(
        "loading",
        isLoading
    );

    elements.checkoutButton.textContent =
        isLoading
            ? "Processing"
            : "Place order";

    updateCartSummary();
}

function setCatalogMessage(
    message,
    type = ""
) {
    elements.catalogMessage.textContent =
        message;

    elements.catalogMessage.className =
        "message";

    if (type === "error") {
        elements.catalogMessage.classList.add(
            "message--error"
        );
    }

    if (type === "success") {
        elements.catalogMessage.classList.add(
            "message--success"
        );
    }
}

function renderCatalogError(message) {
    elements.productList.innerHTML = `
        <div class="empty-state">
            <h3>Catalogue unavailable</h3>

            <p>
                ${escapeHtml(message)}
            </p>
        </div>
    `;
}

function updateBarQueryParameter(barId) {
    const url =
        new URL(window.location.href);

    url.searchParams.set(
        "barId",
        String(barId)
    );

    window.history.replaceState(
        {},
        "",
        url
    );
}

function parsePositiveLong(
    value,
    fieldName
) {
    const parsed =
        Number(value);

    if (
        !Number.isSafeInteger(parsed)
        || parsed <= 0
    ) {
        throw new Error(
            `${fieldName} must be a positive whole number.`
        );
    }

    return parsed;
}

function parsePositiveInteger(
    value,
    fieldName
) {
    const parsed =
        Number(value);

    if (
        !Number.isSafeInteger(parsed)
        || parsed <= 0
    ) {
        throw new Error(
            `${fieldName} must be greater than zero.`
        );
    }

    return parsed;
}

function formatMoney(value) {
    const amount =
        Number(value);

    if (!Number.isFinite(amount)) {
        return "€0.00";
    }

    return new Intl.NumberFormat(
        "en-GB",
        {
            style: "currency",
            currency: CURRENCY
        }
    ).format(amount);
}

function escapeHtml(value) {
    const element =
        document.createElement("div");

    element.textContent =
        value == null
            ? ""
            : String(value);

    return element.innerHTML;
}

let toastTimeout;

function showToast(
    message,
    type = ""
) {
    clearTimeout(
        toastTimeout
    );

    elements.toast.textContent =
        message;

    elements.toast.className =
        "toast toast--visible";

    if (type === "error") {
        elements.toast.classList.add(
            "toast--error"
        );
    }

    if (type === "success") {
        elements.toast.classList.add(
            "toast--success"
        );
    }

    toastTimeout =
        window.setTimeout(
            () => {
                elements.toast.className =
                    "toast";
            },
            3200
        );
}