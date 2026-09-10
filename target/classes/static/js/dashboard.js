const API_BASE_URL = "http://localhost:8080";

const token = localStorage.getItem("jwtToken");

const userEmail = localStorage.getItem("userEmail");

if (!token) {
    window.location.href = "/";
}

document.getElementById("userEmail").textContent =
    userEmail || "User";

let accounts = [];

let currentUserId = null;


/* =========================================================
   CURRENT USER
   ========================================================= */

async function loadCurrentUser() {

    try {

        const response = await fetch(
            `${API_BASE_URL}/users/me`,
            {
                method: "GET",

                headers: {
                    "Authorization": `Bearer ${token}`
                }
            }
        );

        if (response.status === 401) {
            logout();
            return false;
        }

        if (!response.ok) {
            throw new Error("Unable to load current user");
        }

        const user = await response.json();

        currentUserId = user.id;

        document.getElementById("userEmail").textContent =
            user.email;

        return true;

    } catch (error) {

        console.error(
            "Current user error:",
            error
        );

        return false;
    }
}


/* =========================================================
   LOAD ACCOUNTS
   ========================================================= */

async function loadAccounts() {

    try {

        const response = await fetch(
            `${API_BASE_URL}/user/${currentUserId}/account`,
            {
                method: "GET",

                headers: {
                    "Authorization": `Bearer ${token}`
                }
            }
        );

        if (response.status === 401) {
            logout();
            return;
        }

        if (!response.ok) {
            throw new Error("Unable to load accounts");
        }

        accounts = await response.json();

        displayAccounts(accounts);

        populateDepositAccounts();

        populateWithdrawAccounts();

        populateTransferAccounts();

        populateTransactionAccounts();

    } catch (error) {

        console.error(
            "Account loading error:",
            error
        );

        document.getElementById("accountsContainer").innerHTML =
            `<p class="error-message">
                Unable to load accounts.
             </p>`;
    }
}


/* =========================================================
   DISPLAY ACCOUNTS
   ========================================================= */

function displayAccounts(accounts) {

    const container =
        document.getElementById("accountsContainer");

    if (accounts.length === 0) {

        container.innerHTML =
            `<p class="empty-message">
                No accounts found.
             </p>`;

        return;
    }

    container.innerHTML = "";

    accounts.forEach(account => {

        const card = document.createElement("div");

        card.className = "account-card";

        card.innerHTML = `

            <div class="account-header">

                <span>${account.accountType}</span>

                <span class="status">
                    ${account.status}
                </span>

            </div>

            <p class="account-number">
                Account No: ${account.accountNo}
            </p>

            <h2>
                ₹${Number(account.balance).toFixed(2)}
            </h2>

            <p class="account-id">
                Account ID: ${account.acc_id}
            </p>

            ${
                account.status === "ACTIVE"

                ? `

                    <button
                        class="close-account-button"
                        onclick="closeAccount(${account.acc_id})">

                        Close Account

                    </button>

                  `

                : `

                    <p class="closed-label">
                        Account Closed
                    </p>

                  `
            }

        `;

        container.appendChild(card);

    });
}


/* =========================================================
   OPEN ACCOUNT
   ========================================================= */

document
    .getElementById("openAccountButton")
    .addEventListener("click", function () {

        const section =
            document.getElementById(
                "openAccountSection"
            );

        section.classList.toggle("visible");

    });


document
    .getElementById("openAccountForm")
    .addEventListener("submit", async function (event) {

        event.preventDefault();

        const accountNo =
            document.getElementById(
                "newAccountNumber"
            ).value;

        const accountType =
            document.getElementById(
                "newAccountType"
            ).value;

        const message =
            document.getElementById(
                "openAccountMessage"
            );

        message.textContent =
            "Creating account...";

        try {

            const response = await fetch(
                `${API_BASE_URL}/user/${currentUserId}/account`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json",

                        "Authorization":
                            `Bearer ${token}`
                    },

                    body: JSON.stringify({

                        accountNo:
                            Number(accountNo),

                        balance: 0,

                        accountType:
                            accountType,

                        status: "ACTIVE"
                    })
                }
            );

            const data =
                await response.text();

            if (!response.ok) {

                message.textContent =
                    data || "Unable to open account.";

                return;
            }

            message.textContent =
                "Account opened successfully.";

            document
                .getElementById(
                    "newAccountNumber"
                )
                .value = "";

            document
                .getElementById(
                    "newAccountType"
                )
                .value = "SAVINGS";

            await loadAccounts();

        } catch (error) {

            console.error(
                "Open account error:",
                error
            );

            message.textContent =
                "Unable to connect to server.";
        }
    });


/* =========================================================
   DEPOSIT
   ========================================================= */

