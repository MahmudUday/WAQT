package com.example.data

import com.example.model.DistrictLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object BangladeshLocations {

    val allDistricts: List<DistrictLocation> = listOf(
        // Dhaka Division
        DistrictLocation("dhaka", "Dhaka", "ঢাকা", "Dhaka", "ঢাকা", 23.8103, 90.4125),
        DistrictLocation("gazipur", "Gazipur", "গাজীপুর", "Dhaka", "ঢাকা", 23.9999, 90.4203),
        DistrictLocation("narayanganj", "Narayanganj", "নারায়ণগঞ্জ", "Dhaka", "ঢাকা", 23.6238, 90.5000),
        DistrictLocation("tangail", "Tangail", "টাঙ্গাইল", "Dhaka", "ঢাকা", 24.2513, 89.9167),
        DistrictLocation("kishoreganj", "Kishoreganj", "কিশোরগঞ্জ", "Dhaka", "ঢাকা", 24.4449, 90.7766),
        DistrictLocation("manikganj", "Manikganj", "মানিকগঞ্জ", "Dhaka", "ঢাকা", 23.8617, 90.0003),
        DistrictLocation("munshiganj", "Munshiganj", "মুন্সিগঞ্জ", "Dhaka", "ঢাকা", 23.5422, 90.5305),
        DistrictLocation("narsingdi", "Narsingdi", "নরসিংদী", "Dhaka", "ঢাকা", 23.9229, 90.7177),
        DistrictLocation("faridpur", "Faridpur", "ফরিদপুর", "Dhaka", "ঢাকা", 23.6071, 89.8429),
        DistrictLocation("gopalganj", "Gopalganj", "গোপালগঞ্জ", "Dhaka", "ঢাকা", 23.0051, 89.8266),
        DistrictLocation("madaripur", "Madaripur", "মাদারীপুর", "Dhaka", "ঢাকা", 23.1641, 90.1897),
        DistrictLocation("rajbari", "Rajbari", "রাজবাড়ী", "Dhaka", "ঢাকা", 23.7574, 89.6445),
        DistrictLocation("shariatpur", "Shariatpur", "শরীয়তপুর", "Dhaka", "ঢাকা", 23.2423, 90.4348),

        // Chattogram Division (includes Comilla / Cumilla)
        DistrictLocation("chattogram", "Chattogram", "চট্টগ্রাম", "Chattogram", "চট্টগ্রাম", 22.3569, 91.7832),
        DistrictLocation("comilla", "Comilla (Cumilla)", "কুমিল্লা", "Chattogram", "চট্টগ্রাম", 23.4607, 91.1809),
        DistrictLocation("brahmanbaria", "Brahmanbaria", "ব্রাহ্মণবাড়িয়া", "Chattogram", "চট্টগ্রাম", 23.9571, 91.1119),
        DistrictLocation("chandpur", "Chandpur", "চাঁদপুর", "Chattogram", "চট্টগ্রাম", 23.2333, 90.6713),
        DistrictLocation("coxs_bazar", "Cox's Bazar", "কক্সবাজার", "Chattogram", "চট্টগ্রাম", 21.4272, 92.0058),
        DistrictLocation("feni", "Feni", "ফেনী", "Chattogram", "চট্টগ্রাম", 23.0159, 91.3976),
        DistrictLocation("noakhali", "Noakhali", "নোয়াখালী", "Chattogram", "চট্টগ্রাম", 22.8696, 91.0994),
        DistrictLocation("lakshmipur", "Lakshmipur", "লক্ষ্মীপুর", "Chattogram", "চট্টগ্রাম", 22.9447, 90.8282),
        DistrictLocation("bandarban", "Bandarban", "বান্দরবান", "Chattogram", "চট্টগ্রাম", 22.1953, 92.2184),
        DistrictLocation("khagrachhari", "Khagrachhari", "খাগড়াছড়ি", "Chattogram", "চট্টগ্রাম", 23.1193, 91.9847),
        DistrictLocation("rangamati", "Rangamati", "রাঙ্গামাটি", "Chattogram", "চট্টগ্রাম", 22.7324, 92.2985),

        // Sylhet Division
        DistrictLocation("sylhet", "Sylhet", "সিলেট", "Sylhet", "সিলেট", 24.8949, 91.8687),
        DistrictLocation("habiganj", "Habiganj", "হবিগঞ্জ", "Sylhet", "সিলেট", 24.3749, 91.4155),
        DistrictLocation("moulvibazar", "Moulvibazar", "মৌলভীবাজার", "Sylhet", "সিলেট", 24.4829, 91.7774),
        DistrictLocation("sunamganj", "Sunamganj", "সুনামগঞ্জ", "Sylhet", "সিলেট", 25.0658, 91.3950),

        // Rajshahi Division
        DistrictLocation("rajshahi", "Rajshahi", "রাজশাহী", "Rajshahi", "রাজশাহী", 24.3745, 88.6042),
        DistrictLocation("bogura", "Bogura", "বগুড়া", "Rajshahi", "রাজশাহী", 24.8465, 89.3773),
        DistrictLocation("chapainawabganj", "Chapainawabganj", "চাঁপাইনবাবগঞ্জ", "Rajshahi", "রাজশাহী", 24.5965, 88.2775),
        DistrictLocation("joypurhat", "Joypurhat", "জয়পুরহাট", "Rajshahi", "রাজশাহী", 25.0968, 89.0227),
        DistrictLocation("naogaon", "Naogaon", "নওগাঁ", "Rajshahi", "রাজশাহী", 24.7936, 88.9318),
        DistrictLocation("natore", "Natore", "নাটোর", "Rajshahi", "রাজশাহী", 24.4206, 89.0003),
        DistrictLocation("pabna", "Pabna", "পাবনা", "Rajshahi", "রাজশাহী", 24.0064, 89.2372),
        DistrictLocation("sirajganj", "Sirajganj", "সিরাজগঞ্জ", "Rajshahi", "রাজশাহী", 24.4534, 89.7007),

        // Khulna Division
        DistrictLocation("khulna", "Khulna", "খুলনা", "Khulna", "খুলনা", 22.8456, 89.5403),
        DistrictLocation("bagerhat", "Bagerhat", "বাগেরহাট", "Khulna", "খুলনা", 22.6516, 89.7859),
        DistrictLocation("chuadanga", "Chuadanga", "চুয়াডাঙ্গা", "Khulna", "খুলনা", 23.6402, 88.8418),
        DistrictLocation("jashore", "Jashore", "যশোর", "Khulna", "খুলনা", 23.1634, 89.2182),
        DistrictLocation("jhenaidah", "Jhenaidah", "ঝিনাইদহ", "Khulna", "খুলনা", 23.5448, 89.1539),
        DistrictLocation("kushtia", "Kushtia", "কুষ্টিয়া", "Khulna", "খুলনা", 23.9013, 89.1205),
        DistrictLocation("magura", "Magura", "মাগুরা", "Khulna", "খুলনা", 23.4873, 89.4199),
        DistrictLocation("meherpur", "Meherpur", "মেহেরপুর", "Khulna", "খুলনা", 23.7622, 88.6318),
        DistrictLocation("narail", "Narail", "নড়াইল", "Khulna", "খুলনা", 23.1725, 89.5127),
        DistrictLocation("satkhira", "Satkhira", "সাতক্ষীরা", "Khulna", "খুলনা", 22.7185, 89.0705),

        // Barishal Division
        DistrictLocation("barishal", "Barishal", "বরিশাল", "Barishal", "বরিশাল", 22.7010, 90.3535),
        DistrictLocation("barguna", "Barguna", "বরগুনা", "Barishal", "বরিশাল", 22.1592, 90.1257),
        DistrictLocation("bhola", "Bhola", "ভোলা", "Barishal", "বরিশাল", 22.6859, 90.6482),
        DistrictLocation("jhalokathi", "Jhalokathi", "ঝালকাঠি", "Barishal", "বরিশাল", 22.6406, 90.1987),
        DistrictLocation("patuakhali", "Patuakhali", "পটুয়াখালী", "Barishal", "বরিশাল", 22.3596, 90.3299),
        DistrictLocation("pirojpur", "Pirojpur", "পিরোজপুর", "Barishal", "বরিশাল", 22.5841, 89.9720),

        // Rangpur Division
        DistrictLocation("rangpur", "Rangpur", "রংপুর", "Rangpur", "রংপুর", 25.7439, 89.2752),
        DistrictLocation("dinajpur", "Dinajpur", "দিনাজপুর", "Rangpur", "রংপুর", 25.6217, 88.6355),
        DistrictLocation("gaibandha", "Gaibandha", "গাইবান্ধা", "Rangpur", "রংপুর", 25.3288, 89.5281),
        DistrictLocation("kurigram", "Kurigram", "কুড়িগ্রাম", "Rangpur", "রংপুর", 25.8054, 89.6362),
        DistrictLocation("lalmonirhat", "Lalmonirhat", "লালমনিরহাট", "Rangpur", "রংপুর", 25.9923, 89.2847),
        DistrictLocation("nilphamari", "Nilphamari", "নীলফামারী", "Rangpur", "রংপুর", 25.9318, 88.8560),
        DistrictLocation("panchagarh", "Panchagarh", "পঞ্চগড়", "Rangpur", "রংপুর", 26.3411, 88.5542),
        DistrictLocation("thakurgaon", "Thakurgaon", "ঠাকুরগাঁও", "Rangpur", "রংপুর", 26.0337, 88.4617),

        // Mymensingh Division
        DistrictLocation("mymensingh", "Mymensingh", "ময়মনসিংহ", "Mymensingh", "ময়মনসিংহ", 24.7471, 90.4203),
        DistrictLocation("jamalpur", "Jamalpur", "জামালপুর", "Mymensingh", "ময়মনসিংহ", 24.9375, 89.9378),
        DistrictLocation("netrokona", "Netrokona", "নেত্রকোনা", "Mymensingh", "ময়মনসিংহ", 24.8709, 90.7279),
        DistrictLocation("sherpur", "Sherpur", "শেরপুর", "Mymensingh", "ময়মনসিংহ", 25.0205, 90.0153)
    )

    val defaultDistrict: DistrictLocation = allDistricts.first { it.id == "dhaka" }

    fun findById(id: String?): DistrictLocation =
        allDistricts.firstOrNull { it.id == id } ?: defaultDistrict

    fun search(query: String): List<DistrictLocation> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return allDistricts
        return allDistricts.filter {
            it.nameEn.lowercase().contains(trimmed) ||
                it.nameBn.contains(trimmed) ||
                it.divisionEn.lowercase().contains(trimmed) ||
                it.divisionBn.contains(trimmed) ||
                (trimmed.contains("cumilla") && it.id == "comilla")
        }
    }

    fun findNearest(latitude: Double, longitude: Double): DistrictLocation {
        return allDistricts.minByOrNull { district ->
            haversineKm(latitude, longitude, district.latitude, district.longitude)
        } ?: defaultDistrict
    }

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}
