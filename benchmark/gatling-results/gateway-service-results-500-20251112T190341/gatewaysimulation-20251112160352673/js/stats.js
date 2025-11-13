var stats = {
    type: "GROUP",
name: "Global Information",
path: "",
pathFormatted: "group_missing-name-b06d1",
stats: {
    "name": "Global Information",
    "numberOfRequests": {
        "total": "5000",
        "ok": "5000",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "118",
        "ok": "118",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "12210",
        "ok": "12210",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "7375",
        "ok": "7375",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "2043",
        "ok": "2043",
        "ko": "-"
    },
    "percentiles1": {
        "total": "7990",
        "ok": "7990",
        "ko": "-"
    },
    "percentiles2": {
        "total": "8772",
        "ok": "8772",
        "ko": "-"
    },
    "percentiles3": {
        "total": "9854",
        "ok": "9854",
        "ko": "-"
    },
    "percentiles4": {
        "total": "10988",
        "ok": "10988",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 7,
    "percentage": 0
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 7,
    "percentage": 0
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 4986,
    "percentage": 100
},
    "group4": {
    "name": "failed",
    "count": 0,
    "percentage": 0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "263.158",
        "ok": "263.158",
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
        "total": "5000",
        "ok": "5000",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "118",
        "ok": "118",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "12210",
        "ok": "12210",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "7375",
        "ok": "7375",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "2043",
        "ok": "2043",
        "ko": "-"
    },
    "percentiles1": {
        "total": "7990",
        "ok": "7990",
        "ko": "-"
    },
    "percentiles2": {
        "total": "8772",
        "ok": "8772",
        "ko": "-"
    },
    "percentiles3": {
        "total": "9854",
        "ok": "9854",
        "ko": "-"
    },
    "percentiles4": {
        "total": "10988",
        "ok": "10988",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 7,
    "percentage": 0
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 7,
    "percentage": 0
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 4986,
    "percentage": 100
},
    "group4": {
    "name": "failed",
    "count": 0,
    "percentage": 0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "263.158",
        "ok": "263.158",
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
