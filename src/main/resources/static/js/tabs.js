/*global Highcharts, getChart, refreshTarget, console */

function updateHistory(target, response) {
    "use strict";

    var chart = getChart(target);

    chart.series[0].setData(response.gridExport);
    chart.series[1].setData(response.solarConsumption);
    chart.series[2].setData(response.gridImport);
}

function switchToTab(target, refreshUrl) {
    "use strict";

    // Find Source tab and remove active
    var currentTab = document.querySelector("#tabs li.is-active");
    currentTab.classList.remove("is-active");
    // Find Source Data and make hidden
    var currentDataId = currentTab.id + "-data";
    var currentContent = document.getElementById(currentDataId);
    currentContent.classList.add("is-hidden");

    // Use supplied destination and add active
    var destinationTab = document.getElementById(target);
    destinationTab.classList.add("is-active");
    // Find destination Data and make visible
    var destinationDataId = target + "-data";
    var destinationContent = document.getElementById(destinationDataId);
    destinationContent.classList.remove("is-hidden");

    if (refreshUrl !== null) {
        refreshTarget(target + "_graph", refreshUrl, "json", updateHistory);
    }
}
