package com.billora.billing

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
    private val navy = Color.rgb(22, 43, 76)
    private val blue = Color.rgb(38, 102, 214)
    private val bg = Color.rgb(244, 247, 252)
    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout
    private var page = "Home"

    private fun arr(key: String) = try { JSONArray(prefs.getString(key, "[]")) } catch (_: Exception) { JSONArray() }
    private fun save(key: String, a: JSONArray) { prefs.edit().putString(key, a.toString()).apply() }
    private fun money(v: Double) = "₹" + String.format(Locale("en", "IN"), "%,.2f", v)
    private fun today() = SimpleDateFormat("dd MMM yyyy", Locale("en", "IN")).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showPage("Home")
    }

    private fun showPage(name: String) {
        page = name
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(20, 18, 20, 14); setBackgroundColor(navy)
        }
        header.addView(TextView(this).apply {
            text = "BILLORA"; textSize = 25f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })
        header.addView(TextView(this).apply {
            text = prefs.getString("firm", "Smart Billing & Accounts") ?: "Smart Billing & Accounts"
            textSize = 12f; setTextColor(Color.rgb(210, 222, 242))
        })
        root.addView(header)
        val nav = LinearLayout(this).apply { setPadding(6, 5, 6, 5); setBackgroundColor(Color.WHITE) }
        listOf("Home", "Sale", "Purchase", "Parties", "Items", "Reports", "More").forEach { n ->
            nav.addView(TextView(this).apply {
                text = n; textSize = 11f; gravity = Gravity.CENTER
                setTextColor(if (page == n) blue else navy)
                setTypeface(null, if (page == n) Typeface.BOLD else Typeface.NORMAL)
                setPadding(5, 12, 5, 12)
                setOnClickListener { showPage(n) }
            }, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(nav)
        val scroll = ScrollView(this)
        body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 14, 16, 20) }
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        when (name) {
            "Home" -> home()
            "Sale" -> invoiceForm(false)
            "Purchase" -> invoiceForm(true)
            "Parties" -> parties()
            "Items" -> items()
            "Reports" -> reports()
            else -> more()
        }
    }

    private fun heading(s: String) { body.addView(TextView(this).apply {
        text = s; textSize = 19f; typeface = Typeface.DEFAULT_BOLD; setTextColor(navy)
        setPadding(0, 8, 0, 10)
    }) }
    private fun text(s: String, size: Float = 14f, color: Int = Color.DKGRAY) { body.addView(TextView(this).apply {
        text = s; textSize = size; setTextColor(color); setPadding(0, 4, 0, 4)
    }) }
    private fun gap(h: Int = 10) { body.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, h) }) }
    private fun field(hint: String, value: String = "") = EditText(this).apply {
        this.hint = hint; setText(value); textSize = 15f; setSingleLine(true)
        setPadding(14, 9, 14, 9)
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.WHITE); cornerRadius = 12f; setStroke(1, Color.rgb(220, 226, 235))
        }
        val p = LinearLayout.LayoutParams(-1, 52); p.bottomMargin = 9; layoutParams = p
    }
    private fun btn(label: String, action: () -> Unit) { body.addView(Button(this).apply {
        text = label; isAllCaps = false; setTextColor(Color.WHITE); setBackgroundTintList(android.content.res.ColorStateList.valueOf(blue))
        setOnClickListener { action() }
    }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 }) }
    private fun card(label: String, value: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(14, 13, 14, 13)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE); cornerRadius = 16f; setStroke(1, Color.rgb(229, 234, 242))
            }
        }
        box.addView(TextView(this).apply { text = label; textSize = 12f; setTextColor(Color.GRAY) })
        box.addView(TextView(this).apply { text = value; textSize = 21f; typeface = Typeface.DEFAULT_BOLD; setTextColor(navy) })
        body.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 9 })
    }
    private fun total(key: String): Double {
        val a = arr(key); var sum = 0.0
        for (i in 0 until a.length()) sum += a.optJSONObject(i)?.optDouble("total", 0.0) ?: 0.0
        return sum
    }
    private fun home() {
        heading("Business Overview")
        val sales = total("sales"); val buys = total("purchases")
        card("TOTAL SALES", money(sales)); card("TOTAL PURCHASE", money(buys))
        card("CUSTOMERS / PARTIES", arr("parties").length().toString())
        card("ITEMS IN CATALOGUE", arr("items").length().toString())
        gap()
        heading("Quick Actions")
        btn("+ Create Sale Invoice") { showPage("Sale") }
        btn("+ Add Purchase") { showPage("Purchase") }
        btn("+ Add Party") { showPage("Parties") }
        btn("+ Add Item") { showPage("Items") }
        heading("Recent Transactions")
        recent("sales"); recent("purchases")
    }
    private fun recent(key: String) {
        val a = arr(key)
        if (a.length() == 0) { text("No transactions yet."); return }
        for (i in (a.length() - 1 downTo maxOf(0, a.length() - 5))) {
            val o = a.optJSONObject(i) ?: continue
            text("${o.optString("date")} • ${o.optString("party")} • ${money(o.optDouble("total"))}")
        }
    }
    private fun invoiceForm(purchase: Boolean) {
        val kind = if (purchase) "Purchase" else "Sale"
        heading("New $kind")
        text("Date: ${today()}")
        val party = field(if (purchase) "Supplier name" else "Customer name")
        val item = field("Item / Product")
        val qty = field("Quantity", "1")
        val rate = field("Rate (₹)")
        val gst = field("GST %", "0")
        val notes = field("Notes (optional)")
        listOf(party, item, qty, rate, gst, notes).forEach { body.addView(it) }
        val preview = TextView(this).apply { text = "Grand Total: ₹0.00"; textSize = 21f; typeface = Typeface.DEFAULT_BOLD; setTextColor(blue); setPadding(0, 14, 0, 14) }
        body.addView(preview)
        fun calc(): Double {
            val base = (qty.text.toString().toDoubleOrNull() ?: 0.0) * (rate.text.toString().toDoubleOrNull() ?: 0.0)
            return base + base * (gst.text.toString().toDoubleOrNull() ?: 0.0) / 100.0
        }
        val watcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { preview.text = "Grand Total: ${money(calc())}" }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }
        qty.addTextChangedListener(watcher); rate.addTextChangedListener(watcher); gst.addTextChangedListener(watcher)
        btn("Save $kind") {
            val p = party.text.toString().trim(); val it = item.text.toString().trim()
            val q = qty.text.toString().toDoubleOrNull() ?: 0.0
            val r = rate.text.toString().toDoubleOrNull() ?: 0.0
            val g = gst.text.toString().toDoubleOrNull() ?: 0.0
            if (p.isBlank() || it.isBlank() || q <= 0 || r < 0 || g < 0) {
                Toast.makeText(this, "Party, item, quantity aur rate sahi bharein.", Toast.LENGTH_LONG).show(); return@btn
            }
            val base = q * r; val grand = base + base * g / 100.0
            val obj = JSONObject().put("party", p).put("item", it).put("qty", q).put("rate", r)
                .put("gst", g).put("base", base).put("total", grand).put("notes", notes.text.toString()).put("date", today())
            val key = if (purchase) "purchases" else "sales"
            val a = arr(key); a.put(obj); save(key, a)
            Toast.makeText(this, "$kind saved: ${money(grand)}", Toast.LENGTH_LONG).show()
            showPage(kind)
        }
        btn("Share Preview") {
            val msg = "BILLORA $kind\nParty: ${party.text}\nItem: ${item.text}\nQty: ${qty.text}\nRate: ₹${rate.text}\nGST: ${gst.text}%\nTotal: ${money(calc())}"
            share(msg)
        }
    }
    private fun parties() {
        heading("Party Management")
        val name = field("Party / Customer / Supplier name")
        val phone = field("Mobile number")
        val opening = field("Opening balance (₹)", "0")
        body.addView(name); body.addView(phone); body.addView(opening)
        btn("Save Party") {
            if (name.text.isBlank()) { name.error = "Name required"; return@btn }
            val a = arr("parties")
            a.put(JSONObject().put("name", name.text.toString().trim()).put("phone", phone.text.toString()).put("opening", opening.text.toString().toDoubleOrNull() ?: 0.0))
            save("parties", a); showPage("Parties")
            Toast.makeText(this, "Party saved", Toast.LENGTH_SHORT).show()
        }
        heading("Saved Parties")
        val a = arr("parties")
        if (a.length() == 0) text("Abhi koi party add nahi hai.")
        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            text("${i + 1}. ${o.optString("name")}  •  ${o.optString("phone")}")
            text("Opening balance: ${money(o.optDouble("opening"))}", 12f, Color.GRAY)
        }
    }
    private fun items() {
        heading("Item & Stock Catalogue")
        val name = field("Item name")
        val code = field("Item / SKU code")
        val unit = field("Unit (pcs, kg, bag etc.)", "pcs")
        val rate = field("Sale rate (₹)", "0")
        val stock = field("Opening stock", "0")
        val gst = field("GST %", "0")
        listOf(name, code, unit, rate, stock, gst).forEach { body.addView(it) }
        btn("Save Item") {
            if (name.text.isBlank()) { name.error = "Item name required"; return@btn }
            val a = arr("items")
            a.put(JSONObject().put("name", name.text.toString()).put("code", code.text.toString()).put("unit", unit.text.toString())
                .put("rate", rate.text.toString().toDoubleOrNull() ?: 0.0).put("stock", stock.text.toString().toDoubleOrNull() ?: 0.0)
                .put("gst", gst.text.toString().toDoubleOrNull() ?: 0.0))
            save("items", a); showPage("Items"); Toast.makeText(this, "Item saved", Toast.LENGTH_SHORT).show()
        }
        heading("Saved Items")
        val a = arr("items")
        if (a.length() == 0) text("Abhi koi item add nahi hai.")
        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            text("${o.optString("name")} • ${money(o.optDouble("rate"))} / ${o.optString("unit")}")
            text("Stock: ${o.optDouble("stock")}  |  GST: ${o.optDouble("gst")}%  |  Code: ${o.optString("code")}", 12f, Color.GRAY)
        }
    }
    private fun reports() {
        heading("Reports & Business Summary")
        card("SALE REGISTER", money(total("sales")))
        card("PURCHASE REGISTER", money(total("purchases")))
        card("GROSS DIFFERENCE (Sales - Purchase)", money(total("sales") - total("purchases")))
        text("Sale entries: ${arr("sales").length()}")
        text("Purchase entries: ${arr("purchases").length()}")
        gap()
        btn("Share Business Summary") {
            share("BILLORA BUSINESS SUMMARY\nSales: ${money(total("sales"))}\nPurchase: ${money(total("purchases"))}\nDifference: ${money(total("sales") - total("purchases"))}\nSales entries: ${arr("sales").length()}\nPurchase entries: ${arr("purchases").length()}")
        }
        text("Note: Difference is a simple sales-minus-purchase figure, not audited net profit or GST return.")
    }
    private fun more() {
        heading("Business Settings")
        val firm = field("Firm / Business name", prefs.getString("firm", "") ?: "")
        val gstin = field("GSTIN (optional)", prefs.getString("gstin", "") ?: "")
        val address = field("Business address", prefs.getString("address", "") ?: "")
        val phone = field("Contact number", prefs.getString("phone", "") ?: "")
        listOf(firm, gstin, address, phone).forEach { body.addView(it) }
        btn("Save Firm Details") {
            prefs.edit().putString("firm", firm.text.toString()).putString("gstin", gstin.text.toString())
                .putString("address", address.text.toString()).putString("phone", phone.text.toString()).apply()
            Toast.makeText(this, "Firm details saved", Toast.LENGTH_SHORT).show(); showPage("More")
        }
        heading("Other")
        btn("Export / Share Sales Register") { exportList("sales") }
        btn("Export / Share Purchase Register") { exportList("purchases") }
        text("Data is currently stored locally on this phone. Online sync, login, automatic stock accounting, e-invoice/e-way bill, GST returns and print-ready PDF invoices are not included yet.", 12f, Color.GRAY)
    }
    private fun exportList(key: String) {
        val a = arr(key); val out = StringBuilder("BILLORA ${key.uppercase(Locale.ROOT)}\n")
        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            out.append("${o.optString("date")} | ${o.optString("party")} | ${o.optString("item")} | ${o.optDouble("qty")} x ${o.optDouble("rate")} | GST ${o.optDouble("gst")}% | ${money(o.optDouble("total"))}\n")
        }
        share(out.toString())
    }
    private fun share(message: String) {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message)
        }, "Share with"))
    }
}
