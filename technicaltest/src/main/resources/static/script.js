document.addEventListener("DOMContentLoaded", function () {
    fetchOptions();
    showStoreDataForm();
    showGetDataForm();
});

function showSection(sectionId) {
    document.getElementById('store-data').style.display = 'none';
    document.getElementById('get-data').style.display = 'none';
    document.getElementById(sectionId).style.display = 'block';
}

function fetchOptions() {
    fetch('/api/options')
        .then(response => response.json())
        .then(data => {
            window.options = data;
            populateTypeOptions();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('No data in Redis!');
        });
}

function populateTypeOptions() {
    const getTypeSelect = document.getElementById('get-type');

    getTypeSelect.innerHTML = '<option value="" disabled selected>Select</option>';

    if (window.options && Object.keys(window.options).length > 0) {
        Object.keys(window.options).forEach(type => {
            const option = document.createElement('option');
            option.value = type;
            option.textContent = type.charAt(0).toUpperCase() + type.slice(1);
            getTypeSelect.appendChild(option);
        });
    } else {
        const option = document.createElement('option');
        option.textContent = 'No data in Redis!';
        getTypeSelect.appendChild(option);
    }
}

function populateSymbolOptions(type) {
    const getSymbolSelect = document.getElementById('get-symbol');

    getSymbolSelect.innerHTML = '<option value="" disabled selected>Select</option>';

    if (window.options && window.options[type] && window.options[type].length > 0) {
        window.options[type].forEach(symbol => {
            const option = document.createElement('option');
            option.value = symbol;
            option.textContent = symbol;
            getSymbolSelect.appendChild(option);
        });
    } else {
        const option = document.createElement('option');
        option.textContent = 'No data in Redis!';
        getSymbolSelect.appendChild(option);
    }
}

document.addEventListener('change', function(event) {
    if (event.target.id === 'get-type') {
        populateSymbolOptions(event.target.value);
    }
});

function showStoreDataForm() {
    const storeDataForm = `
        <h2 class="text-center">Store Financial Data</h2>
        <form id="store-form" class="mt-4" onsubmit="storeData(event)">
            <div class="form-group">
                <label for="store-type">Type:</label>
                <select id="store-type" class="form-control" name="type" required>
                    <option value="" disabled selected>Select</option>
                    <option value="stock">Stock</option>
                    <option value="forex">Forex</option>
                    <option value="crypto">Crypto</option>
                    <option value="etf">ETF</option>
                    <option value="index">Index</option>
                    <option value="fund">Fund</option>
                </select>
                <small class="form-text text-muted">Choose the type of financial data you want to store.</small>
            </div>
            <div class="form-group" id="symbol-group">
                <label for="store-symbol">Symbol:</label>
                <select id="store-symbol" class="form-control" name="symbol" required>
                    <option value="" disabled selected>Select</option>
                    <option value="AAPL">Apple (AAPL)</option>
                    <option value="AMZN">Amazon (AMZN)</option>
                    <option value="MSFT">Microsoft (MSFT)</option>
                    <option value="GOOGL">Google (GOOGL)</option>
                    <option value="BSET">Bassett Furniture Industries Inc (BSET)</option>
                    <option value="TWKS">Thoughtworks Holding Inc (TWKS)</option>
                </select>
                <small class="form-text text-muted">Enter the symbol for the selected type.</small>
            </div>
            <div class="form-group" id="interval-group">
                <label for="store-interval">Interval:</label>
                <select id="store-interval" class="form-control" name="interval">
                    <option value="" disabled selected>Select</option>
                    <option value="1min">1 minute</option>
                    <option value="5min">5 minutes</option>
                    <option value="15min">15 minutes</option>
                    <option value="30min">30 minutes</option>
                    <option value="1h">1 hour</option>
                    <option value="1day">1 day</option>
                    <option value="1week">1 week</option>
                    <option value="1month">1 month</option>
                </select>
                <small class="form-text text-muted">Select the interval for time series data.</small>
            </div>
            <div class="form-group" id="exchange-group">
                <label for="store-exchange">Exchange:</label>
                <select id="store-exchange" class="form-control" name="exchange">
                    <option value="" disabled selected>Select</option>
                    <option value="NYSE">New York Stock Exchange (NYSE)</option>
                    <option value="NASDAQ">NASDAQ</option>
                    <option value="LSE">London Stock Exchange (LSE)</option>
                    <option value="JPX">Japan Exchange Group (JPX)</option>
                    <option value="SSE">Shanghai Stock Exchange (SSE)</option>
                </select>
                <small class="form-text text-muted">Select the exchange if applicable.</small>
            </div>
            <div class="form-group" id="amount-group">
                <label for="store-amount">Amount:</label>
                <input type="text" id="store-amount" class="form-control" name="amount" placeholder="Enter Amount" pattern="^\\d+(\\.\\d{1,2})?$" title="Please enter a valid amount (e.g., 100.50)">
                <small class="form-text text-muted">Enter the amount for currency conversion. Use a dot as the decimal separator (e.g., 100.50).</small>
            </div>
            <button type="submit" class="btn btn-primary">Store Data</button>
        </form>
        <div id="store-result" class="mt-4"></div>
    `;
    document.getElementById('store-data').innerHTML = storeDataForm;
}

