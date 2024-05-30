package com.yuch.aturdana.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yuch.aturdana.R
import com.yuch.aturdana.data.TransactionAdapter
import com.yuch.aturdana.data.pref.TransactionModel
import com.yuch.aturdana.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var _binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        onViews()
        displayTransactions()
//        setupInsets()
    }
//    private fun setupInsets() {
//        ViewCompat.setOnApplyWindowInsetsListener(_binding.root) { view, insets ->
//            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            view.updatePadding(bottom = systemBarsInsets.bottom)
//            insets
//        }
//    }

    private fun displayTransactions() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val transactionRef = database.child("transaction")
            val query = transactionRef.orderByChild("user_id").equalTo(userId)

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!isAdded || context == null) return

                    val transactions = mutableListOf<TransactionModel>()
                    for (data in dataSnapshot.children) {
                        val transaction = data.getValue(TransactionModel::class.java)
                        if (transaction != null) {
                            transactions.add(transaction)
                        }
                    }

                    // Sort transactions by date
                    transactions.sortByDescending { it.date }

                    // Set up RecyclerView and attach adapter
                    val adapter = TransactionAdapter(transactions)
                    _binding.rvTransaksi.adapter = adapter
                    _binding.rvTransaksi.layoutManager = LinearLayoutManager(requireContext())
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun onViews() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val transactionRef = database.child("transaction")
            val query = transactionRef.orderByChild("user_id").equalTo(userId)

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!isAdded || context == null) return

                    var totalPendapatan = 0
                    var totalPengeluaran = 0
                    var lastUpdatePendapatan = ""
                    var lastUpdatePengeluaran = ""

//                    var lastUpdate = ""
//                    val totalSaldo = totalPendapatan - totalPengeluaran

                    for (data in dataSnapshot.children) {
                        val type = data.child("type").getValue(String::class.java)
                        val amount = data.child("amount").getValue(String::class.java)
                        val date = data.child("date").getValue(String::class.java)
                        val time = data.child("time").getValue(String::class.java)

                        if (type != null && amount != null && date != null && time != null){
                            if (type == "Pendapatan") {
                                totalPendapatan += amount.toInt()
                                Log.d("HomeFragmentIfStatement", "Total Pendapatan: $totalPendapatan")
                                lastUpdatePendapatan = "$date $time"
                            } else if (type == "Pengeluaran") {
                                totalPengeluaran += amount.toInt()
                                lastUpdatePengeluaran = "$date $time"
                            }
                        }
                    }
                    Log.d("HomeFragment", "Total Pendapatan: $totalPendapatan")
                    _binding.apply {
                        tvTotalPendapatan.text = "Rp. $totalPendapatan"
                        tvTotalPengeluaran.text = "Rp. $totalPengeluaran"
                        tvTerakhirUpdatePendapatan.text = "Terakhir update : $lastUpdatePendapatan"
                        tvTerakhirUpdatePengeluaran.text = "Terakhir update : $lastUpdatePengeluaran"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    companion object {

    }
}