var ZAP_RAW_BUILD = "zap-raw.json"
var ZAP_LAST_RAW_BUILD = "zap-raw-old.json"
var ZAP_FALSE_POSITIVES = "zap-false-positives.json"

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
					name:alert.name,
					confidence: alert.confidence,
					riskCode: alert.riskcode,
					description: alert.desc,
					solution: alert.solution,
					wascid: alert.wascid,
					cweid: alert.cweid,
					id : alert.name.replace(/[^a-zA-Z0-9/g]/g, "-").toLowerCase(),
					instances: []
				}

				alert.instances.forEach(instance => {
					alertReformatted.instances.push({
						uri: instance.uri,
						method: instance.method,
						param: instance.param,
						attack: instance.attack,
						evidence: instance.evidence,
					});
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

// Creates a false positive object from a given alert and instance
var createFalsePositive = function(alert, instance) {
	return {
		name : alert.name,
		cweid : alert.cweid,
		wascid : alert.wascid,
		uri: instance.uri,
		method: instance.method,
		param: instance.param,
		attack: instance.attack,
		evidence: instance.evidence
	}
}

// A false positive matches an alert instance if all the values set in the false positive match
// the values in the instance and alert
var falsePositiveMatch = function(falsePositive, alert, instance) {
	return Object.keys(falsePositive).every(key => {
		return !falsePositive.hasOwnProperty(key) || falsePositive[key] === alert[key] || falsePositive[key] === instance[key]
	})
}

// Sets the 'suppressed' flag in each instance, according to the provided false positives array
var setSuppressionFlags = function(alerts, falsePositives) {
    alerts.forEach(alert => {
        alert.instances.forEach(instance => {
            instance.suppressed = falsePositives.some(falsePositive => {
            	return falsePositiveMatch(falsePositive, alert, instance)
            })
        })
    })
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
	$scope.falsePositives = []
	$scope.colors = ['low-alert', 'medium-alert', 'high-alert']
	$scope.warnings = []
	$scope.showAll = false

	$scope.addWarning = (warning, type) => {
	    $scope.warnings.push({warning:warning, type:type})
	}

	$scope.goBack = () => {
	    $window.history.back()
	}

	$scope.updateCounts = (countSuppressed) => {
		var highTotal = 0
		var medTotal = 0
		var lowTotal = 0
		var suppressedCount = 0

		$scope.currentAlerts.forEach(alert => {
		    alert.showInstances = true
		    alert.instances.forEach(instance => {
                instance.showMore = false

                if (!instance.suppressed) {
                    switch(alert.riskCode) {
                        case "3":
                            highTotal += 1
                            break
                        case "2":
                            medTotal += 1
                            break
                        case "1":
                            lowTotal += 1
                            break
                    }
                } else {
                    suppressedCount += 1
                }
		    })
		})

		$scope.counts.high = formatCountString(highTotal, 0, 0)
		$scope.counts.medium = formatCountString(medTotal, 0, 0)
		$scope.counts.low = formatCountString(lowTotal, 0, 0)
		$scope.counts.suppressed = suppressedCount
	}

    // Loads current build, previous build and false positives
	$scope.load = () => {
        $http({
            method: 'get',
            url: ZAP_RAW_BUILD,
        }).then((response, status) => {
            if (response && (response.status==200 || response.status==304) && response.data){
                $scope.currentAlerts = parseRawBuild(response.data)
            }
        }).finally(
            $http({
                method: 'get',
                url: ZAP_LAST_RAW_BUILD
            }).then((response, status) => {
                if (response && (response.status==200 || response.status==304) && response.data){
                    $scope.previousAlerts = parseRawBuild(response.data)
                }
            }).finally(
                $http({
                    method: 'get',
                    url: ZAP_FALSE_POSITIVES
                }).then((response, status) => {
                    if (response && (response.status==200 || response.status==304) && response.data){
                        $scope.falsePositives = response.data
                    }
                }).finally(() => {
			        setSuppressionFlags($scope.currentAlerts, $scope.falsePositives)
			        $scope.updateCounts()
			        $scope.showTrueAlerts()
			    })
            )
        )
	}

	$scope.showAllAlerts = () => {
	    $scope.warnings = []
		$scope.showAll = true
		$scope.alerts = $scope.currentAlerts

		if ($scope.alerts.length <= 0){
		    $scope.addWarning("No alerts for this build", "success")
		}
	}

	$scope.showTrueAlerts = () => {
	    $scope.warnings = []
		$scope.showAll = false
		$scope.alerts = $scope.currentAlerts
		    .map(alert => {
	    		var filteredInstances = alert.instances.filter(instance => !instance.suppressed)
	    		return Object.assign({}, alert, {instances: filteredInstances})
		    }).filter(alert => alert.instances.length > 0)

		if ($scope.currentAlerts.length <= 0 ){
		    $scope.addWarning("No alerts for this build", "success")
		}

		if ($scope.alerts.length <= 0 ){
		    $scope.addWarning("No alerts for this build (false positives hidden).", "success")
		}
	}

	$scope.copyFalsePositiveToClipboard = (alert, instance) => {
		var falsePositive = createFalsePositive(alert, instance);
		var selectBox =  document.createElement("textarea")
	    selectBox.style.position = 'fixed';
	    selectBox.style.left = '0';
	    selectBox.style.top = '0';
	    selectBox.style.opacity = '0';
    	selectBox.value = JSON.stringify(falsePositive, null, 2);
	    document.body.appendChild(selectBox);
	    selectBox.focus();
	    selectBox.select();
	    document.execCommand('copy');
	    document.body.removeChild(selectBox);
	}

	$scope.isTargetCollapsable = (id) => {
		return document.getElementById(id).clientHeight > 50
	}

}).filter('to_trusted', ['$sce', function($sce){
    return function(text) {
    	return $sce.trustAsHtml(text)
    }
}])