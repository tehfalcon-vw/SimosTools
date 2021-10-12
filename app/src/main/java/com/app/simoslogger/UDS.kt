package com.app.simoslogger

import android.content.Context
import android.util.Log
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.sqrt

data class DIDStruct(var address: Long,
                     var length: Int,
                     var signed: Boolean,
                     var progMin: Float,
                     var progMax: Float,
                     var warnMin: Float,
                     var warnMax: Float,
                     var smoothing: Float,
                     var value: Float,
                     var equation: String,
                     var format: String,
                     var name: String,
                     var unit: String)

object DIDs {
    private val TAG = "DIDs"

    var list22: Array<DIDStruct?>? = arrayOf(
        //P1
        DIDStruct(0x2033, 2, false,0f,  220f,   -20f, 200f,   0.0f, 0f,"x / 347.947",           "%06.2f","Speed",               "km/hr"),
        DIDStruct(0xf40C, 2, false,0f,  7000f,  -1f,  6000f,  0.0f, 0f,"x / 4.0",               "%06.1f","RPM",                 "rpm"),
        DIDStruct(0x39c0, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10.0",              "%05.1f","MAP Actual",          "kpa"),
        DIDStruct(0x39c2, 2, false,0f,  300f,   0f,   300f,   0.0f, 0f,"x / 10.0",              "%05.1f","PUT Actual",          "kpa"),
        DIDStruct(0x10c0, 2, false,0f,  2f,     0.7f, 4f,     0.0f, 0f,"x / 1024",              "%05.3f","Lambda Actual",       "l"),
        DIDStruct(0x2004, 2, true, -10f,10f,    -15f, 60f,    0.0f, 0f,"x / 100.0",             "%05.2f","Ignition angle",      "°"),
        DIDStruct(0x39a2, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"100.0 - x / 100.0",     "%02.2f","Wastegate Position",  "%"),
        DIDStruct(0x2032, 2, false,0f,  1500f,  -1f,  1500f,  0.0f, 0f,"x / 2.4",               "%07.2f","Mass Airflow",        "g/s"),
        //P2
        DIDStruct(0x2950, 2, false,0f,  2f,     0.7f, 4f,     0.0f, 0f,"x / 1024",              "%05.3f","Lambda Specified",    "l"),
        DIDStruct(0x13a0, 2, false,0f,  190000f,0f,   185000f,0.0f, 0f,"x / 200",               "%05.2f","Injector PW Cyl 1 DI","ms"),
        DIDStruct(0x437C, 2, true, -50f,450f,   -100f,500f,   0.0f, 0f,"x / 10.0",              "%03.2f","Engine torque",       "Nm"),
        DIDStruct(0x2027, 2, false,0f,  28000f, -1f,  28000f, 0.0f, 0f,"x * 10",                "%03.2f","HFP Actual",          "kpa"),
        DIDStruct(0xf406, 1, false,-25f,25f,    -20f, 20f,    0.0f, 0f,"x / 1.28 - 100.0",      "%02.2f","STFT",                "%"),
        DIDStruct(0x20ba, 2, true, 0f,  100f,   -1f,  101f,   0.0f, 0f,"x / 10.0",              "%05.1f","Throttle Sensor",     "%"),
        DIDStruct(0x1040, 2, false,0f,  190000f,0f,   185000f,0.0f, 0f,"x * 6.1035",            "%08.1f","Turbo Speed",         "rpm"),
        DIDStruct(0x209a, 2, false,0f,  100f,   -1f,  100f,   0.0f, 0f,"x / 100.0",             "%03.2f","HPFP Volume",         "%"),
        //P3
        DIDStruct(0x200a, 2, true, -10f,0f,     -4f,  1f,     0.0f, 0f,"x / 100.0",             "%05.3f","Retard cylinder 1",   "°"),
        DIDStruct(0x200b, 2, true, -10f,0f,     -4f,  1f,     0.0f, 0f,"x / 100.0",             "%05.3f","Retard cylinder 2",   "°"),
        DIDStruct(0x200c, 2, true, -10f,0f,     -4f,  1f,     0.0f, 0f,"x / 100.0",             "%05.3f","Retard cylinder 3",   "°"),
        DIDStruct(0x200d, 2, true, -10f,0f,     -4f,  1f,     0.0f, 0f,"x / 100.0",             "%05.3f","Retard cylinder 4",   "°"),
        DIDStruct(0x2904, 2, false,0f,  20f,    -1f,  10f,    0.0f, 0f,"x",                     "%03.0f","Misfire Sum Global",  ""),
        DIDStruct(0x1001, 1, false,-40f,55f,    -35f, 50f,    0.0f, 0f,"x * 0.75 - 48.0",       "%03.2f","IAT",                 "°C"),
        DIDStruct(0x2025, 2, false,0f,  1500f,  600f, 1500f,  0.0f, 0f,"x / 10.0",              "%03.0f","LFP Actual",          "kpa"),
        DIDStruct(0x293b, 2, false,0f,  25000f, 600f, 25000f, 0.0f, 0f,"x * 10",                "%05.0f","HFP Command",         "kpa"),

        DIDStruct(0x2028, 2, false,0f,  100f,   -1.0f,100f,   0.0f, 0f,"x / 100.0",             "%04.1f","LPFP Duty",           "%"),
        DIDStruct(0x295c, 1, false,0f,  1f,     -1f,  2f,     0.0f, 0f,"x",                     "%01.0f","Flaps Actual",        ""),
        DIDStruct(0xf456, 1, false,-25f,25f,    -20f, 20f,    0.0f, 0f,"x / 1.28 - 100.0",      "%02.2f","LTFT",                "%"),
        DIDStruct(0x202f, 2, false,-50f,130f,   0f,   120f,   0.0f, 0f,"(x - 2731.4) / 10.0",   "%03.2f","Oil temp",            "°C"),
        DIDStruct(0x13ca, 2, false,50f, 120f,   70f,  120.0f, 0.0f, 0f,"x / 120.60176665439",   "%07.2f","Ambient pressure",    "kpa"),
        DIDStruct(0x1004, 2, true, -40f,50f,    -30f, 45f,    0.0f, 0f,"x / 128",               "%07.2f","Ambient air temp",    "°C"),
        DIDStruct(0x4380, 2, true, 0f,  500f,   -100f,500f,   0.0f, 0f,"x / 10.0",              "%04.1f","Torque requested",    "Nm"),
        DIDStruct(0x203c, 2, false,0f,  2f,     -1f,  100f,   0.0f, 0f,"x",                     "%01.0f","Cruise control",      ""),
    )

    var list3E: Array<DIDStruct?>? = arrayOf(
        DIDStruct(0xd0012400, 2, false,0f,   7000f,  -1.0f, 6000f,  0.0f, 0f,"x",                 "%04.0f","Engine Speed",        "rpm"),
        DIDStruct(0xd00136ac, 2, false,0f,   28000f, 0f,    28000f, 0.0f, 0f,"x / 1.884402603",   "%05.0f","Fuel Pressure DI",    "kpa"),
        DIDStruct(0xd000f00c, 1, false,-25f, 25f,    -20f,  20f,    0.7f, 0f,"x / 1.28 - 100.0",  "%04.1f","Fuel Trim Short Term","%"),
        DIDStruct(0xd000c179, 1, false,-50f, 70f,    -20f,  50f,    0.9f, 0f,"(x - 64) / 1.33",   "%03.2f","Intake Air Temp",     "°C"),
        DIDStruct(0xd001988e, 1, false,-5f,  5f,     -3.0f, 3f,     0.8f, 0f,"(x - 128) / 2.66",  "%05.3f","Knock Retard",        "°"),
        DIDStruct(0xd00120e2, 2, false,0.5f, 1.5f,   -0.1f, 5f,     0.0f, 0f,"x / 32767.999999",  "%04.2f","Lambda SAE",          "l"),
        DIDStruct(0xd00098fc, 4, false,0f,   300f,   -1f,   300f,   0.0f, 0f,"x / 1000.0",        "%05.1f","PUT Actual",          "kpa"),
        DIDStruct(0xd0011e76, 2, false,0f,   195000f,-100f, 190000f,0.0f, 0f,"x * 6.1035",        "%05.0f","Turbo Speed",         "rpm"),
        DIDStruct(0xd00097b4, 4, false,0f,   2f,     -100f, 1000f,  0.0f, 0f,"x * 1000",          "%05.3f","Airmass",             "g/stk"),
        DIDStruct(0xd00097fc, 4, false,0f,   2f,     -100f, 1000f,  0.0f, 0f,"x * 1000",          "%05.3f","Airmass Setpoint",    "g/stk"),
        DIDStruct(0xd000c177, 1, false,-25f, 45f,    -100f, 100f,   0.9f, 0f,"(x - 64) / 1.33",   "%03.2f","Ambient Air Temp",    "°C"),
        DIDStruct(0xd0013c76, 2, false,0f,   110f,   0f,    120f,   0.9f, 0f,"x / 120.60176665",  "%03.2f","Ambient Pressure",    "kpa"),
        DIDStruct(0xd0015172, 2, true, 10f,  15f,    7f,    16f,    0.7f, 0f,"x / 51.2",          "%04.1f","Battery Volts",       "V"),
        DIDStruct(0xd000c36e, 1, false,-10f, 10f,    -100f, 100f,   0.0f, 0f,"x",                 "%05.2f","Combustion Mode",     ""),
        DIDStruct(0xd000c6f5, 1, false,-50f, 130f,   -100f, 150f,   0.8f, 0f,"(x - 64) / 1.33",   "%03.0f","Coolant Temp",        "°C"),
        DIDStruct(0xd001397a, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 2.6666666667",  "%01.0f","EOI Limit",           "°"),
        DIDStruct(0xd0013982, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 2.6666666667",  "%01.0f","EOI Actual",          "°"),
        DIDStruct(0xd000c1d4, 1, false,-100f,100f,   -100f, 100f,   0.0f, 0f,"x / 2.55",          "%01.0f","Ethanol Content",     "%"),
        DIDStruct(0xd0011e04, 2, false,0f,   1.5f,   -1f,   10f,    0.0f, 0f,"x / 16384",         "%03.2f","Exhaust Flow Factor", ""),
        DIDStruct(0xd001566e, 2, true, -45f, 45f,    -100f, 100f,   0.0f, 0f,"x / 128",           "%04.1f","Exhaust Cam Position","°"),
        DIDStruct(0xd0011eba, 2, false,0f,   500f,   -100f, 1000f,  0.0f, 0f,"x / 120.60176665",  "%03.0f","Exhaust Pres Desired","kpa"),
        DIDStruct(0xd00135e0, 2, false,-100f,100f,   -100f, 1000f,  0.0f, 0f,"x / 47.181425486",  "%01.0f","Fuel Flow Desired",   "mg/stk"),
        DIDStruct(0xd0013636, 2, false,-100f,100f,   -100f, 1000f,  0.0f, 0f,"x / 47.181425486",  "%01.0f","Fuel Flow",           "mg/stk"),
        DIDStruct(0xd00192b1, 1, false,-100f,100f,   -100f, 100f,   0.0f, 0f,"x / 128",           "%01.0f","Fuel Flow Split MPI", ""),
        DIDStruct(0xd0013600, 2, false,0f,   100f,   -1000f,100f,   0.0f, 0f,"x / 655.35999999",  "%01.0f","Fuel LPFP Duty",      "%"),
        DIDStruct(0xd0011b26, 2, false,0f,   1500f,  -1000f,2000f,  0.0f, 0f,"x / 3.7688052079",  "%04.0f","Fuel LPFR",           "kpa"),
        DIDStruct(0xd001360c, 2, false,0f,   1500f,  -1000f,2000f,  0.0f, 0f,"x / 3.7688052079",  "%04.0f","Fuel LPFR Setpoint",  "kpa"),
        DIDStruct(0xd0013640, 2, false,0f,   28000f, -1000f,30000f, 0.0f, 0f,"x / 1.8844026039",  "%05.0f","Fuel HPFR Setpoint",  "kpa"),
        DIDStruct(0xd001363c, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 655.35999999",  "%01.0f","Fuel HPFP Volume",    "%"),
        DIDStruct(0xd000f00b, 1, false,-25f, 25f,    -20f,  20f,    0.8f, 0f,"x / 1.28 - 100.0",  "%01.0f","Fuel Trim Long Term", "%"),
        DIDStruct(0xd000f39a, 1, false,0f,   6f,     -1f,   7f,     0.0f, 0f,"x",                 "%02.2f","Gear",                "gear"),
        DIDStruct(0xd000e57e, 1, false,-5f,  15f,    -100f, 100f,   0.0f, 0f,"(x - 95.0f) / 2.67","%01.0f","Ignition Table Value","°"),
        DIDStruct(0xd000e59c, 1, false,-5f,  15f,    -100f, 100f,   0.0f, 0f,"(x - 95.0f) / 2.67","%01.0f","Ignition Timing",     "°"),
        DIDStruct(0xd001566c, 2, true, -100f,100f,   -100f, 100f,   0.0f, 0f,"x / 128",           "%01.0f","Intake Cam Position", "°"),
        DIDStruct(0xd0013b16, 2, false,-25f, 25f,    -1000f,1000f,  0.0f, 0f,"x / 250",           "%02.2f","Injector PW DI",      "ms"),
        DIDStruct(0xd0013824, 2, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 250",           "%05.1f","Injector PW MPI",     "ms"),
        DIDStruct(0xd0011e08, 2, false,0f,   20f,    -1000f,1000f,  0.0f, 0f,"x / 16384",         "%03.0f","Intake Flow Factor",  "-"),
        DIDStruct(0xd000efb1, 1, false,0f,   250f,   -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.67",  "%04.2f","Knock Retard Cyl 1",  "°"),
        DIDStruct(0xd000efb2, 1, false,0f,   2f,     -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.67",  "%04.2f","Knock Retard Cyl 2",  "°"),
        DIDStruct(0xd000efb3, 1, false,0f,   2f,     -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.67",  "%04.2f","Knock Retard Cyl 3",  "°"),
        DIDStruct(0xd000efb4, 1, false,0f,   2f,     -5f,   1000f,  0.8f, 0f,"(x - 128) / 2.67",  "%04.2f","Knock Retard Cyl 4",  "°"),
        DIDStruct(0xd00143f6, 2, false,0f,   2f,     -100f, 500f,   0.0f, 0f,"x / 1024",          "%03.2f","Lambda Setpoint",     "l"),
        DIDStruct(0xd00098cc, 4, false,0f,   300f,   -300f, 300f,   0.0f, 0f,"x / 1000.0",        "%05.1f","MAP",                 "kpa"),
        DIDStruct(0xd00098f4, 4, false,0f,   300f,   -300f, 300f,   0.0f, 0f,"x / 1000.0",        "%05.1f","MAP Setpoint",        "kpa"),
        DIDStruct(0xd000c5ae, 1, false,-25f, 120f,   -1000f,1000f,  0.9f, 0f,"x - 40",            "%01.0f","Oil Temp",            "°C"),
        DIDStruct(0xd000e578, 1, true, 0f,   220f,   -1000f,1000f,  0.0f, 0f,"(x - 128) / 2.67",  "%03.2f","openflex_cor",        "°CRK"),
        DIDStruct(0xd001de8d, 1, true, 0f,   7000f,  -1000f,1000f,  0.0f, 0f,"(x - 128) / 2.67",  "%06.1f","openflex_max_cor",    "°CRK"),
        DIDStruct(0xd001de8e, 1, false,0f,   3f,     -1000f,1000f,  0.0f, 0f,"x / 128",           "%05.3f","openflex_fac_cor",    ""),
        DIDStruct(0xd0012028, 2, false,0f,   100f,   -1000f,1000f,  0.0f, 0f,"x / 10.24",         "%04.1f","Pedal Position",      "%"),
        DIDStruct(0xd0000aa1, 1, false,0f,   1f,     -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Port Flap Position",  ""),
        DIDStruct(0xd0011eee, 2, false,0f,   300f,   -1000f,1000f,  0.0f, 0f,"x / 120.6017666543","%05.1f","PUT Setpoint",        "kpa"),
        DIDStruct(0xd0013a42, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 2.666666666667","%01.0f","SOI Limit",           "°"),
        DIDStruct(0xd0013a44, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 2.666666666667","%01.0f","SOI Actual",          "°"),
        DIDStruct(0xd000f377, 1, false,0.5f, 1.3f,   -1000f,1000f,  0.0f, 0f,"x / 2.142128661087","%07.2f","TPS",                 "%"),
        DIDStruct(0xd0015344, 2, true, -40f, 500f,   -1000f,1000f,  0.0f, 0f,"x / 32",            "%07.2f","Torque Actual",       "Nm"),
        DIDStruct(0xd0011f0c, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Torque Limitation",   ""),
        DIDStruct(0xd0012048, 2, true, -40f, 500f,   -1000f,500f,   0.0f, 0f,"x / 32",            "%03.2f","Torque Requested",    "Nm"),
        DIDStruct(0xd000f3c1, 1, false,-100f,100f,   -1000f,120f,   0.9f, 0f,"x - 40",            "%01.0f","Transmission Temp",   "°C"),
        DIDStruct(0xd0019b75, 1, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Valve Lift Position", ""),
        DIDStruct(0xd00155b6, 2, false,0f,   220f,   -1000f,220f,   0.0f, 0f,"x / 100.0",         "%03.0f","Vehicle Speed",       "km/h"),
        DIDStruct(0xd0015c2c, 2, false,-10f, 0f,     -1000f,1000f,  0.0f, 0f,"x / 655.3599999997","%05.3f","Wastegate Setpoint",  "%"),
        DIDStruct(0xd0011e10, 2, false,-10f, 0f,     -1000f,1000f,  0.0f, 0f,"x / 655.3599999997","%05.3f","Wastegate Actual",    "%"),
        DIDStruct(0xd0015c5e, 2, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x / 32",            "%03.1f","Wastegate Flow Req",  "kg/h"),
        DIDStruct(0xd00141ba, 2, false,-10f, 10f,    -1000f,1000f,  0.4f, 0f,"(x-512)/32",        "%05.3f","Accel. Long",         "m/s2"),
        DIDStruct(0xd000ee2a, 1, false,-10f, 10f,    -1000f,1000f,  0.8f, 0f,"(x-127)/10",        "%05.3f","Accel. Lat",          "m/s2"),
        DIDStruct(0xd001b6cd, 1, false,-100f,100f,   -1000f,1000f,  0.0f, 0f,"x",                 "%01.0f","Cruise Control",      ""),
    )

    fun list(): Array<DIDStruct?>? {
        when(UDSLogger.getMode()) {
            UDS_LOGGING_22 -> return list22
            UDS_LOGGING_3E -> return list3E
        }
        return list22
    }

    fun getDID(address: Long): DIDStruct? {
        list()?.let { list ->
            for (i in 0 until list.count()) {
                list[i]?.let { did ->
                    if(did.address == address) {
                        return did
                    }
                }
            }
        }

        return null
    }

    fun setValue(did: DIDStruct?, x: Float): Float {
        if(did == null)
            return 0f

        //Used in smoothing calculation
        val previousValue = did.value

        //eval expression
        try {
            did.value = eval(did.equation.replace("x", x.toString(), true))
        } catch(e: Exception) {
            did.value = 0f
        }

        //Add smoothing
        if(did.smoothing > 0f && did.smoothing < 0.9751f)
            did.value = ((1f-did.smoothing) * did.value) + (did.smoothing * previousValue)

        return did.value
    }

    fun getValue(did: DIDStruct?): Float {
        if (did == null)
            return 0f

        return did.value
    }
}

object UDSLogger {
    private val TAG = "UDSLogger"
    private var mLastEnabled = false
    private var mMode = UDS_LOGGING_22
    private var mTorquePID = -1
    private var mEngineRPMPID = -1
    private var mMS2PID = -1
    private var mGearPID = -1
    private var mVelocityPID = -1
    private var mTireCircumference = -1f
    private var mFoundMS2PIDS = false
    private var mFoundTQPIDS = false

    fun isCalcHP(): Boolean {
        if(Settings.calculateHP && (mFoundTQPIDS || mFoundMS2PIDS)) {
            return true
        }

        return false
    }

    fun getTorque(): Float {
        DIDs.list()?.let { list ->
            if (Settings.calculateHP) {
                if (mFoundMS2PIDS && Settings.useMS2Torque) {
                    try {
                        val gearValue = list[mGearPID]!!.value.toInt()

                        if (gearValue in 1..7) {
                            val ms2Value = sqrt(list[mMS2PID]!!.value)
                            val velValue = list[mVelocityPID]!!.value
                            val weightValue = Settings.curbWeight * KG_TO_N
                            val ratioValue = sqrt(Settings.gearRatios[gearValue - 1] * Settings.gearRatios[7])
                            val dragValue = 1.0 + ((velValue * velValue).toDouble() * Settings.dragCoefficient)

                            return ((weightValue * ms2Value / ratioValue / mTireCircumference / TQ_CONSTANT).toDouble() * dragValue).toFloat()
                        }
                    } catch (e: Exception) {
                        return 0f
                    }

                    return 0f
                } else if (mFoundTQPIDS) {
                    return try {
                        list[mTorquePID]!!.value
                    } catch (e: Exception) {
                        0f
                    }
                }
            }
        }

        return 0f
    }

    fun getHP(tq: Float): Float {
        DIDs.list()?.let { list ->
            if (Settings.calculateHP && mEngineRPMPID != -1) {
                return try {
                    val rpmValue = list[mEngineRPMPID]!!.value
                    tq * rpmValue / 7127f
                } catch (e: Exception) {
                    0f
                }
            }
        }

        return 0f
    }

    fun isEnabled(): Boolean {
        return mLastEnabled
    }

    fun setMode(mode: Int) {
        when(mode) {
            UDS_LOGGING_22 -> {
                mMode = mode
            }
            UDS_LOGGING_3E -> {
                mMode = mode
            }
        }
    }

    fun getMode(): Int {
        return mMode
    }

    fun frameCount(): Int {
        when(mMode) {
            UDS_LOGGING_22 -> {
                return frameCount22()
            }
            UDS_LOGGING_3E -> {
                return frameCount3E()
            }
        }
        return 0
    }

    fun buildFrame(index: Int): ByteArray {
        when(mMode) {
            UDS_LOGGING_22 -> {
                return buildFrame22(index)
            }
            UDS_LOGGING_3E -> {
                return buildFrame3E(index)
            }
        }
        return byteArrayOf()
    }

    fun processFrame(tick: Int, buff: ByteArray?, context: Context): Int {
        when(mMode) {
            UDS_LOGGING_22 -> {
                return processFrame22(tick, buff, context)
            }
            UDS_LOGGING_3E -> {
                return processFrame3E(tick, buff, context)
            }
        }
        return UDS_ERROR_NULL
    }

    private fun resetHPPIDS() {
        mFoundTQPIDS = false
        mFoundMS2PIDS = false
        mTorquePID = -1
        mEngineRPMPID = -1
        mMS2PID = -1
        mGearPID = -1
        mVelocityPID = -1
        mTireCircumference = Settings.tireDiameter * 3.14f
    }

    private fun findHPPIDS() {
        if(mMode == UDS_LOGGING_22) {
            DIDs.list22?.let { list ->
                for (x in 0 until list.count()) {
                    //Look for torque PID
                    if (list[x]?.address == 0x437C.toLong()) {
                        mTorquePID = x
                    }

                    //Look for rpm PID
                    if (list[x]?.address == 0xf40C.toLong()) {
                        mEngineRPMPID = x
                    }
                }
                //Did we find the PIDs required?
                if (mEngineRPMPID != -1 && mTorquePID != -1)
                    mFoundTQPIDS = true
            }
        } else {
            DIDs.list3E?.let { list ->
                for (x in 0 until list.count()) {
                    //Look for torque PID
                    if (list[x]?.address == 0xd0015344) {
                        mTorquePID = x
                    }
                    //Look for rpm PID
                    if (list[x]?.address == 0xd0012400) {
                        mEngineRPMPID = x
                    }

                    //Look for MS2 PID
                    if (list[x]?.address == 0xd00141ba) {
                        mMS2PID = x
                    }

                    //Look for Gear PID
                    if (list[x]?.address == 0xd000f39a) {
                        mGearPID = x
                    }

                    //Look for Velocity PID
                    if (list[x]?.address == 0xd00155b6) {
                        mVelocityPID = x
                    }
                }
            }
            //Did we find the PIDs required?
            if(mEngineRPMPID != -1 && mMS2PID != -1 && mGearPID != -1 && mVelocityPID != -1)
                mFoundMS2PIDS = true

            if(mEngineRPMPID != -1 && mTorquePID != -1)
                mFoundTQPIDS = true
        }
    }

    private fun frameCount22(): Int {
        return 8
    }

    private fun frameCount3E(): Int {
        return try {
            (DIDs.list3E!!.count() * 5 / 0x8F) + 2
        } catch (e: Exception) {
            0
        }
    }

    private fun buildFrame22(index: Int): ByteArray {
        DIDs.list22?.let { list ->
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = 1
            bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_ADD
            if (index == 0) {
                bleHeader.cmdFlags += BLE_COMMAND_FLAG_PER_CLEAR
            }
            if (index == frameCount22() - 1) {
                bleHeader.cmdFlags += BLE_COMMAND_FLAG_PER_ENABLE
            }

            var buff: ByteArray = byteArrayOf(0x22.toByte())
            if (index % 2 == 0) {
                //Write P1 PIDS
                for (i in 0 until 8) {
                    val did: DIDStruct? = list[i]
                    did?.let {
                        bleHeader.cmdSize += 2
                        buff += ((did.address and 0xFF00) shr 8).toByte()
                        buff += (did.address and 0xFF).toByte()
                    }
                }
            } else {
                //Write P2 PIDS
                var startIndex = 8 + ((index % 4) / 2 * 4)
                var endIndex = startIndex + 4
                for (i in startIndex until endIndex) {
                    val did: DIDStruct? = list[i]
                    did?.let {
                        bleHeader.cmdSize += 2
                        buff += ((did.address and 0xFF00) shr 8).toByte()
                        buff += (did.address and 0xFF).toByte()
                    }
                }
                //Write P3 PIDS
                startIndex = 16 + ((index % 8) / 2 * 4)
                endIndex = startIndex + 4
                for (i in startIndex until endIndex) {
                    val did: DIDStruct? = list[i]
                    did?.let {
                        bleHeader.cmdSize += 2
                        buff += ((did.address and 0xFF00) shr 8).toByte()
                        buff += (did.address and 0xFF).toByte()
                    }
                }
            }

            return bleHeader.toByteArray() + buff
        }

        return byteArrayOf()
    }

    private fun buildFrame3E(index: Int): ByteArray {
        DIDs.list3E?.let { list ->
            var addressArray: ByteArray = byteArrayOf()
            for (i in 0 until list.count()) {
                val did: DIDStruct? = list[i]
                did?.let {
                    addressArray += (did.length and 0xFF).toByte()
                    addressArray += did.address.toArray4()
                }
            }
            addressArray += 0

            //Do we even have any PIDs in the range?  If not send persist message
            if ((index * 0x8F >= addressArray.count()) and (index == frameCount3E() - 1)) {
                val bleHeader = BLEHeader()
                bleHeader.cmdSize = 6
                bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_CLEAR or BLE_COMMAND_FLAG_PER_ADD or BLE_COMMAND_FLAG_PER_ENABLE

                val writeBuffer: ByteArray = bleHeader.toByteArray() + byteArrayOf(
                    0x3e.toByte(),
                    0x33.toByte(),
                    0xb0.toByte(),
                    0x01.toByte(),
                    0xe7.toByte(),
                    0x00.toByte()
                )

                Log.d(TAG, writeBuffer.toHex())
                Log.d(TAG, writeBuffer.count().toString())

                return writeBuffer
            }

            //constrain copy range or we will receive an exception
            val endOfArray = if ((1 + index) * 0x8F > addressArray.count()) {
                addressArray.count()
            } else {
                (1 + index) * 0x8F
            }
            val selectArray: ByteArray = addressArray.copyOfRange(index * 0x8F, endOfArray)
            val bleHeader = BLEHeader()
            bleHeader.cmdSize = 8 + selectArray.count()
            bleHeader.cmdFlags = BLE_COMMAND_FLAG_PER_CLEAR

            val memoryOffset = 0xB001E700 + (index * 0x8F)
            val writeBuffer: ByteArray = bleHeader.toByteArray() + byteArrayOf(
                0x3e.toByte(),
                0x32.toByte()
            ) + memoryOffset.toArray4() + selectArray.count().toArray2() + selectArray

            Log.d(TAG, writeBuffer.toHex())
            Log.d(TAG, writeBuffer.count().toString())

            return writeBuffer
        }

        return byteArrayOf()
    }

    fun processFrame22(tick: Int, buff: ByteArray?, context: Context): Int {
        DIDs.list22?.let { list ->
            // if the buffer is null abort
            if (buff == null) {
                return UDS_ERROR_NULL
            }

            // check to make sure ble header byte matches
            val bleHeader = BLEHeader()
            bleHeader.fromByteArray(buff)
            val bData = buff.copyOfRange(8, buff.size)
            if (!bleHeader.isValid()) {
                return UDS_ERROR_HEADER
            }

            // does the size of the data match the header?
            if (bData.count() != bleHeader.cmdSize) {
                return UDS_ERROR_CMDSIZE
            }

            // make sure we received an 'OK' from the ECU
            if (bData[0] != 0x62.toByte()) {
                return UDS_ERROR_RESPONSE
            }

            //In init state
            if (tick <= frameCount22()) {
                return UDS_OK
            }

            // process the data in the buffer
            var i = 1
            while (i < bleHeader.cmdSize - 3) {
                val did: DIDStruct =
                    DIDs.getDID(((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF).toLong())
                        ?: return UDS_ERROR_UNKNOWN
                if (did.length == 1) {
                    if (did.signed) {
                        DIDs.setValue(did, (bData[i++] and 0xFF).toByte().toFloat())
                    } else {
                        DIDs.setValue(did, (bData[i++] and 0xFF).toFloat())
                    }
                } else {
                    if (did.signed) {
                        DIDs.setValue(
                            did,
                            (((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF)).toShort()
                                .toFloat()
                        )
                    } else {
                        DIDs.setValue(
                            did,
                            (((bData[i++] and 0xFF) shl 8) + (bData[i++] and 0xFF)).toFloat()
                        )
                    }
                }
            }

            //Update Log every 2nd tick
            if (tick % 2 == 0) {
                val dEnable = list[list.count() - 1]
                if ((!Settings.invertCruise && dEnable?.value != 0.0f) ||
                    (Settings.invertCruise && dEnable?.value == 0.0f)) {
                    //If we were not enabled before we must open a log to start writing
                    if (!mLastEnabled) {
                        val currentDateTime = LocalDateTime.now()
                        LogFile.create(
                            "simoslogger-${
                                currentDateTime.format(
                                    DateTimeFormatter.ofPattern(
                                        "yyyy_MM_dd-HH_mm_ss"
                                    )
                                )
                            }.csv", context
                        )
                        //Start with time, its required
                        var strItems: String? = "Time"

                        //Add all PIDS
                        for (x in 0 until list.count()) {
                            strItems += ",${list[x]?.name}"
                        }

                        //reset torque / rpm pids
                        resetHPPIDS()

                        //Are we supposed to calculate HP? find PIDS
                        if (Settings.calculateHP) {
                            //Look for PIDS related to calculating HP
                            findHPPIDS()

                            //If we found PIDs add column
                            if (isCalcHP())
                                strItems += ",HP"
                        }

                        LogFile.addLine(strItems)
                    }
                    mLastEnabled = true

                    //Write new values to log
                    var strItems: String? = (bleHeader.tickCount.toFloat() / 1000.0f).toString()
                    for (x in 0 until list.count()) {
                        strItems += ",${list[x]?.value}"
                    }

                    //Calculate HP and found PIDS?
                    if (isCalcHP()) {
                        val calcHP = getHP(getTorque())
                        strItems += ",${calcHP}"
                    }

                    LogFile.addLine(strItems)
                } else {
                    if (mLastEnabled) {
                        LogFile.close()
                    }
                    mLastEnabled = false
                }
            }

            return UDS_OK
        }

        return UDS_ERROR_UNKNOWN
    }

    private fun processFrame3E(tick: Int, buff: ByteArray?, context: Context): Int {
        DIDs.list3E?.let { list ->
            // if the buffer is null abort
            if (buff == null) {
                return UDS_ERROR_NULL
            }

            // check to make sure ble header byte matches
            val bleHeader = BLEHeader()
            bleHeader.fromByteArray(buff)
            val bData = buff.copyOfRange(8, buff.size)
            if (!bleHeader.isValid()) {
                return UDS_ERROR_HEADER
            }

            // does the size of the data match the header?
            if (bData.count() != bleHeader.cmdSize) {
                return UDS_ERROR_CMDSIZE
            }

            // make sure we received an 'OK' from the ECU
            if (bData[0] != 0x7e.toByte()) {
                return UDS_ERROR_RESPONSE
            }

            //still in the initial setup?
            if (tick <= frameCount3E()) {
                return UDS_OK
            }

            //Update PID Values
            var dPos = 1
            for (i in 0 until list.count()) {
                val did: DIDStruct? = list[i]

                try {
                    //make sure we are in range
                    if (dPos + did!!.length > bData.count())
                        break

                    //Build the value in little endian
                    var newValue: Int = bData[dPos + did.length - 1] and 0xFF
                    for (d in 1 until did.length) {
                        newValue = newValue shl 8
                        newValue += bData[dPos + did.length - d - 1] and 0xFF
                    }
                    dPos += did.length

                    //set pid values
                    if (did.signed) {
                        when (did.length) {
                            1 -> DIDs.setValue(did, newValue.toByte().toFloat())
                            2 -> DIDs.setValue(did, newValue.toShort().toFloat())
                            4 -> DIDs.setValue(did, newValue.toFloat())
                        }
                    } else {
                        when (did.length) {
                            1 -> DIDs.setValue(did, newValue.toFloat())
                            2 -> DIDs.setValue(did, newValue.toFloat())
                            4 -> DIDs.setValue(did, Float.fromBits(newValue))
                        }
                    }
                } catch (e: Exception) {
                    return UDS_ERROR_UNKNOWN
                }
            }

            val dEnable = list[list.count() - 1]
            if ((!Settings.invertCruise && dEnable?.value != 0.0f) ||
                (Settings.invertCruise && dEnable?.value == 0.0f)
            ) {
                //If we were not enabled before we must open a log to start writing
                if (!mLastEnabled) {
                    val currentDateTime = LocalDateTime.now()
                    LogFile.create(
                        "simoslogger-${
                            currentDateTime.format(
                                DateTimeFormatter.ofPattern(
                                    "yyyy_MM_dd-HH_mm_ss"
                                )
                            )
                        }.csv", context
                    )

                    //Add time its required
                    var strItems: String? = "Time"

                    //Add PIDs
                    for (x in 0 until list.count()) {
                        strItems += ",${list[x]?.name}"
                    }

                    //reset torque / rpm pids
                    resetHPPIDS()

                    //Are we supposed to calculate HP? find PIDS
                    if (Settings.calculateHP) {
                        //Look for PIDS related to calculating HP
                        findHPPIDS()

                        //If we found PIDs add column
                        if (isCalcHP())
                            strItems += ",TQ,HP"
                    }

                    LogFile.addLine(strItems)
                }
                mLastEnabled = true

                //Write new values to log
                var strItems: String? = (bleHeader.tickCount.toFloat() / 1000.0f).toString()
                for (x in 0 until list.count()) {
                    strItems += ",${list[x]?.value}"
                }

                //Calculate HP and found PIDS?
                if (isCalcHP()) {
                    val calcTQ = getTorque()
                    val calcHP = getHP(calcTQ)
                    strItems += ",${calcTQ},${calcHP}"
                }

                LogFile.addLine(strItems)
            } else {
                if (mLastEnabled) {
                    LogFile.close()
                }
                mLastEnabled = false
            }

            return UDS_OK
        }

        return UDS_ERROR_UNKNOWN
    }
}