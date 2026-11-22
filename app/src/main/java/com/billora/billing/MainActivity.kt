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

    fun showPage(name: String) {
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
        when (name) { "Home" -> AccountingScreens(this).show("Transactions"); "Parties" -> AccountingScreens(this).show("Parties"); "More" -> AccountingScreens(this).show("Menu"); "Items" -> AccountingScreens(this).show("Items"); "Sale" -> invoiceForm(false); "Purchase" -> invoiceForm(true); "Items" -> items(); "Reports" -> reports(); else -> more() }
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
        val key = if (purchase) "purchases" else "sales"
        val invoiceNo = (if (purchase) "PUR/" else "BG/") + (arr(key).length() + 1).toString().padStart(3, '0')
        heading(kind, "Create and save your ${kind.lowercase(Locale.ROOT)} invoice")

        body.addView(label(if (purchase) "PAYMENT TYPE" else "SALE TYPE", 11f, muted, true))
        val payment = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Credit", "Cash"))
            background = shape(Color.WHITE, 12, Color.rgb(222, 229, 239))
        }
        body.addView(payment, LinearLayout.LayoutParams(-1, dp(48)).apply { bottomMargin = dp(12) })

        val infoRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val noBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(10), dp(12), dp(10)); background = shape(Color.WHITE, 12, Color.rgb(222, 229, 239)) }
        noBox.addView(label("INVOICE NO.", 10f, muted, true))
        noBox.addView(label(invoiceNo, 16f, navy, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
        val dateBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(10), dp(12), dp(10)); background = shape(Color.WHITE, 12, Color.rgb(222, 229, 239)) }
        dateBox.addView(label("DATE", 10f, muted, true))
        dateBox.addView(label(today(), 15f, navy, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
        infoRow.addView(noBox, LinearLayout.LayoutParams(0, -2, 1f).apply { rightMargin = dp(6) })
        infoRow.addView(dateBox, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(6) })
        body.addView(infoRow, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })

        body.addView(label(if (purchase) "SUPPLIER *" else "CUSTOMER *", 11f, muted, true))
        val party = field(if (purchase) "Supplier / Party name" else "Customer name")
        val phone = field("Phone Number")
        body.addView(party); body.addView(phone)

        val addItems = TextView(this).apply {
            text = "＋  Add Items (Optional)"; textSize = 14f; typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER; setTextColor(blue); background = shape(Color.rgb(232, 240, 255), 13)
            setPadding(dp(12), dp(15), dp(12), dp(15))
        }
        body.addView(addItems, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4); bottomMargin = dp(12) })

        val itemPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14))
            background = shape(Color.WHITE, 15, Color.rgb(222, 229, 239)); visibility = View.GONE
        }
        itemPanel.addView(label(if (purchase) "Add Items to Purchase" else "Add Items to Sale", 16f, navy, true), LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })
        val item = field("Item Name  •  e.g. Chocolate Cake")
        val qty = field("Quantity", "1")
        val unit = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("pcs", "kg", "gram", "litre", "box", "bag", "dozen"))
            background = shape(Color.WHITE, 11, Color.rgb(222, 229, 239))
        }
        val rate = field("Rate (Price / Unit)")
        val taxMode = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Without Tax", "With Tax"))
            background = shape(Color.WHITE, 11, Color.rgb(222, 229, 239))
        }
        val gst = field("GST %", "0")
        itemPanel.addView(item)
        itemPanel.addView(label("Quantity", 11f, muted, true)); itemPanel.addView(qty)
        itemPanel.addView(label("Unit", 11f, muted, true))
        itemPanel.addView(unit, LinearLayout.LayoutParams(-1, dp(48)).apply { bottomMargin = dp(10) })
        itemPanel.addView(label("Rate (Price / Unit)", 11f, muted, true)); itemPanel.addView(rate)
        itemPanel.addView(label("Tax Type", 11f, muted, true))
        itemPanel.addView(taxMode, LinearLayout.LayoutParams(-1, dp(48)).apply { bottomMargin = dp(10) })
        itemPanel.addView(gst)
        body.addView(itemPanel)
        addItems.setOnClickListener {
            itemPanel.visibility = if (itemPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            addItems.text = if (itemPanel.visibility == View.VISIBLE) "−  Hide Items" else "＋  Add Items (Optional)"
        }

        val totalCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(14), dp(16), dp(14))
            background = shape(Color.WHITE, 16, Color.rgb(220, 230, 246))
        }
        totalCard.addView(label("TOTAL AMOUNT", 11f, muted, true))
        val preview = label("₹0.00", 25f, blue, true)
        totalCard.addView(preview, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5) })
        body.addView(totalCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6); bottomMargin = dp(15) })

        fun calc(): Double {
            val base = (qty.text.toString().toDoubleOrNull() ?: 0.0) * (rate.text.toString().toDoubleOrNull() ?: 0.0)
            val tax = if (taxMode.selectedItemPosition == 1) (gst.text.toString().toDoubleOrNull() ?: 0.0) else 0.0
            return base + base * tax / 100.0
        }
        val watcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { preview.text = money(if (itemPanel.visibility == View.VISIBLE) calc() else 0.0) }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }
        listOf(qty, rate, gst).forEach { it.addTextChangedListener(watcher) }
        taxMode.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                gst.visibility = if (position == 1) View.VISIBLE else View.GONE
                preview.text = money(if (itemPanel.visibility == View.VISIBLE) calc() else 0.0)
            }
        }
        val notes = field("Notes / Description (Optional)")
        body.addView(notes)

        button("Save & New", {
            val p = party.text.toString().trim()
            val hasItem = itemPanel.visibility == View.VISIBLE
            val it = item.text.toString().trim()
            val q = if (hasItem) qty.text.toString().toDoubleOrNull() ?: 0.0 else 0.0
            val r = if (hasItem) rate.text.toString().toDoubleOrNull() ?: 0.0 else 0.0
            val g = if (hasItem && taxMode.selectedItemPosition == 1) gst.text.toString().toDoubleOrNull() ?: 0.0 else 0.0
            if (p.isBlank() || (hasItem && (it.isBlank() || q <= 0 || r < 0 || g < 0))) {
                Toast.makeText(this, "Customer ka naam aur item details sahi bharein.", Toast.LENGTH_LONG).show()
                return@button
            }
            val base = q * r
            val grand = base + base * g / 100.0
            val obj = JSONObject().put("party", p).put("phone", phone.text.toString())
                .put("item", if (hasItem) it else "").put("qty", q).put("unit", unit.selectedItem.toString())
                .put("rate", r).put("gst", g).put("taxMode", if (g > 0) "With Tax" else "Without Tax")
                .put("base", base).put("total", grand).put("notes", notes.text.toString())
                .put("date", today()).put("invoiceNo", invoiceNo).put("paymentType", payment.selectedItem.toString())
            val a = arr(key); a.put(obj); save(key, a)
            Toast.makeText(this, "$kind saved: ${money(grand)}", Toast.LENGTH_LONG).show()
            showPage(kind)
        })
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
