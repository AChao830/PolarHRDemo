package com.example.polarhrdemo

import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHrData
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class TestUtils {

    // 生成假数据
    fun createMockHRDataObservable(): Observable<PolarHrData> {
        return Observable.interval(1, TimeUnit.SECONDS)
            .map {
                val hr = Random.nextInt(60, 200) // 随机生成HR值在60到100之间
                val rrsMs = List(Random.nextInt(1, 10)) { Random.nextInt(600, 1000) } // 随机生成1到10个RR间期值，每个值在600到1000之间
                val sample = PolarHrData.PolarHrSample(hr, rrsMs, true, true, true)
                PolarHrData(listOf(sample))
            }
    }

    fun createMockDeviceINfoObservable(): Observable<PolarDeviceInfo> {
        return Observable.interval(1, TimeUnit.SECONDS)
            .map {
                val deviceId = "testDevice${Random.nextInt(1, 1000)}"
                val address = "testAddress${Random.nextInt(1, 1000)}"
                val rssi = 100
                val name = "testName${Random.nextInt(1, 1000)}"
                val isConnectable = true
                PolarDeviceInfo(deviceId, address, rssi, name, isConnectable)
            }
            .take(3)
    }
}