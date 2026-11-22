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
        when (name) { "Home" -> home(); "Parties" -> AccountingScreens(this).show("Parties"); "More" -> AccountingScreens(this).show("Menu"); "Items" -> AccountingScreens(this).show("Items"); "Sale" -> invoiceForm(false); "Purchase" -> invoiceForm(true); "Items" -> items(); "Reports" -> reports(); else -> more() }
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
        heading(kind, "Create a clean, professional " + kind.lowercase(Locale.ROOT) + " bill")

        body.addView(label(if (purchase) "PURCHASE TYPE" else "SALE TYPE", 11f, muted, true))
        val payment = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Credit", "Cash"))
            background = shape(Color.WHITE, 12, Color.rgb(222, 229, 239))
        }
        body.addView(payment, LinearLayout.LayoutParams(-1, dp(48)).apply { bottomMargin = dp(12) })

        val meta = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val details = listOf("INVOICE NO." to invoiceNo, "DATE" to today())
        details.forEach { pair ->
            val box = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(11), dp(12), dp(11))
                background = shape(Color.WHITE, 13, Color.rgb(225, 232, 243))
                addView(label(pair.first, 10f, muted, true))
                addView(label(pair.second, 15f, navy, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5) })
            }
            meta.addView(box, LinearLayout.LayoutParams(0, -2, 1f).apply {
                if (pair.first == "INVOICE NO.") rightMargin = dp(5) else leftMargin = dp(5)
            })
        }
        body.addView(meta, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })

        heading("Party Details", "Customer or supplier information")
        val party = field(if (purchase) "Supplier name *" else "Customer name *")
        val phone = field("Phone number")
        body.addView(party); body.addView(phone)

        val section = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        section.addView(label("Items", 18f, navy, true), LinearLayout.LayoutParams(0, -2, 1f))
        val add = TextView(this).apply {
            text = "＋ Add Item"; textSize = 13f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            setTextColor(Color.WHITE); background = shape(blue, 12); setPadding(dp(14), dp(10), dp(14), dp(10))
        }
        section.addView(add)
        body.addView(section, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(7) })
        body.addView(label("Add multiple products or services to the same bill.", 11f, muted),
            LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })

        data class BillRow(val card: LinearLayout, val name: EditText, val qty: EditText, val rate: EditText,
                           val gst: EditText, val unit: Spinner, val tax: Spinner, val amount: TextView)
        val rows = mutableListOf<BillRow>()
        val host = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(host)

        val summary = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(15), dp(14), dp(15), dp(14))
            background = shape(Color.WHITE, 16, Color.rgb(216, 229, 247))
        }
        summary.addView(label("BILL SUMMARY", 10f, muted, true))
        val countView = label("0 items", 11f, muted)
        val taxableView = label("Taxable value  ₹0.00", 12f, navy)
        val gstView = label("GST included  ₹0.00", 12f, muted)
        val totalView = label("TOTAL  ₹0.00", 23f, blue, true)
        summary.addView(countView)
        summary.addView(taxableView, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        summary.addView(gstView, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
        summary.addView(totalView, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        body.addView(summary, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(13); bottomMargin = dp(12) })

        fun refreshTotals() {
            var gross = 0.0
            var gstTotal = 0.0
            var count = 0
            rows.forEach { row ->
                val q = row.qty.text.toString().toDoubleOrNull() ?: 0.0
                val r = row.rate.text.toString().toDoubleOrNull() ?: 0.0
                val g = row.gst.text.toString().toDoubleOrNull() ?: 0.0
                val amountValue = q * r
                val taxPart = if (row.tax.selectedItemPosition == 0 && g > 0) amountValue * g / (100 + g) else 0.0
                gross += amountValue
                gstTotal += taxPart
                if (row.name.text.toString().isNotBlank()) count++
                row.amount.text = "Amount: " + money(amountValue)
            }
            countView.text = count.toString() + " items added"
            taxableView.text = "Taxable value  " + money(gross - gstTotal)
            gstView.text = "GST included  " + money(gstTotal)
            totalView.text = "TOTAL  " + money(gross)
        }

        fun addBillRow() {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12))
                background = shape(Color.WHITE, 14, Color.rgb(225, 232, 243))
            }
            val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val title = label("ITEM " + (rows.size + 1), 11f, blue, true)
            head.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
            val remove = TextView(this).apply { text = "Remove"; textSize = 11f; setTextColor(Color.rgb(196, 62, 62)) }
            head.addView(remove)
            card.addView(head, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
            val name = field("Item name / Search item")
            val qty = field("Quantity", "1")
            val rate = field("Rate per unit", "0")
            val gst = field("GST %", "0")
            val unit = Spinner(this).apply {
                adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                    listOf("pcs", "kg", "gram", "litre", "box", "bag", "dozen", "ton"))
                background = shape(Color.WHITE, 10, Color.rgb(222, 229, 239))
            }
            val tax = Spinner(this).apply {
                adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                    listOf("GST Included in Rate", "No GST"))
                background = shape(Color.WHITE, 10, Color.rgb(222, 229, 239))
            }
            card.addView(name)
            val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row1.addView(qty, LinearLayout.LayoutParams(0, dp(50), 1f).apply { rightMargin = dp(5) })
            row1.addView(unit, LinearLayout.LayoutParams(0, dp(50), 1f).apply { leftMargin = dp(5); rightMargin = dp(5); bottomMargin = dp(10) })
            row1.addView(rate, LinearLayout.LayoutParams(0, dp(50), 1f).apply { leftMargin = dp(5) })
            card.addView(row1)
            val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row2.addView(gst, LinearLayout.LayoutParams(0, dp(50), 1f).apply { rightMargin = dp(5) })
            row2.addView(tax, LinearLayout.LayoutParams(0, dp(50), 1f).apply { leftMargin = dp(5); bottomMargin = dp(10) })
            card.addView(row2)
            val amount = label("Amount: ₹0.00", 12f, navy, true)
            card.addView(amount)
            val itemRow = BillRow(card, name, qty, rate, gst, unit, tax, amount)
            rows.add(itemRow)
            val watcher = object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { refreshTotals() }
                override fun afterTextChanged(s: android.text.Editable?) {}
            }
            listOf(qty, rate, gst).forEach { it.addTextChangedListener(watcher) }
            tax.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) { refreshTotals() }
            }
            remove.setOnClickListener {
                if (rows.size > 1) {
                    rows.remove(itemRow)
                    host.removeView(card)
                    rows.forEachIndexed { index, r ->
                        val header = r.card.getChildAt(0) as LinearLayout
                        (header.getChildAt(0) as TextView).text = "ITEM " + (index + 1)
                    }
                    refreshTotals()
                } else {
                    name.setText("")
                    qty.setText("1")
                    rate.setText("0")
                    gst.setText("0")
                }
            }
            host.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) })
            refreshTotals()
        }
        add.setOnClickListener { addBillRow() }
        addBillRow()

        val notes = field("Notes / Description (Optional)")
        body.addView(notes)
        button("Save " + kind + " Bill", {
            val partyName = party.text.toString().trim()
            if (partyName.isBlank()) {
                party.error = "Required"
                Toast.makeText(this, "Enter customer / supplier name.", Toast.LENGTH_LONG).show()
                return@button
            }
            val savedItems = JSONArray()
            var gross = 0.0
            var gstTotal = 0.0
            rows.forEach { row ->
                val n = row.name.text.toString().trim()
                val q = row.qty.text.toString().toDoubleOrNull() ?: 0.0
                val r = row.rate.text.toString().toDoubleOrNull() ?: 0.0
                val g = row.gst.text.toString().toDoubleOrNull() ?: 0.0
                if (n.isBlank() && r == 0.0) return@forEach
                if (n.isBlank() || q <= 0 || r < 0 || g < 0 || g > 100) {
                    Toast.makeText(this, "Check item name, quantity, rate and GST.", Toast.LENGTH_LONG).show()
                    return@button
                }
                val amountValue = q * r
                val taxPart = if (row.tax.selectedItemPosition == 0 && g > 0) amountValue * g / (100 + g) else 0.0
                gross += amountValue
                gstTotal += taxPart
                savedItems.put(JSONObject().put("name", n).put("qty", q).put("unit", row.unit.selectedItem.toString())
                    .put("rate", r).put("gst", g).put("taxMode", if (row.tax.selectedItemPosition == 0) "Included" else "None")
                    .put("amount", amountValue).put("taxAmount", taxPart))
            }
            if (savedItems.length() == 0) {
                Toast.makeText(this, "Add at least one item.", Toast.LENGTH_LONG).show()
                return@button
            }
            val first = savedItems.optJSONObject(0) ?: JSONObject()
            val obj = JSONObject().put("party", partyName).put("phone", phone.text.toString().trim())
                .put("items", savedItems).put("item", first.optString("name")).put("qty", first.optDouble("qty"))
                .put("unit", first.optString("unit")).put("rate", first.optDouble("rate"))
                .put("gst", if (gross > gstTotal) gstTotal * 100 / (gross - gstTotal) else 0.0)
                .put("taxMode", "Included").put("base", gross - gstTotal).put("taxAmount", gstTotal)
                .put("total", gross).put("notes", notes.text.toString().trim()).put("date", today())
                .put("invoiceNo", invoiceNo).put("paymentType", payment.selectedItem.toString())
            val saved = arr(key)
            saved.put(obj)
            save(key, saved)
            Toast.makeText(this, kind + " bill saved: " + money(gross), Toast.LENGTH_LONG).show()
            showPage("Home")
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
