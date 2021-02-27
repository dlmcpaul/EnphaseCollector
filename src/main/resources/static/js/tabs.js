/*global Highcharts, getChart, refreshTarget, console */

function add(x, y) {
    "use strict";

    return x + y;
}

function updateHistory(target, response) {
    "use strict";

    const chart = getChart(target);

    chart.series[0].setData(response.gridExport);
    chart.series[1].setData(response.solarConsumption);
    chart.series[2].setData(response.gridImport);

    const billTarget = target.substring(0, target.length-6) + "-bill";

    const labelValue = document.getElementById(billTarget);
    labelValue.innerText = "Estimated Cost for this period is $" +
        parseFloat(response.billEstimate).toFixed(2) +
        " which is calculated from $" +
        add(parseFloat(response.importCost), (parseFloat(response.baseCost))).toFixed(2) +
        " in electricity costs and $" +
        parseFloat(response.exportEarnings).toFixed(2) +
        " of export credits";
}

// Function works if content element is named the same as the tab id with -data appended
function switchToTab(source, target) {
    "use strict";

    // Find current tab and remove active
    const currentTab = document.querySelector("#" + source + " li.is-active");
    currentTab.classList.remove("is-active");
    // Find associated content element and make hidden
    const currentContentId = currentTab.id + "-data";
    const currentContent = document.getElementById(currentContentId);
    currentContent.classList.add("is-hidden");

    // Use supplied target and add active
    const targetTab = document.getElementById(target);
    targetTab.classList.add("is-active");
    // Find target content element and make visible
    const targetContentId = target + "-data";
    const targetContent = document.getElementById(targetContentId);
    targetContent.classList.remove("is-hidden");
}

// Function works if content element is named the same as the tab id with -data appended
// and if the graph to refresh is named the same as the tab id with -graph appended
function switchToHistoryTab(source, target, refreshUrl) {
    "use strict";

    switchToTab(source, target);

    if (refreshUrl !== null) {
        refreshTarget(target + "-graph", refreshUrl, "json", updateHistory);
    }
}
