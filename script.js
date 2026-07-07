let isAed = true;
let isAr = true;
let goldData = null;
const aedRate = 3.6725;

const aedSvg = `<span class="aed-symbol-inline"><svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 4V20H11C15.4183 20 19 16.4183 19 12C19 7.58172 15.4183 4 11 4H5ZM7 6H11C14.3137 6 17 8.68629 17 12C17 15.3137 14.3137 18 11 18H7V6Z" fill="currentColor"/><path d="M3 10H15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M3 14H15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></span>`;

const translations = {
    ar: {
        title: "سوق الذهب في الإمارات",
        toggleCurrency: "تغيير العملة (درهم/دولار)",
        toggleLang: "English",
        lastUpdated: "آخر تحديث: ",
        loading: "جاري التحميل...",
        aed: "درهم",
        usd: "دولار",
        aedSymbol: "د.إ",
        usdSymbol: "$",
        priceLabel: "سعر الذهب الخام الإرشادي",
        gram: "غرام",
        ounce: "أونصة",
        marketSummaryTemplate: "سعر الذهب اليوم في الإمارات عيار 24 هو {p24} {currency}. سعر الأونصة العالمي يبلغ {ounceUsd} دولار، ما يعادل {ounceAed} درهم إماراتي. السوق حالياً مستقر.",
        ounceTitle: "سعر الذهب العالمي للأونصة",
        calculatedFrom: "محسوب من سعر الغرام الخام",
        live: "مباشر"
    },
    en: {
        title: "UAE Gold Market",
        toggleCurrency: "Switch Currency (AED/USD)",
        toggleLang: "العربية",
        lastUpdated: "Last Updated: ",
        loading: "Loading...",
        aed: "AED",
        usd: "USD",
        aedSymbol: "AED",
        usdSymbol: "$",
        priceLabel: "Indicative raw gold rate",
        gram: "Gram",
        ounce: "Ounce",
        marketSummaryTemplate: "Today's gold price in UAE for 24K is {p24} {currency}. The global gold ounce price is {ounceUsd} USD, equivalent to {ounceAed} AED. The market is currently stable.",
        ounceTitle: "Global Gold Ounce Price",
        calculatedFrom: "Calculated from raw gram price",
        live: "LIVE"
    }
};

async function fetchPrices() {
    try {
        const response = await fetch('https://api.gold-api.com/price/XAU');
        const data = await response.json();
        const ounceToGram = 31.1034768;
        const price24k = data.price / ounceToGram;
        
        goldData = {
            p24: price24k,
            p22: price24k * (22/24),
            p21: price24k * (21/24),
            p18: price24k * (18/24),
            ounceUsd: data.price,
            updatedAt: new Date(data.updatedAt).toLocaleTimeString(isAr ? 'ar-EG' : 'en-US', {hour: '2-digit', minute:'2-digit'})
        };
        
        document.getElementById('loading').classList.add('hidden');
        document.getElementById('main-content').classList.remove('hidden');
        render();
    } catch (error) {
        console.error(error);
        document.getElementById('loading').innerHTML = "<p>خطأ في تحميل البيانات / Error loading data</p>";
    }
}

