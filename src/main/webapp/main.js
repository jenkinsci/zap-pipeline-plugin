var ZAP_RAW_BUILD = "zap-raw.json"
var ZAP_LAST_RAW_BUILD = "zap-raw-old.json"
var ZAP_FALSE_POSITIVES = "zap-false-positives.json"

// Converts the build report to an easier format
var parseRawBuild = function(build) {
	var alerts = []

	if (!build || !build.site)
		return alerts

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

// Compares two alerts to check if they are the same
var alertsAreEqual = function(alert1, alert2) {
    return alert1.name === alert2.name && alert1.cweid === alert2.cweid && alert1.wascid === alert2.wascid &&
           alert1.riskCode === alert2.riskCode
}

// Compares two instances to check if they are the same
var instancesAreEqual = function(instance1, instance2) {
    return instance1.uri === instance2.uri && instance1.method === instance2.method &&
           instance1.param === instance2.param
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
App.controller('mainController', function($scope, $rootScope, $http, $window, $q) {

	// Initialise variables
	$scope.counts = {
		high : "0 (0)",
		medium : "0 (0)",
		low : "0 (0)",
		suppressed : 0
	}
	$scope.alerts = []
	$scope.suppressedAlerts = []
	$scope.currentAlerts = null
	$scope.previousAlerts = null
	$scope.falsePositives = null
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
		var highNew = 0
		var highFixed = 0
		var medTotal = 0
		var medNew = 0
		var medFixed = 0
		var lowTotal = 0
		var lowNew = 0
		var lowFixed = 0
		var suppressedCount = 0
        var previousAlerts = angular.copy($scope.previousAlerts)

        // Loop over all alerts
		$scope.currentAlerts.forEach(alert => {
		    alert.showInstances = true
		    // Get any matching previous alert, and remove from array
		    var prevAlertIndex = previousAlerts.findIndex(prevAlert => alertsAreEqual(prevAlert, alert))
            var prevAlert = undefined
            if(prevAlertIndex >= 0) {
                prevAlert = previousAlerts.splice(prevAlertIndex, 1)[0]
            }

            // Loop over all instances in current alert
		    alert.instances.forEach(instance => {
		        // Find matching previous instance (if there is one) and remove it, and set newAlert flag
                var prevInstanceIndex = prevAlert ? prevAlert.instances.findIndex(
                        prevInstance => instancesAreEqual(prevInstance, instance)
                    ) : -1
                if (prevInstanceIndex >= 0) {
                    prevAlert.instances.splice(prevInstanceIndex, 1)
                }
                instance.showMore = false
                instance.newAlert = prevInstanceIndex === -1

                // Increment risk-based counters
                if (!instance.suppressed) {
                    switch(alert.riskCode) {
                        case "3":
                            highTotal += 1
                            highNew += instance.newAlert ? 1 : 0
                            break
                        case "2":
                            medTotal += 1
                            medNew += instance.newAlert ? 1 : 0
                            break
                        case "1":
                            lowTotal += 1
                            lowNew += instance.newAlert ? 1 : 0
                            break
                    }
                } else {
                    suppressedCount += 1
                }
		    })

		    // Add any remaining instances in previous alert to risk-based counters
		    if (prevAlert) {
                switch(prevAlert.riskCode) {
                    case "3":
                        highFixed += prevAlert.instances.length || 0
                        break
                    case "2":
                        medFixed += prevAlert.instances.length || 0
                        break
                    case "1":
                        lowFixed += prevAlert.instances.length || 0
                        break
                }
            }
		})

		// Loop over any remaining previous alerts, and add to risk based counters
		previousAlerts.forEach(prevAlert => {
            switch(prevAlert.riskCode) {
                case "3":
                    highFixed += prevAlert.instances.length || 0
                    break
                case "2":
                    medFixed += prevAlert.instances.length || 0
                    break
                case "1":
                    lowFixed += prevAlert.instances.length || 0
                    break
            }
		})

        // Format and set counts
		$scope.counts.high = formatCountString(highTotal, highNew, highFixed)
		$scope.counts.medium = formatCountString(medTotal, medNew, medFixed)
		$scope.counts.low = formatCountString(lowTotal, lowNew, lowFixed)
		$scope.counts.suppressed = suppressedCount
	}

	$scope.updateData = () => {
		if ($scope.currentAlerts && $scope.falsePositives && $scope.previousAlerts) {
	        setSuppressionFlags($scope.currentAlerts, $scope.falsePositives)
	        $scope.updateCounts()
	        $scope.showTrueAlerts()
		}
	}

    // Loads current build, previous build and false positives
	$scope.load = () => {
		// Collect current build data
		$http({
            method: 'get',
            url: ZAP_RAW_BUILD,
        }).success(data => {
            $scope.currentAlerts = parseRawBuild(data)
        }).error(() => {
        	$scope.currentAlerts = []
        }).finally($scope.updateData)

        // Collect last build data
        $http({
            method: 'get',
            url: ZAP_LAST_RAW_BUILD
        }).success(data => {
            $scope.previousAlerts = parseRawBuild(data)
        }).error(() => {
        	$scope.previousAlerts = []
        }).finally($scope.updateData)

        // Collect false positives file
        $http({
            method: 'get',
            url: ZAP_FALSE_POSITIVES
        }).success(data => {
            $scope.falsePositives = data || []
        }).error(() => {
        	$scope.falsePositives = []
        }).finally($scope.updateData)
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
		} else if ($scope.alerts.length <= 0 ){
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
	    falsePositive.uri = '^' + falsePositive.uri.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&') + '$';
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