# BILLORA — Native Android Billing & Accounts

A fresh Kotlin Android project for an original billing and bookkeeping app inspired by the broad workflow of small-business billing apps. BILLORA is independently branded and does not reuse LEDGIX source or assets.

## Current foundation
- Home dashboard with sales, purchase, party and item counts
- Sale and purchase entry with quantity, rate, GST percentage and total calculation
- Local saved transaction registers
- Party directory with mobile number and opening balance
- Item catalogue with SKU, unit, sale rate, opening stock and GST rate
- Basic business summary and shareable sale/purchase registers
- Firm name, GSTIN, address and contact settings
- Android share sheet for transaction previews and summaries

## Build
Open in Android Studio or run:
`gradle :app:assembleDebug`

GitHub Actions builds a debug APK on pushes to `main` and manual workflow dispatch. The artifact is named `BILLORA-debug-apk`.

## Important scope notes
This is an early functional foundation, not yet a complete Vyapar-equivalent or production accounting system. Data is stored locally on the device. Automatic stock deduction, party running ledgers, payment/cash/bank book, invoice numbering, polished PDF/print templates, GST reports/returns, e-invoice/e-way bill, user login, cloud backup and multi-device sync still need implementation and testing. The simple sales-minus-purchase figure is not net profit.

## Technical
- Kotlin, native Android views
- Application ID: `com.billora.billing`
- Min SDK 23; compile/target SDK 35
