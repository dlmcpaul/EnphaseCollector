/*global setupCharts, setupClickEvents, setupQnA */
const tz= /*[[${TZ}]]*/ 'Australia/Sydney';
const contextPath= /*[[${contextPath}]]*/ '/solar';
const refreshInterval= /*[[${refreshInterval}]]*/ 60000;
const refreshBarInterval= /*[[${refreshBarInterval}]]*/ '60';
const exportLimit= /*[[${exportLimit}]]*/ 0;

setupCharts(tz, contextPath, refreshInterval, exportLimit);
setupClickEvents(contextPath);
setupQnA();

document.getElementById("time-bar-id").style.setProperty('--duration', refreshBarInterval);

function updateStatusList(target, response) {
    "use strict";

    document.getElementById(target).outerHTML = response;
    document.getElementById("time-bar-id").style.setProperty('--duration', refreshBarInterval);
}