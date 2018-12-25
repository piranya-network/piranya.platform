
function config($stateProvider, $urlRouterProvider, $ocLazyLoadProvider) {
    //$urlRouterProvider.otherwise("/index/main");
    $urlRouterProvider.otherwise("/page/" + uiStructure.defaultPageShortId);

    $ocLazyLoadProvider.config({
        // Set to true if you want to see what and when is dynamically loaded
        debug: false
    });

    $stateProvider
        .state('page', {
            abstract: true,
            url: "/page",
            templateUrl: "views/common/content.html",
        });
        /*
        .state('index.main', {
            url: "/main",
            templateUrl: "views/main.html",
            data: { pageTitle: 'Example view' }
        })
        .state('index.test2', {
            url: "/test2",
            templateUrl: "views/test2.html",
            data: { pageTitle: 'Test view' }
        })
        .state('index.minor', {
            url: "/minor",
            templateUrl: "views/minor.html",
            data: { pageTitle: 'Example view' }
        })
        */
    for (var i = 0; i < uiStructure.workspaces.length; i++) {
        var workspace = uiStructure.workspaces[i];
        for (var j = 0; j < workspace.pages.length; j++) {
            var page = workspace.pages[j];
            $stateProvider.state('page.' + page.shortId, { url: '/' + page.shortId, templateUrl: page.viewUrl, data: { workspaceId: workspace.id } });
        }
    }
    for (var i = 0; i < uiStructure.extraPages.length; i++) {
        var page = uiStructure.extraPages.pages[i];
        $stateProvider.state('page.' + page.shortId, { url: '/' + page.shortId, templateUrl: page.viewUrl });
    }
}
angular
    .module('piranya')
    .config(config)
    .run(function($rootScope, $state) {
        $rootScope.$state = $state;
    });