function populateDepositAccounts() {

    const select =
        document.getElementById("depositAccount");

    if (!select) {
        return;
    }

    select.innerHTML = "";

    const activeAccounts =
        accounts.filter(
            account => account.status === "ACTIVE"
        );

    activeAccounts.forEach(account => {

        const option =
            document.createElement("option");

        option.value = account.acc_id;

        option.textContent =
            `${account.accountType} - ${account.accountNo}`;

        select.appendChild(option);
    });
}


document
    .getElementById("depositButton")
    .addEventListener("click", function () {

        const section =
            document.getElementById("depositSection");

        section.classList.toggle("visible");

        populateDepositAccounts();

    });


document
    .getElementById("depositForm")
    .addEventListener("submit", async function (event) {

        event.preventDefault();

        const accountId =
            document.getElementById(
                "depositAccount"
            ).value;

        const amount =
            document.getElementById(
                "depositAmount"
            ).value;

        const message =
            document.getElementById(
                "depositMessage"
            );

        message.textContent =
            "Processing...";

        try {

            const response = await fetch(
                `${API_BASE_URL}/transactions/deposit/${accountId}?amount=${amount}`,
                {
                    method: "POST",

                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );

            const data =
                await response.text();

            if (!response.ok) {

                message.textContent =
                    data || "Deposit failed";

                return;
            }

            message.textContent =
                "Deposit successful.";

            document
                .getElementById(
                    "depositAmount"
                )
                .value = "";

            await loadAccounts();

        } catch (error) {

            console.error(
                "Deposit error:",
                error
            );

            message.textContent =
                "Unable to connect to server.";
        }
    });


/* =========================================================
   WITHDRAW
   ========================================================= */

function populateWithdrawAccounts() {

    const select =
        document.getElementById("withdrawAccount");

    if (!select) {
        return;
    }

    select.innerHTML = "";

    const activeAccounts =
        accounts.filter(
            account => account.status === "ACTIVE"
        );

    activeAccounts.forEach(account => {

        const option =
            document.createElement("option");

        option.value = account.acc_id;

        option.textContent =
            `${account.accountType} - ${account.accountNo}`;

        select.appendChild(option);
    });
}


document
    .getElementById("withdrawButton")
    .addEventListener("click", function () {

        const section =
            document.getElementById("withdrawSection");

        section.classList.toggle("visible");

        populateWithdrawAccounts();

    });


document
    .getElementById("withdrawForm")
    .addEventListener("submit", async function (event) {

        event.preventDefault();

        const accountId =
            document.getElementById(
                "withdrawAccount"
            ).value;

        const amount =
            document.getElementById(
                "withdrawAmount"
            ).value;

        const message =
            document.getElementById(
                "withdrawMessage"
            );

        message.textContent =
            "Processing...";

        try {

            const response = await fetch(
                `${API_BASE_URL}/transactions/withdraw/${accountId}?amount=${amount}`,
                {
                    method: "POST",

                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );

            const data =
                await response.text();

            if (!response.ok) {

                message.textContent =
                    data || "Withdrawal failed";

                return;
            }

            message.textContent =
                "Withdrawal successful.";

            document
                .getElementById(
                    "withdrawAmount"
                )
                .value = "";

            await loadAccounts();

        } catch (error) {

            console.error(
                "Withdrawal error:",
                error
            );

            message.textContent =
                "Unable to connect to server.";
        }
    });


/* =========================================================
   TRANSFER
   ========================================================= */

function populateTransferAccounts() {

    const select =
        document.getElementById(
            "transferFromAccount"
        );

    if (!select) {
        return;
    }

    select.innerHTML = "";

    const activeAccounts =
        accounts.filter(
            account => account.status === "ACTIVE"
        );

    activeAccounts.forEach(account => {

        const option =
            document.createElement("option");

        option.value =
            account.acc_id;

        option.textContent =
            `${account.accountType} - ${account.accountNo}`;

        select.appendChild(option);
    });
}


document
    .getElementById("transferButton")
    .addEventListener("click", function () {

        const section =
            document.getElementById(
                "transferSection"
            );

        section.classList.toggle("visible");

        populateTransferAccounts();

    });


document
    .getElementById("transferForm")
    .addEventListener("submit", async function (event) {

        event.preventDefault();

        const fromAccountId =
            document.getElementById(
                "transferFromAccount"
            ).value;

        const toAccountId =
            document.getElementById(
                "transferToAccount"
            ).value;

        const amount =
            document.getElementById(
                "transferAmount"
            ).value;

        const message =
            document.getElementById(
                "transferMessage"
            );

        message.textContent =
            "Processing...";

        try {

            const response = await fetch(
                `${API_BASE_URL}/transactions/transfer` +
                `?fromAccountId=${fromAccountId}` +
                `&toAccountId=${toAccountId}` +
                `&amount=${amount}`,

                {
                    method: "POST",

                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );

            const data =
                await response.text();

            if (!response.ok) {

                message.textContent =
                    data || "Transfer failed";

                return;
            }

            message.textContent =
                "Transfer successful.";

            document
                .getElementById(
                    "transferToAccount"
                )
                .value = "";

            document
                .getElementById(
                    "transferAmount"
                )
                .value = "";

            await loadAccounts();

        } catch (error) {

            console.error(
                "Transfer error:",
                error
            );

            message.textContent =
                "Unable to connect to server.";
        }
    });


/* =========================================================
   TRANSACTION HISTORY
   ========================================================= */

function populateTransactionAccounts() {

    const select =
        document.getElementById(
            "transactionAccount"
        );

    if (!select) {
        return;
    }

    select.innerHTML = "";

    accounts.forEach(account => {

        const option =
            document.createElement("option");

        option.value =
            account.acc_id;

        option.textContent =
            `${account.accountType} - ${account.accountNo}`;

        select.appendChild(option);
    });

    if (accounts.length > 0) {

        loadTransactions(
            accounts[0].acc_id
        );
    }
}


document
    .getElementById("transactionsButton")
    .addEventListener("click", function () {

        const section =
            document.getElementById(
                "transactionsSection"
            );

        section.classList.toggle("visible");

        populateTransactionAccounts();

    });


document
    .getElementById("transactionAccount")
    .addEventListener("change", function () {

        const accountId =
            this.value;

        loadTransactions(accountId);

    });


async function loadTransactions(accountId) {

    const container =
        document.getElementById(
            "transactionsContainer"
        );

    container.innerHTML =
        "<p>Loading transactions...</p>";

    try {

        const response = await fetch(
            `${API_BASE_URL}/transactions/account/${accountId}`,

            {
                method: "GET",

                headers: {
                    "Authorization":
                        `Bearer ${token}`
                }
            }
        );

        if (response.status === 401) {

            logout();

            return;
        }

        if (!response.ok) {

            const message =
                await response.text();

            container.innerHTML =
                `<p class="error-message">
                    ${message ||
                    "Unable to load transactions."}
                 </p>`;

            return;
        }

        const transactions =
            await response.json();

        displayTransactions(transactions);

    } catch (error) {

        console.error(
            "Transaction history error:",
            error
        );

        container.innerHTML =
            `<p class="error-message">
                Unable to connect to server.
             </p>`;
    }
}


function displayTransactions(transactions) {

    const container =
        document.getElementById(
            "transactionsContainer"
        );

    if (transactions.length === 0) {

        container.innerHTML =
            `<p class="empty-message">
                No transactions found.
             </p>`;

        return;
    }

    let table = `

        <div class="transaction-table-wrapper">

            <table class="transaction-table">

                <thead>

                    <tr>

                        <th>Date</th>

                        <th>Type</th>

                        <th>Amount</th>

                        <th>Status</th>

                        <th>Description</th>

                    </tr>

                </thead>

                <tbody>

    `;

    transactions.forEach(transaction => {

        const date =
            new Date(
                transaction.transactionDate
            ).toLocaleString();

        const amount =
            Number(transaction.amount)
                .toFixed(2);

        table += `

            <tr>

                <td>${date}</td>

                <td>
                    ${transaction.transactionType}
                </td>

                <td>
                    ₹${amount}
                </td>

                <td>
                    ${transaction.status}
                </td>

                <td>
                    ${transaction.description || "-"}
                </td>

            </tr>

        `;
    });

    table += `

                </tbody>

            </table>

        </div>

    `;

    container.innerHTML = table;
}


/* =========================================================
   CLOSE ACCOUNT
   ========================================================= */

async function closeAccount(accountId) {

    const confirmed =
        confirm(
            "Are you sure you want to close this account?"
        );

    if (!confirmed) {
        return;
    }

    try {

        const response = await fetch(
            `${API_BASE_URL}/account/${accountId}/close`,

            {
                method: "DELETE",

                headers: {
                    "Authorization":
                        `Bearer ${token}`
                }
            }
        );

        const data =
            await response.text();

        if (response.status === 401) {

            logout();

            return;
        }

        if (!response.ok) {

            alert(
                data || "Unable to close account."
            );

            return;
        }

        alert(
            "Account closed successfully."
        );

        await loadAccounts();

    } catch (error) {

        console.error(
            "Close account error:",
            error
        );

        alert(
            "Unable to connect to server."
        );
    }
}


/* =========================================================
   LOGOUT
   ========================================================= */

function logout() {

    localStorage.removeItem("jwtToken");

    localStorage.removeItem("userEmail");

    window.location.href = "/";
}


document
    .getElementById("logoutButton")
    .addEventListener("click", logout);


/* =========================================================
   DASHBOARD INITIALIZATION
   ========================================================= */

async function initializeDashboard() {

    const userLoaded =
        await loadCurrentUser();

    if (!userLoaded) {
        return;
    }

    await loadAccounts();
}

initializeDashboard();