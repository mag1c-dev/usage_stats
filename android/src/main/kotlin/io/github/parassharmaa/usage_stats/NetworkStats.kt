package io.github.parassharmaa.usage_stats

import android.annotation.SuppressLint
import android.app.usage.NetworkStats
import android.app.usage.NetworkStats.Bucket
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi


object NetworkStats {

    @RequiresApi(Build.VERSION_CODES.M)
    fun queryNetworkUsageStats(
        context: Context,
        startDate: Long,
        endDate: Long,
        type: Int
    ): List<Map<String, String>> {
        val networkType: NetworkType = when (type) {
            1 -> NetworkType.All
            2 -> NetworkType.WiFi
            3 -> NetworkType.Mobile
            else -> NetworkType.All
        }

        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val subscriberID = getSubscriberId(context)

        val installedApplications: MutableList<ApplicationInfo> =
            context.packageManager.getInstalledApplications(0)

        return installedApplications.map { appInfo: ApplicationInfo ->

            val totalAppSummary = appInfo.getNetworkSummary(
                networkStatsManager = networkStatsManager,
                startDate = startDate,
                endDate = endDate,
                networkType = networkType,
                subscriberID = subscriberID,
            )

            mapOf(
                "packageName" to appInfo.packageName,
                "rxTotalBytes" to totalAppSummary.rxTotalBytes.toString(),
                "txTotalBytes" to totalAppSummary.txTotalBytes.toString()
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun queryNetworkUsageStatsByPackage(
        context: Context,
        startDate: Long,
        endDate: Long,
        type: Int,
        packageName: String
    ): Map<String, String> {
        val networkType: NetworkType = when (type) {
            1 -> NetworkType.All
            2 -> NetworkType.WiFi
            3 -> NetworkType.Mobile
            else -> NetworkType.All
        }

        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val subscriberID = getSubscriberId(context)

        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)


        val totalAppSummary = appInfo.getNetworkSummary(
            networkStatsManager = networkStatsManager,
            startDate = startDate,
            endDate = endDate,
            networkType = networkType,
            subscriberID = subscriberID,
        )

        return mapOf(
            "packageName" to appInfo.packageName,
            "rxTotalBytes" to totalAppSummary.rxTotalBytes.toString(),
            "txTotalBytes" to totalAppSummary.txTotalBytes.toString()
        )
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun ApplicationInfo.getNetworkSummary(
        networkStatsManager: NetworkStatsManager,
        startDate: Long,
        endDate: Long,
        networkType: NetworkType,
        subscriberID: String? = null,
    ): AppNetworkStats {
        val rxTotalBytes: Long
        val txTotalBytes: Long

        when (networkType) {
            NetworkType.Mobile -> {


                val mobileStats = this.getNetworkSummary(
                    networkType = NetworkCapabilities.TRANSPORT_CELLULAR,
                    networkStatsManager = networkStatsManager,
                    startDate = startDate,
                    endDate = endDate,
                    subscriberID = subscriberID,
                )

                rxTotalBytes = mobileStats.rxTotalBytes
                txTotalBytes = mobileStats.txTotalBytes
            }

            NetworkType.WiFi -> {
                val wifiStats = this.getNetworkSummary(
                    networkType = NetworkCapabilities.TRANSPORT_WIFI,
                    networkStatsManager = networkStatsManager,
                    startDate = startDate,
                    endDate = endDate,
                )

                rxTotalBytes = wifiStats.rxTotalBytes
                txTotalBytes = wifiStats.txTotalBytes
            }

            NetworkType.All -> {
                val wifiStats = this.getNetworkSummary(
                    networkType = NetworkCapabilities.TRANSPORT_WIFI,
                    networkStatsManager = networkStatsManager,
                    startDate = startDate,
                    endDate = endDate,
                )

                val mobileStats = this.getNetworkSummary(
                    networkType = NetworkCapabilities.TRANSPORT_CELLULAR,
                    networkStatsManager = networkStatsManager,
                    startDate = startDate,
                    endDate = endDate,
                    subscriberID = subscriberID,
                )

                rxTotalBytes = wifiStats.rxTotalBytes + mobileStats.rxTotalBytes
                txTotalBytes = wifiStats.txTotalBytes + mobileStats.txTotalBytes
            }
        }

        return AppNetworkStats(
            rxTotalBytes = rxTotalBytes,
            txTotalBytes = txTotalBytes
        )
    }


    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun ApplicationInfo.getNetworkSummary(
        networkType: Int,
        networkStatsManager: NetworkStatsManager,
        startDate: Long,
        endDate: Long,
        subscriberID: String? = null,
    ): AppNetworkStats {
        try {
            val queryDetailsForUid: NetworkStats = networkStatsManager.queryDetailsForUid(
                networkType, subscriberID, startDate, endDate, uid
            )

            val tmpBucket = Bucket()
            var rxTotal = 0L
            var txTotal = 0L
            while (queryDetailsForUid.hasNextBucket()) {
                queryDetailsForUid.getNextBucket(tmpBucket)
                txTotal += tmpBucket.txBytes
                rxTotal += tmpBucket.rxBytes
            }
            return AppNetworkStats(rxTotal, txTotal)
        } catch (err: Exception) {

            return AppNetworkStats(0, 0)
        }

    }

    @SuppressLint("MissingPermission", "HardwareIds")
    private fun getSubscriberId(
        context: Context,
    ): String? {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                return null

            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
            return telephonyManager?.subscriberId
        } catch (_: Exception) {
        }

        return ""
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun queryNetworkBuckets(context: Context, startDate: Long, endDate: Long, type: Int): List<Map<String, Any>> {
        val networkType: NetworkType = when (type) {
            1 -> NetworkType.All
            2 -> NetworkType.WiFi
            3 -> NetworkType.Mobile
            else -> NetworkType.All
        }

        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val subscriberID = getSubscriberId(context)

        val installedApplications: MutableList<ApplicationInfo> =
            context.packageManager.getInstalledApplications(0)

        return installedApplications.map { appInfo: ApplicationInfo ->

            val buckets = appInfo.getNetworkBuckets(
                networkStatsManager = networkStatsManager,
                startDate = startDate,
                endDate = endDate,
                networkType = networkType,
                subscriberID = subscriberID,
            )
            mapOf(
                "packageName" to appInfo.packageName,
                "data" to buckets.map {
                    bucket -> mapOf(
                        "rxBytes" to bucket.rxBytes,
                        "rxPackets" to bucket.rxPackets,
                        "txBytes" to bucket.txBytes,
                        "txPackets" to bucket.txPackets,
                        "startTimeStamp" to bucket.startTimeStamp,
                        "endTimeStamp" to bucket.endTimeStamp,
                    )
                }
            )
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun ApplicationInfo.getNetworkBuckets(
        networkType: NetworkType,
        networkStatsManager: NetworkStatsManager,
        startDate: Long,
        endDate: Long,
        subscriberID: String? = null,
    ): List<Bucket> {
        when(networkType){
            NetworkType.Mobile -> {
                return getNetworkBuckets(
                    NetworkCapabilities.TRANSPORT_CELLULAR,
                    networkStatsManager, startDate, endDate, subscriberID
                )
            }
            NetworkType.WiFi -> {
                return getNetworkBuckets(
                    NetworkCapabilities.TRANSPORT_WIFI,
                    networkStatsManager, startDate, endDate, subscriberID
                )
            }
            else -> {
//                var mobileStats = getNetworkBuckets(
//                    NetworkCapabilities.TRANSPORT_CELLULAR,
//                    networkStatsManager, startDate, endDate
//                )
//                var wifiStats = getNetworkBuckets(
//                    NetworkCapabilities.TRANSPORT_WIFI,
//                    networkStatsManager, startDate, endDate
//                )
                return emptyList()
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun ApplicationInfo.getNetworkBuckets(
        networkType: Int,
        networkStatsManager: NetworkStatsManager,
        startDate: Long,
        endDate: Long,
        subscriberID: String? = null,
    ): List<Bucket> {
        return try {
            val result = ArrayList<Bucket>()
            val queryDetailsForUid: NetworkStats = networkStatsManager.queryDetailsForUid(
                networkType, subscriberID, startDate, endDate, uid
            )
            while (queryDetailsForUid.hasNextBucket()) {
                val tmpBucket = Bucket()
                queryDetailsForUid.getNextBucket(tmpBucket)
                result.add(tmpBucket)
            }
            result
        } catch (err: Exception) {
            emptyList()
        }
    }

    private data class AppNetworkStats(
        val rxTotalBytes: Long,
        val txTotalBytes: Long
    )

    private enum class NetworkType {
        All,
        WiFi,
        Mobile,
    }
}
