
function MainController($scope) {
    $scope.workspacesByCategory = CollectionUtils.groupBy(uiStructure.workspaces, 'category');
    
    this.userName = 'Example user';
    this.helloText = 'Welcome in SeedProject';
    this.descriptionText = 'It is an application skeleton for a typical AngularJS web app. You can use it to quickly bootstrap your angular webapp projects and dev environment for these projects.';
    
    setTimeout(function() {
        for (var i = 0; i < 6; i++) {
        $("#mw_line_chart" + i).sparkline([345, 404, 305, 455, 378, 567, 586, 685, 458, 742, 565], {
	        type: 'line',
	        width: '100%',
	        height: '40',
	        spotRadius: 0,
	        lineWidth: 1,
	        lineColor: '#ffffff',
	        fillColor: false,
	        minSpotColor: false,
	        maxSpotColor: false,
	        highlightLineColor: '#ffffff',
	        highlightSpotColor: '#ffffff',
	        tooltipChartTitle: 'Page Views',
	        spotColor: '#ffffff',
	        valueSpots: {
	            '0:': '#ffffff'
	        }
	    });
        }
        /*
        var data = [{
	            y: '2016',
	            a: 35,
	            b: 90
	        }, {
	            y: '2017',
	            a: 45,
	            b: 75
	        }, {
	            y: '2018',
	            a: 55,
	            b: 50
	        }, {
	            y: '2019',
	            a: 65,
	            b: 60
	        }, {
	            y: '2020',
	            a: 75,
	            b: 65
	        }, {
	            y: '2021',
	            a: 90,
	            b: 70
	        }, {
	            y: '2022',
	            a: 95,
	            b: 75
	        }, {
	            y: '2023',
	            a: 105,
	            b: 75
	        }, {
	            y: '2024',
	            a: 115,
	            b: 85
	        }, {
	            y: '2025',
	            a: 125,
	            b: 85
	        }, {
	            y: '2026',
	            a: 145,
	            b: 95
	        }],
	            config = {
	            data: data,
	            xkey: 'y',
	            ykeys: ['a', 'b'],
	            labels: ['Total Income', 'Total Outcome'],
	            fillOpacity: 0.6,
	            hideHover: 'auto',
	            behaveLikeLine: true,
	            resize: true,
	            pointFillColors: ['#ffffff'],
	            pointStrokeColors: ['black']//,
	            //lineColors: [MaterialLab.APP_COLORS.mw_purple, MaterialLab.APP_COLORS.success],
	            //barColors: [MaterialLab.APP_COLORS.mw_purple, MaterialLab.APP_COLORS.success]
	        };
	        config.element = 'sparkline1';
	        Morris.Area(config);
        */
    }, 200);
    

	//alert('MainController.init');
};


angular.module('piranya').controller('MainController', MainController);