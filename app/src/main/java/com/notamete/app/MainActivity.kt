package com.notamete.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// 1. DATA MODELS
// ==========================================
data class ItemTimbangan(val kg: Double, val ekor: Int)

data class RincianNota(
    var ekor: Int,
    var kotor: Double,
    var keranjang: Double = 11.0
) {
    val bersih: Double get() = (kotor - keranjang).coerceAtLeast(0.0)
    fun calculateSubtotal(hargaPerKg: Double): Double = bersih * hargaPerKg
}

data class DataBakul(
    var hargaPerKg: Double = 25000.0,
    var dibayar: Double = 0.0,
    var sisaKemarin: Double = 0.0,
    val rincian: MutableList<RincianNota> = mutableListOf()
)

// ==========================================
// 2. TEMA WARNA
// ==========================================
val BgColor = Color(0xFF0F0F0F)
val SurfaceColor = Color(0xFF1A1A1A)
val Surface2Color = Color(0xFF242424)
val BorderColor = Color(0xFF333333)
val TextWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0xFFA0A0A0)
val AccentBlue = Color(0xFF3B82F6)
val AccentGreen = Color(0xFF22C55E)
val DangerRed = Color(0xFFEF4444)
val GoldColor = Color(0xFFD4AF37)

// ==========================================
// 3. REPOSITORY & STORAGE (SHARED PREFERENCES)
// ==========================================
class NotaMeteRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("NotaMetePrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getDaftarTimbangan(): MutableList<ItemTimbangan> {
        val json = prefs.getString("notaMete_timbangan", null) ?: return defaultTimbanganList()
        return gson.fromJson(json, object : TypeToken<MutableList<ItemTimbangan>>() {}.type)
    }

    fun saveDaftarTimbangan(list: List<ItemTimbangan>) {
        prefs.edit().putString("notaMete_timbangan", gson.toJson(list)).apply()
    }

    private fun defaultTimbanganList(): MutableList<ItemTimbangan> = mutableListOf(
        ItemTimbangan(11.2, 25), ItemTimbangan(10.8, 25), ItemTimbangan(11.5, 25), ItemTimbangan(11.1, 25),
        ItemTimbangan(10.9, 25), ItemTimbangan(11.3, 25), ItemTimbangan(11.0, 25), ItemTimbangan(11.4, 25),
        ItemTimbangan(11.6, 25), ItemTimbangan(10.7, 25), ItemTimbangan(11.2, 25), ItemTimbangan(11.0, 25),
        ItemTimbangan(11.1, 25), ItemTimbangan(11.5, 25), ItemTimbangan(10.8, 25), ItemTimbangan(11.3, 25),
        ItemTimbangan(10.6, 25), ItemTimbangan(11.2, 25), ItemTimbangan(11.1, 25), ItemTimbangan(11.5, 25),
        ItemTimbangan(11.4, 25), ItemTimbangan(10.9, 25), ItemTimbangan(11.7, 25), ItemTimbangan(10.8, 25),
        ItemTimbangan(11.0, 25), ItemTimbangan(11.1, 25), ItemTimbangan(11.3, 25), ItemTimbangan(11.6, 25),
        ItemTimbangan(11.3, 25), ItemTimbangan(10.6, 25), ItemTimbangan(11.4, 25), ItemTimbangan(11.2, 25),
        ItemTimbangan(10.7, 25), ItemTimbangan(11.0, 25), ItemTimbangan(10.9, 25), ItemTimbangan(11.1, 25),
        ItemTimbangan(11.5, 25), ItemTimbangan(11.4, 25), ItemTimbangan(11.0, 25), ItemTimbangan(11.3, 25)
    )

    fun getMasterBakul(): MutableList<String> {
        val json = prefs.getString("notaMete_masterBakul", null)
        return if (json != null) gson.fromJson(json, object : TypeToken<MutableList<String>>() {}.type)
        else mutableListOf("Bakul Haji Somad", "Bakul Bu Sri", "Bakul Pak Budi", "Bakul Marni", "Bakul Suroso", "Bakul Roni", "Bakul Aisyah")
    }

    fun saveMasterBakul(list: List<String>) {
        prefs.edit().putString("notaMete_masterBakul", gson.toJson(list)).apply()
    }

    fun getBakulAktif(): String = prefs.getString("notaMete_bakulAktif", getMasterBakul().firstOrNull() ?: "Bakul Haji Somad")!!
    fun setBakulAktif(nama: String) = prefs.edit().putString("notaMete_bakulAktif", nama).apply()

    fun getDataBakul(namaBakul: String): DataBakul {
        val json = prefs.getString("notaMete_data_$namaBakul", null) ?: return DataBakul()
        return gson.fromJson(json, DataBakul::class.java)
    }

    fun saveDataBakul(namaBakul: String, data: DataBakul) {
        prefs.edit().putString("notaMete_data_$namaBakul", gson.toJson(data)).apply()
    }

    fun removeDataBakul(namaBakul: String) {
        prefs.edit().remove("notaMete_data_$namaBakul").apply()
    }

    fun cekGantiHari() {
        val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val lastDate = prefs.getString("notaMete_tanggalTerakhir", null)

        if (lastDate == null) {
            prefs.edit().putString("notaMete_tanggalTerakhir", todayStr).apply()
            return
        }

        if (todayStr != lastDate) {
            for (bakul in getMasterBakul()) {
                val data = getDataBakul(bakul)
                var subTotalKemarin = 0.0
                data.rincian.forEach { subTotalKemarin += it.bersih * data.hargaPerKg }
                val sisaAkhirMalam = (subTotalKemarin + data.sisaKemarin) - data.dibayar

                data.sisaKemarin = sisaAkhirMalam
                data.rincian.clear()
                data.dibayar = 0.0
                saveDataBakul(bakul, data)
            }
            prefs.edit().putString("notaMete_tanggalTerakhir", todayStr).apply()
        }
    }
}

