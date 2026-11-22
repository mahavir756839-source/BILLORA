package com.billora.billing

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("billora_v2", MODE_PRIVATE) }
    private val navy = Color.rgb(20, 39, 70)
    private val blue = Color.rgb(39, 105, 220)
    private val pale = Color.rgb(239, 244, 251)
    private val muted = Color.rgb(112, 126, 148)
    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout
    private var page = "Home"
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun arr(k: String) = try { JSONArray(prefs.getString(k, "[]")) } catch (_: Exception) { JSONArray() }
    private fun save(k: String, a: JSONArray) { prefs.edit().putString(k, a.toString()).apply() }
    private fun money(v: Double) = "₹" + String.format(Locale("en", "IN"), "%,.2f", v)
    private fun today() = SimpleDateFormat("dd MMM yyyy", Locale("en", "IN")).format(Date())
    private fun shape(color: Int, radius: Int = 18, stroke: Int = 0): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat(); if (stroke != 0) setStroke(dp(1), stroke) }

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); window.statusBarColor = navy; window.navigationBarColor = Color.WHITE; window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR; showPage("Home") }

    private fun showPage(name: String) {
        page = name
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(pale) }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(17), dp(18), dp(17)); setBackgroundColor(navy)
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(TextView(this).apply { text = "BILLORA"; textSize = 23f; letterSpacing = .08f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.WHITE) })
        brand.addView(TextView(this).apply { text = prefs.getString("firm", "Your Business")?.ifBlank { "Your Business" } ?: "Your Business"; textSize = 12f; setTextColor(Color.rgb(195, 211, 235)); setPadding(0, dp(3), 0, 0) })
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val settings = TextView(this).apply { text = "⚙"; textSize = 24f; gravity = Gravity.CENTER; setTextColor(Color.WHITE); setPadding(dp(12), dp(5), dp(5), dp(5)); setOnClickListener { showPage("More") } }
        header.addView(settings)
        root.addView(header)

        val scroll = ScrollView(this).apply { clipToPadding = false; isFillViewport = true }
        body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(17), dp(17), dp(17), dp(22)) }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(dp(4), dp(7), dp(4), dp(7)); setBackgroundColor(Color.WHITE); elevation = dp(12).toFloat() }
        val tabs = listOf(Triple("⌂", "Home", "Home"), Triple("＋", "Sale", "Sale"), Triple("▤", "Purchase", "Purchase"), Triple("♙", "Parties", "Parties"), Triple("☰", "More", "More"))
        tabs.forEach { (symbol, label, target) ->
            val selected = page == target || (page == "Items" && target == "More") || (page == "Reports" && target == "More")
            val item = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(0, dp(4), 0, dp(3)); setOnClickListener { showPage(target) } }
            item.addView(TextView(this).apply { text = symbol; textSize = 20f; gravity = Gravity.CENTER; setTextColor(if (selected) blue else muted) })
            item.addView(TextView(this).apply { text = label; textSize = 10f; gravity = Gravity.CENTER; typeface = if (selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT; setTextColor(if (selected) blue else muted); setPadding(0, dp(2), 0, 0) })
            bottom.addView(item, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(bottom)
        setContentView(root)
        when (name) { "Home" -> home(); "Sale" -> invoiceForm(false); "Purchase" -> invoiceForm(true); "Parties" -> parties(); "Items" -> items(); "Reports" -> reports(); else -> more() }
    }

    private fun label(s: String, size: Float = 15f, color: Int = navy, bold: Boolean = false): TextView =
        TextView(this).apply { text = s; textSize = size; setTextColor(color); if (bold) typeface = Typeface.DEFAULT_BOLD }
    private fun heading(s: String, sub: String? = null) {
        body.addView(label(s, 19f, navy, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7); bottomMargin = dp(5) })
        if (sub != null) body.addView(label(sub, 12f, muted), LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })
        else body.addView(View(this), LinearLayout.LayoutParams(1, dp(8)))
    }
    private fun field(hint: String, value: String = "") = EditText(this).apply {
        this.hint = hint; setText(value); textSize = 14f; setSingleLine(true); setTextColor(navy); setHintTextColor(Color.rgb(150, 161, 179))
        setPadding(dp(14), 0, dp(14), 0); background = shape(Color.WHITE, 13, Color.rgb(222, 229, 239))
        layoutParams = LinearLayout.LayoutParams(-1, dp(52)).apply { bottomMargin = dp(10) }
    }
    private fun button(label: String, action: () -> Unit, secondary: Boolean = false) {
        val b = TextView(this).apply {
            text = label; textSize = 14f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            setTextColor(if (secondary) blue else Color.WHITE); background = shape(if (secondary) Color.rgb(232, 240, 255) else blue, 14)
            setPadding(dp(12), dp(14), dp(12), dp(14)); setOnClickListener { action() }
        }
        body.addView(b, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) })
    }
    private fun metric(labelText: String, value: String, icon: String, tint: Int) : View {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(13), dp(12), dp(14)); background = shape(Color.WHITE, 17, Color.rgb(231, 236, 244)) }
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        top.addView(label(icon, 18f, tint, true))
        top.addView(label(labelText, 11f, muted, true), LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(7) })
        box.addView(top)
        box.addView(label(value, 19f, navy, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })
        return box
    }
    private fun metricGrid() {
        val grid = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val left = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val right = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        left.addView(metric("TOTAL SALES", money(total("sales")), "↗", Color.rgb(27, 163, 111)))
        right.addView(metric("PURCHASE", money(total("purchases")), "↙", Color.rgb(227, 145, 44)))
        left.addView(metric("PARTIES", arr("parties").length().toString(), "♙", blue))
        right.addView(metric("ITEMS", arr("items").length().toString(), "▦", Color.rgb(139, 91, 204)))
        grid.addView(left, LinearLayout.LayoutParams(0, -2, 1f).apply { rightMargin = dp(6) })
        grid.addView(right, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(6) })
        body.addView(grid)
    }
    private fun total(key: String): Double { val a = arr(key); var sum = 0.0; for (i in 0 until a.length()) sum += a.optJSONObject(i)?.optDouble("total", 0.0) ?: 0.0; return sum }

    private fun home() {
        val welcome = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(17), dp(18), dp(18)); background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(navy, Color.rgb(43, 91, 157))).apply { cornerRadius = dp(19).toFloat() } }
        welcome.addView(label("Good business starts here", 17f, Color.WHITE, true))
        welcome.addView(label(today() + "  •  Your business at a glance", 11f, Color.rgb(210, 224, 246)), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) })
        welcome.addView(label("Manage bills. Track payments. Grow faster.", 12f, Color.WHITE), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) })
        body.addView(welcome, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(18) })
        heading("Business Overview", "Your sales and purchase summary")
        metricGrid()
        heading("Quick Actions", "Create a new entry in seconds")
        val actions = listOf(Triple("＋  New Sale", "Sale", false), Triple("＋  Purchase", "Purchase", true), Triple("♙  Add Party", "Parties", true), Triple("▦  Add Item", "Items", true))
        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.forEachIndexed { i, item ->
            val b = TextView(this).apply { text = item.first; textSize = 13f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setTextColor(if (i == 0) Color.WHITE else blue); background = shape(if (i == 0) blue else Color.WHITE, 14, if (i == 0) 0 else Color.rgb(225, 232, 243)); setPadding(dp(5), dp(17), dp(5), dp(17)); setOnClickListener { showPage(item.second) } }
            (if (i < 2) row1 else row2).addView(b, LinearLayout.LayoutParams(0, -2, 1f).apply { if (i % 2 == 0) rightMargin = dp(6) else leftMargin = dp(6) })
        }
        body.addView(row1); body.addView(View(this), LinearLayout.LayoutParams(1, dp(9))); body.addView(row2)
        heading("Recent Transactions", "Latest saved entries")
        recent("sales", "Sales"); recent("purchases", "Purchases")
        button("View Reports  →", { showPage("Reports") }, true)
    }
    private fun recent(key: String, title: String) {
        body.addView(label(title, 13f, navy, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8); bottomMargin = dp(5) })
        val a = arr(key)
        if (a.length() == 0) {
            body.addView(label("No entries yet", 12f, muted), LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
            return
        }
        for (i in a.length() - 1 downTo maxOf(0, a.length() - 3)) {
            val o = a.optJSONObject(i) ?: continue
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = shape(Color.WHITE, 13, Color.rgb(232, 237, 245)) }
            val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            info.addView(label(o.optString("party"), 13f, navy, true))
            info.addView(label("${o.optString("date")}  •  ${o.optString("item")}", 10f, muted))
            row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(label(money(o.optDouble("total")), 13f, blue, true))
            body.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(6) })
        }
    }
    private fun invoiceForm(purchase: Boolean) {
        val kind = if (purchase) "Purchase" else "Sale"
        heading("Create $kind", "Enter transaction details below")
        body.addView(label("DATE  •  ${today()}", 11f, muted, true), LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })
        val party = field(if (purchase) "Supplier / Party name" else "Customer / Party name")
        val item = field("Item / Product name")
        val qty = field("Quantity", "1")
        val rate = field("Rate (₹)")
        val gst = field("GST %", "0")
        val notes = field("Notes (optional)")
        listOf(party, item, qty, rate, gst, notes).forEach { body.addView(it) }
        val preview = label("Grand Total   ₹0.00", 21f, blue, true)
        val totalCard = LinearLayout(this).apply { setPadding(dp(16), dp(15), dp(16), dp(15)); background = shape(Color.WHITE, 16, Color.rgb(220, 230, 246)); addView(preview) }
        body.addView(totalCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4); bottomMargin = dp(14) })
        fun calc(): Double { val base = (qty.text.toString().toDoubleOrNull() ?: 0.0) * (rate.text.toString().toDoubleOrNull() ?: 0.0); return base + base * (gst.text.toString().toDoubleOrNull() ?: 0.0) / 100.0 }
        val watcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { preview.text = "Grand Total   ${money(calc())}" }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }
        qty.addTextChangedListener(watcher); rate.addTextChangedListener(watcher); gst.addTextChangedListener(watcher)
        button("Save $kind", {
            val p = party.text.toString().trim(); val it = item.text.toString().trim()
            val q = qty.text.toString().toDoubleOrNull() ?: 0.0; val r = rate.text.toString().toDoubleOrNull() ?: 0.0; val g = gst.text.toString().toDoubleOrNull() ?: 0.0
            if (p.isBlank() || it.isBlank() || q <= 0 || r < 0 || g < 0) { Toast.makeText(this, "Party, item, quantity aur rate sahi bharein.", Toast.LENGTH_LONG).show(); return@button }
            val base = q * r; val grand = base + base * g / 100.0
            val obj = JSONObject().put("party", p).put("item", it).put("qty", q).put("rate", r).put("gst", g).put("base", base).put("total", grand).put("notes", notes.text.toString()).put("date", today())
            val key = if (purchase) "purchases" else "sales"; val a = arr(key); a.put(obj); save(key, a)
            Toast.makeText(this, "$kind saved: ${money(grand)}", Toast.LENGTH_LONG).show(); showPage(kind)
        })
        button("Share Preview", { share("BILLORA $kind\nParty: ${party.text}\nItem: ${item.text}\nQty: ${qty.text}\nRate: ₹${rate.text}\nGST: ${gst.text}%\nTotal: ${money(calc())}") }, true)
    }
    private fun parties() {
        heading("Parties", "Customers and suppliers")
        val name = field("Party / Customer / Supplier name"); val phone = field("Mobile number"); val opening = field("Opening balance (₹)", "0")
        body.addView(name); body.addView(phone); body.addView(opening)
        button("Save Party", {
            if (name.text.isBlank()) { name.error = "Name required"; return@button }
            val a = arr("parties"); a.put(JSONObject().put("name", name.text.toString().trim()).put("phone", phone.text.toString()).put("opening", opening.text.toString().toDoubleOrNull() ?: 0.0)); save("parties", a)
            Toast.makeText(this, "Party saved", Toast.LENGTH_SHORT).show(); showPage("Parties")
        })
        heading("Saved Parties")
        val a = arr("parties")
        if (a.length() == 0) body.addView(label("No parties added yet.", 13f, muted))
        for (i in 0 until a.length()) { val o = a.optJSONObject(i) ?: continue; val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)); background = shape(Color.WHITE, 14, Color.rgb(230, 236, 245)) }; box.addView(label(o.optString("name"), 14f, navy, true)); box.addView(label("${o.optString("phone")}  •  Opening: ${money(o.optDouble("opening"))}", 11f, muted)); body.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(7) }) }
    }
    private fun items() {
        heading("Items & Stock", "Manage your product catalogue")
        val name = field("Item name"); val code = field("Item / SKU code"); val unit = field("Unit (pcs, kg, bag etc.)", "pcs"); val rate = field("Sale rate (₹)", "0"); val stock = field("Opening stock", "0"); val gst = field("GST %", "0")
        listOf(name, code, unit, rate, stock, gst).forEach { body.addView(it) }
        button("Save Item", {
            if (name.text.isBlank()) { name.error = "Item name required"; return@button }
            val a = arr("items"); a.put(JSONObject().put("name", name.text.toString()).put("code", code.text.toString()).put("unit", unit.text.toString()).put("rate", rate.text.toString().toDoubleOrNull() ?: 0.0).put("stock", stock.text.toString().toDoubleOrNull() ?: 0.0).put("gst", gst.text.toString().toDoubleOrNull() ?: 0.0)); save("items", a)
            Toast.makeText(this, "Item saved", Toast.LENGTH_SHORT).show(); showPage("Items")
        })
        heading("Saved Items")
        val a = arr("items")
        if (a.length() == 0) body.addView(label("No items added yet.", 13f, muted))
        for (i in 0 until a.length()) { val o = a.optJSONObject(i) ?: continue; val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)); background = shape(Color.WHITE, 14, Color.rgb(230, 236, 245)) }; box.addView(label(o.optString("name"), 14f, navy, true)); box.addView(label("${money(o.optDouble("rate"))} / ${o.optString("unit")}  •  Stock: ${o.optDouble("stock")}  •  GST: ${o.optDouble("gst")}%", 11f, muted)); body.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(7) }) }
    }
    private fun reports() {
        heading("Reports", "Your business performance at a glance")
        reportCard("Total Sales", money(total("sales"))); reportCard("Total Purchase", money(total("purchases"))); reportCard("Sales - Purchase", money(total("sales") - total("purchases")))
        body.addView(label("Sale entries: ${arr("sales").length()}    •    Purchase entries: ${arr("purchases").length()}", 12f, muted))
        button("Share Business Summary", { share("BILLORA SUMMARY\nSales: ${money(total("sales"))}\nPurchase: ${money(total("purchases"))}\nDifference: ${money(total("sales") - total("purchases"))}") })
        body.addView(label("Note: Sales minus purchase is not audited net profit or a GST return.", 11f, muted))
    }
    private fun reportCard(title: String, value: String) { val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(15), dp(13), dp(15), dp(13)); background = shape(Color.WHITE, 15, Color.rgb(230, 236, 245)) }; box.addView(label(title, 12f, muted)); box.addView(label(value, 21f, navy, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5) }); body.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) }) }
    private fun more() {
        heading("Business Settings", "Your firm information")
        val firm = field("Firm / Business name", prefs.getString("firm", "") ?: ""); val gstin = field("GSTIN (optional)", prefs.getString("gstin", "") ?: ""); val address = field("Business address", prefs.getString("address", "") ?: ""); val phone = field("Contact number", prefs.getString("phone", "") ?: "")
        listOf(firm, gstin, address, phone).forEach { body.addView(it) }
        button("Save Firm Details", { prefs.edit().putString("firm", firm.text.toString()).putString("gstin", gstin.text.toString()).putString("address", address.text.toString()).putString("phone", phone.text.toString()).apply(); Toast.makeText(this, "Firm details saved", Toast.LENGTH_SHORT).show(); showPage("More") })
        heading("More Options")
        button("Item Catalogue", { showPage("Items") }, true); button("Business Reports", { showPage("Reports") }, true)
        button("Share Sales Register", { exportList("sales") }, true); button("Share Purchase Register", { exportList("purchases") }, true)
        body.addView(label("Your entries are stored on this phone. Cloud sync, automated stock ledger, PDF invoice printing, GST returns and multi-device login are future development items.", 11f, muted))
    }
    private fun exportList(key: String) { val a = arr(key); val out = StringBuilder("BILLORA ${key.uppercase(Locale.ROOT)}\n"); for (i in 0 until a.length()) { val o = a.optJSONObject(i) ?: continue; out.append("${o.optString("date")} | ${o.optString("party")} | ${o.optString("item")} | ${o.optDouble("qty")} x ${o.optDouble("rate")} | GST ${o.optDouble("gst")}% | ${money(o.optDouble("total"))}\n") }; share(out.toString()) }
    private fun share(message: String) { startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message) }, "Share with")) }
}
