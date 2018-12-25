
var uiStructure = {
    defaultPageShortId: 'basic_trading',
    workspaces: [
        {
            id: 'trading',
            title: 'Trading',
            icon: 'fa-exchange-alt',
            category: 'manual',
            pages: [
                { id: 'foundation.piranya.extensions.basic.pages.BasicTradingPage', shortId: 'basic_trading', title: 'Basic Trading', icon: 'fa-desktop', viewUrl: 'views/main.html' }
            ]
        },
        {
            id: 'analysis',
            title: 'Analysis',
            icon: 'fa-graduation-cap',
            category: 'manual',
            pages: [ ]
        },
        {
            id: 'auto_trading',
            title: 'Auto Trad.',
            icon: 'fa-keyboard',
            category: 'auto',
            pages: [ ]
        },
        {
            id: 'structures',
            title: 'Structures',
            icon: 'fa-snowflake',
            category: 'auto',
            pages: [ ]
        },
        {
            id: 'account',
            title: 'Account',
            icon: 'fa-address-card',
            category: 'accounting',
            pages: [ ]
        },
        {
            id: 'liquidity',
            title: 'Liquidity',
            icon: 'fa-address-book',
            category: 'accounting',
            pages: [ ]
        }
    ],
    extraPages: []
};
