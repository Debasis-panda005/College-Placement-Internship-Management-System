/**
 * Placement Analytics & Illustrative Demo Dataset
 * STRICT SEPARATION NOTE:
 * This data is used solely for the public landing page demonstration and UI preview.
 * It is completely decoupled from MySQL database tables and actual applicant records.
 */

var PlacementAnalytics = (function() {

    // Multi-year illustrative dataset
    var demoDataset = {
        "2022-23": {
            companiesVisited: 25,
            studentsEligible: 300,
            studentsPlaced: 180,
            placementPercentage: 60,
            averageLpa: 4.2,
            highestLpa: 12.0,
            lowestLpa: 2.4
        },
        "2023-24": {
            companiesVisited: 32,
            studentsEligible: 350,
            studentsPlaced: 245,
            placementPercentage: 70,
            averageLpa: 4.8,
            highestLpa: 16.0,
            lowestLpa: 2.8
        },
        "2024-25": {
            companiesVisited: 38,
            studentsEligible: 400,
            studentsPlaced: 300,
            placementPercentage: 75,
            averageLpa: 5.2,
            highestLpa: 18.0,
            lowestLpa: 3.0
        },
        "2025-26": {
            companiesVisited: 45,
            studentsEligible: 450,
            studentsPlaced: 360,
            placementPercentage: 80,
            averageLpa: 5.8,
            highestLpa: 22.0,
            lowestLpa: 3.2
        }
    };

    var years = ["2022-23", "2023-24", "2024-25", "2025-26"];

    var chartInstances = {};

    function initCharts() {
        var pctCtx = document.getElementById("chartPlacementPct");
        var lpaCtx = document.getElementById("chartLpaComparison");
        var compCtx = document.getElementById("chartCompaniesVisited");
        var placedCtx = document.getElementById("chartStudentsPlaced");

        if (!pctCtx || !lpaCtx || !compCtx || !placedCtx) return;

        // Chart 1: Year-wise Placement Percentage
        chartInstances.pct = new Chart(pctCtx, {
            type: 'line',
            data: {
                labels: years,
                datasets: [{
                    label: 'Placement %',
                    data: years.map(function(y) { return demoDataset[y].placementPercentage; }),
                    borderColor: '#1a365d',
                    backgroundColor: 'rgba(26, 54, 93, 0.12)',
                    fill: true,
                    borderWidth: 3
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        max: 100,
                        ticks: {
                            callback: function(v) { return v + '%'; }
                        }
                    }
                }
            }
        });

        // Chart 2: Average, Highest, and Lowest LPA Comparison
        chartInstances.lpa = new Chart(lpaCtx, {
            type: 'bar',
            data: {
                labels: years,
                datasets: [
                    {
                        label: 'Lowest LPA',
                        data: years.map(function(y) { return demoDataset[y].lowestLpa; }),
                        backgroundColor: '#94a3b8'
                    },
                    {
                        label: 'Average LPA',
                        data: years.map(function(y) { return demoDataset[y].averageLpa; }),
                        backgroundColor: '#1a365d'
                    },
                    {
                        label: 'Highest LPA',
                        data: years.map(function(y) { return demoDataset[y].highestLpa; }),
                        backgroundColor: '#d97706'
                    }
                ]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        ticks: {
                            callback: function(v) { return v + ' LPA'; }
                        }
                    }
                }
            }
        });

        // Chart 3: Companies Visited Each Year
        chartInstances.comp = new Chart(compCtx, {
            type: 'bar',
            data: {
                labels: years,
                datasets: [{
                    label: 'Companies Visited',
                    data: years.map(function(y) { return demoDataset[y].companiesVisited; }),
                    backgroundColor: ['#3b82f6', '#0ea5e9', '#0284c7', '#0369a1']
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        ticks: {
                            callback: function(v) { return v; }
                        }
                    }
                }
            }
        });

        // Chart 4: Students Placed vs Eligible Each Year
        chartInstances.placed = new Chart(placedCtx, {
            type: 'bar',
            data: {
                labels: years,
                datasets: [
                    {
                        label: 'Eligible Students',
                        data: years.map(function(y) { return demoDataset[y].studentsEligible; }),
                        backgroundColor: '#cbd5e1'
                    },
                    {
                        label: 'Students Placed',
                        data: years.map(function(y) { return demoDataset[y].studentsPlaced; }),
                        backgroundColor: '#15803d'
                    }
                ]
            },
            options: {
                responsive: true
            }
        });
    }

    function selectYear(year) {
        if (!demoDataset[year]) year = "2025-26";

        var data = demoDataset[year];

        // Animate / update KPI cards
        updateKpi("kpiCompanies", data.companiesVisited);
        updateKpi("kpiPlaced", data.studentsPlaced);
        updateKpi("kpiPercentage", data.placementPercentage + "%");
        updateKpi("kpiAverage", data.averageLpa + " LPA");
        updateKpi("kpiHighest", data.highestLpa + " LPA");
        updateKpi("kpiLowest", data.lowestLpa + " LPA");

        var badge = document.getElementById("selectedYearBadge");
        if (badge) {
            badge.textContent = year;
        }

        // Update active class on year buttons
        var buttons = document.querySelectorAll(".btn-year-select");
        buttons.forEach(function(btn) {
            if (btn.getAttribute("data-year") === year) {
                btn.classList.add("active");
            } else {
                btn.classList.remove("active");
            }
        });
    }

    function updateKpi(elementId, val) {
        var el = document.getElementById(elementId);
        if (!el) return;
        el.style.transform = "scale(0.95)";
        el.style.opacity = "0.7";
        setTimeout(function() {
            el.textContent = val;
            el.style.transform = "scale(1)";
            el.style.opacity = "1";
        }, 120);
    }

    return {
        init: function() {
            initCharts();
            selectYear("2025-26");
        },
        selectYear: selectYear,
        getDataset: function() {
            return demoDataset;
        }
    };
})();

document.addEventListener("DOMContentLoaded", function() {
    PlacementAnalytics.init();
});
