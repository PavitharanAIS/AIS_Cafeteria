package com.examples.aiscafeteria.Fragment

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.widget.Toast
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.examples.aiscafeteria.ContactUsActivity
import com.examples.aiscafeteria.R
import com.examples.aiscafeteria.databinding.FragmentProfileBinding
import com.examples.aiscafeteria.model.UserModel
import com.examples.aiscafeteria.UserDatabaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private lateinit var binding: FragmentProfileBinding
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var adminReference: DatabaseReference
    private lateinit var userDatabaseHelper: UserDatabaseHelper
    private var isNetworkAvailable: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        adminReference = database.reference.child("user")
        userDatabaseHelper = UserDatabaseHelper(requireContext())

        setupNetworkCallback()

        binding.profileContactUsButton.setOnClickListener {
            val intent = Intent(requireContext(), ContactUsActivity::class.java)
            startActivity(intent)
        }

        setUserData()

        binding.profileName.isEnabled = false
        binding.profileAddress.isEnabled = false
        binding.profileEmail.isEnabled = false
        binding.profilePhone.isEnabled = false

        var isEditable = false
        binding.profileEditButton.setOnClickListener {
            isEditable = !isEditable
            binding.profileName.isEnabled = isEditable
            binding.profileAddress.isEnabled = isEditable
            binding.profileEmail.isEnabled = isEditable
            binding.profilePhone.isEnabled = isEditable

            if (isEditable) {
                binding.profileName.requestFocus()
            }
        }

        binding.profileSaveButton.setOnClickListener {
            val name = binding.profileName.text.toString()
            val email = binding.profileEmail.text.toString()
            val address = binding.profileAddress.text.toString()
            val phone = binding.profilePhone.text.toString()

            updateUserData()
        }
        return binding.root
    }

    private fun setupNetworkCallback() {
        connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d("NetworkCallback", "Network available, syncing data...")
                syncLocalDataWithFirebase()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d("NetworkCallback", "Network lost")
            }
        }

        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            networkCallback
        )
    }

    private fun updateUserData() {
        val currentUserUid = auth.currentUser?.uid ?: return Toast.makeText(
            requireContext(),
            "Failed to update profile. User not logged in.",
            Toast.LENGTH_SHORT
        ).show()

        val dbHelper = UserDatabaseHelper(requireContext())
        val existingUser = dbHelper.getUserByUid(currentUserUid)
        val password = existingUser?.password // Retain the password from local storage

        val updatedUser = UserModel(
            uid = currentUserUid,
            name = binding.profileName.text.toString(),
            email = binding.profileEmail.text.toString(),
            password = password,
            phone = binding.profilePhone.text.toString(),
            address = binding.profileAddress.text.toString()
        )

        val isOnline = isDeviceOnline(requireContext())
        if (isOnline) {
            adminReference.child(currentUserUid).setValue(updatedUser).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                        Toast.makeText(requireContext(),"Profile updated successfully.",Toast.LENGTH_SHORT).show()
                    userDatabaseHelper.updateUser(updatedUser)
                } else {
                    Toast.makeText(requireContext(),"Failed to update profile in Firebase.",Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val rowsUpdated = userDatabaseHelper.updateUser(updatedUser)
            if (rowsUpdated > 0) {
                Toast.makeText(requireContext(),"Profile updated locally.",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(),"Failed to update profile locally.",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val isOnline = isDeviceOnline(requireContext())
            if (isOnline) {
                val userReference = database.getReference("user").child(userId)
                userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userProfile = snapshot.getValue(UserModel::class.java)
                            if (userProfile != null) {
                                updateUI(userProfile)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Error loading data: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                val dbHelper = UserDatabaseHelper(requireContext())
                val user = dbHelper.getUserByUid(userId)
                if (user != null) {
                    updateUI(user)
                } else {
                    Toast.makeText(requireContext(), "No data available offline.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(user: UserModel) {
        binding.profileName.setText(user.name)
        binding.profileAddress.setText(user.address)
        binding.profileEmail.setText(user.email)
        binding.profilePhone.setText(user.phone)
    }

    private fun isDeviceOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun syncLocalDataWithFirebase() {
        Log.d("syncFunction", "Starting Sync Function...")

        // Check if network is available before proceeding
        val isOnline = isDeviceOnline(requireContext())
        if (!isOnline) {
            Log.d("syncFunction", "Network not available. Skipping sync.")
            return
        }

        val currentUserUid = auth.currentUser?.uid ?: return
        Log.d("syncFunction", currentUserUid)

        if (userDatabaseHelper.isUserModified(currentUserUid)) {
            Log.d("syncFunction", "Data modified, syncing with Firebase...")
            val user = userDatabaseHelper.getUserByUid(currentUserUid)
            Log.d("UserData", "${user}")

            if (user != null) {
                adminReference.child(currentUserUid).setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Profile synced with Firebase.", Toast.LENGTH_SHORT).show()
                        userDatabaseHelper.markAsSynced(currentUserUid)
                        Log.d("UserDataAfterSync", "${adminReference}")
                    } else {
                        Toast.makeText(requireContext(), "Failed to sync profile with Firebase.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Log.d("syncFunction", "No changes detected in local data. Skipping sync.")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
