package com.gncaitech.flowlink.data

data class Subject(
    val name: String,
    val pid: String,
    val age: Int,
    val gender: String,
    val surgery: String,  // e.g. "D+24"
    val maturity: Int,    // 0-100
    val program: String,
    val scheduled: String,
    val status: String,   // "ready" or "watch"
)

val sampleSubjects = listOf(
    Subject("김선영", "P-2026-04812", 58, "F", "D+24", 64, "공쥐기 3세트", "09:30", "ready"),
    Subject("이재호", "P-2026-04787", 64, "M", "D+32", 48, "공쥐기 + 덤벨컬", "10:00", "watch"),
    Subject("박미경", "P-2026-04812", 71, "F", "D+48", 89, "덤벨컬 2세트", "10:30", "ready"),
    Subject("정태석", "P-2026-04823", 49, "M", "D+18", 22, "공쥐기 2세트", "11:00", "ready"),
)
