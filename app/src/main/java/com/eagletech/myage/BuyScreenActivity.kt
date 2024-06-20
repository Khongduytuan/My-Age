package com.eagletech.myage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.FulfillmentResult
import com.amazon.device.iap.model.ProductDataResponse
import com.amazon.device.iap.model.PurchaseResponse
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import com.amazon.device.iap.model.UserDataResponse
import com.eagletech.myage.data.ManagerData
import com.eagletech.myage.databinding.ActivityBuyScreenBinding

class BuyScreenActivity : AppCompatActivity() {
    private lateinit var sBinding: ActivityBuyScreenBinding
    private lateinit var myData: ManagerData
    private lateinit var currentUserId: String
    private lateinit var currentMarketplace: String


    companion object {
        const val age5 = "com.eagletech.myage.age5"
        const val age10 = "com.eagletech.myage.age10"
        const val age15 = "com.eagletech.myage.age15"
        const val ageSub = "com.eagletech.myage.longtermregistrations"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityBuyScreenBinding.inflate(layoutInflater)
        setContentView(sBinding.root)
        myData = ManagerData.getInstance(this)
        setupIAPOnCreate()
        setClickItems()

    }

    private fun setClickItems() {
        sBinding.age5.setOnClickListener {
//            myData.addData(2)
            PurchasingService.purchase(age5)
        }
        sBinding.age10.setOnClickListener {
            PurchasingService.purchase(age10)
        }
        sBinding.age15.setOnClickListener {
            PurchasingService.purchase(age15)
        }
        sBinding.ageSub.setOnClickListener {
            PurchasingService.purchase(ageSub)
        }
        sBinding.finish.setOnClickListener { finish() }
        sBinding.back.setOnClickListener { finish() }
    }

    private fun setupIAPOnCreate() {
        val purchasingListener: PurchasingListener = object : PurchasingListener {
            override fun onUserDataResponse(response: UserDataResponse) {
                when (response.requestStatus!!) {
                    UserDataResponse.RequestStatus.SUCCESSFUL -> {
                        currentUserId = response.userData.userId
                        currentMarketplace = response.userData.marketplace
                        myData.userId(currentUserId)
                    }

                    UserDataResponse.RequestStatus.FAILED, UserDataResponse.RequestStatus.NOT_SUPPORTED -> Log.v(
                        "IAP SDK",
                        "loading failed"
                    )
                }
            }

            override fun onProductDataResponse(productDataResponse: ProductDataResponse) {
                when (productDataResponse.requestStatus) {
                    ProductDataResponse.RequestStatus.SUCCESSFUL -> {
                        val products = productDataResponse.productData
                        for (key in products.keys) {
                            val product = products[key]
                            Log.v(
                                "Product:", String.format(
                                    "Product: %s\n Type: %s\n SKU: %s\n Price: %s\n Description: %s\n",
                                    product!!.title,
                                    product.productType,
                                    product.sku,
                                    product.price,
                                    product.description
                                )
                            )
                        }
                        //get all unavailable SKUs
                        for (s in productDataResponse.unavailableSkus) {
                            Log.v("Unavailable SKU:$s", "Unavailable SKU:$s")
                        }
                    }

                    ProductDataResponse.RequestStatus.FAILED -> Log.v("FAILED", "FAILED")
                    else -> {}
                }
            }

            override fun onPurchaseResponse(purchaseResponse: PurchaseResponse) {
                when (purchaseResponse.requestStatus) {
                    PurchaseResponse.RequestStatus.SUCCESSFUL -> {

                        if (purchaseResponse.receipt.sku == age5) {
                            myData.addData(5)
                            showInfoDialog()
                        }
                        if (purchaseResponse.receipt.sku == age10) {
                            myData.addData(10)
                            showInfoDialog()
                        }
                        if (purchaseResponse.receipt.sku == age15) {
                            myData.addData(15)
                            showInfoDialog()
                        }
                        if (purchaseResponse.receipt.sku == ageSub) {
                            myData.isPremium = true
                            showInfoDialog()
                        }
                        PurchasingService.notifyFulfillment(
                            purchaseResponse.receipt.receiptId, FulfillmentResult.FULFILLED
                        )

                        Log.v("FAILED", "FAILED")
                    }

                    PurchaseResponse.RequestStatus.FAILED -> {}
                    else -> {}
                }
            }

            override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) {
                // Process receipts
                when (response.requestStatus) {
                    PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL -> {
                        for (receipt in response.receipts) {
                            myData.isPremium = !receipt.isCanceled
                        }
                        if (response.hasMore()) {
                            PurchasingService.getPurchaseUpdates(false)
                        }

                    }

                    PurchaseUpdatesResponse.RequestStatus.FAILED -> Log.d("FAILED", "FAILED")
                    else -> {}
                }
            }
        }
        PurchasingService.registerListener(this, purchasingListener)
        Log.d(
            "DetailBuyAct", "Appstore SDK Mode: " + LicensingService.getAppstoreSDKMode()
        )
    }

    private fun showInfoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dailog_info_buy, null)
        val messageTextView = dialogView.findViewById<TextView>(R.id.tvMessageBuy)
        val confirmButton = dialogView.findViewById<Button>(R.id.btnConfirmBuy)

        if (myData.isPremium == true) {
            messageTextView.text = "You have successfully registered"
        } else {
            messageTextView.text = "You have ${myData.getData()} uses"
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Info")
            .create()

        confirmButton.setOnClickListener {
            dialog.dismiss()
            finish()  // Navigates back to the previous activity
        }

        dialog.show()
    }


    override fun onResume() {
        super.onResume()
        PurchasingService.getUserData()
        val productSkus: MutableSet<String> = HashSet()
        productSkus.add(ageSub)
        productSkus.add(age5)
        productSkus.add(age10)
        productSkus.add(age15)
        PurchasingService.getProductData(productSkus)
        PurchasingService.getPurchaseUpdates(false)
    }
}
