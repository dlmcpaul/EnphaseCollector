/*global Highcharts, refreshHistoryChart, console */

function switchTabs(destination, refreshUrl) {
    "use strict";
    // Find Source tab and remove active
    var currentTab = document.querySelector("#tabs li.is-active");
    currentTab.classList.remove("is-active");
    // Find Source Data and make hidden
    var currentDataId = currentTab.id + "-data";
    var currentContent = document.getElementById(currentDataId);
    currentContent.classList.add("is-hidden");

    // Use supplied destination and add active
    var destinationTab = document.getElementById(destination);
    destinationTab.classList.add("is-active");
    // Find destination Data and make visible
    var destinationDataId = destination + "-data";
    var destinationContent = document.getElementById(destinationDataId);
    destinationContent.classList.remove("is-hidden");

    if (refreshUrl !== null) {
        var chartDom = document.getElementById(destination + "_graph");
        var chart = Highcharts.charts[Highcharts.attr(chartDom, "data-highcharts-chart")];
        refreshHistoryChart(chart, refreshUrl);
    }

}
