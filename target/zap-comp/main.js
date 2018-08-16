var rawNewJson = {}
var rawOldJson = {}

var currentBuildParsed;
var lastBuild;
var addWarning = () => {
}


var error = false

// Converts the build report to an easier format
var parseRawBuild = function(build){
	var alerts = []

	// If there is only one site, it's put into build.site by zap, otherwise [build.site]
	if (!Array.isArray(build.site)) { build.site = [build.site] }
    console.log(build)
	build.site.forEach(function(data){
		if (data && data.alerts && data.alerts.length != 0){
			data.alerts.forEach((alert) => {
				
				var alert_ = {
					alert:alert.alert,
					confidence: alert.confidence,
					riskCode: alert.riskcode,
					description: alert.desc,
					solution: alert.solution,
					wascid: alert.wascid,
					instances: []            
				}

				alert.instances.forEach((instance) => {
	
					alert_.instances.push({
						uri: instance.uri,
						method: instance.method,
						param: instance.param,
						evidence: instance.evidence,
					})                    
				})

				alerts.push(alert_)
			})
		}
	})
	return alerts
}

var getNew = function(){
	var currentBuildParsed = parseRawBuild(rawNewJson)

	if (rawOldJson){
		var newAlerts = []
		var newAlertInstances = []

		var lastBuild = parseRawBuild(rawOldJson)
		currentBuildParsed.forEach((alert) => {

			var foundAlert = false
			lastBuild.forEach((oldAlert) => {
				if (oldAlert.wascid == alert.wascid && oldAlert.alert == alert.alert && !alert.done && !oldAlert.done){
					alert.done = true;
					oldAlert.done = true;

					foundAlert = true

					alert.instances.forEach((instance) => {
						var newInstance = true
						oldAlert.instances.forEach((lastInstance) => {

							if (instance.uri.split("?")[0] == lastInstance.uri.split("?")[0]){
								newInstance = false
			
							}
						})

						if (newInstance){
							if (!newAlertInstances[alert.uri]){
								newAlertInstances[alert.uri] = angular.copy(alert)
								newAlertInstances[alert.uri].instances = []
								newAlertInstances[alert.uri].hasNewInstances = true
							}

							newAlertInstances[alert.uri].instances.push(instance)
						}
					})
				}
			})

			if (!foundAlert){
				alert.isNew = true
				newAlerts.push(alert)
			}
		})

		newAlertInstances = Object.values(newAlertInstances)
		var res = newAlerts.concat(newAlertInstances)

        if (!res.length>0 && !error){
            addWarning("No new alerts for this build.", "success")
        }

		return res
	} else{
		return currentBuildParsed
	}
}


var App = angular.module("zap", [])
App.controller('mainController', function($scope, $rootScope, $http, $window){

	$scope.counts = {
		high: 0,
		medium: 0,
		low: 0
	}

	$scope.warnings = []
	$scope.addWarning = (warning, type) => {
	    $scope.warnings.push({warning:warning, type:type})
	}

	addWarning = $scope.addWarning;


	$scope.colors = ['low-alert', 'medium-alert', 'high-alert']
	$scope.alerts = []

	$scope.parseAlerts = () => {
		Object.keys($scope.counts).forEach((k) => { $scope.counts[k] = 0})

		$scope.alerts.sort((a, b) => {
			return a.riskCode < b.riskCode
		})


		$scope.alerts.forEach((data) => {
		    data.showInstances = true

			switch(data.riskCode){
				case "3":
					$scope.counts.high++;
					break;
				case "2":
					$scope.counts.medium++;
					break;
				case "1":
					$scope.counts.low++;
					break;
			}
		})
	}

	$scope.load = () => {

	    $http({
	        method: 'get',
	        url: "zap-raw.json",

	    }).then((data, status) => {
	        if (data && (data.status==200 || data.status==304) && data.data){
	            rawNewJson = data.data;
	        }

	        return $http({
	            method: 'get',
	            url: "zap-raw-old.json"
	         })

	    }).then((data, status) => {
	        if (data && (data.status==200 || data.status==304) && data.data){
	            rawOldJson = data.data;
	        }

	        return true
	    }).catch((e) => {
	        console.log(e)
	            $scope.addWarning("Could not retrieve previous build report. All alerts are shown.", "failure")
	            $scope.showAll = true
	            error = true
	    }).finally(() => {
		    $scope.alerts = getNew();
		    $scope.parseAlerts();

	    })
	}

	$scope.showAll = false
	$scope.loadAll = () => {
	    $scope.warnings = []
		$scope.alerts = parseRawBuild(rawNewJson)
		$scope.showAll = true

		$scope.parseAlerts();

		if ($scope.alerts.length <= 0){
		    $scope.addWarning("No alerts for this build", "success")
		}
	}

	$scope.goBack = () => {
	    $window.history.back();
	}

}).filter('to_trusted', ['$sce', function($sce){
    return function(text) {
    	return $sce.trustAsHtml(text);
    };
}]);