// ==========================================
// 4. MAIN ACTIVITY & APP ROUTER
// ==========================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = NotaMeteRepository(this)
        repository.cekGantiHari()

        setContent {
            MaterialTheme {
                var currentScreen by remember { mutableStateOf("home") }

                Surface(modifier = Modifier.fillMaxSize(), color = BgColor) {
                    when (currentScreen) {
                        "home" -> HomeScreen(
                            onNavigateToNota = { currentScreen = "nota" },
                            onNavigateToTimbang = { currentScreen = "timbang" },
                            onExitApp = { finish() }
                        )
                        "timbang" -> TimbangScreen(
                            repository = repository,
                            onBack = { currentScreen = "home" }
                        )
                        "nota" -> NotaScreen(
                            repository = repository,
                            onBack = { currentScreen = "home" }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. SCREEN 1: HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(onNavigateToNota: () -> Unit, onNavigateToTimbang: () -> Unit, onExitApp: () -> Unit) {
    var showModalKeluar by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BgColor).padding(24.dp)) {
        IconButton(
            onClick = { showModalKeluar = true },
            modifier = Modifier.align(Alignment.TopEnd).size(40.dp).background(SurfaceColor, CircleShape).border(1.5.dp, BorderColor, CircleShape)
        ) {
            Text("✕", color = TextMuted, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(40.dp))
                Box(
                    modifier = Modifier.size(88.dp).clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF1E1E1E), Color(0xFF2A2A2A))))
                        .border(1.dp, BorderColor, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NM", color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Nota Mete", color = TextWhite, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                Text("Aplikasi Nota & Data Timbang", color = TextMuted, fontSize = 15.sp)
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Button(
                    onClick = onNavigateToNota,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("📋   Masuk Nota", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }

                OutlinedButton(
                    onClick = onNavigateToTimbang,
                    shape = RoundedCornerShape(50),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceColor),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("⚖️   Data Timbang", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
            }

            Text("by mete farm", color = TextMuted, fontSize = 13.sp)
        }
    }

    if (showModalKeluar) {
        AlertDialog(
            onDismissRequest = { showModalKeluar = false },
            containerColor = SurfaceColor,
            title = { Text("Keluar Aplikasi?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi Nota Mete?", color = TextMuted) },
            confirmButton = {
                Button(onClick = onExitApp, colors = ButtonDefaults.buttonColors(containerColor = DangerRed)) {
                    Text("Keluar", color = TextWhite)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showModalKeluar = false }) { Text("Batal", color = TextWhite) }
            }
        )
    }
}

// ==========================================
// 6. SCREEN 2: DATA TIMBANG
// ==========================================
@Composable
fun TimbangScreen(repository: NotaMeteRepository, onBack: () -> Unit) {
    var daftarTimbangan by remember { mutableStateOf(repository.getDaftarTimbangan()) }
    var inputKgText by remember { mutableStateOf("") }
    var settingKeranjang by remember { mutableStateOf("11.0") }
    var settingEkor by remember { mutableStateOf("25") }

    var itemToDeleteIndex by remember { mutableStateOf<Int?>(null) }
    var showResetModal by remember { mutableStateOf(false) }
    var showSaveModal by remember { mutableStateOf(false) }

    val currentDateStr = remember {
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date())
    }

    val beratKeranjangSatuan = settingKeranjang.toDoubleOrNull() ?: 0.0
    val totalKg = daftarTimbangan.sumOf { it.kg }
    val totalEkor = daftarTimbangan.sumOf { it.ekor }
    val totalKeranjang = daftarTimbangan.size * beratKeranjangSatuan
    val beratBersih = totalKg - totalKeranjang
    val rataBobot = if (totalEkor > 0) beratBersih / totalEkor else 0.0

    Column(modifier = Modifier.fillMaxSize().background(BgColor).padding(12.dp)) {
        Text("← Kembali", color = TextMuted, modifier = Modifier.clickable { onBack() }, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Data Timbang", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text(currentDateStr, color = TextMuted, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor), shape = RoundedCornerShape(18.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("INPUT TIMBANGAN BARU", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputKgText,
                        onValueChange = { inputKgText = it },
                        placeholder = { Text("Kg (misal: 11.2)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val kgVal = inputKgText.toDoubleOrNull()
                            val ekorVal = settingEkor.toIntOrNull() ?: 25
                            if (kgVal != null && kgVal > 0) {
                                daftarTimbangan = (daftarTimbangan + ItemTimbangan(kgVal, ekorVal)).toMutableList()
                                repository.saveDaftarTimbangan(daftarTimbangan)
                                inputKgText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Text("Tambah", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = settingKeranjang,
                        onValueChange = { settingKeranjang = it },
                        label = { Text("SETT. KERANJANG") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = settingEkor,
                        onValueChange = { settingEkor = it },
                        label = { Text("SETT. EKOR") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor), shape = RoundedCornerShape(18.dp), modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    repeat(4) { Text("EKR KG.", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(columns = GridCells.Fixed(4), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    itemsIndexed(daftarTimbangan) { index, item ->
                        Box(
                            modifier = Modifier.background(Surface2Color, RoundedCornerShape(8.dp)).clickable { itemToDeleteIndex = index }.padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${item.ekor}", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(String.format(Locale.US, "%.1f", item.kg), color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor), shape = RoundedCornerShape(18.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                SummaryRow("Jml Total", String.format(Locale.US, "%.1f kg", totalKg))
                SummaryRow("Berat Keranjang", String.format(Locale.US, "%.1f kg", totalKeranjang))
                SummaryRow("Jumlah Ekor", "$totalEkor ekor")
                SummaryRow("Berat Bersih", String.format(Locale.US, "%.1f kg", beratBersih), isHighlight = true)
                SummaryRow("Rata2 Bobot/Ekor", String.format(Locale.US, "%.2f kg", rataBobot))

                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            repository.saveDaftarTimbangan(daftarTimbangan)
                            showSaveModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simpan Data", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = { showResetModal = true }, modifier = Modifier.weight(1f)) {
                        Text("Reset Data", color = DangerRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (itemToDeleteIndex != null) {
        val idx = itemToDeleteIndex!!
        val item = daftarTimbangan[idx]
        AlertDialog(
            onDismissRequest = { itemToDeleteIndex = null },
            containerColor = SurfaceColor,
            title = { Text("Hapus Item", color = TextWhite) },
            text = { Text("Hapus data timbangan ke-${idx + 1} (${item.ekor} ekr / ${item.kg} kg)?", color = TextMuted) },
            confirmButton = {
                Button(onClick = {
                    daftarTimbangan = daftarTimbangan.toMutableList().apply { removeAt(idx) }
                    repository.saveDaftarTimbangan(daftarTimbangan)
                    itemToDeleteIndex = null
                }, colors = ButtonDefaults.buttonColors(containerColor = DangerRed)) { Text("Hapus") }
            },
            dismissButton = { OutlinedButton(onClick = { itemToDeleteIndex = null }) { Text("Batal") } }
        )
    }

    if (showResetModal) {
        AlertDialog(
            onDismissRequest = { showResetModal = false },
            containerColor = SurfaceColor,
            title = { Text("Reset Data", color = TextWhite) },
            text = { Text("Apakah Anda yakin ingin menghapus SELURUH data timbangan?", color = TextMuted) },
            confirmButton = {
                Button(onClick = {
                    daftarTimbangan = mutableListOf()
                    repository.saveDaftarTimbangan(daftarTimbangan)
                    showResetModal = false
                }, colors = ButtonDefaults.buttonColors(containerColor = DangerRed)) { Text("Reset") }
            },
            dismissButton = { OutlinedButton(onClick = { showResetModal = false }) { Text("Batal") } }
        )
    }

    if (showSaveModal) {
        AlertDialog(
            onDismissRequest = { showSaveModal = false },
            containerColor = SurfaceColor,
            title = { Text("Berhasil", color = TextWhite) },
            text = { Text("Data timbangan berhasil disimpan!", color = TextMuted) },
            confirmButton = { Button(onClick = { showSaveModal = false }) { Text("OK") } }
        )
    }
}

// ==========================================
// 7. SCREEN 3: NOTA PENJUALAN
// ==========================================
@Composable
fun NotaScreen(repository: NotaMeteRepository, onBack: () -> Unit) {
    val context = LocalContext.current
    var masterBakul by remember { mutableStateOf(repository.getMasterBakul()) }
    var bakulAktif by remember { mutableStateOf(repository.getBakulAktif()) }
    var dataNota by remember { mutableStateOf(repository.getDataBakul(bakulAktif)) }

    var timeText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }

    var showBakulModal by remember { mutableStateOf(false) }
    var showInputModal by remember { mutableStateOf(false) }
    var showTambahBakulModal by remember { mutableStateOf(false) }
    var showEditBakulModal by remember { mutableStateOf(false) }
    var showAksiBakulModal by remember { mutableStateOf(false) }
    var showDeleteRowModal by remember { mutableStateOf<Int?>(null) }
    var showSavedNotifModal by remember { mutableStateOf(false) }

    var bakulTargetAksi by remember { mutableStateOf("") }
    var inputNamaBakulBaru by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val hariNama = arrayOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
        while (true) {
            val now = Calendar.getInstance()
            val hari = hariNama[now.get(Calendar.DAY_OF_WEEK) - 1]
            val tgl = now.get(Calendar.DAY_OF_MONTH)
            val bln = now.get(Calendar.MONTH) + 1
            val thn = now.get(Calendar.YEAR).toString().takeLast(2)
            dateText = "$hari, $tgl/$bln/'$thn"

            timeText = String.format(Locale.US, "%02d:%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND))
            repository.cekGantiHari()
            delay(1000)
        }
    }

    val formatRp = { nominal: Double -> "Rp " + NumberFormat.getInstance(Locale("id", "ID")).format(nominal) }

    val totalBersihKg = dataNota.rincian.sumOf { it.bersih }
    val akumulasiTotalHarga = dataNota.rincian.sumOf { it.calculateSubtotal(dataNota.hargaPerKg) }
    val jumlahTotal = akumulasiTotalHarga + dataNota.sisaKemarin
    val sisa = jumlahTotal - dataNota.dibayar

    Column(modifier = Modifier.fillMaxSize().background(BgColor).padding(12.dp)) {
        Text("← Kembali", color = TextMuted, modifier = Modifier.clickable { onBack() }, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { showBakulModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                border = androidx.compose.foundation.BorderStroke(2.dp, AccentBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f).height(50.dp)
            ) { Text("Pilih Bakul", color = TextWhite, fontWeight = FontWeight.Bold) }

            Button(
                onClick = { showInputModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                border = androidx.compose.foundation.BorderStroke(2.dp, AccentBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f).height(50.dp)
            ) { Text("Input Nota", color = TextWhite, fontWeight = FontWeight.Bold) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(bakulAktif, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Column(horizontalAlignment = Alignment.End) {
                Text(dateText, color = TextMuted, fontSize = 13.sp)
                Text(timeText, color = GoldColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Hrg. /kg Rp ", color = TextMuted, fontSize = 15.sp)
            OutlinedTextField(
                value = dataNota.hargaPerKg.toInt().toString(),
                onValueChange = {
                    dataNota = dataNota.copy(hargaPerKg = it.toDoubleOrNull() ?: 0.0)
                    repository.saveDataBakul(bakulAktif, dataNota)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor), shape = RoundedCornerShape(20.dp), modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ekor", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Timbangan-Krj", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Bersih", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Harga", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("", modifier = Modifier.width(20.dp))
                }

                Divider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(dataNota.rincian) { idx, item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.ekor}", color = TextWhite, fontSize = 14.sp)
                            Text("${item.kotor} - ${item.keranjang}", color = TextWhite, fontSize = 14.sp)
                            Text(String.format(Locale.US, "%.1f", item.bersih), color = TextWhite, fontSize = 14.sp)
                            Text(formatRp(item.calculateSubtotal(dataNota.hargaPerKg)), color = TextWhite, fontSize = 14.sp)
                            IconButton(onClick = { showDeleteRowModal = idx }, modifier = Modifier.size(24.dp)) {
                                Text("✕", color = DangerRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Divider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))

                SummaryRow("Sisa Kemarin", formatRp(dataNota.sisaKemarin))
                SummaryRow("Jumlah Total", formatRp(jumlahTotal))
                SummaryRow("Dibayar", formatRp(dataNota.dibayar))
                SummaryRow("Sisa", formatRp(sisa), isHighlight = true)

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        repository.saveDataBakul(bakulAktif, dataNota)
                        showSavedNotifModal = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("💾 Simpan Nota", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showBakulModal) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredBakul = masterBakul.filter { it.contains(searchQuery, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showBakulModal = false },
            containerColor = SurfaceColor,
            title = { Text("Pilih Bakul", color = TextWhite) },
            text = {
                Column(modifier = Modifier.height(300.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari Nama Bakul...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        itemsIndexed(filteredBakul) { _, nama ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    bakulAktif = nama
                                    repository.setBakulAktif(nama)
                                    dataNota = repository.getDataBakul(nama)
                                }.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(nama, color = if (nama == bakulAktif) AccentBlue else TextWhite, fontWeight = FontWeight.Bold)
                                IconButton(onClick = {
                                    bakulTargetAksi = nama
                                    showAksiBakulModal = true
                                }) { Text("⋮", color = TextMuted) }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showBakulModal = false }) { Text("Oke") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTambahBakulModal = true }) { Text("+ Tambah Bakul") }
            }
        )
    }

    if (showInputModal) {
        var inputEkor by remember { mutableStateOf("") }
        var inputKotor by remember { mutableStateOf("") }
        var inputDibayar by remember { mutableStateOf(if (dataNota.dibayar > 0) dataNota.dibayar.toInt().toString() else "") }

        AlertDialog(
            onDismissRequest = { showInputModal = false },
            containerColor = SurfaceColor,
            title = { Text("Input Nota", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputEkor,
                        onValueChange = { inputEkor = it },
                        label = { Text("Jumlah Ekor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputKotor,
                        onValueChange = { inputKotor = it },
                        label = { Text("Berat Timbangan (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputDibayar,
                        onValueChange = { inputDibayar = it },
                        label = { Text("Dibayar (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val ekor = inputEkor.toIntOrNull() ?: 0
                    val kotor = inputKotor.toDoubleOrNull() ?: 0.0
                    val dibayar = inputDibayar.toDoubleOrNull() ?: dataNota.dibayar

                    val newRincian = dataNota.rincian.toMutableList()
                    if (ekor > 0 || kotor > 0) {
                        newRincian.add(RincianNota(ekor, kotor, 11.0))
                    }
                    dataNota = dataNota.copy(dibayar = dibayar, rincian = newRincian)
                    repository.saveDataBakul(bakulAktif, dataNota)
                    showInputModal = false
                }) { Text("Simpan") }
            },
            dismissButton = { OutlinedButton(onClick = { showInputModal = false }) { Text("Batal") } }
        )
    }

    if (showSavedNotifModal) {
        AlertDialog(
            onDismissRequest = { showSavedNotifModal = false },
            containerColor = SurfaceColor,
            title = { Text("✅ Nota Berhasil Disimpan!", color = TextWhite) },
            text = { Text("Data rincian nota telah tersimpan aman ke sistem.", color = TextMuted) },
            confirmButton = {
                Button(onClick = { showSavedNotifModal = false }) { Text("Selesai & Oke") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    val txtContent = buildNotaTxt(bakulAktif, dateText, timeText, dataNota)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Nota Penjualan $bakulAktif")
                        putExtra(Intent.EXTRA_TEXT, txtContent)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Download / Share Struk Nota"))
                }) { Text("📄 Download Struk (.txt)") }
            }
        )
    }

    if (showAksiBakulModal) {
        AlertDialog(
            onDismissRequest = { showAksiBakulModal = false },
            containerColor = SurfaceColor,
            title = { Text("Konfirmasi", color = TextWhite) },
            text = { Text("Pilih aksi untuk \"$bakulTargetAksi\"", color = TextMuted) },
            confirmButton = {
                Button(onClick = {
                    showAksiBakulModal = false
                    inputNamaBakulBaru = bakulTargetAksi
                    showEditBakulModal = true
                }) { Text("Edit") }
            },
            dismissButton = {
                Button(
                    onClick = {
                        if (masterBakul.size > 1) {
                            masterBakul = masterBakul.toMutableList().apply { remove(bakulTargetAksi) }
                            repository.saveMasterBakul(masterBakul)
                            repository.removeDataBakul(bakulTargetAksi)
                            if (bakulAktif == bakulTargetAksi) {
                                bakulAktif = masterBakul.first()
                                repository.setBakulAktif(bakulAktif)
                                dataNota = repository.getDataBakul(bakulAktif)
                            }
                            showAksiBakulModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) { Text("Hapus") }
            }
        )
    }

    if (showTambahBakulModal) {
        var newBakulName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showTambahBakulModal = false },
            containerColor = SurfaceColor,
            title = { Text("Tambah Bakul Baru", color = TextWhite) },
            text = {
                OutlinedTextField(
                    value = newBakulName,
                    onValueChange = { newBakulName = it },
                    placeholder = { Text("contoh: Bakul Anto") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newBakulName.isNotBlank() && !masterBakul.contains(newBakulName)) {
                        masterBakul = (masterBakul + newBakulName.trim()).toMutableList()
                        repository.saveMasterBakul(masterBakul)
                        bakulAktif = newBakulName.trim()
                        repository.setBakulAktif(bakulAktif)
                        dataNota = repository.getDataBakul(bakulAktif)
                    }
                    showTambahBakulModal = false
                }) { Text("Simpan") }
            },
            dismissButton = { OutlinedButton(onClick = { showTambahBakulModal = false }) { Text("Batal") } }
        )
    }

    if (showDeleteRowModal != null) {
        val idx = showDeleteRowModal!!
        AlertDialog(
            onDismissRequest = { showDeleteRowModal = null },
            containerColor = SurfaceColor,
            title = { Text("Hapus Baris?", color = TextWhite) },
            text = { Text("Apakah Anda yakin ingin menghapus data rincian baris ini?", color = TextMuted) },
            confirmButton = {
                Button(onClick = {
                    val updatedRincian = dataNota.rincian.toMutableList().apply { removeAt(idx) }
                    dataNota = dataNota.copy(rincian = updatedRincian)
                    repository.saveDataBakul(bakulAktif, dataNota)
                    showDeleteRowModal = null
                }, colors = ButtonDefaults.buttonColors(containerColor = DangerRed)) { Text("Hapus") }
            },
            dismissButton = { OutlinedButton(onClick = { showDeleteRowModal = null }) { Text("Batal") } }
        )
    }
}

// ==========================================
// 8. HELPER EKSPOR STRUK TEKS
// ==========================================
fun buildNotaTxt(bakul: String, date: String, time: String, data: DataBakul): String {
    val fmt = NumberFormat.getInstance(Locale("id", "ID"))
    var txt = "========================================\n"
    txt += "            NOTA PENJUALAN              \n"
    txt += "              METE FARM                 \n"
    txt += "========================================\n"
    txt += "Nama Bakul : $bakul\n"
    txt += "Tanggal    : $date\n"
    txt += "Jam        : $time\n"
    txt += "Harga / kg : Rp ${fmt.format(data.hargaPerKg)}\n"
    txt += "----------------------------------------\n"
    txt += "No  Ekor  Timbangan-Krj  Bersih    Subtotal\n"
    txt += "----------------------------------------\n"

    var totalBersih = 0.0
    var totalHargaTimbangan = 0.0

    data.rincian.forEachIndexed { i, item ->
        val subtotal = item.calculateSubtotal(data.hargaPerKg)
        totalBersih += item.bersih
        totalHargaTimbangan += subtotal
        txt += String.format(Locale.US, "%2d. %4d  %-13s  %-8.1f  Rp %s\n", i + 1, item.ekor, "${item.kotor}-${item.keranjang}", item.bersih, fmt.format(subtotal))
    }

    val totalAkhir = totalHargaTimbangan + data.sisaKemarin
    val sisa = totalAkhir - data.dibayar

    txt += "----------------------------------------\n"
    txt += "Total Berat  : ${String.format(Locale.US, "%.1f", totalBersih)} kg\n"
    txt += "Sisa Kemarin : Rp ${fmt.format(data.sisaKemarin)}\n"
    txt += "Jumlah Total : Rp ${fmt.format(totalAkhir)}\n"
    txt += "Dibayar      : Rp ${fmt.format(data.dibayar)}\n"
    txt += "Sisa         : Rp ${fmt.format(sisa)}\n"
    txt += "========================================\n"
    txt += "Terima kasih atas kerjasamanya!\n"
    return txt
}

// ==========================================
// 9. HELPER COMPONENT: SUMMARY ROW
// ==========================================
@Composable
fun SummaryRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (isHighlight) AccentGreen else TextMuted,
            fontSize = if (isHighlight) 16.sp else 14.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            color = if (isHighlight) AccentGreen else TextWhite,
            fontSize = if (isHighlight) 16.sp else 14.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
        )
    }
}
