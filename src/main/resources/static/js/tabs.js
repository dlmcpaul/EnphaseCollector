function switchTabs(destination) {
    // Find Source tab and remove active
    currentTab = document.querySelector('#tabs li.is-active');
    currentTab.classList.remove("is-active");
    // Find Source Data and make hidden
    currentDataId = currentTab.id + '-data';
    currentContent = document.getElementById(currentDataId);
    currentContent.classList.add("is-hidden");

    // Use supplied destination and add active
    destinationTab = document.getElementById(destination);
    destinationTab.classList.add("is-active");
    // Find destination Data and make visible
    destinationDataId = destination + '-data';
    destinationContent = document.getElementById(destinationDataId);
    destinationContent.classList.remove("is-hidden");
}
