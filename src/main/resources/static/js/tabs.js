/*global Highcharts, getChart, refreshTarget, console */

function add(x, y) {
    "use strict";

    return x + y;
}

function updateHistory(target, response) {
    "use strict";

    var chart = getChart(target);

    chart.series[0].setData(response.gridExport);
    chart.series[1].setData(response.solarConsumption);
    chart.series[2].setData(response.gridImport);

    var billTarget = target.substring(0,target.length-6) + "-bill";

    var labelValue = document.getElementById(billTarget);
    labelValue.innerText = "Estimated Cost for this period is $" +
        parseFloat(response.billEstimate).toFixed(2) +
        " which is calculated from $" +
        add(parseFloat(response.importCost), (parseFloat(response.baseCost))).toFixed(2) +
        " in electricity costs and $" +
        parseFloat(response.exportEarnings).toFixed(2) +
        " of export credits";
}

// Function works if content element is named the same as the tab id with -data appended
function switchToTab(target, refreshUrl) {
    "use strict";

    // Find current tab and remove active
    var currentTab = document.querySelector("#tabs li.is-active");
    currentTab.classList.remove("is-active");
    // Find associated content element and make hidden
    var currentContentId = currentTab.id + "-data";
    var currentContent = document.getElementById(currentContentId);
    currentContent.classList.add("is-hidden");

    // Use supplied target and add active
    var targetTab = document.getElementById(target);
    targetTab.classList.add("is-active");
    // Find target content element and make visible
    var targetContentId = target + "-data";
    var targetContent = document.getElementById(targetContentId);
    targetContent.classList.remove("is-hidden");

    if (refreshUrl !== null) {
        refreshTarget(target + "-graph", refreshUrl, "json", updateHistory);
    }
}