function showGetDataForm() {
    const getDataForm = `
        <h2 class="text-center">Get Financial Data</h2>
        <form id="get-form" class="mt-4" onsubmit="getData(event)">
            <div class="form-group">
                <label for="get-type">Type:</label>
                <select id="get-type" class="form-control" name="type" required>
                    <option value="" disabled selected>Select</option>
                </select>
                <small class="form-text text-muted">Choose the type of financial data you want to get.</small>
            </div>
            <div class="form-group" id="symbol-group">
                <label for="get-symbol">Symbol:</label>
                <select id="get-symbol" class="form-control" name="symbol" required>
                    <option value="" disabled selected>Select</option>
                </select>
                <small class="form-text text-muted">Select the symbol for the selected type.</small>
            </div>
            <button type="submit" class="btn btn-primary">Get Data</button>
        </form>
        <div id="get-result" class="mt-4"></div>
    `;
    document.getElementById('get-data').innerHTML = getDataForm;
}

function storeData(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const amount = parseFloat(formData.get('amount'));

    if (isNaN(amount) || amount <= 0) {
        alert('Introduce un valor válido. El valor debe ser un número positivo.');
        return;
    }

    const data = {
        type: formData.get('type'),
        symbol: formData.get('symbol'),
        interval: formData.get('interval'),
        exchange: formData.get('exchange'),
        amount: amount
    };

    fetch('/api/financialData', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => response.text())
    .then(result => {
        const resultContainer = document.getElementById('store-result');
        resultContainer.innerHTML = `<div class="alert alert-success">${result}</div>`;
        fetchOptions();
    })
    .catch(error => {
        console.error('Error:', error);
        const resultContainer = document.getElementById('store-result');
        resultContainer.innerHTML = `<div class="alert alert-danger">Error: ${error.message}</div>`;
    });
}

function getData(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const type = formData.get('type');
    const symbol = formData.get('symbol');

    fetch(`/api/financialData?type=${type}&symbol=${symbol}`)
    .then(response => response.json())
    .then(data => {
        const resultContainer = document.getElementById('get-result');
        resultContainer.innerHTML = formatData(data);
    })
    .catch(error => {
        console.error('Error:', error);
        const resultContainer = document.getElementById('get-result');
        if (error.message.includes("Unexpected token 'N'")) {
            resultContainer.innerHTML = `<div class="alert alert-warning">No data found for this case, don't forget to upload it in Store Data!</div>`;
        } else {
            resultContainer.innerHTML = `<div class="alert alert-danger">Error: ${error.message}</div>`;
        }
    });
}

function formatData(data) {
    if (!data || Object.keys(data).length === 0) {
        return `<div class="alert alert-warning">No data found</div>`;
    }

    if (data.meta) {
        const meta = data.meta;
        const values = data.values;

        const formattedMeta = `
            <div class="card">
                <div class="card-header">
                    <h5>${meta.symbol} (${meta.type})</h5>
                </div>
                <div class="card-body">
                    <p><strong>Exchange:</strong> ${meta.exchange}</p>
                    <p><strong>Currency:</strong> ${meta.currency}</p>
                    <p><strong>Interval:</strong> ${meta.interval}</p>
                    <p><strong>Exchange Timezone:</strong> ${meta.exchange_timezone}</p>
                </div>
            </div>
        `;

        const formattedValues = values.map(value => `
            <div class="card mt-3">
                <div class="card-header">
                    <h6>Timestamp: ${value.datetime}</h6>
                </div>
                <div class="card-body">
                    <p><strong>Open:</strong> ${value.open}</p>
                    <p><strong>High:</strong> ${value.high}</p>
                    <p><strong>Low:</strong> ${value.low}</p>
                    <p><strong>Close:</strong> ${value.close}</p>
                    <p><strong>Volume:</strong> ${value.volume}</p>
                </div>
            </div>
        `).join('');

        return formattedMeta + formattedValues;
    } else {
        const formattedData = `
            <div class="card">
                <div class="card-header">
                    <h5>${data.symbol} (${data.name})</h5>
                </div>
                <div class="card-body">
                    <p><strong>Exchange:</strong> ${data.exchange}</p>
                    <p><strong>Currency:</strong> ${data.currency}</p>
                    <p><strong>Datetime:</strong> ${data.datetime}</p>
                    <p><strong>Open:</strong> ${data.open}</p>
                    <p><strong>High:</strong> ${data.high}</p>
                    <p><strong>Low:</strong> ${data.low}</p>
                    <p><strong>Close:</strong> ${data.close}</p>
                    <p><strong>Volume:</strong> ${data.volume}</p>
                    <p><strong>Previous Close:</strong> ${data.previous_close}</p>
                    <p><strong>Change:</strong> ${data.change}</p>
                    <p><strong>Percent Change:</strong> ${data.percent_change}</p>
                    <p><strong>Average Volume:</strong> ${data.average_volume}</p>
                    <p><strong>Market Open:</strong> ${data.is_market_open}</p>
                    <p><strong>52 Week Low:</strong> ${data.fifty_two_week.low}</p>
                    <p><strong>52 Week High:</strong> ${data.fifty_two_week.high}</p>
                    <p><strong>52 Week Low Change:</strong> ${data.fifty_two_week.low_change}</p>
                    <p><strong>52 Week High Change:</strong> ${data.fifty_two_week.high_change}</p>
                    <p><strong>52 Week Low Change Percent:</strong> ${data.fifty_two_week.low_change_percent}</p>
                    <p><strong>52 Week High Change Percent:</strong> ${data.fifty_two_week.high_change_percent}</p>
                    <p><strong>52 Week Range:</strong> ${data.fifty_two_week.range}</p>
                </div>
            </div>
        `;

        return formattedData;
    }
}