function render() {
    const lang = isAr ? 'ar' : 'en';
    const currency = isAed ? translations[lang].aed : translations[lang].usd;
    const symbol = isAed ? translations[lang].aedSymbol : translations[lang].usdSymbol;
    const multiplier = isAed ? aedRate : 1;

    document.getElementById('title').innerText = translations[lang].title;
    document.getElementById('currencyText').innerText = translations[lang].toggleCurrency;
    document.getElementById('toggleLang').innerText = translations[lang].toggleLang;
    document.getElementById('updatedAt').innerText = translations[lang].lastUpdated + goldData.updatedAt;
    document.getElementById('statusText').innerText = translations[lang].live;

    // Render Price Cards
    const priceGrid = document.querySelector('.price-grid');
    priceGrid.innerHTML = '';
    const karats = [
        {k: '24', id: 'p24', labelAr: 'عيار 24', labelEn: '24K Gold'},
        {k: '22', id: 'p22', labelAr: 'عيار 22', labelEn: '22K Gold'},
        {k: '21', id: 'p21', labelAr: 'عيار 21', labelEn: '21K Gold'},
        {k: '18', id: 'p18', labelAr: 'عيار 18', labelEn: '18K Gold'}
    ];

    karats.forEach(item => {
        const price = (goldData[item.id] * multiplier).toFixed(2);
        const card = document.createElement('div');
        card.className = 'card price-card';
        const displayCurrency = isAed ? aedSvg : currency;
        card.innerHTML = `
            <div class="karat-badge">${item.k}K</div>
            <div class="card-label">${isAr ? item.labelAr : item.labelEn}</div>
            <div class="card-price">${price}</div>
            <div class="card-currency">${displayCurrency} / ${translations[lang].gram}</div>
            <div class="card-footer-label">${translations[lang].priceLabel}</div>
        `;
        priceGrid.appendChild(card);
    });

    // Render Ounce Card
    const ounceCard = document.getElementById('ounceCard');
    const ounceAed = (goldData.ounceUsd * aedRate).toFixed(2);
    const ounceUsd = (goldData.ounceUsd).toFixed(2);
    const displayCurrency = isAed ? aedSvg : currency;
    ounceCard.innerHTML = `
        <div class="ounce-card-content">
            <div class="ounce-main">
                <div class="xau-badge">XAU</div>
                <h3 class="card-title">${translations[lang].ounceTitle}</h3>
                <div class="card-price">${isAed ? ounceAed : ounceUsd} <span class="card-currency">${displayCurrency}</span></div>
                ${isAed ? `<div class="card-currency">${ounceUsd} USD</div>` : `<div class="card-currency">${ounceAed} ${isAed ? '' : aedSvg}</div>`}
            </div>
            <div class="ounce-footer">
                <p class="disclaimer-text sm">${translations[lang].calculatedFrom}</p>
                <p class="disclaimer-text sm">${translations[lang].lastUpdated} ${goldData.updatedAt}</p>
            </div>
        </div>
    `;

    // Market Summary
    const summary = translations[lang].marketSummaryTemplate
        .replace('{p24}', (goldData.p24 * multiplier).toFixed(2))
        .replace('{currency}', currency)
        .replace('{ounceUsd}', ounceUsd)
        .replace('{ounceAed}', ounceAed);
    document.getElementById('marketSummary').innerText = summary;

    // Static text translations
    document.querySelectorAll('[data-ar]').forEach(el => {
        el.innerText = isAr ? el.getAttribute('data-ar') : el.getAttribute('data-en');
    });

    document.body.dir = isAr ? 'rtl' : 'ltr';
    document.documentElement.lang = lang;

    updateCalculator();
}

function updateCalculator() {
    if (!goldData) return;
    const weight = parseFloat(document.getElementById('calcWeight').value) || 0;
    const karat = document.getElementById('calcKarat').value;
    const lang = isAr ? 'ar' : 'en';
    const multiplier = isAed ? aedRate : 1;
    const currency = isAed ? translations[lang].aed : translations[lang].usd;

    let pricePerGram = goldData.p24;
    if (karat === '22') pricePerGram = goldData.p22;
    if (karat === '21') pricePerGram = goldData.p21;
    if (karat === '18') pricePerGram = goldData.p18;

    const result = weight * pricePerGram * multiplier;
    const displayCurrency = isAed ? aedSvg : currency;
    document.getElementById('calcValue').innerHTML = `${result.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})} ${displayCurrency}`;
}

document.getElementById('toggleCurrency').onclick = () => {
    isAed = !isAed;
    render();
};

document.getElementById('toggleLang').onclick = () => {
    isAr = !isAr;
    render();
};

document.getElementById('calcWeight').oninput = updateCalculator;
document.getElementById('calcKarat').onchange = updateCalculator;

fetchPrices();
setInterval(fetchPrices, 60000);
