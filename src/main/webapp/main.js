// Converts the build report to an easier format
var parseRawBuild = function(build) {
	var alerts = []

	// If there is only one site, it's put into build.site by zap, otherwise [build.site]
	if (!Array.isArray(build.site)) {
	 	build.site = [build.site]
	}

	build.site.forEach(function(data) {
		if (data && data.alerts && data.alerts.length != 0) {
			data.alerts.forEach((alert) => {
				
				var alertReformatted = {
					alert:alert.alert,
					confidence: alert.confidence,
					riskCode: alert.riskcode,
					description: alert.desc,
					solution: alert.solution,
					wascid: alert.wascid,
					instances: []            
				}

				alert.instances.forEach((instance) => {
					alertReformatted.instances.push({
						uri: instance.uri,
						method: instance.method,
						param: instance.param,
						evidence: instance.evidence,
					})                    
				})

				alerts.push(alertReformatted)
			})
		}
	})

    //Sort alerts into risk order (highest first)
	alerts.sort((a, b) => {
		return a.riskCode > b.riskCode ? -1 : (a.riskCode < b.riskCode ? 1 : 0)
	})
	
	return alerts
}

// Formats a string of the totalcounts, with new/fixed counts
var formatCountString = function(totalCounts, newCounts, fixedCounts) {
	if (newCounts > 0 && fixedCounts > 0)
		return totalCounts + " (+" + newCounts +  ", -" + fixedCounts + ")"

	if (newCounts > 0)
		return totalCounts + " (+" + newCounts + ")"

	if (fixedCounts > 0)
		return totalCounts + " (-" + fixedCounts + ")"

	return totalCounts + " (0)"
}

// Main App
var App = angular.module("zap", [])
App.controller('mainController', function($scope, $rootScope, $http, $window) {

	// Initialise variables
	$scope.counts = {
		high : "0 (0)",
		medium : "0 (0)",
		low : "0 (0)"
	}
	$scope.alerts = []
	$scope.suppressedAlerts = []
	$scope.currentAlerts = []
	$scope.previousAlerts = []
	$scope.colors = ['low-alert', 'medium-alert', 'high-alert']
	$scope.warnings = []
	$scope.showAll = false
	$scope.currentBuild = {}
	$scope.lastBuild = {}

	$scope.addWarning = (warning, type) => {
	    $scope.warnings.push({warning:warning, type:type})
	}

	$scope.goBack = () => {
	    $window.history.back();
	}

	$scope.updateCounts = () => {
		highTotal = 0
		medTotal = 0
		lowTotal = 0

		$scope.currentAlerts.forEach((data) => {
		    data.showInstances = true

			switch(data.riskCode) {
				case "3":
					highTotal += data.instances.length;
					break;
				case "2":
					medTotal += data.instances.length;
					break;
				case "1":
					lowTotal += data.instances.length;
					break;
			}
		})

		$scope.counts.high = formatCountString(highTotal, 0, 0)
		$scope.counts.medium = formatCountString(medTotal, 0, 0)
		$scope.counts.low = formatCountString(lowTotal, 0, 0)
	}

	$scope.load = () => {
	    $http({
	        method: 'get',
	        url: "zap-raw.json",
	    }).then((data, status) => {
	        if (data && (data.status==200 || data.status==304) && data.data){
	            $scope.currentBuild = data.data;
	        }
	        return $http({
	            method: 'get',
	            url: "zap-raw-old.json"
	         })
	    }).then((data, status) => {
	        if (data && (data.status==200 || data.status==304) && data.data){
	            $scope.lastBuild = data.data;
	        }
	        return true
	    }).catch((e) => {
            $scope.addWarning("Could not retrieve previous build report. All alerts are shown.", "failure")
            $scope.showAll = true
            error = true
	    }).finally(() => {
		    $scope.currentAlerts = parseRawBuild($scope.currentBuild);
		    $scope.alerts = $scope.currentAlerts;
		    $scope.previousAlerts = parseRawBuild($scope.lastBuild);
		    $scope.updateCounts();
	    })
	}

	$scope.loadAll = () => {
	    $scope.warnings = []
		$scope.showAll = true
		$scope.alerts = $scope.currentAlerts + $scope.previousAlerts
		$scope.alerts.sort((a, b) => {
			return a.riskCode > b.riskCode ? -1 : (a.riskCode < b.riskCode ? 1 : 0)
		});

		if ($scope.alerts.length <= 0){
		    $scope.addWarning("No alerts for this build", "success")
		}
	}

}).filter('to_trusted', ['$sce', function($sce){
    return function(text) {
    	return $sce.trustAsHtml(text);
    };
}]);