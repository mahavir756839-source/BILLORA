package com.billora.billing

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.widget.*

class MainActivity : Activity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var party: EditText
    private lateinit var item: EditText
    private lateinit var quantity: EditText
    private lateinit var rate: EditText
    private lateinit var totalView: TextView
    private lateinit var history: TextView
    private var total = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("billora_data", MODE_PRIVATE)
        buildScreen()
        refreshHistory()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 22, 22, 22)
            setBackgroundColor(Color.rgb(246, 248, 252))
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val header = TextView(this).apply {
            text = "BILLORA"
            textSize = 30f
            setTextColor(Color.rgb(21, 48, 86))
            typeface = Typeface.DEFAULT_BOLD
        }
        content.addView(header)
        content.addView(label("SMART BILLING & ACCOUNTS"))
        content.addView(space(18))
        content.addView(section("Create New Bill"))
        party = field("Party / Customer Name")
        item = field("Item / Product Name")
        quantity = field("Quantity", "1")
        rate = field("Rate")
        content.addView(party); content.addView(item)
        content.addView(quantity); content.addView(rate)
        content.addView(button("Calculate Total") { calculate() })
        totalView = TextView(this).apply {
            text = "Total: ₹0.00"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(17, 110, 75))
            setPadding(0, 16, 0, 16)
        }
        content.addView(totalView)
        content.addView(button("Save Bill") { saveBill() })
        content.addView(button("Share Bill") { shareBill() })
        content.addView(space(18))
        content.addView(section("Recent Bills"))
        history = label("No bills saved yet.")
        content.addView(history)
        content.addView(space(20))
        content.addView(label("BILLORA • Your business, organised."))
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun label(text: String) = TextView(this).apply {
        this.text = text; textSize = 13f; setTextColor(Color.DKGRAY)
        setPadding(0, 5, 0, 5)
    }
    private fun section(text: String) = TextView(this).apply {
        this.text = text; textSize = 19f; typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.rgb(28, 52, 82)); setPadding(0, 8, 0, 10)
    }
    private fun space(h: Int) = TextView(this).apply { height = h }
    private fun field(hintText: String, value: String = "") = EditText(this).apply {
        hint = hintText; setText(value); textSize = 16f
        setSingleLine(true)
        setPadding(14, 10, 14, 10)
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.WHITE); cornerRadius = 14f
            setStroke(1, Color.rgb(220, 226, 235))
        }
        val lp = LinearLayout.LayoutParams(-1, 54); lp.bottomMargin = 10
        layoutParams = lp
    }
    private fun button(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text
        setOnClickListener { action() }
        val lp = LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = 8
        layoutParams = lp
    }
    private fun calculate() {
        val q = quantity.text.toString().toDoubleOrNull() ?: 0.0
        val r = rate.text.toString().toDoubleOrNull() ?: 0.0
        total = q * r
        totalView.text = "Total: ₹" + String.format("%.2f", total)
    }
    private fun saveBill() {
        calculate()
        val p = party.text.toString().trim()
        val i = item.text.toString().trim()
        if (p.isEmpty() || i.isEmpty() || total <= 0.0) {
            Toast.makeText(this, "Party, item, quantity aur rate sahi bharein.", Toast.LENGTH_LONG).show()
            return
        }
        val record = "$p | $i | Qty ${quantity.text} × ₹${rate.text} = ₹${String.format("%.2f", total)}"
        val old = prefs.getString("bills", "") ?: ""
        prefs.edit().putString("bills", if (old.isBlank()) record else "$record\n$old").apply()
        refreshHistory()
        Toast.makeText(this, "Bill saved on this device.", Toast.LENGTH_SHORT).show()
    }
    private fun shareBill() {
        calculate()
        val message = "BILLORA BILL\nParty: ${party.text}\nItem: ${item.text}\nQuantity: ${quantity.text}\nRate: ₹${rate.text}\nTotal: ₹${String.format("%.2f", total)}"
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message)
        }, "Share bill"))
    }
    private fun refreshHistory() {
        history.text = prefs.getString("bills", "")?.takeIf { it.isNotBlank() } ?: "No bills saved yet."
    }
}