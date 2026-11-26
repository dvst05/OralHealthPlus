package com.kelompok4.oralhealthplus.feature.detection

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoLoaderTest {
    @Test fun fotoKecil_tidakDiperkecil() = assertEquals(1, hitungSampleSize(800, 600, 1024))
    @Test fun belumDuaKaliBatas_tidakDiperkecil() = assertEquals(1, hitungSampleSize(2047, 1500, 1024))
    @Test fun tepatDuaKaliBatas_diperkecilDua() = assertEquals(2, hitungSampleSize(2048, 1536, 1024))
    @Test fun fotoSangatBesarPotret_diperkecilEmpat() = assertEquals(4, hitungSampleSize(3000, 4624, 1024))
    @Test fun ukuranTidakDiketahui_aman() = assertEquals(1, hitungSampleSize(-1, -1, 1024))
}
