package com.billora.billing

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class AccountingScreens(private val a:Activity) {
 private val p=a.getSharedPreferences("billora_v2",Activity.MODE_PRIVATE)
 private val navy=Color.rgb(20,39,70); private val blue=Color.rgb(39,105,220); private val pale=Color.rgb(244,247,252); private val muted=Color.rgb(112,126,148)
 private lateinit var body:LinearLayout
 private fun dp(v:Int)=(v*a.resources.displayMetrics.density).toInt()
 private fun arr(k:String)=try{JSONArray(p.getString(k,"[]"))}catch(e:Exception){JSONArray()}
 private fun money(v:Double)="₹"+String.format(Locale("en","IN"),"%,.2f",v)
 private fun bg(c:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(14).toFloat();setStroke(dp(1),Color.rgb(229,235,244))}
 private fun t(s:String,z:Float=14f,c:Int=navy,b:Boolean=false)=TextView(a).apply{text=s;textSize=z;setTextColor(c);if(b)typeface=Typeface.DEFAULT_BOLD}
 private fun line(s:String,z:Float=14f,c:Int=navy,b:Boolean=false){body.addView(t(s,z,c,b),LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(8)})}
 private fun btn(s:String,act:()->Unit){body.addView(t(s,14f,blue,true).apply{gravity=Gravity.CENTER;setPadding(dp(10),dp(14),dp(10),dp(14));background=bg(Color.WHITE);setOnClickListener{act()}},LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(8)})}
 private fun field(s:String,v:String="")=EditText(a).apply{hint=s;setText(v);textSize=14f;setSingleLine(true);setPadding(dp(12),0,dp(12),0);background=bg(Color.WHITE);layoutParams=LinearLayout.LayoutParams(-1,dp(48)).apply{bottomMargin=dp(9)}}
 fun show(which:String){
  val page=which
  val root=LinearLayout(a).apply{orientation=1;setBackgroundColor(pale)}
  val header=LinearLayout(a).apply{orientation=0;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(18),dp(16),dp(18),dp(16));setBackgroundColor(navy)}
  val brand=LinearLayout(a).apply{orientation=1}
  brand.addView(t(p.getString("firm","BAJRANG GROUP")?.ifBlank{"BAJRANG GROUP"}?:"BAJRANG GROUP",18f,Color.WHITE,true))
  brand.addView(t(if(page=="Statement")"Party Statement" else "Transaction Details",11f,Color.LTGRAY))
  header.addView(brand,LinearLayout.LayoutParams(0,-2,1f));header.addView(t("⋮",24f,Color.WHITE,true).apply{setOnClickListener{show(if(page=="Parties")"PartyOptions" else "TxnOptions")}})
  root.addView(header)
  val scroll=ScrollView(a);body=LinearLayout(a).apply{orientation=1;setPadding(dp(16),dp(16),dp(16),dp(20))};scroll.addView(body);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
  val nav=LinearLayout(a).apply{orientation=0;setBackgroundColor(Color.WHITE)}
  listOf("Home","Dashboard","Items","Menu").forEach{n->nav.addView(t(n,11f,blue,true).apply{gravity=Gravity.CENTER;setPadding(0,dp(16),0,dp(16));setOnClickListener{when(n){"Home"->show("Transactions");"Dashboard"->(a as MainActivity).showPage("Home");"Items"->(a as MainActivity).showPage("Items");else->(a as MainActivity).showPage("More")}}},LinearLayout.LayoutParams(0,-2,1f))}
  root.addView(nav);a.setContentView(root)
  when{page=="Transactions"->transactions();page=="Parties"->parties();page=="Statement"->statement("");page.startsWith("Statement:")->statement(page.substringAfter(":"));page=="TxnOptions"->options(false);page=="PartyOptions"->options(true);page=="SaleReport"->saleReport();page=="Menu"->menu();page=="Items"->itemCatalogue();page=="Utilities"->utilities();else->transactions()}
 }
 private fun quick(title:String,items:List<Pair<String,()->Unit>>){line(title,15f,navy,true);val row=LinearLayout(a).apply{orientation=0};items.forEach{pair->row.addView(t(pair.first,11f,blue,true).apply{gravity=Gravity.CENTER;setPadding(dp(5),dp(15),dp(5),dp(15));background=bg(Color.WHITE);setOnClickListener{pair.second()}},LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(3);rightMargin=dp(3)})};body.addView(row)}
 private fun transactions(){
  line("Transaction Details",19f,navy,true)
  quick("Quick Links",listOf("＋ Add Txn" to {(a as MainActivity).showPage("Sale")},"Sale Report" to {show("SaleReport")},"Txn Settings" to {show("TxnOptions")}))
  btn("Show All Transactions"){}
  body.addView(field("Search for a transaction"))
  val s=arr("sales")
  if(s.length()==0)line("No transactions yet.",13f,muted)
  for(i in s.length()-1 downTo 0){val o=s.optJSONObject(i)?:continue;val card=LinearLayout(a).apply{orientation=1;setPadding(dp(13),dp(12),dp(13),dp(12));background=bg(Color.WHITE)}
   card.addView(t(o.optString("party"),14f,navy,true))
   card.addView(t("SALE   #"+o.optString("invoiceNo","BG/"+(i+1)),10f,muted))
   card.addView(t(o.optString("date"),10f,muted))
   card.addView(t("Total   "+money(o.optDouble("total"))+"     Balance   "+money(o.optDouble("total")),12f,blue,true))
   body.addView(card,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(8)})
  }
  btn("＋ Add New Sale"){(a as MainActivity).showPage("Sale")}
 }
 private fun parties(){
  line("Party Details",19f,navy,true)
  quick("Quick Links",listOf("Loyalty Points" to {show("PartyOptions")},"Party Statement" to {show("Statement")},"Party Settings" to {show("PartyOptions")}))
  btn("Show All Parties"){}
  body.addView(field("Search any party"))
  val names=LinkedHashMap<String,Double>();val pa=arr("parties")
  for(i in 0 until pa.length()){val o=pa.optJSONObject(i)?:continue;names[o.optString("name")]=o.optDouble("opening")}
  val s=arr("sales");for(i in 0 until s.length()){val o=s.optJSONObject(i)?:continue;val n=o.optString("party");names[n]=(names[n]?:0.0)+o.optDouble("total")}
  names.forEach{(n,v)->val card=LinearLayout(a).apply{orientation=0;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(12),dp(13),dp(12),dp(13));background=bg(Color.WHITE)}
   val left=LinearLayout(a).apply{orientation=1};left.addView(t(n,13f,navy,true));left.addView(t("You'll Get",10f,muted));card.addView(left,LinearLayout.LayoutParams(0,-2,1f));card.addView(t(money(v),13f,blue,true));card.setOnClickListener{show("Statement:"+n)}
   body.addView(card,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(7)})
  }
  btn("＋ Add New Party"){(a as MainActivity).showPage("Parties")}
 }
 private fun statement(party:String){
  line("Party Statement",19f,navy,true);line("This Month     01/11/2026  TO  30/11/2026",12f,muted)
  val name=field("Select Party",party);body.addView(name);body.addView(field("From Date","01/11/2026"));body.addView(field("To Date","30/11/2026"))
  btn("Filters  •  Theme - Accounting View"){}
  val selected=party.ifBlank{name.text.toString()};val matches=mutableListOf<JSONObject>();val s=arr("sales")
  for(i in 0 until s.length()){val o=s.optJSONObject(i)?:continue;if(o.optString("party").equals(selected,true))matches.add(o)}
  var debit=0.0;matches.forEach{debit+=it.optDouble("total")}
  quick("Export / Share",listOf("PDF" to {share(statementText(selected,matches,debit))},"XLS" to {share(csv(selected,matches))},"CA" to {share(statementText(selected,matches,debit))}))
  val box=LinearLayout(a).apply{orientation=1;setPadding(dp(14),dp(14),dp(14),dp(14));background=bg(Color.WHITE)}
  box.addView(t(selected.ifBlank{"Choose party"},14f,navy,true));box.addView(t("Total Debit     "+money(debit),13f,navy,true));box.addView(t("Total Credit    "+money(0.0),13f,navy,true));box.addView(t("Closing Balance   "+money(debit),17f,blue,true));body.addView(box)
  line("Transactions",15f,navy,true)
  if(matches.isEmpty())line("No transactions found for this party.",12f,muted)
  matches.forEach{line(it.optString("date")+"   SALE     "+money(it.optDouble("total")),12f,navy,true)}
 }
 private fun statementText(n:String,m:List<JSONObject>,d:Double)="BILLORA PARTY STATEMENT\n"+n+"\nTotal Debit: "+money(d)+"\nTotal Credit: "+money(0.0)+"\nClosing Balance: "+money(d)+"\n"+m.joinToString("\n"){it.optString("date")+" | SALE | "+money(it.optDouble("total"))}
 private fun csv(n:String,m:List<JSONObject>)="Party,Date,Type,Amount\n"+m.joinToString("\n"){n+","+it.optString("date")+",SALE,"+it.optDouble("total")}
 private fun share(s:String){a.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,s)},"Share"))}
 private fun options(party:Boolean){line("More Options",19f,navy,true);(if(party)listOf("Invite Party","Partywise PnL","All Parties Report","Reminder Settings","WhatsApp Marketing","Import Party","Party Settings","Loyalty Points") else listOf("Bank Accounts","Day Book","All Txns Report","Profit & Loss","Balance Sheet","Billwise PnL","Print Settings","Txn SMS Settings","Recycle Bin")).forEach{btn(it){Toast.makeText(a,it+" module not yet connected",Toast.LENGTH_SHORT).show()}}}
 private fun saleReport(){line("Sale Report",19f,navy,true);val s=arr("sales");var sum=0.0;for(i in 0 until s.length())sum+=s.optJSONObject(i)?.optDouble("total")?:0.0;line("Total Sales  "+money(sum),18f,blue,true);btn("Share Sales Register"){share(csv("All Parties",(0 until s.length()).mapNotNull{s.optJSONObject(it)}))}}

 private fun menu(){
  line("My Business",18f,navy,true)
  listOf("Sale" to "Sale","Purchase" to "Purchase","Expenses" to "Expenses","My Online Store" to "Online Store","Reports" to "Reports","Loyalty Points" to "Loyalty Points").forEach{(s,target)->btn("▣   $s     ›"){when(target){"Sale"->(a as MainActivity).showPage("Sale");"Purchase"->(a as MainActivity).showPage("Purchase");"Reports"->(a as MainActivity).showPage("Reports");else->Toast.makeText(a,"$s module will be added",Toast.LENGTH_SHORT).show()}}}
  line("Cash & Bank",16f,navy,true)
  listOf("Bank Accounts","Cash In-Hand","Cheques","Loan Accounts").forEach{btn("▤   $it     ›"){Toast.makeText(a,"$it module will be added",Toast.LENGTH_SHORT).show()}}
  line("Important Utilities",16f,navy,true)
  btn("Sync & Share     ›"){show("Utilities")};btn("Manage Companies     ›"){Toast.makeText(a,"Multi-company support is not connected yet",Toast.LENGTH_SHORT).show()}
  btn("Backup / Restore     ›"){show("Utilities")}
  line("Others",16f,navy,true)
  listOf("Plans & Pricing","Get Desktop Billing Software","Other Products","Grow Your Business","Settings","Help & Support","Rate this app").forEach{btn("$it     ›"){if(it=="Settings")(a as MainActivity).showPage("More") else Toast.makeText(a,"$it will be available in a later update",Toast.LENGTH_SHORT).show()}}
  line("App Version   1.0.0",11f,muted)
 }
 private fun utilities(){
  line("Important Utilities",19f,navy,true)
  btn("Sync & Share"){Toast.makeText(a,"Cloud sync is not enabled. Current data is stored on this phone.",Toast.LENGTH_LONG).show()}
  btn("Backup / Restore"){share("BILLORA BACKUP SUMMARY\\nParties: "+arr("parties").length()+"\\nItems: "+arr("items").length()+"\\nSales: "+arr("sales").length()+"\\nPurchases: "+arr("purchases").length())}
  btn("Restore from backup"){Toast.makeText(a,"Restore import is not implemented yet.",Toast.LENGTH_SHORT).show()}
  btn("‹ Back to Menu"){show("Menu")}
 }
 private fun itemCatalogue(){
  line("Items",19f,navy,true)
  quick("Quick Links",listOf("Online Store" to {Toast.makeText(a,"Online store is not connected",Toast.LENGTH_SHORT).show()},"Stock Summary" to {stockSummary()},"Item Settings" to {Toast.makeText(a,"Item settings",Toast.LENGTH_SHORT).show()}))
  btn("Show All Items"){}
  body.addView(field("Search for an item or code"))
  val items=arr("items")
  if(items.length()==0)line("No items added yet. Add items from Items screen.",12f,muted)
  for(i in 0 until items.length()){
   val o=items.optJSONObject(i)?:continue
   val card=LinearLayout(a).apply{orientation=1;setPadding(dp(14),dp(13),dp(14),dp(13));background=bg(Color.WHITE)}
   card.addView(t(o.optString("name"),14f,navy,true))
   card.addView(t(o.optString("code"),10f,muted))
   card.addView(t("Sale Price     "+money(o.optDouble("rate")),12f,navy,true))
   card.addView(t("Purchase Price     "+money(o.optDouble("purchaseRate")),12f,muted))
   card.addView(t("In Stock     "+o.optDouble("stock").toString()+" "+o.optString("unit"),12f,blue,true))
   body.addView(card,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(8)})
  }
  btn("＋ Add New Item"){(a as MainActivity).showPage("Items")}
  line("More Options",16f,navy,true)
  listOf("Import Items","Export Items","Item wise PnL","Additional Fields","Item Details","Low Stock Summary").forEach{btn(it){when(it){"Export Items"->share("Item,Code,Sale Price,Stock\\n"+(0 until items.length()).mapNotNull{items.optJSONObject(it)}.joinToString("\\n"){it.optString("name")+","+it.optString("code")+","+it.optDouble("rate")+","+it.optDouble("stock")});"Low Stock Summary"->stockSummary();else->Toast.makeText(a,"$it is not yet connected",Toast.LENGTH_SHORT).show()}}}
 }
 private fun stockSummary(){
  line("Stock Summary",18f,navy,true);val a1=arr("items");var qty=0.0
  for(i in 0 until a1.length())qty+=a1.optJSONObject(i)?.optDouble("stock")?:0.0
  line("Total item types: "+a1.length(),13f,muted);line("Total opening stock quantity: "+qty,16f,blue,true)
 }

}
