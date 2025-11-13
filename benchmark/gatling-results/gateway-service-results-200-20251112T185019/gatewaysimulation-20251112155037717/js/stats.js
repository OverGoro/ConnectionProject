var stats = {
    type: "GROUP",
name: "Global Information",
path: "",
pathFormatted: "group_missing-name-b06d1",
stats: {
    "name": "Global Information",
    "numberOfRequests": {
        "total": "2000",
        "ok": "2000",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "99",
        "ok": "99",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "6927",
        "ok": "6927",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "1919",
        "ok": "1919",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "831",
        "ok": "831",
        "ko": "-"
    },
    "percentiles1": {
        "total": "1752",
        "ok": "1752",
        "ko": "-"
    },
    "percentiles2": {
        "total": "2332",
        "ok": "2332",
        "ko": "-"
    },
    "percentiles3": {
        "total": "3454",
        "ok": "3454",
        "ko": "-"
    },
    "percentiles4": {
        "total": "4489",
        "ok": "4489",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 82,
    "percentage": 4
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 245,
    "percentage": 12
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 1673,
    "percentage": 84
},
    "group4": {
    "name": "failed",
    "count": 0,
    "percentage": 0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "166.667",
        "ok": "166.667",
        "ko": "-"
    }
},
contents: {
"req_health-check-9583d": {
        type: "REQUEST",
        name: "Health Check",
path: "Health Check",
pathFormatted: "req_health-check-9583d",
stats: {
    "name": "Health Check",
    "numberOfRequests": {
        "total": "2000",
        "ok": "2000",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "99",
        "ok": "99",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "6927",
        "ok": "6927",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "1919",
        "ok": "1919",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "831",
        "ok": "831",
        "ko": "-"
    },
    "percentiles1": {
        "total": "1752",
        "ok": "1752",
        "ko": "-"
    },
    "percentiles2": {
        "total": "2332",
        "ok": "2332",
        "ko": "-"
    },
    "percentiles3": {
        "total": "3454",
        "ok": "3454",
        "ko": "-"
    },
    "percentiles4": {
        "total": "4489",
        "ok": "4489",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 82,
    "percentage": 4
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 245,
    "percentage": 12
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 1673,
    "percentage": 84
},
    "group4": {
    "name": "failed",
    "count": 0,
    "percentage": 0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "166.667",
        "ok": "166.667",
        "ko": "-"
    }
}
    }
}

}

function fillStats(stat){
    $("#numberOfRequests").append(stat.numberOfRequests.total);
    $("#numberOfRequestsOK").append(stat.numberOfRequests.ok);
    $("#numberOfRequestsKO").append(stat.numberOfRequests.ko);

    $("#minResponseTime").append(stat.minResponseTime.total);
    $("#minResponseTimeOK").append(stat.minResponseTime.ok);
    $("#minResponseTimeKO").append(stat.minResponseTime.ko);

    $("#maxResponseTime").append(stat.maxResponseTime.total);
    $("#maxResponseTimeOK").append(stat.maxResponseTime.ok);
    $("#maxResponseTimeKO").append(stat.maxResponseTime.ko);

    $("#meanResponseTime").append(stat.meanResponseTime.total);
    $("#meanResponseTimeOK").append(stat.meanResponseTime.ok);
    $("#meanResponseTimeKO").append(stat.meanResponseTime.ko);

    $("#standardDeviation").append(stat.standardDeviation.total);
    $("#standardDeviationOK").append(stat.standardDeviation.ok);
    $("#standardDeviationKO").append(stat.standardDeviation.ko);

    $("#percentiles1").append(stat.percentiles1.total);
    $("#percentiles1OK").append(stat.percentiles1.ok);
    $("#percentiles1KO").append(stat.percentiles1.ko);

    $("#percentiles2").append(stat.percentiles2.total);
    $("#percentiles2OK").append(stat.percentiles2.ok);
    $("#percentiles2KO").append(stat.percentiles2.ko);

    $("#percentiles3").append(stat.percentiles3.total);
    $("#percentiles3OK").append(stat.percentiles3.ok);
    $("#percentiles3KO").append(stat.percentiles3.ko);

    $("#percentiles4").append(stat.percentiles4.total);
    $("#percentiles4OK").append(stat.percentiles4.ok);
    $("#percentiles4KO").append(stat.percentiles4.ko);

    $("#meanNumberOfRequestsPerSecond").append(stat.meanNumberOfRequestsPerSecond.total);
    $("#meanNumberOfRequestsPerSecondOK").append(stat.meanNumberOfRequestsPerSecond.ok);
    $("#meanNumberOfRequestsPerSecondKO").append(stat.meanNumberOfRequestsPerSecond.ko);
}
